// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'heatmap_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$heatmapSummaryHash() => r'4cfb7aacf2254c28a37effff518b898e6012fe51';

/// Provider for heatmap summary statistics.
///
/// Copied from [heatmapSummary].
@ProviderFor(heatmapSummary)
final heatmapSummaryProvider = AutoDisposeProvider<HeatmapSummary>.internal(
  heatmapSummary,
  name: r'heatmapSummaryProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$heatmapSummaryHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef HeatmapSummaryRef = AutoDisposeProviderRef<HeatmapSummary>;
String _$allZonesNotifierHash() => r'aad1b5ba0580aca09779ef452993be6b12ddb949';

/// Provider for all zone statistics (NGO view).
///
/// Copied from [AllZonesNotifier].
@ProviderFor(AllZonesNotifier)
final allZonesNotifierProvider = AutoDisposeAsyncNotifierProvider<
    AllZonesNotifier, List<ZoneStatsModel>>.internal(
  AllZonesNotifier.new,
  name: r'allZonesNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$allZonesNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$AllZonesNotifier = AutoDisposeAsyncNotifier<List<ZoneStatsModel>>;
String _$volunteerZonesNotifierHash() =>
    r'9692771f3a22f2880a5f88aced4aa6968013774b';

/// Provider for volunteer's zone statistics (Volunteer view).
/// Filters zones based on volunteer's assigned zones.
///
/// Copied from [VolunteerZonesNotifier].
@ProviderFor(VolunteerZonesNotifier)
final volunteerZonesNotifierProvider = AutoDisposeAsyncNotifierProvider<
    VolunteerZonesNotifier, List<ZoneStatsModel>>.internal(
  VolunteerZonesNotifier.new,
  name: r'volunteerZonesNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$volunteerZonesNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$VolunteerZonesNotifier
    = AutoDisposeAsyncNotifier<List<ZoneStatsModel>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
