import 'package:freezed_annotation/freezed_annotation.dart';

part 'dashboard_stats_model.freezed.dart';
part 'dashboard_stats_model.g.dart';

/// Model representing dashboard statistics.
/// Maps to the backend DashboardStatsDto.
@freezed
class DashboardStatsModel with _$DashboardStatsModel {
  const factory DashboardStatsModel({
    required int totalMothers,
    required int totalVolunteers,
    required int activeVolunteers,
    required int pendingRequests,
    required int activeRequests,
    required int completedToday,
    @Default({}) Map<String, int> mothersByZone,
    @Default({}) Map<String, int> requestsByStatus,
    @Default({}) Map<String, int> volunteersBySkill,
    @Default([]) List<DueDateCluster> upcomingDueDates,
  }) = _DashboardStatsModel;

  const DashboardStatsModel._();

  factory DashboardStatsModel.fromJson(Map<String, dynamic> json) =>
      _$DashboardStatsModelFromJson(json);

  /// Get total active cases (pending + accepted).
  int get totalActiveCases => pendingRequests + activeRequests;

  /// Get volunteer availability ratio as formatted string.
  String get volunteerAvailabilityRatio =>
      '$activeVolunteers/$totalVolunteers';

  /// Check if there are pending emergencies.
  bool get hasPendingEmergencies => pendingRequests > 0;

  /// Get the zone with most mothers.
  String? get topZone {
    if (mothersByZone.isEmpty) return null;
    return mothersByZone.entries
        .reduce((a, b) => a.value > b.value ? a : b)
        .key;
  }
}

/// Model representing a cluster of due dates.
@freezed
class DueDateCluster with _$DueDateCluster {
  const factory DueDateCluster({
    required String date,
    required int count,
  }) = _DueDateCluster;

  factory DueDateCluster.fromJson(Map<String, dynamic> json) =>
      _$DueDateClusterFromJson(json);
}

/// Heat level for zone load visualization.
enum HeatLevel {
  low,      // Green - manageable load
  medium,   // Yellow - moderate load
  high,     // Orange - high load
  critical, // Red - critical load
}

/// Model representing zone statistics.
@freezed
class ZoneStatsModel with _$ZoneStatsModel {
  const factory ZoneStatsModel({
    required String zone,
    required int motherCount,
    required int volunteerCount,
    required int pendingCases,
    required int activeCases,
  }) = _ZoneStatsModel;

  const ZoneStatsModel._();

  factory ZoneStatsModel.fromJson(Map<String, dynamic> json) =>
      _$ZoneStatsModelFromJson(json);

  /// Calculate load score: cases per volunteer.
  /// Higher score = more cases per volunteer = higher load.
  double get loadScore =>
      (pendingCases + activeCases) / (volunteerCount > 0 ? volunteerCount : 1);

  /// Get heat level based on load score.
  HeatLevel get heatLevel {
    if (loadScore > 3.0) return HeatLevel.critical;
    if (loadScore > 1.5) return HeatLevel.high;
    if (loadScore > 0.5) return HeatLevel.medium;
    return HeatLevel.low;
  }

  /// Get total active case count.
  int get totalCases => pendingCases + activeCases;
}
