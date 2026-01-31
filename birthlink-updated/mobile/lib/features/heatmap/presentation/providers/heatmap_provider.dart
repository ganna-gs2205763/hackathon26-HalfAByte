import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../dashboard/data/models/dashboard_stats_model.dart';
import '../../../dashboard/data/repositories/dashboard_repository.dart';
import '../../../settings/presentation/providers/volunteer_provider.dart';

part 'heatmap_provider.g.dart';

/// Provider for all zone statistics (NGO view).
@riverpod
class AllZonesNotifier extends _$AllZonesNotifier {
  @override
  Future<List<ZoneStatsModel>> build() async {
    final repository = ref.watch(dashboardRepositoryProvider);
    return repository.fetchZoneStats();
  }

  /// Refresh zones from server.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final repository = ref.read(dashboardRepositoryProvider);
      return repository.fetchZoneStats();
    });
  }
}

/// Provider for volunteer's zone statistics (Volunteer view).
/// Filters zones based on volunteer's assigned zones.
@riverpod
class VolunteerZonesNotifier extends _$VolunteerZonesNotifier {
  @override
  Future<List<ZoneStatsModel>> build() async {
    // Get volunteer's assigned zones
    final volunteerAsync = ref.watch(volunteerProfileNotifierProvider);
    final volunteer = volunteerAsync.valueOrNull;

    if (volunteer == null) {
      return [];
    }

    // Fetch all zone stats
    final repository = ref.watch(dashboardRepositoryProvider);
    final allZones = await repository.fetchZoneStats();

    // Filter to only volunteer's zones
    final volunteerZones = volunteer.zones.map((z) => z.toLowerCase()).toSet();
    return allZones
        .where((zone) => volunteerZones.contains(zone.zone.toLowerCase()))
        .toList();
  }

  /// Refresh zones from server.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final volunteerAsync = ref.read(volunteerProfileNotifierProvider);
      final volunteer = volunteerAsync.valueOrNull;

      if (volunteer == null) {
        return <ZoneStatsModel>[];
      }

      final repository = ref.read(dashboardRepositoryProvider);
      final allZones = await repository.fetchZoneStats();

      final volunteerZones = volunteer.zones.map((z) => z.toLowerCase()).toSet();
      return allZones
          .where((zone) => volunteerZones.contains(zone.zone.toLowerCase()))
          .toList();
    });
  }
}

/// Provider for heatmap summary statistics.
@riverpod
HeatmapSummary heatmapSummary(HeatmapSummaryRef ref) {
  final zonesAsync = ref.watch(allZonesNotifierProvider);

  return zonesAsync.maybeWhen(
    data: (zones) {
      final totalCases = zones.fold(0, (sum, z) => sum + z.totalCases);
      final totalVolunteers = zones.fold(0, (sum, z) => sum + z.volunteerCount);
      final criticalZones = zones.where((z) => z.heatLevel == HeatLevel.critical).length;
      final highZones = zones.where((z) => z.heatLevel == HeatLevel.high).length;

      return HeatmapSummary(
        totalZones: zones.length,
        totalCases: totalCases,
        totalVolunteers: totalVolunteers,
        criticalZones: criticalZones,
        highLoadZones: highZones,
      );
    },
    orElse: () => const HeatmapSummary(
      totalZones: 0,
      totalCases: 0,
      totalVolunteers: 0,
      criticalZones: 0,
      highLoadZones: 0,
    ),
  );
}

/// Summary data class for heatmap statistics.
class HeatmapSummary {
  final int totalZones;
  final int totalCases;
  final int totalVolunteers;
  final int criticalZones;
  final int highLoadZones;

  const HeatmapSummary({
    required this.totalZones,
    required this.totalCases,
    required this.totalVolunteers,
    required this.criticalZones,
    required this.highLoadZones,
  });

  bool get hasAlerts => criticalZones > 0 || highLoadZones > 0;
}
