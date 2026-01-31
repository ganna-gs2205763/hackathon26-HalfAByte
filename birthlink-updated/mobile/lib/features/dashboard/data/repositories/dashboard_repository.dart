import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/api_endpoints.dart';
import '../../../../core/network/api_exceptions.dart';
import '../../../../core/network/dio_client.dart';
import '../models/dashboard_stats_model.dart';

/// Provider for the dashboard repository.
final dashboardRepositoryProvider = Provider<DashboardRepository>((ref) {
  final dioClient = ref.watch(dioClientProvider);
  return DashboardRepository(dioClient: dioClient);
});

/// Repository for dashboard data operations.
class DashboardRepository {
  final DioClient _dioClient;

  DashboardRepository({
    required DioClient dioClient,
  }) : _dioClient = dioClient;

  /// Fetch dashboard statistics from the server.
  Future<DashboardStatsModel> fetchStats() async {
    try {
      final response = await _dioClient.get<Map<String, dynamic>>(
        ApiEndpoints.dashboardStats,
      );

      if (response.data != null) {
        return DashboardStatsModel.fromJson(response.data!);
      }

      throw UnknownException('No data received from server');
    } on DioException catch (e) {
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }

  /// Fetch zone statistics.
  Future<List<ZoneStatsModel>> fetchZoneStats() async {
    try {
      final response = await _dioClient.get<List<dynamic>>(
        ApiEndpoints.dashboardZones,
      );

      return (response.data ?? [])
          .map((json) => ZoneStatsModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      if (e.error is ApiException) {
        rethrow;
      }
      throw NetworkException();
    }
  }
}
