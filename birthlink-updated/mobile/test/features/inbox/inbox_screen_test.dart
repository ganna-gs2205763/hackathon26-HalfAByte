// SafeBirth Connect - Inbox Screen Widget Tests
//
// Tests for the InboxScreen widget including:
// - Loading state display
// - Error state display
// - Empty state display
// - Case list rendering
// - Filter functionality
// - RTL Arabic layout
// - Accept/Complete actions

import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:safebirth_connect/core/constants/app_colors.dart';
import 'package:safebirth_connect/core/localization/app_localizations.dart';
import 'package:safebirth_connect/features/inbox/data/models/case_model.dart';
import 'package:safebirth_connect/features/inbox/presentation/providers/inbox_provider.dart';
import 'package:safebirth_connect/features/inbox/presentation/screens/inbox_screen.dart';
import 'package:safebirth_connect/features/inbox/presentation/widgets/case_card.dart';
import 'package:safebirth_connect/shared/widgets/error_view.dart';
import 'package:safebirth_connect/shared/widgets/loading_indicator.dart';

void main() {
  group('InboxScreen', () {
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
          home: const InboxScreen(),
        ),
      );
    }

    testWidgets('shows loading indicator when loading', (tester) async {
      await tester.pumpWidget(createTestWidget());
      
      // First frame should show loading state
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('shows error view when error occurs', (tester) async {
      // Create provider override that throws error
      final errorProvider = inboxNotifierProvider.overrideWith(() {
        return _MockErrorInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [errorProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show error view with retry button
      expect(find.byType(ErrorView), findsOneWidget);
      expect(find.text('Retry'), findsOneWidget);
    });

    testWidgets('shows empty state when no cases', (tester) async {
      final emptyProvider = inboxNotifierProvider.overrideWith(() {
        return _MockEmptyInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [emptyProvider],
      ));
      
      await tester.pumpAndSettle();
      
      expect(find.text('No cases yet'), findsOneWidget);
    });

    testWidgets('displays cases when data is available', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should display case cards
      expect(find.byType(CaseCard), findsNWidgets(2));
      expect(find.text('HR-0001'), findsOneWidget);
      expect(find.text('HR-0002'), findsOneWidget);
    });

    testWidgets('displays filter chips', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show filter chip options
      expect(find.text('All'), findsWidgets);
      expect(find.text('Pending'), findsWidgets);
      expect(find.text('Emergency'), findsWidgets);
    });

    testWidgets('displays correctly in Arabic (RTL)', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
        locale: const Locale('ar'),
      ));
      
      await tester.pumpAndSettle();
      
      // Should display Arabic text
      expect(find.text('صندوق الوارد'), findsOneWidget);  // nav_inbox in Arabic
    });

    testWidgets('has pull-to-refresh functionality', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Verify RefreshIndicator is present
      expect(find.byType(RefreshIndicator), findsOneWidget);
    });

    testWidgets('displays emergency badge for emergency cases', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show emergency label for HR-0001 (which is an emergency)
      expect(find.text('Emergency'), findsWidgets);
    });

    testWidgets('shows accept button for pending cases', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show accept button
      expect(find.text('Accept'), findsWidgets);
    });

    testWidgets('refresh button in app bar triggers refresh', (tester) async {
      final casesProvider = inboxNotifierProvider.overrideWith(() {
        return _MockCasesInboxNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [casesProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Find and tap refresh button
      final refreshButton = find.byIcon(Icons.refresh);
      expect(refreshButton, findsOneWidget);
      
      await tester.tap(refreshButton);
      await tester.pump();
      
      // Should trigger loading state
      expect(find.byType(CircularProgressIndicator), findsWidgets);
    });
  });
}

// Mock notifiers for testing

class _MockErrorInboxNotifier extends InboxNotifier {
  @override
  Future<List<CaseModel>> build() async {
    throw Exception('Network error');
  }
}

class _MockEmptyInboxNotifier extends InboxNotifier {
  @override
  Future<List<CaseModel>> build() async {
    return [];
  }
}

class _MockCasesInboxNotifier extends InboxNotifier {
  @override
  Future<List<CaseModel>> build() async {
    return [
      const CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 3',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        riskLevel: 'HIGH',
        createdAt: '2024-01-15T10:30:00',
        dueDate: '2024-02-15',
      ),
      const CaseModel(
        caseId: 'HR-0002',
        zone: 'Zone 5',
        requestType: 'SUPPORT',
        status: 'PENDING',
        riskLevel: 'MEDIUM',
        createdAt: '2024-01-14T08:00:00',
      ),
    ];
  }
  
  @override
  Future<void> refresh() async {
    state = const AsyncLoading();
    await Future.delayed(const Duration(milliseconds: 100));
    state = AsyncData(await build());
  }
}
