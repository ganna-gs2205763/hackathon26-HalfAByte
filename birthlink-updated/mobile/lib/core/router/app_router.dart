import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/dashboard/presentation/screens/dashboard_screen.dart';
import '../../features/heatmap/presentation/screens/ngo_heatmap_screen.dart';
import '../../features/heatmap/presentation/screens/volunteer_heatmap_screen.dart';
import '../../features/inbox/presentation/screens/case_details_screen.dart';
import '../../features/inbox/presentation/screens/inbox_screen.dart';
import '../../features/role_selection/presentation/screens/role_selection_screen.dart';
import '../../features/settings/presentation/screens/settings_screen.dart';
import '../../shared/widgets/main_scaffold.dart';
import '../providers/role_provider.dart';

/// Provider for the app router.
final appRouterProvider = Provider<GoRouter>((ref) {
  final role = ref.watch(roleProvider);

  return GoRouter(
    initialLocation: '/heatmap',  // Start at heatmap (primary screen)
    debugLogDiagnostics: true,
    routes: [
      // Role selection (outside shell)
      GoRoute(
        path: '/role-selection',
        name: 'role_selection',
        builder: (context, state) => const RoleSelectionScreen(),
      ),
      // Shell route for bottom navigation
      ShellRoute(
        builder: (context, state, child) {
          return MainScaffold(child: child);
        },
        routes: [
          GoRoute(
            path: '/dashboard',
            name: 'dashboard',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: DashboardScreen(),
            ),
          ),
          GoRoute(
            path: '/inbox',
            name: 'inbox',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: InboxScreen(),
            ),
          ),
          GoRoute(
            path: '/heatmap',
            name: 'heatmap',
            pageBuilder: (context, state) => NoTransitionPage(
              child: role == UserRole.ngo
                  ? const NgoHeatmapScreen()
                  : const VolunteerHeatmapScreen(),
            ),
          ),
          GoRoute(
            path: '/settings',
            name: 'settings',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: SettingsScreen(),
            ),
          ),
        ],
      ),
      // Case details (outside shell)
      GoRoute(
        path: '/case/:caseId',
        name: 'case_details',
        builder: (context, state) {
          final caseId = state.pathParameters['caseId']!;
          return CaseDetailsScreen(caseId: caseId);
        },
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Text('Page not found: ${state.uri}'),
      ),
    ),
  );
});

/// Route names for type-safe navigation.
class AppRoutes {
  AppRoutes._();

  static const String dashboard = 'dashboard';
  static const String inbox = 'inbox';
  static const String heatmap = 'heatmap';
  static const String settings = 'settings';
  static const String caseDetails = 'case_details';
  static const String roleSelection = 'role_selection';
}
