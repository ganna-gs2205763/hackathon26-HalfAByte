import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Key for storing volunteer phone number.
const String _kPhoneNumberKey = 'volunteer_phone_number';

/// Provider for the current volunteer's phone number.
/// This is used for authentication via X-Phone-Number header.
final phoneNumberProvider = StateNotifierProvider<PhoneNumberNotifier, String?>((ref) {
  return PhoneNumberNotifier();
});

/// Notifier for managing the volunteer phone number.
class PhoneNumberNotifier extends StateNotifier<String?> {
  PhoneNumberNotifier() : super(null) {
    _loadPhoneNumber();
  }

  /// Load saved phone number from preferences.
  Future<void> _loadPhoneNumber() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getString(_kPhoneNumberKey);
  }

  /// Set the volunteer phone number.
  Future<void> setPhoneNumber(String phoneNumber) async {
    state = phoneNumber;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_kPhoneNumberKey, phoneNumber);
  }

  /// Clear the phone number (logout).
  Future<void> clearPhoneNumber() async {
    state = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_kPhoneNumberKey);
  }
}

/// Provider that indicates if user is logged in (has phone number set).
final isLoggedInProvider = Provider<bool>((ref) {
  final phone = ref.watch(phoneNumberProvider);
  return phone != null && phone.isNotEmpty;
});
