import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sqflite/sqflite.dart';

import '../../../../core/constants/api_endpoints.dart';
import '../../../../core/database/tables.dart';
import '../../../../core/network/api_exceptions.dart';
import '../../../../core/network/dio_client.dart';
import '../../../../shared/providers/database_provider.dart';
import '../../../../shared/providers/phone_provider.dart';
import '../models/volunteer_model.dart';

/// Provider for the volunteer repository.
final volunteerRepositoryProvider = Provider<VolunteerRepository>((ref) {
  final dioClient = ref.watch(dioClientProvider);
  final dbAsync = ref.watch(databaseProvider);
  final phoneNumber = ref.watch(phoneNumberProvider);

  // Set phone number header if available
  if (phoneNumber != null) {
    dioClient.setVolunteerPhone(phoneNumber);
  }

  return VolunteerRepository(
    dioClient: dioClient,
    database: dbAsync.valueOrNull,
    phoneNumber: phoneNumber,
  );
});

/// Repository for volunteer data operations with offline support.
class VolunteerRepository {
  final DioClient _dioClient;
  final Database? _database;
  final String? _phoneNumber;

  VolunteerRepository({
    required DioClient dioClient,
    Database? database,
    String? phoneNumber,
  })  : _dioClient = dioClient,
        _database = database,
        _phoneNumber = phoneNumber;

  /// Fetch current volunteer profile from server.
  Future<VolunteerModel> fetchProfile() async {
    if (_phoneNumber == null || _phoneNumber!.isEmpty) {
      throw UnauthorizedException();
    }

    try {
      final response = await _dioClient.get<Map<String, dynamic>>(
        ApiEndpoints.volunteerMe,
      );

      if (response.data != null) {
        final volunteer = VolunteerModel.fromJson(response.data!);
        await _cacheVolunteer(volunteer);
        return volunteer;
      }

      throw UnknownException('No data received from server');
    } on DioException catch (e) {
      // Try local cache
      final cached = await _getCachedVolunteer();
      if (cached != null) {
        return cached;
      }

      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Get volunteer profile with offline fallback.
  Future<VolunteerModel?> getProfile() async {
    try {
      return await fetchProfile();
    } catch (e) {
      return await _getCachedVolunteer();
    }
  }

  /// Update volunteer availability status.
  Future<VolunteerModel> updateAvailability(String availability) async {
    if (_phoneNumber == null || _phoneNumber!.isEmpty) {
      throw UnauthorizedException();
    }

    try {
      final response = await _dioClient.put<Map<String, dynamic>>(
        ApiEndpoints.volunteerAvailability,
        data: {'availability': availability},
      );

      if (response.data != null) {
        final volunteer = VolunteerModel.fromJson(response.data!);
        await _cacheVolunteer(volunteer);
        return volunteer;
      }

      throw UnknownException('No data received from server');
    } on DioException catch (e) {
      // Queue for offline sync
      await _queueAvailabilityUpdate(availability);

      // Update local cache optimistically
      await _updateLocalAvailability(availability);

      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Fetch all volunteers (for dashboard/coordinators).
  Future<List<VolunteerModel>> fetchAllVolunteers() async {
    try {
      final response = await _dioClient.get<List<dynamic>>(
        ApiEndpoints.dashboardVolunteers,
      );

      return (response.data ?? [])
          .map((json) => VolunteerModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Cache volunteer to local database.
  Future<void> _cacheVolunteer(VolunteerModel volunteer) async {
    if (_database == null) return;

    await _database!.insert(
      Tables.volunteers,
      volunteer.toDbMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  /// Get cached volunteer from local database.
  Future<VolunteerModel?> _getCachedVolunteer() async {
    if (_database == null || _phoneNumber == null) return null;

    final maps = await _database!.query(
      Tables.volunteers,
      where: 'phone_number = ?',
      whereArgs: [_phoneNumber],
    );

    if (maps.isNotEmpty) {
      return VolunteerModel.fromDbMap(maps.first);
    }
    return null;
  }

  /// Update local volunteer availability.
  Future<void> _updateLocalAvailability(String availability) async {
    if (_database == null || _phoneNumber == null) return;

    await _database!.update(
      Tables.volunteers,
      {'availability': availability},
      where: 'phone_number = ?',
      whereArgs: [_phoneNumber],
    );
  }

  /// Queue availability update for offline sync.
  Future<void> _queueAvailabilityUpdate(String availability) async {
    if (_database == null) return;

    await _database!.insert(
      Tables.syncQueue,
      {
        'action_type': 'UPDATE_AVAILABILITY',
        'payload': availability,
        'created_at': DateTime.now().toIso8601String(),
      },
    );
  }

  /// Process queued offline availability updates.
  Future<void> processOfflineQueue() async {
    if (_database == null) return;

    final queue = await _database!.query(
      Tables.syncQueue,
      where: 'action_type = ?',
      whereArgs: ['UPDATE_AVAILABILITY'],
    );

    for (final item in queue) {
      final availability = item['payload'] as String;

      try {
        await updateAvailability(availability);

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
