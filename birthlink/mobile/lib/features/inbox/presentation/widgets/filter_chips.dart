import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../providers/inbox_provider.dart';

/// Widget for displaying filter chips in the inbox.
class InboxFilterChips extends ConsumerWidget {
  const InboxFilterChips({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentFilter = ref.watch(inboxFilterNotifierProvider);
    final counts = ref.watch(caseCountsProvider);
    final l10n = context.l10n;

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          _buildFilterChip(
            context,
            ref,
            filter: InboxFilter.all,
            label: l10n.translate('cases_title'),
            count: counts['total'] ?? 0,
            isSelected: currentFilter == InboxFilter.all,
          ),
          const SizedBox(width: 8),
          _buildFilterChip(
            context,
            ref,
            filter: InboxFilter.pending,
            label: l10n.translate('pending'),
            count: counts['pending'] ?? 0,
            isSelected: currentFilter == InboxFilter.pending,
            color: AppColors.statusPending,
          ),
          const SizedBox(width: 8),
          _buildFilterChip(
            context,
            ref,
            filter: InboxFilter.accepted,
            label: l10n.translate('accepted'),
            count: counts['accepted'] ?? 0,
            isSelected: currentFilter == InboxFilter.accepted,
            color: AppColors.statusAccepted,
          ),
          const SizedBox(width: 8),
          _buildFilterChip(
            context,
            ref,
            filter: InboxFilter.emergency,
            label: l10n.translate('emergency'),
            count: counts['emergency'] ?? 0,
            isSelected: currentFilter == InboxFilter.emergency,
            color: AppColors.emergency,
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(
    BuildContext context,
    WidgetRef ref, {
    required InboxFilter filter,
    required String label,
    required int count,
    required bool isSelected,
    Color? color,
  }) {
    final chipColor = color ?? AppColors.primary;

    return FilterChip(
      label: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(label),
          if (count > 0) ...[
            const SizedBox(width: 4),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
              decoration: BoxDecoration(
                color: isSelected
                    ? Colors.white.withValues(alpha: 0.3)
                    : chipColor.withValues(alpha: 0.2),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                '$count',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: isSelected ? Colors.white : chipColor,
                ),
              ),
            ),
          ],
        ],
      ),
      selected: isSelected,
      selectedColor: chipColor,
      checkmarkColor: Colors.white,
      labelStyle: TextStyle(
        color: isSelected ? Colors.white : AppColors.textPrimary,
      ),
      onSelected: (selected) {
        ref.read(inboxFilterNotifierProvider.notifier).setFilter(filter);
      },
    );
  }
}
