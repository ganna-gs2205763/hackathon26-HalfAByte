import 'package:flutter/material.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../dashboard/data/models/dashboard_stats_model.dart';
import '../widgets/heatmap_legend.dart';
import '../widgets/zone_detail_sheet.dart';
import '../widgets/zone_grid.dart';

/// NGO Heatmap screen showing all zones globally (static demo).
class NgoHeatmapScreen extends StatelessWidget {
  const NgoHeatmapScreen({super.key});

  /// Static zone data for NGO view - larger area with more data (12-15 zones).
  static const List<ZoneStatsModel> _staticZones = [
    // Critical zones
    ZoneStatsModel(
      zone: 'Kibera West',
      motherCount: 145,
      volunteerCount: 8,
      pendingCases: 12,
      activeCases: 18,
    ),
    ZoneStatsModel(
      zone: 'Mathare North',
      motherCount: 128,
      volunteerCount: 6,
      pendingCases: 9,
      activeCases: 14,
    ),
    // High load zones
    ZoneStatsModel(
      zone: 'Kibera Central',
      motherCount: 112,
      volunteerCount: 12,
      pendingCases: 8,
      activeCases: 11,
    ),
    ZoneStatsModel(
      zone: 'Dandora Phase 1',
      motherCount: 98,
      volunteerCount: 9,
      pendingCases: 6,
      activeCases: 9,
    ),
    ZoneStatsModel(
      zone: 'Korogocho',
      motherCount: 87,
      volunteerCount: 7,
      pendingCases: 5,
      activeCases: 8,
    ),
    // Medium load zones
    ZoneStatsModel(
      zone: 'Kibera East',
      motherCount: 76,
      volunteerCount: 10,
      pendingCases: 3,
      activeCases: 5,
    ),
    ZoneStatsModel(
      zone: 'Mukuru Kwa Njenga',
      motherCount: 68,
      volunteerCount: 8,
      pendingCases: 2,
      activeCases: 4,
    ),
    ZoneStatsModel(
      zone: 'Langata North',
      motherCount: 54,
      volunteerCount: 6,
      pendingCases: 2,
      activeCases: 3,
    ),
    ZoneStatsModel(
      zone: 'Mathare South',
      motherCount: 62,
      volunteerCount: 7,
      pendingCases: 1,
      activeCases: 4,
    ),
    // Low load zones
    ZoneStatsModel(
      zone: 'Kawangware',
      motherCount: 45,
      volunteerCount: 8,
      pendingCases: 1,
      activeCases: 2,
    ),
    ZoneStatsModel(
      zone: 'Embakasi East',
      motherCount: 38,
      volunteerCount: 6,
      pendingCases: 1,
      activeCases: 1,
    ),
    ZoneStatsModel(
      zone: 'Ruaraka',
      motherCount: 32,
      volunteerCount: 5,
      pendingCases: 0,
      activeCases: 2,
    ),
    ZoneStatsModel(
      zone: 'Roysambu',
      motherCount: 28,
      volunteerCount: 5,
      pendingCases: 1,
      activeCases: 1,
    ),
    ZoneStatsModel(
      zone: 'Kasarani Central',
      motherCount: 24,
      volunteerCount: 4,
      pendingCases: 0,
      activeCases: 1,
    ),
    ZoneStatsModel(
      zone: 'Dagoretti South',
      motherCount: 22,
      volunteerCount: 4,
      pendingCases: 0,
      activeCases: 1,
    ),
  ];

  /// Static summary data for NGO view.
  static const _StaticSummary _staticSummary = _StaticSummary(
    totalZones: 15,
    totalCases: 134,
    totalVolunteers: 105,
    criticalZones: 2,
    highLoadZones: 3,
  );

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('heatmap_title')),
      ),
      body: Column(
        children: [
          // Summary header
          const _SummaryHeader(summary: _staticSummary),
          // Legend
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 16),
            child: HeatmapLegend(isCompact: true),
          ),
          const SizedBox(height: 8),
          // Zone grid with static data
          Expanded(
            child: ZoneGrid(
              zones: _staticZones,
              onZoneTap: (zone) => ZoneDetailSheet.show(context, zone),
              emptyMessage: l10n.translate('no_zones'),
            ),
          ),
        ],
      ),
    );
  }
}

/// Static summary model for demo purposes.
class _StaticSummary {
  final int totalZones;
  final int totalCases;
  final int totalVolunteers;
  final int criticalZones;
  final int highLoadZones;

  const _StaticSummary({
    required this.totalZones,
    required this.totalCases,
    required this.totalVolunteers,
    required this.criticalZones,
    required this.highLoadZones,
  });

  bool get hasAlerts => criticalZones > 0 || highLoadZones > 0;
}

class _SummaryHeader extends StatelessWidget {
  final _StaticSummary summary;

  const _SummaryHeader({required this.summary});

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.primary.withValues(alpha: 0.1),
            AppColors.primaryLight.withValues(alpha: 0.1),
          ],
        ),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.primary.withValues(alpha: 0.2)),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _SummaryItem(
                icon: Icons.map,
                value: summary.totalZones.toString(),
                label: l10n.translate('total_zones'),
                color: AppColors.info,
              ),
              _SummaryItem(
                icon: Icons.folder_open,
                value: summary.totalCases.toString(),
                label: l10n.translate('active_requests'),
                color: AppColors.warning,
              ),
              _SummaryItem(
                icon: Icons.people,
                value: summary.totalVolunteers.toString(),
                label: l10n.translate('total_volunteers'),
                color: AppColors.success,
              ),
            ],
          ),
          if (summary.hasAlerts) ...[
            const SizedBox(height: 12),
            const Divider(),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                if (summary.criticalZones > 0) ...[
                  Icon(Icons.warning, size: 16, color: AppColors.emergency),
                  const SizedBox(width: 4),
                  Text(
                    '${summary.criticalZones} ${l10n.translate('critical_zones')}',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: AppColors.emergency,
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(width: 16),
                ],
                if (summary.highLoadZones > 0) ...[
                  Icon(Icons.trending_up, size: 16, color: AppColors.secondary),
                  const SizedBox(width: 4),
                  Text(
                    '${summary.highLoadZones} ${l10n.translate('high_load_zones')}',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: AppColors.secondary,
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                ],
              ],
            ),
          ],
        ],
      ),
    );
  }
}

class _SummaryItem extends StatelessWidget {
  final IconData icon;
  final String value;
  final String label;
  final Color color;

  const _SummaryItem({
    required this.icon,
    required this.value,
    required this.label,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Icon(icon, color: color, size: 24),
        const SizedBox(height: 4),
        Text(
          value,
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimary,
              ),
        ),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.textSecondary,
              ),
        ),
      ],
    );
  }
}
