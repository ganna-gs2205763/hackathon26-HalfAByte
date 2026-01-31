import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../core/constants/app_strings.dart';

/// Provider for the current app locale.
final localeProvider = StateNotifierProvider<LocaleNotifier, Locale>((ref) {
  return LocaleNotifier();
});

/// Notifier for managing app locale state.
/// Default locale is Arabic (primary user base in crisis settings).
class LocaleNotifier extends StateNotifier<Locale> {
  LocaleNotifier() : super(const Locale('ar')) {
    _loadSavedLocale();
  }

  /// Load the saved locale from preferences.
  Future<void> _loadSavedLocale() async {
    final prefs = await SharedPreferences.getInstance();
    final savedLocale = prefs.getString(AppStrings.keyLocale);
    if (savedLocale != null) {
      state = Locale(savedLocale);
    }
  }

  /// Set the app locale and save to preferences.
  Future<void> setLocale(Locale locale) async {
    state = locale;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(AppStrings.keyLocale, locale.languageCode);
  }

  /// Toggle between English and Arabic.
  Future<void> toggleLocale() async {
    final newLocale = state.languageCode == 'en' 
        ? const Locale('ar') 
        : const Locale('en');
    await setLocale(newLocale);
  }
}
