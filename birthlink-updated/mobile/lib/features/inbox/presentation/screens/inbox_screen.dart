import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../../shared/widgets/error_view.dart';
import '../../../../shared/widgets/loading_indicator.dart';
import '../providers/inbox_provider.dart';
import '../widgets/case_card.dart';
import '../widgets/filter_chips.dart';

/// Inbox screen showing volunteer's assigned cases.
class InboxScreen extends ConsumerStatefulWidget {
  const InboxScreen({super.key});

  @override
  ConsumerState<InboxScreen> createState() => _InboxScreenState();
}

class _InboxScreenState extends ConsumerState<InboxScreen> {
  String? _loadingCaseId;

  @override
  Widget build(BuildContext context) {
    final casesAsync = ref.watch(inboxNotifierProvider);
    final filteredCasesList = ref.watch(filteredCasesProvider);
    final l10n = context.l10n;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('nav_inbox')),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => _refreshCases(),
            tooltip: l10n.translate('refresh'),
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Filter chips
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 8),
              child: InboxFilterChips(),
            ),
            const Divider(height: 1),

            // Cases list
            Expanded(
              child: casesAsync.when(
                loading: () => LoadingIndicator(
                  message: l10n.translate('loading'),
                ),
                error: (error, stack) => ErrorView(
                  message: _getErrorMessage(l10n, error),
                  onRetry: () => _refreshCases(),
                ),
                data: (cases) {
                  if (filteredCasesList.isEmpty) {
                    return _buildEmptyState(context, l10n);
                  }

                  return RefreshIndicator(
                    onRefresh: () async {
                      await ref.read(inboxNotifierProvider.notifier).refresh();
                    },
                    child: ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: filteredCasesList.length,
                      itemBuilder: (context, index) {
                        final caseModel = filteredCasesList[index];
                        return CaseCard(
                          caseModel: caseModel,
                          isLoading: _loadingCaseId == caseModel.caseId,
                          onAccept: caseModel.isPending
                              ? () => _acceptCase(caseModel.caseId)
                              : null,
                          onComplete: caseModel.isAccepted
                              ? () => _completeCase(caseModel.caseId)
                              : null,
                        );
                      },
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context, AppLocalizations l10n) {
    final filter = ref.watch(inboxFilterNotifierProvider);

    String message;
    IconData icon;

    switch (filter) {
      case InboxFilter.pending:
        message = l10n.translate('no_cases');
        icon = Icons.pending_actions_outlined;
        break;
      case InboxFilter.accepted:
        message = l10n.translate('no_cases');
        icon = Icons.assignment_turned_in_outlined;
        break;
      case InboxFilter.emergency:
        message = l10n.translate('no_cases');
        icon = Icons.warning_amber_outlined;
        break;
      case InboxFilter.all:
      default:
        message = l10n.translate('no_cases');
        icon = Icons.inbox_outlined;
    }

    return EmptyView(
      message: message,
      icon: icon,
      action: ElevatedButton.icon(
        onPressed: () => _refreshCases(),
        icon: const Icon(Icons.refresh),
        label: Text(l10n.translate('refresh')),
      ),
    );
  }

  Future<void> _refreshCases() async {
    await ref.read(inboxNotifierProvider.notifier).refresh();
  }

  Future<void> _acceptCase(String caseId) async {
    setState(() {
      _loadingCaseId = caseId;
    });

    try {
      await ref.read(inboxNotifierProvider.notifier).acceptCase(caseId);
      if (mounted) {
        _showSnackBar(context.l10n.translate('accepted'), isSuccess: true);
      }
    } catch (e) {
      if (mounted) {
        _showSnackBar(_getErrorMessage(context.l10n, e), isError: true);
      }
    } finally {
      if (mounted) {
        setState(() {
          _loadingCaseId = null;
        });
      }
    }
  }

  Future<void> _completeCase(String caseId) async {
    final l10n = context.l10n;

    // Show confirmation dialog
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.translate('confirm')),
        content: Text('${l10n.translate('complete')} $caseId?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text(l10n.translate('cancel')),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text(l10n.translate('complete')),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    setState(() {
      _loadingCaseId = caseId;
    });

    try {
      await ref.read(inboxNotifierProvider.notifier).completeCase(caseId);
      if (mounted) {
        _showSnackBar(l10n.translate('completed'), isSuccess: true);
      }
    } catch (e) {
      if (mounted) {
        _showSnackBar(_getErrorMessage(l10n, e), isError: true);
      }
    } finally {
      if (mounted) {
        setState(() {
          _loadingCaseId = null;
        });
      }
    }
  }

  void _showSnackBar(String message, {bool isSuccess = false, bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isSuccess
            ? AppColors.success
            : isError
                ? AppColors.emergency
                : null,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  String _getErrorMessage(AppLocalizations l10n, Object error) {
    // Check for specific error types
    final errorStr = error.toString().toLowerCase();
    if (errorStr.contains('network') || errorStr.contains('socket')) {
      return l10n.translate('error_network');
    }
    if (errorStr.contains('timeout')) {
      return l10n.translate('error_timeout');
    }
    if (errorStr.contains('server') || errorStr.contains('500')) {
      return l10n.translate('error_server');
    }
    return l10n.translate('error_unknown');
  }
}
