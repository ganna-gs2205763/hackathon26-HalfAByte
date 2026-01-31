import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/localization/app_localizations.dart';

/// Badge for displaying request status.
class StatusBadge extends StatelessWidget {
  final String status;
  final String? label;
  final bool isCompact;

  const StatusBadge({
    super.key,
    required this.status,
    this.label,
    this.isCompact = false,
  });

  @override
  Widget build(BuildContext context) {
    final color = _getStatusColor(status);
    final text = label ?? _getStatusText(context, status);

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: isCompact ? 8 : 12,
        vertical: isCompact ? 4 : 6,
      ),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.5)),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: color,
          fontSize: isCompact ? 11 : 12,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  Color _getStatusColor(String status) {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return AppColors.statusPending;
      case 'ACCEPTED':
        return AppColors.statusAccepted;
      case 'IN_PROGRESS':
        return AppColors.statusInProgress;
      case 'COMPLETED':
        return AppColors.statusCompleted;
      case 'CANCELLED':
        return AppColors.statusCancelled;
      default:
        return AppColors.textSecondary;
    }
  }

  String _getStatusText(BuildContext context, String status) {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return context.tr('status_pending');
      case 'ACCEPTED':
        return context.tr('status_accepted');
      case 'IN_PROGRESS':
        return context.tr('status_in_progress');
      case 'COMPLETED':
        return context.tr('status_completed');
      case 'CANCELLED':
        return context.tr('status_cancelled');
      default:
        return status;
    }
  }
}

/// Badge for displaying risk level.
class RiskBadge extends StatelessWidget {
  final String riskLevel;
  final bool isCompact;

  const RiskBadge({
    super.key,
    required this.riskLevel,
    this.isCompact = false,
  });

  @override
  Widget build(BuildContext context) {
    final color = _getRiskColor(riskLevel);
    final text = _getRiskText(context, riskLevel);

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: isCompact ? 6 : 10,
        vertical: isCompact ? 2 : 4,
      ),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: Colors.white,
          fontSize: isCompact ? 10 : 11,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  Color _getRiskColor(String level) {
    switch (level.toUpperCase()) {
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

  String _getRiskText(BuildContext context, String level) {
    switch (level.toUpperCase()) {
      case 'LOW':
        return context.tr('risk_low');
      case 'MEDIUM':
        return context.tr('risk_medium');
      case 'HIGH':
        return context.tr('risk_high');
      default:
        return level;
    }
  }
}

/// Badge for emergency type indicator.
class EmergencyBadge extends StatelessWidget {
  final bool isEmergency;

  const EmergencyBadge({
    super.key,
    required this.isEmergency,
  });

  @override
  Widget build(BuildContext context) {
    if (!isEmergency) return const SizedBox.shrink();

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: AppColors.emergency,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.warning_amber, color: Colors.white, size: 14),
          const SizedBox(width: 4),
          Text(
            context.tr('case_emergency'),
            style: const TextStyle(
              color: Colors.white,
              fontSize: 11,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }
}
