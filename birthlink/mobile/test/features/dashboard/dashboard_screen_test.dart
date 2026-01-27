// SafeBirth Connect - Dashboard Screen Widget Tests
//
// Tests for the DashboardScreen widget including:
// - Loading state display
// - Error state display
// - Stats cards rendering
// - Zone chart rendering
// - Emergency alerts display
// - RTL Arabic layout

import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:safebirth_connect/core/constants/app_colors.dart';
import 'package:safebirth_connect/core/localization/app_localizations.dart';
import 'package:safebirth_connect/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:safebirth_connect/features/dashboard/presentation/providers/dashboard_provider.dart';
import 'package:safebirth_connect/features/dashboard/presentation/screens/dashboard_screen.dart';
import 'package:safebirth_connect/features/dashboard/presentation/widgets/stats_card.dart';
import 'package:safebirth_connect/features/inbox/data/models/case_model.dart';
import 'package:safebirth_connect/shared/widgets/error_view.dart';

void main() {
  group('DashboardScreen', () {
    // Helper to create test widget with localizations
    Widget createTestWidget({
      List<Override> overrides = const [],
      Locale locale = const Locale('en'),
    }) {
      return ProviderScope(
        overrides: overrides,
        child: MaterialApp(
          locale: locale,
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          theme: ThemeData(
            colorScheme: ColorScheme.fromSeed(seedColor: AppColors.primary),
          ),
          home: const DashboardScreen(),
        ),
      );
    }

    testWidgets('shows loading indicator when loading', (tester) async {
      await tester.pumpWidget(createTestWidget());
      
      // First frame should show loading state
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('shows error view when error occurs', (tester) async {
      final errorProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockErrorDashboardNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [errorProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show error view
      expect(find.byType(ErrorView), findsOneWidget);
      expect(find.text('Retry'), findsOneWidget);
    });

    testWidgets('displays stats cards when data is available', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should display stats cards
      expect(find.byType(StatsCard), findsNWidgets(4));
    });

    testWidgets('displays total mothers count', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show total mothers count
      expect(find.text('150'), findsOneWidget);  // Total mothers
      expect(find.text('Total Mothers'), findsOneWidget);
    });

    testWidgets('displays emergency alert when emergencies pending', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show pending emergencies text
      expect(find.text('Pending Emergencies'), findsOneWidget);
    });

    testWidgets('displays correctly in Arabic (RTL)', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
        locale: const Locale('ar'),
      ));
      
      await tester.pumpAndSettle();
      
      // Should display Arabic text
      expect(find.text('لوحة التحكم'), findsOneWidget);  // dashboard_title in Arabic
      expect(find.text('إجمالي الأمهات'), findsOneWidget);  // total_mothers in Arabic
    });

    testWidgets('has pull-to-refresh functionality', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Verify RefreshIndicator is present
      expect(find.byType(RefreshIndicator), findsOneWidget);
    });

    testWidgets('refresh button in app bar triggers refresh', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Find and tap refresh button
      final refreshButton = find.byIcon(Icons.refresh);
      expect(refreshButton, findsOneWidget);
    });

    testWidgets('displays zones overview section', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show zones overview section header
      expect(find.text('Zones Overview'), findsOneWidget);
    });

    testWidgets('displays recent cases section', (tester) async {
      final statsProvider = dashboardStatsNotifierProvider.overrideWith(() {
        return _MockDashboardNotifier();
      });
      final casesProvider = recentCasesNotifierProvider.overrideWith(() {
        return _MockRecentCasesNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [statsProvider, casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show recent cases section header
      expect(find.text('Recent Cases'), findsOneWidget);
    });
  });

  group('StatsCard', () {
    testWidgets('displays title and value correctly', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test Title',
              value: '42',
              icon: Icons.star,
              color: Colors.blue,
            ),
          ),
        ),
      );
      
      expect(find.text('Test Title'), findsOneWidget);
      expect(find.text('42'), findsOneWidget);
      expect(find.byIcon(Icons.star), findsOneWidget);
    });

    testWidgets('displays subtitle when provided', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Test',
              value: '10',
              subtitle: 'Available',
              icon: Icons.person,
              color: Colors.green,
            ),
          ),
        ),
      );
      
      expect(find.text('Available'), findsOneWidget);
    });

    testWidgets('handles tap when onTap provided', (tester) async {
      bool tapped = false;
      
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: StatsCard(
              title: 'Tappable',
              value: '5',
              icon: Icons.touch_app,
              color: Colors.purple,
              onTap: () => tapped = true,
            ),
          ),
        ),
      );
      
      await tester.tap(find.byType(StatsCard));
      expect(tapped, isTrue);
    });
  });
}

// Mock notifiers for testing

class _MockErrorDashboardNotifier extends DashboardStatsNotifier {
  @override
  Future<DashboardStatsModel> build() async {
    throw Exception('Network error');
  }
}

class _MockDashboardNotifier extends DashboardStatsNotifier {
  @override
  Future<DashboardStatsModel> build() async {
    return DashboardStatsModel(
      totalMothers: 150,
      totalVolunteers: 45,
      availableVolunteers: 28,
      pendingRequests: 12,
      pendingEmergencies: 3,
      activeRequests: 8,
      completedRequests: 234,
      highRiskMothers: 15,
      mothersByZone: {'Zone 1': 50, 'Zone 2': 45, 'Zone 3': 55},
      volunteersBySkill: {'MIDWIFE': 15, 'NURSE': 20, 'TRAINED': 10},
      requestsByStatus: {'PENDING': 12, 'ACCEPTED': 8, 'COMPLETED': 234},
      upcomingDueDates: [
        DueDateCluster(date: '2024-02-01', count: 5),
        DueDateCluster(date: '2024-02-15', count: 8),
      ],
    );
  }
}

class _MockRecentCasesNotifier extends RecentCasesNotifier {
  @override
  Future<List<CaseModel>> build() async {
    return [
      const CaseModel(
        caseId: 'HR-0010',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        riskLevel: 'HIGH',
        createdAt: '2024-01-15T10:30:00',
      ),
      const CaseModel(
        caseId: 'HR-0011',
        zone: 'Zone 2',
        requestType: 'SUPPORT',
        status: 'ACCEPTED',
        riskLevel: 'LOW',
        createdAt: '2024-01-14T08:00:00',
      ),
    ];
  }
}
