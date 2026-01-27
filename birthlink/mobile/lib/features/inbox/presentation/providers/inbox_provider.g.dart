// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'inbox_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$filteredCasesHash() => r'3df9d3da652fa0cde6abb92f45af25fdb8706f14';

/// Provider for filtered cases based on current filter.
///
/// Copied from [filteredCases].
@ProviderFor(filteredCases)
final filteredCasesProvider = AutoDisposeProvider<List<CaseModel>>.internal(
  filteredCases,
  name: r'filteredCasesProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$filteredCasesHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef FilteredCasesRef = AutoDisposeProviderRef<List<CaseModel>>;
String _$caseCountsHash() => r'ff9bb362b801dcd24b205f45b73ff57b8e9e5b77';

/// Provider for case counts by status.
///
/// Copied from [caseCounts].
@ProviderFor(caseCounts)
final caseCountsProvider = AutoDisposeProvider<Map<String, int>>.internal(
  caseCounts,
  name: r'caseCountsProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$caseCountsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef CaseCountsRef = AutoDisposeProviderRef<Map<String, int>>;
String _$inboxFilterNotifierHash() =>
    r'bcc4eaa05dd9f730ba6f1cb69643d038655dc370';

/// Provider for the current inbox filter.
///
/// Copied from [InboxFilterNotifier].
@ProviderFor(InboxFilterNotifier)
final inboxFilterNotifierProvider =
    AutoDisposeNotifierProvider<InboxFilterNotifier, InboxFilter>.internal(
  InboxFilterNotifier.new,
  name: r'inboxFilterNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$inboxFilterNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$InboxFilterNotifier = AutoDisposeNotifier<InboxFilter>;
String _$inboxNotifierHash() => r'40a74354ec776714503bd902346bc0e5e1764282';

/// Provider for inbox cases with filtering and refresh support.
///
/// Copied from [InboxNotifier].
@ProviderFor(InboxNotifier)
final inboxNotifierProvider =
    AutoDisposeAsyncNotifierProvider<InboxNotifier, List<CaseModel>>.internal(
  InboxNotifier.new,
  name: r'inboxNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$inboxNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$InboxNotifier = AutoDisposeAsyncNotifier<List<CaseModel>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
