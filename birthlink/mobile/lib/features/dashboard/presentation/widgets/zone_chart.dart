import 'package:flutter/material.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';

/// Widget for displaying zone distribution as horizontal bar chart.
class ZoneChart extends StatelessWidget {
  final Map<String, int> data;
  final String title;

  const ZoneChart({
    super.key,
    required this.data,
    this.title = '',
  });

  @override
  Widget build(BuildContext context) {
    if (data.isEmpty) {
      return const SizedBox.shrink();
    }

    final sortedEntries = data.entries.toList()
      ..sort((a, b) => b.value.compareTo(a.value));
    final maxValue = sortedEntries.isEmpty ? 1 : sortedEntries.first.value;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (title.isNotEmpty) ...[
          Text(
            title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 12),
        ],
        ...sortedEntries.map((entry) => _buildBar(context, entry, maxValue)),
      ],
    );
  }

  Widget _buildBar(
      BuildContext context, MapEntry<String, int> entry, int maxValue) {
    final l10n = context.l10n;
    final percentage = maxValue > 0 ? entry.value / maxValue : 0.0;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          SizedBox(
            width: 70,
            child: Text(
              '${l10n.translate('zone')} ${entry.key}',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                  ),
            ),
          ),
          Expanded(
            child: Stack(
              children: [
                Container(
                  height: 24,
                  decoration: BoxDecoration(
                    color: AppColors.divider.withValues(alpha: 0.3),
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                FractionallySizedBox(
                  widthFactor: percentage,
                  child: Container(
                    height: 24,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          AppColors.primary,
                          AppColors.primaryLight,
                        ],
                      ),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          SizedBox(
            width: 32,
            child: Text(
              '${entry.value}',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
              textAlign: TextAlign.end,
            ),
          ),
        ],
      ),
    );
  }
}

/// Widget for displaying status distribution as a pie-like summary.
class StatusDistribution extends StatelessWidget {
  final Map<String, int> data;
  final String title;

  const StatusDistribution({
    super.key,
    required this.data,
    this.title = '',
  });

  @override
  Widget build(BuildContext context) {
    if (data.isEmpty) {
      return const SizedBox.shrink();
    }

    final total = data.values.fold(0, (sum, value) => sum + value);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (title.isNotEmpty) ...[
          Text(
            title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 12),
        ],
        Wrap(
          spacing: 12,
          runSpacing: 8,
          children: data.entries.map((entry) {
            final color = _getStatusColor(entry.key);
            final percentage =
                total > 0 ? (entry.value / total * 100).toStringAsFixed(0) : '0';

            return _buildStatusChip(context, entry.key, entry.value,
                percentage, color);
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildStatusChip(BuildContext context, String status, int count,
      String percentage, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(
              color: color,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 6),
          Text(
            _formatStatus(status),
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: color,
                  fontWeight: FontWeight.w500,
                ),
          ),
          const SizedBox(width: 4),
          Text(
            '$count ($percentage%)',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: AppColors.textSecondary,
                ),
          ),
        ],
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

  String _formatStatus(String status) {
    return status.replaceAll('_', ' ').toLowerCase().split(' ').map((word) {
      if (word.isEmpty) return word;
      return word[0].toUpperCase() + word.substring(1);
    }).join(' ');
  }
}

/// Widget for displaying upcoming due dates.
class DueDateList extends StatelessWidget {
  final List<MapEntry<String, int>> dueDates;
  final String title;

  const DueDateList({
    super.key,
    required this.dueDates,
    this.title = '',
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    if (dueDates.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (title.isNotEmpty) ...[
          Text(
            title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 12),
        ],
        ...dueDates.take(5).map((entry) => _buildDateRow(context, l10n, entry)),
      ],
    );
  }

  Widget _buildDateRow(
      BuildContext context, AppLocalizations l10n, MapEntry<String, int> entry) {
    final isUrgent = _isWithinDays(entry.key, 7);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Icon(
            Icons.calendar_today,
            size: 16,
            color: isUrgent ? AppColors.emergency : AppColors.textSecondary,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              entry.key,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: isUrgent ? AppColors.emergency : null,
                    fontWeight: isUrgent ? FontWeight.bold : null,
                  ),
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
            decoration: BoxDecoration(
              color: isUrgent
                  ? AppColors.emergency.withValues(alpha: 0.1)
                  : AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              '${entry.value} ${l10n.translate(entry.value == 1 ? 'case_emergency' : 'cases_title').toLowerCase()}',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: isUrgent ? AppColors.emergency : AppColors.primary,
                    fontWeight: FontWeight.bold,
                  ),
            ),
          ),
        ],
      ),
    );
  }

  bool _isWithinDays(String dateStr, int days) {
    try {
      final date = DateTime.parse(dateStr);
      final diff = date.difference(DateTime.now()).inDays;
      return diff >= 0 && diff <= days;
    } catch (e) {
      return false;
    }
  }
}
