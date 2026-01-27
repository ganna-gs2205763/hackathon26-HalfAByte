import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/error_view.dart';
import '../../../../shared/widgets/loading_indicator.dart';
import '../../../../shared/widgets/status_badge.dart';
import '../../data/models/case_model.dart';
import '../../data/repositories/case_repository.dart';
import '../providers/inbox_provider.dart';

/// Provider for fetching a single case by ID.
final caseDetailsProvider =
    FutureProvider.family<CaseModel?, String>((ref, caseId) async {
  final repository = ref.watch(caseRepositoryProvider);
  return repository.getCaseById(caseId);
});

/// Screen showing detailed information about a case.
class CaseDetailsScreen extends ConsumerStatefulWidget {
  final String caseId;

  const CaseDetailsScreen({
    super.key,
    required this.caseId,
  });

  @override
  ConsumerState<CaseDetailsScreen> createState() => _CaseDetailsScreenState();
}

class _CaseDetailsScreenState extends ConsumerState<CaseDetailsScreen> {
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    final caseAsync = ref.watch(caseDetailsProvider(widget.caseId));
    final l10n = context.l10n;

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.caseId),
      ),
      body: SafeArea(
        child: caseAsync.when(
          loading: () => LoadingIndicator(
            message: l10n.translate('loading'),
          ),
          error: (error, stack) => ErrorView(
            message: _getErrorMessage(l10n, error),
            onRetry: () => ref.invalidate(caseDetailsProvider(widget.caseId)),
          ),
          data: (caseModel) {
            if (caseModel == null) {
              return ErrorView(
                message: l10n.translate('error_not_found'),
                icon: Icons.search_off,
              );
            }

            return SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header Card
                  _buildHeaderCard(context, l10n, caseModel),
                  const SizedBox(height: 16),

                  // Case Info Section
                  _buildInfoSection(context, l10n, caseModel),
                  const SizedBox(height: 16),

                  // Timeline Section
                  _buildTimelineSection(context, l10n, caseModel),
                  const SizedBox(height: 24),

                  // Action Buttons
                  _buildActionButtons(context, l10n, caseModel),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildHeaderCard(
      BuildContext context, AppLocalizations l10n, CaseModel caseModel) {
    final isEmergency = caseModel.isEmergency;

    return Card(
      color: isEmergency ? AppColors.emergency.withValues(alpha: 0.05) : null,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // Type and Status Row
            Row(
              children: [
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: isEmergency ? AppColors.emergency : AppColors.primary,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        isEmergency ? Icons.warning_amber : Icons.support_agent,
                        color: Colors.white,
                        size: 18,
                      ),
                      const SizedBox(width: 6),
                      Text(
                        isEmergency
                            ? l10n.translate('emergency')
                            : l10n.translate('support'),
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
                const Spacer(),
                StatusBadge(
                  status: caseModel.status,
                  label: _getStatusLabel(l10n, caseModel.status),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Case ID
            Text(
              caseModel.caseId,
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    fontFamily: 'monospace',
                  ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoSection(
      BuildContext context, AppLocalizations l10n, CaseModel caseModel) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              l10n.translate('case_details'),
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const Divider(),

            // Zone
            _buildInfoRow(
              context,
              Icons.location_on_outlined,
              l10n.translate('zone'),
              caseModel.zone,
            ),

            // Risk Level
            if (caseModel.riskLevel != null)
              _buildInfoRow(
                context,
                Icons.health_and_safety_outlined,
                l10n.translate('risk_level'),
                _getRiskLevelDisplay(l10n, caseModel.riskLevel!),
                valueColor: _getRiskColor(caseModel.riskLevel!),
              ),

            // Due Date
            if (caseModel.dueDate != null)
              _buildInfoRow(
                context,
                Icons.calendar_today_outlined,
                l10n.translate('due_date'),
                caseModel.dueDate!,
              ),

            // Assigned Volunteer
            if (caseModel.volunteerName != null)
              _buildInfoRow(
                context,
                Icons.person_outline,
                l10n.translate('assigned_volunteer'),
                caseModel.volunteerName!,
              ),

            // Notes
            if (caseModel.notes != null && caseModel.notes!.isNotEmpty) ...[
              const Divider(),
              Text(
                'Notes',
                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Text(caseModel.notes!),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(
    BuildContext context,
    IconData icon,
    String label,
    String value, {
    Color? valueColor,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: AppColors.textSecondary),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: AppColors.textSecondary,
                      ),
                ),
                Text(
                  value,
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.w500,
                        color: valueColor,
                      ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineSection(
      BuildContext context, AppLocalizations l10n, CaseModel caseModel) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Timeline',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const Divider(),

            // Created
            _buildTimelineItem(
              context,
              Icons.add_circle_outline,
              l10n.translate('created_at'),
              _formatDateTime(caseModel.createdAt),
              AppColors.primary,
              isFirst: true,
            ),

            // Accepted
            if (caseModel.acceptedAt != null)
              _buildTimelineItem(
                context,
                Icons.check_circle_outline,
                l10n.translate('accepted_at'),
                _formatDateTime(caseModel.acceptedAt!),
                AppColors.success,
              ),

            // Closed
            if (caseModel.closedAt != null)
              _buildTimelineItem(
                context,
                caseModel.isCompleted
                    ? Icons.done_all
                    : Icons.cancel_outlined,
                l10n.translate('closed_at'),
                _formatDateTime(caseModel.closedAt!),
                caseModel.isCompleted
                    ? AppColors.success
                    : AppColors.textSecondary,
                isLast: true,
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimelineItem(
    BuildContext context,
    IconData icon,
    String label,
    String time,
    Color color, {
    bool isFirst = false,
    bool isLast = false,
  }) {
    return IntrinsicHeight(
      child: Row(
        children: [
          Column(
            children: [
              if (!isFirst)
                Container(
                  width: 2,
                  height: 12,
                  color: AppColors.divider,
                ),
              Icon(icon, color: color, size: 24),
              if (!isLast)
                Expanded(
                  child: Container(
                    width: 2,
                    color: AppColors.divider,
                  ),
                ),
            ],
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: AppColors.textSecondary,
                        ),
                  ),
                  Text(
                    time,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.w500,
                        ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButtons(
      BuildContext context, AppLocalizations l10n, CaseModel caseModel) {
    if (caseModel.isCompleted || caseModel.isCancelled) {
      return const SizedBox.shrink();
    }

    return Column(
      children: [
        if (caseModel.isPending)
          SizedBox(
            width: double.infinity,
            child: AppButton(
              text: l10n.translate('accept'),
              style: caseModel.isEmergency
                  ? AppButtonStyle.emergency
                  : AppButtonStyle.primary,
              isLoading: _isLoading,
              isFullWidth: true,
              onPressed: _isLoading ? null : () => _acceptCase(caseModel.caseId),
              icon: Icons.check_circle_outline,
            ),
          ),
        if (caseModel.isAccepted) ...[
          SizedBox(
            width: double.infinity,
            child: AppButton(
              text: l10n.translate('complete'),
              style: AppButtonStyle.primary,
              isLoading: _isLoading,
              isFullWidth: true,
              onPressed: _isLoading ? null : () => _completeCase(caseModel.caseId),
              icon: Icons.done_all,
            ),
          ),
        ],
      ],
    );
  }

  Future<void> _acceptCase(String caseId) async {
    setState(() {
      _isLoading = true;
    });

    try {
      await ref.read(inboxNotifierProvider.notifier).acceptCase(caseId);
      ref.invalidate(caseDetailsProvider(caseId));
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
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _completeCase(String caseId) async {
    final l10n = context.l10n;

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
      _isLoading = true;
    });

    try {
      await ref.read(inboxNotifierProvider.notifier).completeCase(caseId);
      ref.invalidate(caseDetailsProvider(caseId));
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
          _isLoading = false;
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

  String _getStatusLabel(AppLocalizations l10n, String status) {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return l10n.translate('pending');
      case 'ACCEPTED':
        return l10n.translate('accepted');
      case 'IN_PROGRESS':
        return l10n.translate('status_in_progress');
      case 'COMPLETED':
        return l10n.translate('completed');
      case 'CANCELLED':
        return l10n.translate('status_cancelled');
      default:
        return status;
    }
  }

  String _getRiskLevelDisplay(AppLocalizations l10n, String riskLevel) {
    switch (riskLevel.toUpperCase()) {
      case 'LOW':
        return l10n.translate('risk_low');
      case 'MEDIUM':
        return l10n.translate('risk_medium');
      case 'HIGH':
        return l10n.translate('risk_high');
      default:
        return riskLevel;
    }
  }

  Color _getRiskColor(String riskLevel) {
    switch (riskLevel.toUpperCase()) {
      case 'LOW':
        return AppColors.riskLow;
      case 'MEDIUM':
        return AppColors.riskMedium;
      case 'HIGH':
        return AppColors.riskHigh;
      default:
        return AppColors.textSecondary;
    }
  }

  String _formatDateTime(String dateStr) {
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
    } catch (e) {
      return dateStr;
    }
  }

  String _getErrorMessage(AppLocalizations l10n, Object error) {
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
    if (errorStr.contains('not found') || errorStr.contains('404')) {
      return l10n.translate('error_not_found');
    }
    return l10n.translate('error_unknown');
  }
}
