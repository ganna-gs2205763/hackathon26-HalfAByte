import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/status_badge.dart';
import '../../data/models/case_model.dart';

/// Card widget for displaying a case in the inbox.
class CaseCard extends StatelessWidget {
  final CaseModel caseModel;
  final VoidCallback? onAccept;
  final VoidCallback? onComplete;
  final bool isLoading;

  const CaseCard({
    super.key,
    required this.caseModel,
    this.onAccept,
    this.onComplete,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final isEmergency = caseModel.isEmergency;

    return Card(
      color: isEmergency ? AppColors.emergency.withValues(alpha: 0.05) : null,
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => context.push('/case/${caseModel.caseId}'),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header: Type Badge + Case ID
              _buildHeader(context, l10n, isEmergency),
              const SizedBox(height: 12),

              // Details: Zone, Risk, Due Date
              _buildDetails(context, l10n),
              const SizedBox(height: 12),

              // Status Badge
              _buildStatusRow(context, l10n),

              // Action Buttons
              if (caseModel.isPending && onAccept != null)
                _buildAcceptButton(context, l10n),
              if (caseModel.isAccepted && onComplete != null)
                _buildCompleteButton(context, l10n),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context, AppLocalizations l10n, bool isEmergency) {
    return Row(
      children: [
        // Type badge
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          decoration: BoxDecoration(
            color: isEmergency ? AppColors.emergency : AppColors.primary,
            borderRadius: BorderRadius.circular(4),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                isEmergency ? Icons.warning_amber : Icons.support_agent,
                color: Colors.white,
                size: 14,
              ),
              const SizedBox(width: 4),
              Text(
                isEmergency
                    ? l10n.translate('emergency')
                    : l10n.translate('support'),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
        const Spacer(),
        // Case ID
        Text(
          caseModel.caseId,
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.textSecondary,
                fontFamily: 'monospace',
              ),
        ),
      ],
    );
  }

  Widget _buildDetails(BuildContext context, AppLocalizations l10n) {
    return Column(
      children: [
        // Zone
        _buildDetailRow(
          context,
          Icons.location_on_outlined,
          '${l10n.translate('zone')}: ${caseModel.zone}',
        ),
        if (caseModel.riskLevel != null) ...[
          const SizedBox(height: 4),
          _buildDetailRow(
            context,
            Icons.health_and_safety_outlined,
            '${l10n.translate('risk_level')}: ${_getRiskLevelDisplay(l10n, caseModel.riskLevel!)}',
            color: _getRiskColor(caseModel.riskLevel!),
          ),
        ],
        if (caseModel.dueDate != null) ...[
          const SizedBox(height: 4),
          _buildDetailRow(
            context,
            Icons.calendar_today_outlined,
            '${l10n.translate('due_date')}: ${caseModel.dueDate}',
          ),
        ],
      ],
    );
  }

  Widget _buildDetailRow(BuildContext context, IconData icon, String text,
      {Color? color}) {
    return Row(
      children: [
        Icon(
          icon,
          size: 16,
          color: color ?? AppColors.textSecondary,
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: color ?? AppColors.textSecondary,
                ),
          ),
        ),
      ],
    );
  }

  Widget _buildStatusRow(BuildContext context, AppLocalizations l10n) {
    return Row(
      children: [
        StatusBadge(
          status: caseModel.status,
          label: _getStatusLabel(l10n, caseModel.status),
        ),
        const Spacer(),
        Text(
          _formatDate(caseModel.createdAt),
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.textSecondary,
              ),
        ),
      ],
    );
  }

  Widget _buildAcceptButton(BuildContext context, AppLocalizations l10n) {
    return Padding(
      padding: const EdgeInsets.only(top: 12),
      child: SizedBox(
        width: double.infinity,
        child: AppButton(
          text: l10n.translate('accept'),
          style: caseModel.isEmergency
              ? AppButtonStyle.emergency
              : AppButtonStyle.primary,
          isLoading: isLoading,
          isFullWidth: true,
          onPressed: isLoading ? null : onAccept,
          icon: Icons.check_circle_outline,
        ),
      ),
    );
  }

  Widget _buildCompleteButton(BuildContext context, AppLocalizations l10n) {
    return Padding(
      padding: const EdgeInsets.only(top: 12),
      child: SizedBox(
        width: double.infinity,
        child: AppButton(
          text: l10n.translate('complete'),
          style: AppButtonStyle.secondary,
          isLoading: isLoading,
          isFullWidth: true,
          onPressed: isLoading ? null : onComplete,
          icon: Icons.done_all,
        ),
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
        return l10n.translate('low');
      case 'MEDIUM':
        return l10n.translate('medium');
      case 'HIGH':
        return l10n.translate('high');
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

  String _formatDate(String dateStr) {
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year}';
    } catch (e) {
      return dateStr;
    }
  }
}
