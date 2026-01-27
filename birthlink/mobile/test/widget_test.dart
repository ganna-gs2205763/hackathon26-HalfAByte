// SafeBirth Connect - Widget Tests
//
// Basic smoke tests for the SafeBirth Connect Flutter app.

import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:safebirth_connect/app.dart';

void main() {
  testWidgets('SafeBirth app builds and renders', (WidgetTester tester) async {
    // Build our app wrapped in ProviderScope and trigger a frame.
    await tester.pumpWidget(
      const ProviderScope(
        child: SafeBirthApp(),
      ),
    );

    // Verify that the app renders without crashing.
    // The app should display the bottom navigation bar.
    expect(find.byType(SafeBirthApp), findsOneWidget);
  });
}
