import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../../shared/widgets/error_view.dart';
import '../../../../shared/widgets/loading_indicator.dart';
import '../providers/dashboard_provider.dart';
import '../widgets/stats_card.dart';
import '../widgets/zone_chart.dart';
import '../../../inbox/presentation/widgets/case_card.dart';

/// Dashboard screen showing overview statistics for NGO coordinators.
class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statsAsync = ref.watch(dashboardStatsNotifierProvider);
    final recentCasesAsync = ref.watch(recentCasesNotifierProvider);
    final pendingEmergencies = ref.watch(pendingEmergenciesProvider);
    final l10n = context.l10n;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('dashboard_title')),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => _refreshDashboard(ref),
            tooltip: l10n.translate('refresh'),
          ),
        ],
      ),
      body: SafeArea(
        child: statsAsync.when(
          loading: () => LoadingIndicator(
            message: l10n.translate('loading'),
          ),
          error: (error, stack) => ErrorView(
            message: _getErrorMessage(l10n, error),
            onRetry: () => _refreshDashboard(ref),
          ),
          data: (stats) => RefreshIndicator(
            onRefresh: () async => _refreshDashboard(ref),
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Emergency Alert Section (if any)
                  if (pendingEmergencies.isNotEmpty)
                    _buildEmergencyAlert(context, l10n, pendingEmergencies.length),

                  // Summary Stats Row 1
                  Row(
                    children: [
                      Expanded(
                        child: StatsCard(
                          title: l10n.translate('total_mothers'),
                          value: '${stats.totalMothers}',
                          icon: Icons.pregnant_woman,
                          color: Colors.pink,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: StatsCard(
                          title: l10n.translate('total_volunteers'),
                          value: stats.volunteerAvailabilityRatio,
                          subtitle: l10n.translate('available_volunteers'),
                          icon: Icons.people,
                          color: AppColors.info,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),

                  // Summary Stats Row 2
                  Row(
                    children: [
                      Expanded(
                        child: StatsCard(
                          title: l10n.translate('pending'),
                          value: '${stats.pendingRequests}',
                          icon: Icons.pending_actions,
                          color: AppColors.warning,
                          onTap: () => context.go('/inbox'),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: StatsCard(
                          title: l10n.translate('active_requests'),
                          value: '${stats.activeRequests}',
                          icon: Icons.local_hospital,
                          color: AppColors.success,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),

                  // Zone Distribution Section
                  if (stats.mothersByZone.isNotEmpty) ...[
                    _buildSectionHeader(
                      context,
                      l10n.translate('zones_overview'),
                      Icons.map_outlined,
                      AppColors.primary,
                    ),
                    const SizedBox(height: 12),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: ZoneChart(data: stats.mothersByZone),
                      ),
                    ),
                    const SizedBox(height: 24),
                  ],

                  // Request Status Distribution
                  if (stats.requestsByStatus.isNotEmpty) ...[
                    _buildSectionHeader(
                      context,
                      l10n.translate('cases_title'),
                      Icons.pie_chart_outline,
                      AppColors.secondary,
                    ),
                    const SizedBox(height: 12),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: StatusDistribution(data: stats.requestsByStatus),
                      ),
                    ),
                    const SizedBox(height: 24),
                  ],

                  // Upcoming Due Dates
                  if (stats.upcomingDueDates.isNotEmpty) ...[
                    _buildSectionHeader(
                      context,
                      l10n.translate('due_date'),
                      Icons.calendar_today_outlined,
                      AppColors.info,
                    ),
                    const SizedBox(height: 12),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: DueDateList(
                          dueDates: stats.upcomingDueDates
                              .map((d) => MapEntry(d.date, d.count))
                              .toList(),
                        ),
                      ),
                    ),
                    const SizedBox(height: 24),
                  ],

                  // Recent Cases Section
                  _buildSectionHeader(
                    context,
                    l10n.translate('recent_cases'),
                    Icons.folder_outlined,
                    AppColors.primary,
                  ),
                  const SizedBox(height: 12),
                  recentCasesAsync.when(
                    loading: () => const Card(
                      child: Padding(
                        padding: EdgeInsets.all(32),
                        child: Center(child: CircularProgressIndicator()),
                      ),
                    ),
                    error: (error, stack) => Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Text(
                          _getErrorMessage(l10n, error),
                          style: TextStyle(color: AppColors.emergency),
                        ),
                      ),
                    ),
                    data: (cases) {
                      if (cases.isEmpty) {
                        return Card(
                          child: Padding(
                            padding: const EdgeInsets.all(24),
                            child: Center(
                              child: Text(
                                l10n.translate('no_cases'),
                                style: TextStyle(
                                  color: AppColors.textSecondary,
                                ),
                              ),
                            ),
                          ),
                        );
                      }

                      return Column(
                        children: cases
                            .map((caseModel) => CaseCard(
                                  caseModel: caseModel,
                                  onAccept: null,
                                  onComplete: null,
                                ))
                            .toList(),
                      );
                    },
                  ),

                  // Bottom padding
                  const SizedBox(height: 32),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildEmergencyAlert(
      BuildContext context, AppLocalizations l10n, int count) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Card(
        color: AppColors.emergency.withValues(alpha: 0.1),
        child: InkWell(
          onTap: () => context.go('/inbox'),
          borderRadius: BorderRadius.circular(12),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.emergency.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.warning_amber,
                    color: AppColors.emergency,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        l10n.translate('pending_emergencies'),
                        style:
                            Theme.of(context).textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.emergency,
                                ),
                      ),
                      Text(
                        '$count ${l10n.translate('active_requests').toLowerCase()}',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: AppColors.emergency.withValues(alpha: 0.8),
                            ),
                      ),
                    ],
                  ),
                ),
                const Icon(
                  Icons.arrow_forward_ios,
                  size: 16,
                  color: AppColors.emergency,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSectionHeader(
    BuildContext context,
    String title,
    IconData icon,
    Color color,
  ) {
    return Row(
      children: [
        Icon(icon, color: color, size: 20),
        const SizedBox(width: 8),
        Text(
          title,
          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
        ),
      ],
    );
  }

  void _refreshDashboard(WidgetRef ref) {
    ref.read(dashboardStatsNotifierProvider.notifier).refresh();
    ref.read(recentCasesNotifierProvider.notifier).refresh();
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
    return l10n.translate('error_unknown');
  }
}
