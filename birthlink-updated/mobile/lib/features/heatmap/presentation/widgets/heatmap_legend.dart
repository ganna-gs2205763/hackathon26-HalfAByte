import 'package:flutter/material.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';

/// Color legend for the heatmap showing load levels.
class HeatmapLegend extends StatelessWidget {
  final bool isCompact;

  const HeatmapLegend({
    super.key,
    this.isCompact = false,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    final items = [
      _LegendItem(
        color: AppColors.success,
        label: l10n.translate('zone_load_low'),
        description: '< 0.5',
      ),
      _LegendItem(
        color: AppColors.warning,
        label: l10n.translate('zone_load_medium'),
        description: '0.5 - 1.5',
      ),
      _LegendItem(
        color: AppColors.secondary,
        label: l10n.translate('zone_load_high'),
        description: '1.5 - 3.0',
      ),
      _LegendItem(
        color: AppColors.emergency,
        label: l10n.translate('zone_load_critical'),
        description: '> 3.0',
      ),
    ];

    if (isCompact) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(8),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.05),
              blurRadius: 4,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: items.map((item) => _CompactLegendChip(item: item)).toList(),
        ),
      );
    }

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            l10n.translate('heatmap_legend_title'),
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 16,
            runSpacing: 8,
            children: items.map((item) => _ExpandedLegendItem(item: item)).toList(),
          ),
        ],
      ),
    );
  }
}

class _LegendItem {
  final Color color;
  final String label;
  final String description;

  const _LegendItem({
    required this.color,
    required this.label,
    required this.description,
  });
}

class _CompactLegendChip extends StatelessWidget {
  final _LegendItem item;

  const _CompactLegendChip({required this.item});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: item.color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 4),
        Text(
          item.label,
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ],
    );
  }
}

class _ExpandedLegendItem extends StatelessWidget {
  final _LegendItem item;

  const _ExpandedLegendItem({required this.item});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 16,
          height: 16,
          decoration: BoxDecoration(
            color: item.color,
            borderRadius: BorderRadius.circular(4),
          ),
        ),
        const SizedBox(width: 8),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              item.label,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            Text(
              item.description,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.textSecondary,
                    fontSize: 10,
                  ),
            ),
          ],
        ),
      ],
    );
  }
}
