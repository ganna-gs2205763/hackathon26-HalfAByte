import 'package:flutter/material.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../dashboard/data/models/dashboard_stats_model.dart';
import '../widgets/heatmap_legend.dart';
import '../widgets/zone_detail_sheet.dart';
import '../widgets/zone_grid.dart';

/// Volunteer Heatmap screen showing only assigned zones (static demo).
class VolunteerHeatmapScreen extends StatelessWidget {
  const VolunteerHeatmapScreen({super.key});

  /// Static zone data for volunteer view - smaller area (3-4 zones).
  static const List<ZoneStatsModel> _staticZones = [
    ZoneStatsModel(
      zone: 'Kibera West',
      motherCount: 28,
      volunteerCount: 3,
      pendingCases: 2,
      activeCases: 4,
    ),
    ZoneStatsModel(
      zone: 'Kibera Central',
      motherCount: 35,
      volunteerCount: 4,
      pendingCases: 1,
      activeCases: 3,
    ),
    ZoneStatsModel(
      zone: 'Kibera East',
      motherCount: 22,
      volunteerCount: 2,
      pendingCases: 3,
      activeCases: 2,
    ),
    ZoneStatsModel(
      zone: 'Langata North',
      motherCount: 18,
      volunteerCount: 2,
      pendingCases: 1,
      activeCases: 1,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('my_zones')),
      ),
      body: Column(
        children: [
          // Volunteer info header
          const _VolunteerHeader(
            name: 'Sarah Wanjiku',
            zones: ['Kibera West', 'Kibera Central', 'Kibera East', 'Langata North'],
          ),
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
              emptyMessage: l10n.translate('no_zones_assigned'),
            ),
          ),
        ],
      ),
    );
  }
}

class _VolunteerHeader extends StatelessWidget {
  final String name;
  final List<String> zones;

  const _VolunteerHeader({
    required this.name,
    required this.zones,
  });

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
      child: Row(
        children: [
          CircleAvatar(
            backgroundColor: AppColors.primary.withValues(alpha: 0.2),
            child: Text(
              name.isNotEmpty ? name[0].toUpperCase() : '?',
              style: const TextStyle(
                color: AppColors.primary,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 4),
                Text(
                  '${zones.length} ${l10n.translate('zones_assigned')}',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: AppColors.textSecondary,
                      ),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Text(
              l10n.translate('my_zones'),
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.primary,
                    fontWeight: FontWeight.bold,
                  ),
            ),
          ),
        ],
      ),
    );
  }
}
