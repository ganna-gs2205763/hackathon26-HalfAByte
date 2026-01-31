import 'package:intl/intl.dart';

/// Date and time utility functions.
class AppDateUtils {
  AppDateUtils._();

  static final DateFormat _dateFormat = DateFormat('yyyy-MM-dd');
  static final DateFormat _timeFormat = DateFormat('HH:mm');
  static final DateFormat _dateTimeFormat = DateFormat('yyyy-MM-dd HH:mm');
  static final DateFormat _displayDateFormat = DateFormat('MMM d, yyyy');
  static final DateFormat _displayDateTimeFormat = DateFormat('MMM d, yyyy HH:mm');

  /// Format a date for API communication.
  static String formatDate(DateTime date) => _dateFormat.format(date);

  /// Format a time.
  static String formatTime(DateTime time) => _timeFormat.format(time);

  /// Format a datetime for API communication.
  static String formatDateTime(DateTime dateTime) => _dateTimeFormat.format(dateTime);

  /// Format a date for display.
  static String formatDisplayDate(DateTime date) => _displayDateFormat.format(date);

  /// Format a datetime for display.
  static String formatDisplayDateTime(DateTime dateTime) => _displayDateTimeFormat.format(dateTime);

  /// Parse a date string from API.
  static DateTime? parseDate(String? dateStr) {
    if (dateStr == null || dateStr.isEmpty) return null;
    try {
      return DateTime.parse(dateStr);
    } catch (_) {
      return null;
    }
  }

  /// Get time ago string (e.g., "2 hours ago").
  static String timeAgo(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 30) {
      return formatDisplayDate(dateTime);
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m ago';
    } else {
      return 'Just now';
    }
  }

  /// Get time ago string in Arabic.
  static String timeAgoAr(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 30) {
      return formatDisplayDate(dateTime);
    } else if (difference.inDays > 0) {
      return 'منذ ${difference.inDays} يوم';
    } else if (difference.inHours > 0) {
      return 'منذ ${difference.inHours} ساعة';
    } else if (difference.inMinutes > 0) {
      return 'منذ ${difference.inMinutes} دقيقة';
    } else {
      return 'الآن';
    }
  }
}
