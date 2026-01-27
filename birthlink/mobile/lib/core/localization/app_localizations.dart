import 'package:flutter/material.dart';

/// Application localization support for Arabic and English.
class AppLocalizations {
  final Locale locale;

  AppLocalizations(this.locale);

  /// Get the current AppLocalizations instance.
  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate = 
      _AppLocalizationsDelegate();

  /// Supported locales (Arabic first as default).
  static const List<Locale> supportedLocales = [
    Locale('ar'),  // Arabic - primary user base
    Locale('en'),  // English
  ];

  /// Check if current locale is RTL.
  bool get isRtl => locale.languageCode == 'ar';

  // Localized strings - comprehensive translations for both Arabic and English
  static final Map<String, Map<String, String>> _localizedStrings = {
    'en': {
      // General
      'app_name': 'SafeBirth Connect',
      'app_title': 'SafeBirth Connect',
      'loading': 'Loading...',
      'error': 'Error',
      'retry': 'Retry',
      'cancel': 'Cancel',
      'save': 'Save',
      'close': 'Close',
      'ok': 'OK',
      'confirm': 'Confirm',
      'delete': 'Delete',
      'edit': 'Edit',
      'view': 'View',
      'search': 'Search',
      'filter': 'Filter',
      'refresh': 'Refresh',
      'no_data': 'No data available',
      'pull_to_refresh': 'Pull to refresh',
      
      // Navigation
      'nav_dashboard': 'Dashboard',
      'nav_inbox': 'Inbox',
      'nav_heatmap': 'Heatmap',
      'nav_settings': 'Settings',
      'inbox': 'Case Inbox',
      'dashboard': 'Dashboard',
      'settings': 'Settings',
      
      // Dashboard
      'dashboard_title': 'Dashboard',
      'total_mothers': 'Total Mothers',
      'total_volunteers': 'Volunteers',
      'active_requests': 'Active Requests',
      'pending_emergencies': 'Pending Emergencies',
      'available_volunteers': 'Available',
      'recent_cases': 'Recent Cases',
      'zones_overview': 'Zones Overview',
      
      // Cases
      'cases_title': 'Cases',
      'case_emergency': 'Emergency',
      'case_support': 'Support',
      'emergency': 'Emergency',
      'support': 'Support Request',
      'status_pending': 'Pending',
      'status_accepted': 'Accepted',
      'status_in_progress': 'In Progress',
      'status_completed': 'Completed',
      'status_cancelled': 'Cancelled',
      'pending': 'Pending',
      'accepted': 'Accepted',
      'completed': 'Completed',
      'no_cases': 'No cases yet',
      'accept': 'Accept',
      'complete': 'Complete',
      'case_details': 'Case Details',
      'assigned_volunteer': 'Assigned Volunteer',
      'created_at': 'Created At',
      'accepted_at': 'Accepted At',
      'closed_at': 'Closed At',
      
      // Risk levels
      'risk_level': 'Risk Level',
      'risk_low': 'Low Risk',
      'risk_medium': 'Medium Risk',
      'risk_high': 'High Risk',
      'high': 'High',
      'medium': 'Medium',
      'low': 'Low',
      
      // Zone
      'zone': 'Zone',
      'camp': 'Camp',
      'due_date': 'Due Date',
      
      // Volunteers
      'volunteers_title': 'Volunteers',
      'skill_midwife': 'Midwife',
      'skill_nurse': 'Nurse',
      'skill_trained': 'Trained Attendant',
      'skill_community': 'Community Volunteer',
      'availability_available': 'Available',
      'availability_busy': 'Busy',
      'availability_offline': 'Offline',
      'available': 'Available',
      'busy': 'Busy',
      'volunteer_profile': 'Volunteer Profile',
      'my_cases': 'My Cases',
      'update_availability': 'Update Availability',
      
      // Mothers
      'mother_name': 'Mother Name',
      'mother_phone': 'Mother Phone',
      'mother_details': 'Mother Details',
      
      // Settings
      'settings_title': 'Settings',
      'settings_language': 'Language',
      'settings_notifications': 'Notifications',
      'settings_about': 'About',
      'settings_logout': 'Logout',
      'settings_profile': 'Profile',
      'language_english': 'English',
      'language_arabic': 'Arabic',
      
      // Errors
      'error_network': 'No internet connection',
      'error_server': 'Server error. Please try again.',
      'error_unknown': 'An error occurred',
      'error_timeout': 'Request timed out',
      'error_not_found': 'Not found',
      
      // Actions
      'submit': 'Submit',
      'update': 'Update',
      'add': 'Add',
      'remove': 'Remove',
      'select': 'Select',
      'continue_text': 'Continue',
      'back': 'Back',
      'next': 'Next',
      'done': 'Done',
      'skip': 'Skip',

      // Heatmap
      'heatmap_title': 'Zone Heatmap',
      'heatmap_legend_title': 'Load Level',
      'zone_load_low': 'Low',
      'zone_load_medium': 'Medium',
      'zone_load_high': 'High',
      'zone_load_critical': 'Critical',
      'my_zones': 'My Zones',
      'total_zones': 'Zones',
      'critical_zones': 'critical',
      'high_load_zones': 'high load',
      'no_zones': 'No zones available',
      'no_zones_assigned': 'No zones assigned',
      'zones_assigned': 'zones assigned',
      'cases': 'cases',
      'volunteers': 'volunteers',
      'load_score': 'Load Score',
      'cases_per_volunteer': 'cases/volunteer',
      'set_phone_to_see_zones': 'Set your phone number in Settings to view your assigned zones',
      'contact_coordinator': 'Contact your coordinator to be assigned zones',

      // Role Selection
      'role_selection_title': 'Select Your Role',
      'role_selection_subtitle': 'Choose how you will use the app',
      'role_ngo': 'NGO Coordinator',
      'role_ngo_description': 'View all zones and cases across the region',
      'role_volunteer': 'Volunteer',
      'role_volunteer_description': 'View only your assigned zones and cases',
      'role_change_hint': 'You can change this later in Settings',
      'settings_role': 'User Role',
      'role_current': 'Current role',
    },
    'ar': {
      // General
      'app_name': 'سيف بيرث كونكت',
      'app_title': 'سيف بيرث كونكت',
      'loading': 'جاري التحميل...',
      'error': 'خطأ',
      'retry': 'إعادة المحاولة',
      'cancel': 'إلغاء',
      'save': 'حفظ',
      'close': 'إغلاق',
      'ok': 'حسناً',
      'confirm': 'تأكيد',
      'delete': 'حذف',
      'edit': 'تعديل',
      'view': 'عرض',
      'search': 'بحث',
      'filter': 'تصفية',
      'refresh': 'تحديث',
      'no_data': 'لا توجد بيانات',
      'pull_to_refresh': 'اسحب للتحديث',
      
      // Navigation
      'nav_dashboard': 'لوحة التحكم',
      'nav_inbox': 'صندوق الوارد',
      'nav_heatmap': 'خريطة الحرارة',
      'nav_settings': 'الإعدادات',
      'inbox': 'صندوق الحالات',
      'dashboard': 'لوحة التحكم',
      'settings': 'الإعدادات',
      
      // Dashboard
      'dashboard_title': 'لوحة التحكم',
      'total_mothers': 'إجمالي الأمهات',
      'total_volunteers': 'المتطوعين',
      'active_requests': 'الطلبات النشطة',
      'pending_emergencies': 'حالات الطوارئ المعلقة',
      'available_volunteers': 'متاح',
      'recent_cases': 'الحالات الأخيرة',
      'zones_overview': 'نظرة عامة على المناطق',
      
      // Cases
      'cases_title': 'الحالات',
      'case_emergency': 'طوارئ',
      'case_support': 'دعم',
      'emergency': 'طوارئ',
      'support': 'طلب مساعدة',
      'status_pending': 'معلق',
      'status_accepted': 'مقبول',
      'status_in_progress': 'قيد التنفيذ',
      'status_completed': 'مكتمل',
      'status_cancelled': 'ملغى',
      'pending': 'معلق',
      'accepted': 'مقبول',
      'completed': 'مكتمل',
      'no_cases': 'لا توجد حالات بعد',
      'accept': 'قبول',
      'complete': 'إنهاء',
      'case_details': 'تفاصيل الحالة',
      'assigned_volunteer': 'المتطوع المعين',
      'created_at': 'تاريخ الإنشاء',
      'accepted_at': 'تاريخ القبول',
      'closed_at': 'تاريخ الإغلاق',
      
      // Risk levels
      'risk_level': 'مستوى الخطر',
      'risk_low': 'خطر منخفض',
      'risk_medium': 'خطر متوسط',
      'risk_high': 'خطر عالي',
      'high': 'عالي',
      'medium': 'متوسط',
      'low': 'منخفض',
      
      // Zone
      'zone': 'المنطقة',
      'camp': 'المخيم',
      'due_date': 'تاريخ الولادة المتوقع',
      
      // Volunteers
      'volunteers_title': 'المتطوعين',
      'skill_midwife': 'قابلة',
      'skill_nurse': 'ممرضة',
      'skill_trained': 'مساعدة مدربة',
      'skill_community': 'متطوع مجتمعي',
      'availability_available': 'متاح',
      'availability_busy': 'مشغول',
      'availability_offline': 'غير متصل',
      'available': 'متاح',
      'busy': 'مشغول',
      'volunteer_profile': 'ملف المتطوع',
      'my_cases': 'حالاتي',
      'update_availability': 'تحديث التوفر',
      
      // Mothers
      'mother_name': 'اسم الأم',
      'mother_phone': 'هاتف الأم',
      'mother_details': 'تفاصيل الأم',
      
      // Settings
      'settings_title': 'الإعدادات',
      'settings_language': 'اللغة',
      'settings_notifications': 'الإشعارات',
      'settings_about': 'حول التطبيق',
      'settings_logout': 'تسجيل الخروج',
      'settings_profile': 'الملف الشخصي',
      'language_english': 'الإنجليزية',
      'language_arabic': 'العربية',
      
      // Errors
      'error_network': 'لا يوجد اتصال بالإنترنت',
      'error_server': 'خطأ في الخادم. يرجى المحاولة مرة أخرى.',
      'error_unknown': 'حدث خطأ',
      'error_timeout': 'انتهت مهلة الطلب',
      'error_not_found': 'غير موجود',
      
      // Actions
      'submit': 'إرسال',
      'update': 'تحديث',
      'add': 'إضافة',
      'remove': 'إزالة',
      'select': 'اختيار',
      'continue_text': 'متابعة',
      'back': 'رجوع',
      'next': 'التالي',
      'done': 'تم',
      'skip': 'تخطي',

      // Heatmap
      'heatmap_title': 'خريطة المناطق',
      'heatmap_legend_title': 'مستوى الحمل',
      'zone_load_low': 'منخفض',
      'zone_load_medium': 'متوسط',
      'zone_load_high': 'مرتفع',
      'zone_load_critical': 'حرج',
      'my_zones': 'مناطقي',
      'total_zones': 'المناطق',
      'critical_zones': 'حرجة',
      'high_load_zones': 'حمل عالي',
      'no_zones': 'لا توجد مناطق',
      'no_zones_assigned': 'لم يتم تعيين مناطق',
      'zones_assigned': 'مناطق معينة',
      'cases': 'حالات',
      'volunteers': 'متطوعين',
      'load_score': 'درجة الحمل',
      'cases_per_volunteer': 'حالة/متطوع',
      'set_phone_to_see_zones': 'أدخل رقم هاتفك في الإعدادات لرؤية المناطق المعينة لك',
      'contact_coordinator': 'تواصل مع المنسق لتعيين مناطق',

      // Role Selection
      'role_selection_title': 'اختر دورك',
      'role_selection_subtitle': 'اختر طريقة استخدامك للتطبيق',
      'role_ngo': 'منسق المنظمة',
      'role_ngo_description': 'عرض جميع المناطق والحالات في المنطقة',
      'role_volunteer': 'متطوع',
      'role_volunteer_description': 'عرض المناطق والحالات المعينة لك فقط',
      'role_change_hint': 'يمكنك تغيير هذا لاحقاً في الإعدادات',
      'settings_role': 'دور المستخدم',
      'role_current': 'الدور الحالي',
    },
  };

  /// Get a localized string by key.
  String translate(String key) {
    return _localizedStrings[locale.languageCode]?[key] ?? key;
  }
}

/// Delegate for loading AppLocalizations.
class _AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) {
    return ['en', 'ar'].contains(locale.languageCode);
  }

  @override
  Future<AppLocalizations> load(Locale locale) async {
    return AppLocalizations(locale);
  }

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

/// Extension for easy translation access.
extension TranslateX on BuildContext {
  /// Translate a key to localized string.
  String tr(String key) => AppLocalizations.of(this).translate(key);
  
  /// Check if current locale is RTL (Arabic).
  bool get isRtl => AppLocalizations.of(this).isRtl;
}

/// Extension for easy localization access (alternative syntax).
extension LocalizationExtension on BuildContext {
  /// Get AppLocalizations instance.
  AppLocalizations get l10n => AppLocalizations.of(this);
}
