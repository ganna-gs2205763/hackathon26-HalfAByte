import 'package:flutter/material.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../dashboard/data/models/dashboard_stats_model.dart';

/// A tile representing a zone in the heatmap grid.
/// Color indicates load level (green → yellow → orange → red).
class ZoneTile extends StatelessWidget {
  final ZoneStatsModel zone;
  final VoidCallback? onTap;

  const ZoneTile({
    super.key,
    required this.zone,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final color = _getHeatColor(zone.heatLevel);
    final l10n = context.l10n;

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      color: color.withValues(alpha: 0.15),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: color.withValues(alpha: 0.5),
              width: 2,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // Zone name with heat indicator
              Row(
                children: [
                  Expanded(
                    child: Text(
                      zone.zone,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimary,
                          ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  Container(
                    width: 12,
                    height: 12,
                    decoration: BoxDecoration(
                      color: color,
                      shape: BoxShape.circle,
                    ),
                  ),
                ],
              ),
              const Spacer(),
              // Stats row
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  _StatChip(
                    icon: Icons.folder_open,
                    value: zone.totalCases.toString(),
                    label: l10n.translate('cases'),
                    color: color,
                  ),
                  _StatChip(
                    icon: Icons.people_outline,
                    value: zone.volunteerCount.toString(),
                    label: l10n.translate('volunteers'),
                    color: AppColors.primary,
                  ),
                ],
              ),
              const SizedBox(height: 8),
              // Load score indicator
              Row(
                children: [
                  Expanded(
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(4),
                      child: LinearProgressIndicator(
                        value: _getLoadProgress(zone.loadScore),
                        backgroundColor: Colors.grey.shade200,
                        valueColor: AlwaysStoppedAnimation<Color>(color),
                        minHeight: 6,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    zone.loadScore.toStringAsFixed(1),
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: color,
                        ),
                  ),
                ],
              ),
            ],
          ),
        ),
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

  double _getLoadProgress(double loadScore) {
    // Normalize to 0-1 range (max at 5.0)
    return (loadScore / 5.0).clamp(0.0, 1.0);
  }
}

/// Small stat chip widget.
class _StatChip extends StatelessWidget {
  final IconData icon;
  final String value;
  final String label;
  final Color color;

  const _StatChip({
    required this.icon,
    required this.value,
    required this.label,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 14, color: color),
        const SizedBox(width: 4),
        Text(
          value,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimary,
              ),
        ),
      ],
    );
  }
}
