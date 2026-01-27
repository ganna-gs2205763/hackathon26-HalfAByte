import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/case_model.dart';
import '../../data/repositories/case_repository.dart';

part 'inbox_provider.g.dart';

/// Filter options for the inbox.
enum InboxFilter {
  all,
  pending,
  accepted,
  emergency,
}

/// Provider for the current inbox filter.
@riverpod
class InboxFilterNotifier extends _$InboxFilterNotifier {
  @override
  InboxFilter build() => InboxFilter.all;

  void setFilter(InboxFilter filter) {
    state = filter;
  }
}

/// Provider for inbox cases with filtering and refresh support.
@riverpod
class InboxNotifier extends _$InboxNotifier {
  @override
  Future<List<CaseModel>> build() async {
    final repository = ref.watch(caseRepositoryProvider);
    return repository.getCases();
  }

  /// Refresh cases from server.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final repository = ref.read(caseRepositoryProvider);
      await repository.syncFromServer();
      return repository.getCases();
    });
  }

  /// Accept a case.
  Future<void> acceptCase(String caseId) async {
    try {
      await ref.read(caseRepositoryProvider).acceptCase(caseId);
      ref.invalidateSelf();
    } catch (e) {
      // Show error but don't change state
      rethrow;
    }
  }

  /// Complete a case.
  Future<void> completeCase(String caseId) async {
    try {
      await ref.read(caseRepositoryProvider).completeCase(caseId);
      ref.invalidateSelf();
    } catch (e) {
      // Show error but don't change state
      rethrow;
    }
  }
}

/// Provider for filtered cases based on current filter.
@riverpod
List<CaseModel> filteredCases(FilteredCasesRef ref) {
  final casesAsync = ref.watch(inboxNotifierProvider);
  final filter = ref.watch(inboxFilterNotifierProvider);

  return casesAsync.maybeWhen(
    data: (cases) {
      switch (filter) {
        case InboxFilter.all:
          return cases;
        case InboxFilter.pending:
          return cases.where((c) => c.isPending).toList();
        case InboxFilter.accepted:
          return cases.where((c) => c.isAccepted || c.isInProgress).toList();
        case InboxFilter.emergency:
          return cases.where((c) => c.isEmergency).toList();
      }
    },
    orElse: () => [],
  );
}

/// Provider for case counts by status.
@riverpod
Map<String, int> caseCounts(CaseCountsRef ref) {
  final casesAsync = ref.watch(inboxNotifierProvider);

  return casesAsync.maybeWhen(
    data: (cases) => {
      'total': cases.length,
      'pending': cases.where((c) => c.isPending).length,
      'accepted': cases.where((c) => c.isAccepted || c.isInProgress).length,
      'emergency': cases.where((c) => c.isEmergency).length,
      'completed': cases.where((c) => c.isCompleted).length,
    },
    orElse: () => {
      'total': 0,
      'pending': 0,
      'accepted': 0,
      'emergency': 0,
      'completed': 0,
    },
  );
}
