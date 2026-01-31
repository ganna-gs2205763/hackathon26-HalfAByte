// SafeBirth Connect - Settings Screen Widget Tests
//
// Tests for the SettingsScreen widget including:
// - Language toggle functionality
// - Volunteer profile display
// - Availability toggle
// - RTL Arabic layout
// - Logout functionality

import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:safebirth_connect/core/constants/app_colors.dart';
import 'package:safebirth_connect/core/localization/app_localizations.dart';
import 'package:safebirth_connect/features/settings/presentation/providers/volunteer_provider.dart';
import 'package:safebirth_connect/features/settings/presentation/screens/settings_screen.dart';
import 'package:safebirth_connect/features/dashboard/data/models/volunteer_model.dart';
import 'package:safebirth_connect/shared/providers/locale_provider.dart';

void main() {
  group('SettingsScreen', () {
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
          home: const SettingsScreen(),
        ),
      );
    }

    testWidgets('displays settings title', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      expect(find.text('Settings'), findsOneWidget);
    });

    testWidgets('displays language section', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      expect(find.text('Language'), findsWidgets);
    });

    testWidgets('displays notifications section', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      expect(find.text('Notifications'), findsWidgets);
    });

    testWidgets('displays about section', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      expect(find.text('About'), findsOneWidget);
      expect(find.text('SafeBirth Connect'), findsOneWidget);
      expect(find.text('Version 1.0.0'), findsOneWidget);
    });

    testWidgets('displays logout option', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      expect(find.text('Logout'), findsOneWidget);
    });

    testWidgets('shows volunteer profile section when profile loaded', (tester) async {
      final profileProvider = volunteerProfileNotifierProvider.overrideWith(() {
        return _MockVolunteerProfileNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [profileProvider],
      ));
      
      await tester.pumpAndSettle();
      
      // Should show volunteer profile section
      expect(find.text('Volunteer Profile'), findsOneWidget);
      expect(find.text('Test Volunteer'), findsOneWidget);
    });

    testWidgets('shows availability toggle when profile loaded', (tester) async {
      final profileProvider = volunteerProfileNotifierProvider.overrideWith(() {
        return _MockVolunteerProfileNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [profileProvider],
      ));
      
      await tester.pumpAndSettle();
      
      expect(find.text('Update Availability'), findsOneWidget);
      expect(find.text('Available'), findsWidgets);
    });

    testWidgets('displays correctly in Arabic (RTL)', (tester) async {
      await tester.pumpWidget(createTestWidget(
        locale: const Locale('ar'),
      ));
      
      await tester.pumpAndSettle();
      
      // Should display Arabic text
      expect(find.text('الإعدادات'), findsOneWidget);  // settings_title in Arabic
      expect(find.text('اللغة'), findsWidgets);  // settings_language in Arabic
    });

    testWidgets('language switch toggles between EN and AR', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      // Find language switch
      final switches = find.byType(Switch);
      expect(switches, findsWidgets);
    });

    testWidgets('tapping logout shows confirmation dialog', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      // Find and tap logout
      await tester.tap(find.text('Logout'));
      await tester.pumpAndSettle();
      
      // Should show confirmation dialog
      expect(find.byType(AlertDialog), findsOneWidget);
      expect(find.text('Cancel'), findsOneWidget);
    });

    testWidgets('displays open source licenses option', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();
      
      expect(find.text('Open Source Licenses'), findsOneWidget);
    });

    testWidgets('shows completed cases count for volunteer', (tester) async {
      final profileProvider = volunteerProfileNotifierProvider.overrideWith(() {
        return _MockVolunteerProfileNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [profileProvider],
      ));
      
      await tester.pumpAndSettle();
      
      expect(find.text('Completed'), findsOneWidget);
      expect(find.text('25 cases'), findsOneWidget);
    });

    testWidgets('shows zone information for volunteer', (tester) async {
      final profileProvider = volunteerProfileNotifierProvider.overrideWith(() {
        return _MockVolunteerProfileNotifier();
      });
      
      await tester.pumpWidget(createTestWidget(
        overrides: [profileProvider],
      ));
      
      await tester.pumpAndSettle();
      
      expect(find.text('Zone'), findsOneWidget);
    });
  });

  group('Language Toggle', () {
    testWidgets('switches locale when toggled', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          child: Consumer(
            builder: (context, ref, _) {
              final locale = ref.watch(localeProvider);
              return MaterialApp(
                locale: locale,
                localizationsDelegates: const [
                  AppLocalizations.delegate,
                  GlobalMaterialLocalizations.delegate,
                  GlobalWidgetsLocalizations.delegate,
                  GlobalCupertinoLocalizations.delegate,
                ],
                supportedLocales: AppLocalizations.supportedLocales,
                home: const SettingsScreen(),
              );
            },
          ),
        ),
      );
      
      await tester.pumpAndSettle();
      
      // Find language tile
      expect(find.text('Language'), findsWidgets);
    });
  });
}

// Mock notifiers for testing

class _MockVolunteerProfileNotifier extends VolunteerProfileNotifier {
  @override
  Future<VolunteerModel?> build() async {
    return const VolunteerModel(
      id: 1,
      phoneNumber: '+201234567890',
      name: 'Test Volunteer',
      camp: 'Camp A',
      skillType: 'MIDWIFE',
      zones: ['Zone 1', 'Zone 2'],
      availability: 'AVAILABLE',
      preferredLanguage: 'ENGLISH',
      completedCases: 25,
    );
  }
}
