import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sqflite/sqflite.dart';

import '../../../../core/constants/api_endpoints.dart';
import '../../../../core/database/tables.dart';
import '../../../../core/network/api_exceptions.dart';
import '../../../../core/network/dio_client.dart';
import '../../../../shared/providers/database_provider.dart';
import '../../../../shared/providers/phone_provider.dart';
import '../models/case_model.dart';

/// Provider for the case repository.
final caseRepositoryProvider = Provider<CaseRepository>((ref) {
  final dioClient = ref.watch(dioClientProvider);
  final dbAsync = ref.watch(databaseProvider);
  final phoneNumber = ref.watch(phoneNumberProvider);
  
  // Set phone number header if available
  if (phoneNumber != null) {
    dioClient.setVolunteerPhone(phoneNumber);
  }
  
  return CaseRepository(
    dioClient: dioClient,
    database: dbAsync.valueOrNull,
  );
});

/// Repository for managing case data with offline support.
class CaseRepository {
  final DioClient _dioClient;
  final Database? _database;

  CaseRepository({
    required DioClient dioClient,
    Database? database,
  })  : _dioClient = dioClient,
        _database = database;

  /// Fetch cases from the server.
  /// Returns the volunteer's assigned cases.
  Future<List<CaseModel>> fetchCasesFromServer() async {
    try {
      final response = await _dioClient.get<List<dynamic>>(
        ApiEndpoints.volunteerMyCases,
      );

      final cases = (response.data ?? [])
          .map((json) => CaseModel.fromJson(json as Map<String, dynamic>))
          .toList();

      // Cache to local database
      await _cacheCases(cases);

      return cases;
    } on DioException catch (e) {
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Fetch all cases for dashboard (coordinators view).
  Future<List<CaseModel>> fetchAllCases({
    String? zone,
    String? status,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final queryParams = <String, dynamic>{
        'page': page,
        'size': size,
      };
      if (zone != null) queryParams['zone'] = zone;
      if (status != null) queryParams['status'] = status;

      final response = await _dioClient.get<Map<String, dynamic>>(
        ApiEndpoints.dashboardCases,
        queryParameters: queryParams,
      );

      final content = response.data?['content'] as List<dynamic>? ?? [];
      return content
          .map((json) => CaseModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Get cases from local database (offline mode).
  Future<List<CaseModel>> getCasesFromLocal() async {
    if (_database == null) return [];

    final maps = await _database!.query(
      Tables.cases,
      orderBy: 'created_at DESC',
    );

    return maps.map((map) => CaseModel.fromDbMap(map)).toList();
  }

  /// Get cases with offline fallback.
  /// Tries server first, falls back to local cache on error.
  Future<List<CaseModel>> getCases() async {
    try {
      return await fetchCasesFromServer();
    } catch (e) {
      // Fallback to local cache
      return await getCasesFromLocal();
    }
  }

  /// Sync cases from server to local database.
  Future<void> syncFromServer() async {
    final cases = await fetchCasesFromServer();
    await _cacheCases(cases);
  }

  /// Get a single case by ID.
  Future<CaseModel?> getCaseById(String caseId) async {
    try {
      final response = await _dioClient.get<Map<String, dynamic>>(
        ApiEndpoints.caseDetails(caseId),
      );

      if (response.data != null) {
        return CaseModel.fromJson(response.data!);
      }
      return null;
    } on DioException catch (e) {
      // Try local cache
      if (_database != null) {
        final maps = await _database!.query(
          Tables.cases,
          where: 'case_id = ?',
          whereArgs: [caseId],
        );
        if (maps.isNotEmpty) {
          return CaseModel.fromDbMap(maps.first);
        }
      }
      if (e.error is ApiException) {
        rethrow;
      }
      return null;
    }
  }

  /// Accept a case via SMS command simulation.
  /// In production, this would trigger an SMS or API call.
  Future<void> acceptCase(String caseId) async {
    try {
      // For POC, we'll simulate by calling the SMS simulate endpoint
      await _dioClient.post<dynamic>(
        ApiEndpoints.smsSimulate,
        data: {
          'Body': 'ACCEPT $caseId',
        },
      );

      // Update local cache
      await _updateLocalCaseStatus(caseId, 'ACCEPTED');
    } on DioException catch (e) {
      // Queue for offline sync
      await _queueAction('ACCEPT', caseId);
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Complete a case via SMS command simulation.
  Future<void> completeCase(String caseId) async {
    try {
      await _dioClient.post<dynamic>(
        ApiEndpoints.smsSimulate,
        data: {
          'Body': 'COMPLETE $caseId',
        },
      );

      // Update local cache
      await _updateLocalCaseStatus(caseId, 'COMPLETED');
    } on DioException catch (e) {
      // Queue for offline sync
      await _queueAction('COMPLETE', caseId);
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Cache cases to local database.
  Future<void> _cacheCases(List<CaseModel> cases) async {
    if (_database == null) return;

    final batch = _database!.batch();
    
    for (final caseModel in cases) {
      batch.insert(
        Tables.cases,
        caseModel.toDbMap(),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }

    await batch.commit(noResult: true);
  }

  /// Update local case status.
  Future<void> _updateLocalCaseStatus(String caseId, String status) async {
    if (_database == null) return;

    await _database!.update(
      Tables.cases,
      {'status': status},
      where: 'case_id = ?',
      whereArgs: [caseId],
    );
  }

  /// Queue an action for offline sync.
  Future<void> _queueAction(String actionType, String caseId) async {
    if (_database == null) return;

    await _database!.insert(
      Tables.syncQueue,
      {
        'action_type': actionType,
        'payload': caseId,
        'created_at': DateTime.now().toIso8601String(),
      },
    );
  }

  /// Process queued offline actions.
  Future<void> processOfflineQueue() async {
    if (_database == null) return;

    final queue = await _database!.query(Tables.syncQueue);

    for (final item in queue) {
      final actionType = item['action_type'] as String;
      final payload = item['payload'] as String;

      try {
        switch (actionType) {
          case 'ACCEPT':
            await acceptCase(payload);
            break;
          case 'COMPLETE':
            await completeCase(payload);
            break;
        }

        // Remove from queue on success
        await _database!.delete(
          Tables.syncQueue,
          where: 'id = ?',
          whereArgs: [item['id']],
        );
      } catch (e) {
        // Update attempt count
        await _database!.update(
          Tables.syncQueue,
          {
            'attempts': (item['attempts'] as int? ?? 0) + 1,
            'last_attempt_at': DateTime.now().toIso8601String(),
            'error': e.toString(),
          },
          where: 'id = ?',
          whereArgs: [item['id']],
        );
      }
    }
  }
}
