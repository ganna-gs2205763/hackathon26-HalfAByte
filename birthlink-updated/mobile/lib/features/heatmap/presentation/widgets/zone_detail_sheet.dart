import 'package:flutter/material.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../dashboard/data/models/dashboard_stats_model.dart';

/// Bottom sheet showing detailed zone statistics.
class ZoneDetailSheet extends StatelessWidget {
  final ZoneStatsModel zone;

  const ZoneDetailSheet({
    super.key,
    required this.zone,
  });

  /// Show the zone detail bottom sheet.
  static void show(BuildContext context, ZoneStatsModel zone) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => ZoneDetailSheet(zone: zone),
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final color = _getHeatColor(zone.heatLevel);

    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Handle bar
          Container(
            margin: const EdgeInsets.only(top: 12),
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.grey.shade300,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          // Header
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: color.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        Icons.location_on,
                        color: color,
                        size: 28,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            zone.zone,
                            style: Theme.of(context)
                                .textTheme
                                .headlineSmall
                                ?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                          ),
                          const SizedBox(height: 4),
                          _HeatBadge(heatLevel: zone.heatLevel, color: color),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.pop(context),
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                // Stats grid
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade50,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: _StatItem(
                              icon: Icons.pregnant_woman,
                              label: l10n.translate('total_mothers'),
                              value: zone.motherCount.toString(),
                              color: AppColors.primary,
                            ),
                          ),
                          Expanded(
                            child: _StatItem(
                              icon: Icons.people,
                              label: l10n.translate('total_volunteers'),
                              value: zone.volunteerCount.toString(),
                              color: AppColors.info,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Expanded(
                            child: _StatItem(
                              icon: Icons.pending_actions,
                              label: l10n.translate('pending'),
                              value: zone.pendingCases.toString(),
                              color: AppColors.warning,
                            ),
                          ),
                          Expanded(
                            child: _StatItem(
                              icon: Icons.play_circle_outline,
                              label: l10n.translate('active_requests'),
                              value: zone.activeCases.toString(),
                              color: AppColors.success,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                // Load score explanation
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: color.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: color.withValues(alpha: 0.3)),
                  ),
                  child: Row(
                    children: [
                      Icon(Icons.analytics_outlined, color: color),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              l10n.translate('load_score'),
                              style: Theme.of(context)
                                  .textTheme
                                  .bodySmall
                                  ?.copyWith(
                                    color: AppColors.textSecondary,
                                  ),
                            ),
                            Text(
                              '${zone.loadScore.toStringAsFixed(2)} ${l10n.translate('cases_per_volunteer')}',
                              style: Theme.of(context)
                                  .textTheme
                                  .titleMedium
                                  ?.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: color,
                                  ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  Color _getHeatColor(HeatLevel level) {
    switch (level) {
      case HeatLevel.low:
        return AppColors.success;
      case HeatLevel.medium:
        return AppColors.warning;
      case HeatLevel.high:
        return AppColors.secondary;
      case HeatLevel.critical:
        return AppColors.emergency;
    }
  }
}

class _HeatBadge extends StatelessWidget {
  final HeatLevel heatLevel;
  final Color color;

  const _HeatBadge({
    required this.heatLevel,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final label = _getLabel(l10n);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.5)),
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
            label,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: color,
                  fontWeight: FontWeight.bold,
                ),
          ),
        ],
      ),
    );
  }

  String _getLabel(AppLocalizations l10n) {
    switch (heatLevel) {
      case HeatLevel.low:
        return l10n.translate('zone_load_low');
      case HeatLevel.medium:
        return l10n.translate('zone_load_medium');
      case HeatLevel.high:
        return l10n.translate('zone_load_high');
      case HeatLevel.critical:
        return l10n.translate('zone_load_critical');
    }
  }
}

class _StatItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color color;

  const _StatItem({
    required this.icon,
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Icon(icon, color: color, size: 24),
        const SizedBox(height: 8),
        Text(
          value,
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimary,
              ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.textSecondary,
              ),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
}
