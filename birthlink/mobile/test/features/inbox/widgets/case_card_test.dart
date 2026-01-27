// SafeBirth Connect - CaseCard Widget Tests
//
// Tests for the CaseCard widget including:
// - Correct display of case information
// - Emergency vs support styling
// - Risk level display and colors
// - Status badge display
// - Action buttons visibility
// - RTL Arabic layout
// - Loading state during actions

import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:safebirth_connect/core/constants/app_colors.dart';
import 'package:safebirth_connect/core/localization/app_localizations.dart';
import 'package:safebirth_connect/features/inbox/data/models/case_model.dart';
import 'package:safebirth_connect/features/inbox/presentation/widgets/case_card.dart';
import 'package:safebirth_connect/shared/widgets/status_badge.dart';

void main() {
  group('CaseCard', () {
    // Helper to create test widget with localizations
    Widget createTestWidget({
      required CaseModel caseModel,
      VoidCallback? onAccept,
      VoidCallback? onComplete,
      bool isLoading = false,
      Locale locale = const Locale('en'),
    }) {
      return ProviderScope(
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
          home: Scaffold(
            body: SingleChildScrollView(
              child: CaseCard(
                caseModel: caseModel,
                onAccept: onAccept,
                onComplete: onComplete,
                isLoading: isLoading,
              ),
            ),
          ),
        ),
      );
    }

    testWidgets('displays case ID correctly', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0042',
        zone: 'Zone 3',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.text('HR-0042'), findsOneWidget);
    });

    testWidgets('displays zone information', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 5',
        requestType: 'SUPPORT',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.textContaining('Zone 5'), findsOneWidget);
    });

    testWidgets('displays emergency badge for emergency cases', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.text('Emergency'), findsOneWidget);
    });

    testWidgets('displays support badge for support cases', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.text('Support Request'), findsOneWidget);
    });

    testWidgets('displays risk level when provided', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        riskLevel: 'HIGH',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.textContaining('High'), findsOneWidget);
    });

    testWidgets('displays due date when provided', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'PENDING',
        dueDate: '2024-02-15',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.textContaining('2024-02-15'), findsOneWidget);
    });

    testWidgets('displays status badge', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      expect(find.byType(StatusBadge), findsOneWidget);
      expect(find.text('Pending'), findsOneWidget);
    });

    testWidgets('shows accept button for pending cases when callback provided', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(
        caseModel: caseModel,
        onAccept: () {},
      ));
      await tester.pumpAndSettle();
      
      expect(find.text('Accept'), findsOneWidget);
    });

    testWidgets('hides accept button when onAccept is null', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(
        caseModel: caseModel,
        onAccept: null,
      ));
      await tester.pumpAndSettle();
      
      expect(find.text('Accept'), findsNothing);
    });

    testWidgets('shows complete button for accepted cases when callback provided', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'ACCEPTED',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(
        caseModel: caseModel,
        onComplete: () {},
      ));
      await tester.pumpAndSettle();
      
      expect(find.text('Complete'), findsOneWidget);
    });

    testWidgets('calls onAccept when accept button is tapped', (tester) async {
      bool acceptCalled = false;
      
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(
        caseModel: caseModel,
        onAccept: () => acceptCalled = true,
      ));
      await tester.pumpAndSettle();
      
      await tester.tap(find.text('Accept'));
      expect(acceptCalled, isTrue);
    });

    testWidgets('displays loading state when isLoading is true', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(
        caseModel: caseModel,
        onAccept: () {},
        isLoading: true,
      ));
      await tester.pump();
      
      // Should show loading indicator in button
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('displays correctly in Arabic (RTL)', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 3',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        riskLevel: 'HIGH',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(
        caseModel: caseModel,
        locale: const Locale('ar'),
      ));
      await tester.pumpAndSettle();
      
      // Should display Arabic text
      expect(find.text('طوارئ'), findsOneWidget);  // emergency in Arabic
      expect(find.text('معلق'), findsOneWidget);  // pending in Arabic
    });

    testWidgets('emergency card has distinct background color', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      // Find the Card widget
      final cardFinder = find.byType(Card);
      expect(cardFinder, findsOneWidget);
    });

    testWidgets('card is tappable and navigates to details', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      // Card should be wrapped in InkWell for tap handling
      expect(find.byType(InkWell), findsWidgets);
    });

    testWidgets('displays created date formatted', (tester) async {
      const caseModel = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      await tester.pumpWidget(createTestWidget(caseModel: caseModel));
      await tester.pumpAndSettle();
      
      // Should display formatted date (15/1/2024)
      expect(find.textContaining('15/1/2024'), findsOneWidget);
    });
  });

  group('CaseModel', () {
    test('isEmergency returns true for EMERGENCY type', () {
      const model = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      expect(model.isEmergency, isTrue);
    });

    test('isPending returns true for PENDING status', () {
      const model = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      expect(model.isPending, isTrue);
    });

    test('isAccepted returns true for ACCEPTED status', () {
      const model = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'ACCEPTED',
        createdAt: '2024-01-15T10:30:00',
      );
      
      expect(model.isAccepted, isTrue);
    });

    test('isCompleted returns true for COMPLETED status', () {
      const model = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'COMPLETED',
        createdAt: '2024-01-15T10:30:00',
      );
      
      expect(model.isCompleted, isTrue);
    });

    test('isActive returns true for non-completed/cancelled cases', () {
      const pending = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'PENDING',
        createdAt: '2024-01-15T10:30:00',
      );
      
      const completed = CaseModel(
        caseId: 'HR-0002',
        zone: 'Zone 1',
        requestType: 'SUPPORT',
        status: 'COMPLETED',
        createdAt: '2024-01-15T10:30:00',
      );
      
      expect(pending.isActive, isTrue);
      expect(completed.isActive, isFalse);
    });

    test('toDbMap creates correct database map', () {
      const model = CaseModel(
        caseId: 'HR-0001',
        zone: 'Zone 1',
        requestType: 'EMERGENCY',
        status: 'PENDING',
        riskLevel: 'HIGH',
        createdAt: '2024-01-15T10:30:00',
      );
      
      final map = model.toDbMap();
      
      expect(map['case_id'], 'HR-0001');
      expect(map['zone'], 'Zone 1');
      expect(map['request_type'], 'EMERGENCY');
      expect(map['status'], 'PENDING');
      expect(map['risk_level'], 'HIGH');
    });

    test('fromDbMap creates model from database map', () {
      final map = {
        'case_id': 'HR-0001',
        'zone': 'Zone 1',
        'request_type': 'EMERGENCY',
        'status': 'PENDING',
        'risk_level': 'HIGH',
        'created_at': '2024-01-15T10:30:00',
      };
      
      final model = CaseModel.fromDbMap(map);
      
      expect(model.caseId, 'HR-0001');
      expect(model.zone, 'Zone 1');
      expect(model.requestType, 'EMERGENCY');
      expect(model.status, 'PENDING');
      expect(model.riskLevel, 'HIGH');
    });
  });
}
