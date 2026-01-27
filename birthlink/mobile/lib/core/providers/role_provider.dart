import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// User role in the application.
enum UserRole {
  ngo,       // NGO Coordinator - sees all zones
  volunteer, // Volunteer - sees only assigned zones
}

/// Storage key for role preference.
const String _keyUserRole = 'user_role';
const String _keyHasSelectedRole = 'has_selected_role';

/// Provider for the current user role.
final roleProvider = StateNotifierProvider<RoleNotifier, UserRole>((ref) {
  return RoleNotifier();
});

/// Provider to check if user has selected a role.
final hasSelectedRoleProvider = FutureProvider<bool>((ref) async {
  final prefs = await SharedPreferences.getInstance();
  return prefs.getBool(_keyHasSelectedRole) ?? false;
});

/// Notifier for managing user role state.
class RoleNotifier extends StateNotifier<UserRole> {
  RoleNotifier() : super(UserRole.volunteer) {
    _loadSavedRole();
  }

  /// Load the saved role from preferences.
  Future<void> _loadSavedRole() async {
    final prefs = await SharedPreferences.getInstance();
    final savedRole = prefs.getString(_keyUserRole);
    if (savedRole != null) {
      state = savedRole == 'ngo' ? UserRole.ngo : UserRole.volunteer;
    }
  }

  /// Set the user role and save to preferences.
  Future<void> setRole(UserRole role) async {
    state = role;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyUserRole, role == UserRole.ngo ? 'ngo' : 'volunteer');
    await prefs.setBool(_keyHasSelectedRole, true);
  }

  /// Toggle between NGO and Volunteer roles.
  Future<void> toggleRole() async {
    final newRole = state == UserRole.ngo ? UserRole.volunteer : UserRole.ngo;
    await setRole(newRole);
  }

  /// Check if current role is NGO.
  bool get isNgo => state == UserRole.ngo;

  /// Check if current role is Volunteer.
  bool get isVolunteer => state == UserRole.volunteer;
}

/// Extension for easy role display.
extension UserRoleExtension on UserRole {
  String get displayName {
    switch (this) {
      case UserRole.ngo:
        return 'NGO Coordinator';
      case UserRole.volunteer:
        return 'Volunteer';
    }
  }

  String get displayNameAr {
    switch (this) {
      case UserRole.ngo:
        return 'منسق المنظمة';
      case UserRole.volunteer:
        return 'متطوع';
    }
  }
}
