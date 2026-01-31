/// Static strings and constants for the application.
/// Note: User-facing strings should use localization instead.
class AppStrings {
  AppStrings._();

  // App info
  static const String appName = 'SafeBirth Connect';
  static const String appVersion = '1.0.0';

  // Asset paths
  static const String assetsImages = 'assets/images/';
  static const String assetsIcons = 'assets/icons/';
  static const String assetsFonts = 'assets/fonts/';

  // Storage keys
  static const String keyLocale = 'app_locale';
  static const String keyThemeMode = 'theme_mode';
  static const String keyVolunteerPhone = 'volunteer_phone';
  static const String keyLastSync = 'last_sync';

  // Date/time formats
  static const String dateFormat = 'yyyy-MM-dd';
  static const String timeFormat = 'HH:mm';
  static const String dateTimeFormat = 'yyyy-MM-dd HH:mm';
}
