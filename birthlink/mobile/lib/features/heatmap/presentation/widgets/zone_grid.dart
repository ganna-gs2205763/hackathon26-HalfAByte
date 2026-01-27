import 'package:flutter/material.dart';

import '../../../dashboard/data/models/dashboard_stats_model.dart';
import 'zone_tile.dart';

/// Responsive grid layout of zone tiles.
class ZoneGrid extends StatelessWidget {
  final List<ZoneStatsModel> zones;
  final void Function(ZoneStatsModel zone)? onZoneTap;
  final bool isLoading;
  final String? emptyMessage;

  const ZoneGrid({
    super.key,
    required this.zones,
    this.onZoneTap,
    this.isLoading = false,
    this.emptyMessage,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (zones.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.grid_off,
              size: 64,
              color: Colors.grey.shade400,
            ),
            const SizedBox(height: 16),
            Text(
              emptyMessage ?? 'No zones available',
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: Colors.grey.shade600,
                  ),
            ),
          ],
        ),
      );
    }

    // Sort zones by load score (highest first)
    final sortedZones = List<ZoneStatsModel>.from(zones)
      ..sort((a, b) => b.loadScore.compareTo(a.loadScore));

    return LayoutBuilder(
      builder: (context, constraints) {
        // Calculate cross axis count based on available width
        final crossAxisCount = _calculateCrossAxisCount(constraints.maxWidth);

        return GridView.builder(
          padding: const EdgeInsets.all(16),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: crossAxisCount,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            childAspectRatio: 1.1,
          ),
          itemCount: sortedZones.length,
          itemBuilder: (context, index) {
            final zone = sortedZones[index];
            return ZoneTile(
              zone: zone,
              onTap: onZoneTap != null ? () => onZoneTap!(zone) : null,
            );
          },
        );
      },
    );
  }

  int _calculateCrossAxisCount(double width) {
    if (width < 400) return 2;
    if (width < 600) return 2;
    if (width < 900) return 3;
    return 4;
  }
}
