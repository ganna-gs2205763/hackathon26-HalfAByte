import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/volunteer_model.dart';
import '../../data/repositories/volunteer_repository.dart';

part 'volunteer_provider.g.dart';

/// Provider for current volunteer profile.
@riverpod
class VolunteerProfileNotifier extends _$VolunteerProfileNotifier {
  @override
  Future<VolunteerModel?> build() async {
    final repository = ref.watch(volunteerRepositoryProvider);
    return repository.getProfile();
  }

  /// Refresh profile from server.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final repository = ref.read(volunteerRepositoryProvider);
      return repository.fetchProfile();
    });
  }

  /// Update availability status.
  Future<void> updateAvailability(String availability) async {
    final currentState = state;
    
    // Optimistic update
    if (currentState.hasValue && currentState.value != null) {
      state = AsyncData(currentState.value!.copyWith(
        availability: availability,
      ));
    }

    try {
      final repository = ref.read(volunteerRepositoryProvider);
      final updated = await repository.updateAvailability(availability);
      state = AsyncData(updated);
    } catch (e) {
      // Revert on error
      state = currentState;
      rethrow;
    }
  }

  /// Toggle availability between AVAILABLE and BUSY.
  Future<void> toggleAvailability() async {
    final current = state.valueOrNull;
    if (current == null) return;

    final newStatus = current.isAvailable ? 'BUSY' : 'AVAILABLE';
    await updateAvailability(newStatus);
  }
}

/// Provider for checking if volunteer is available.
@riverpod
bool isVolunteerAvailable(IsVolunteerAvailableRef ref) {
  final profileAsync = ref.watch(volunteerProfileNotifierProvider);
  return profileAsync.maybeWhen(
    data: (profile) => profile?.isAvailable ?? false,
    orElse: () => false,
  );
}

/// Provider for volunteer display name.
@riverpod
String volunteerDisplayName(VolunteerDisplayNameRef ref) {
  final profileAsync = ref.watch(volunteerProfileNotifierProvider);
  return profileAsync.maybeWhen(
    data: (profile) => profile?.displayName ?? '',
    orElse: () => '',
  );
}
