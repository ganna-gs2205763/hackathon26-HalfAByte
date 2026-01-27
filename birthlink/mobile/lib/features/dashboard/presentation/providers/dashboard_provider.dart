import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/dashboard_stats_model.dart';
import '../../data/repositories/dashboard_repository.dart';
import '../../../inbox/data/models/case_model.dart';
import '../../../inbox/data/repositories/case_repository.dart';

part 'dashboard_provider.g.dart';

/// Provider for dashboard statistics.
@riverpod
class DashboardStatsNotifier extends _$DashboardStatsNotifier {
  @override
  Future<DashboardStatsModel> build() async {
    final repository = ref.watch(dashboardRepositoryProvider);
    return repository.fetchStats();
  }

  /// Refresh statistics from server.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final repository = ref.read(dashboardRepositoryProvider);
      return repository.fetchStats();
    });
  }
}

/// Provider for zone statistics.
@riverpod
Future<List<ZoneStatsModel>> zoneStats(ZoneStatsRef ref) async {
  final repository = ref.watch(dashboardRepositoryProvider);
  return repository.fetchZoneStats();
}

/// Provider for recent cases (for dashboard overview).
@riverpod
class RecentCasesNotifier extends _$RecentCasesNotifier {
  @override
  Future<List<CaseModel>> build() async {
    final repository = ref.watch(caseRepositoryProvider);
    // Fetch recent cases with limit
    return repository.fetchAllCases(size: 5);
  }

  /// Refresh recent cases.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final repository = ref.read(caseRepositoryProvider);
      return repository.fetchAllCases(size: 5);
    });
  }
}

/// Provider for pending emergency cases (for dashboard alerts).
@riverpod
List<CaseModel> pendingEmergencies(PendingEmergenciesRef ref) {
  final casesAsync = ref.watch(recentCasesNotifierProvider);

  return casesAsync.maybeWhen(
    data: (cases) => cases
        .where((c) => c.isEmergency && c.isPending)
        .toList(),
    orElse: () => [],
  );
}
