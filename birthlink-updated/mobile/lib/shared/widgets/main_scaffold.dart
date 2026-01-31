import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../core/localization/app_localizations.dart';

/// Main scaffold with bottom navigation for the app shell.
class MainScaffold extends StatelessWidget {
  final Widget child;

  const MainScaffold({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: child,
      bottomNavigationBar: _BottomNavBar(),
    );
  }
}

class _BottomNavBar extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;

    // Inbox first (primary volunteer screen), then dashboard, heatmap, then settings
    int currentIndex = 0;
    if (location.startsWith('/dashboard')) {
      currentIndex = 1;
    } else if (location.startsWith('/heatmap')) {
      currentIndex = 2;
    } else if (location.startsWith('/settings')) {
      currentIndex = 3;
    }

    return NavigationBar(
      selectedIndex: currentIndex,
      onDestinationSelected: (index) {
        switch (index) {
          case 0:
            context.go('/inbox');
            break;
          case 1:
            context.go('/dashboard');
            break;
          case 2:
            context.go('/heatmap');
            break;
          case 3:
            context.go('/settings');
            break;
        }
      },
      destinations: [
        NavigationDestination(
          icon: const Icon(Icons.inbox_outlined),
          selectedIcon: const Icon(Icons.inbox),
          label: context.tr('nav_inbox'),
        ),
        NavigationDestination(
          icon: const Icon(Icons.dashboard_outlined),
          selectedIcon: const Icon(Icons.dashboard),
          label: context.tr('nav_dashboard'),
        ),
        NavigationDestination(
          icon: const Icon(Icons.grid_view_outlined),
          selectedIcon: const Icon(Icons.grid_view),
          label: context.tr('nav_heatmap'),
        ),
        NavigationDestination(
          icon: const Icon(Icons.settings_outlined),
          selectedIcon: const Icon(Icons.settings),
          label: context.tr('nav_settings'),
        ),
      ],
    );
  }
}
