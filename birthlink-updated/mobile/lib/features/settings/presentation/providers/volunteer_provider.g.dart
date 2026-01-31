// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'volunteer_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$isVolunteerAvailableHash() =>
    r'71a1f0ed8d31adbf6706232d4b4d19f7d34d5bc1';

/// Provider for checking if volunteer is available.
///
/// Copied from [isVolunteerAvailable].
@ProviderFor(isVolunteerAvailable)
final isVolunteerAvailableProvider = AutoDisposeProvider<bool>.internal(
  isVolunteerAvailable,
  name: r'isVolunteerAvailableProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$isVolunteerAvailableHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef IsVolunteerAvailableRef = AutoDisposeProviderRef<bool>;
String _$volunteerDisplayNameHash() =>
    r'39fb5169e784b5354df207aecc7b1d66ed9f23c4';

/// Provider for volunteer display name.
///
/// Copied from [volunteerDisplayName].
@ProviderFor(volunteerDisplayName)
final volunteerDisplayNameProvider = AutoDisposeProvider<String>.internal(
  volunteerDisplayName,
  name: r'volunteerDisplayNameProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$volunteerDisplayNameHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef VolunteerDisplayNameRef = AutoDisposeProviderRef<String>;
String _$volunteerProfileNotifierHash() =>
    r'f81f76f391fe3a4b9673e06f696e7885a31adf78';

/// Provider for current volunteer profile.
///
/// Copied from [VolunteerProfileNotifier].
@ProviderFor(VolunteerProfileNotifier)
final volunteerProfileNotifierProvider = AutoDisposeAsyncNotifierProvider<
    VolunteerProfileNotifier, VolunteerModel?>.internal(
  VolunteerProfileNotifier.new,
  name: r'volunteerProfileNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$volunteerProfileNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$VolunteerProfileNotifier = AutoDisposeAsyncNotifier<VolunteerModel?>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
