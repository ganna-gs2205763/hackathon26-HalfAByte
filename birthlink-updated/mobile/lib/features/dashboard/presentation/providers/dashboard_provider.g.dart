// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dashboard_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$zoneStatsHash() => r'127c9c459be439ebce8536b6bebb0435d1f45656';

/// Provider for zone statistics.
///
/// Copied from [zoneStats].
@ProviderFor(zoneStats)
final zoneStatsProvider =
    AutoDisposeFutureProvider<List<ZoneStatsModel>>.internal(
  zoneStats,
  name: r'zoneStatsProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$zoneStatsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef ZoneStatsRef = AutoDisposeFutureProviderRef<List<ZoneStatsModel>>;
String _$pendingEmergenciesHash() =>
    r'873f9a575180412fa4e205eaf933a815332d07b0';

/// Provider for pending emergency cases (for dashboard alerts).
///
/// Copied from [pendingEmergencies].
@ProviderFor(pendingEmergencies)
final pendingEmergenciesProvider =
    AutoDisposeProvider<List<CaseModel>>.internal(
  pendingEmergencies,
  name: r'pendingEmergenciesProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$pendingEmergenciesHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef PendingEmergenciesRef = AutoDisposeProviderRef<List<CaseModel>>;
String _$dashboardStatsNotifierHash() =>
    r'dbf6b224bcd9f2de78fceb74f100135c9b7fb73a';

/// Provider for dashboard statistics.
///
/// Copied from [DashboardStatsNotifier].
@ProviderFor(DashboardStatsNotifier)
final dashboardStatsNotifierProvider = AutoDisposeAsyncNotifierProvider<
    DashboardStatsNotifier, DashboardStatsModel>.internal(
  DashboardStatsNotifier.new,
  name: r'dashboardStatsNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$dashboardStatsNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$DashboardStatsNotifier
    = AutoDisposeAsyncNotifier<DashboardStatsModel>;
String _$recentCasesNotifierHash() =>
    r'44887fb2931e7afd1ef2ddcde1b61fe9109ce6ab';

/// Provider for recent cases (for dashboard overview).
///
/// Copied from [RecentCasesNotifier].
@ProviderFor(RecentCasesNotifier)
final recentCasesNotifierProvider = AutoDisposeAsyncNotifierProvider<
    RecentCasesNotifier, List<CaseModel>>.internal(
  RecentCasesNotifier.new,
  name: r'recentCasesNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$recentCasesNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$RecentCasesNotifier = AutoDisposeAsyncNotifier<List<CaseModel>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
