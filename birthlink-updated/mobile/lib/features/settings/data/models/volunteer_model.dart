import 'package:freezed_annotation/freezed_annotation.dart';

part 'volunteer_model.freezed.dart';
part 'volunteer_model.g.dart';

/// Model representing a volunteer.
/// Maps to the backend VolunteerDto.
@freezed
class VolunteerModel with _$VolunteerModel {
  const factory VolunteerModel({
    required int id,
    required String formattedId,
    required String phoneNumber,
    String? name,
    required String camp,
    required String skillType,
    required List<String> zones,
    required String availability,
    required String preferredLanguage,
    required String registeredAt,
    String? lastActiveAt,
    @Default(0) int completedCases,
  }) = _VolunteerModel;

  const VolunteerModel._();

  factory VolunteerModel.fromJson(Map<String, dynamic> json) =>
      _$VolunteerModelFromJson(json);

  /// Check if volunteer is available.
  bool get isAvailable => availability.toUpperCase() == 'AVAILABLE';

  /// Check if volunteer is busy.
  bool get isBusy => availability.toUpperCase() == 'BUSY';

  /// Check if volunteer is offline.
  bool get isOffline => availability.toUpperCase() == 'OFFLINE';

  /// Get display name (name or phone number).
  String get displayName => name?.isNotEmpty == true ? name! : phoneNumber;

  /// Get skill type display string.
  String get skillTypeDisplay {
    switch (skillType.toUpperCase()) {
      case 'MIDWIFE':
        return 'Midwife';
      case 'NURSE':
        return 'Nurse';
      case 'TRAINED_ATTENDANT':
        return 'Trained Attendant';
      case 'COMMUNITY_VOLUNTEER':
        return 'Community Volunteer';
      default:
        return skillType;
    }
  }

  /// Get availability display string.
  String get availabilityDisplay {
    switch (availability.toUpperCase()) {
      case 'AVAILABLE':
        return 'Available';
      case 'BUSY':
        return 'Busy';
      case 'OFFLINE':
        return 'Offline';
      default:
        return availability;
    }
  }

  /// Get zones as comma-separated string.
  String get zonesDisplay => zones.join(', ');

  /// Convert to database map for offline storage.
  Map<String, dynamic> toDbMap() {
    return {
      'id': id,
      'formatted_id': formattedId,
      'phone_number': phoneNumber,
      'name': name,
      'camp': camp,
      'skill_type': skillType,
      'zones': zones.join(','),
      'availability': availability,
      'preferred_language': preferredLanguage,
      'registered_at': registeredAt,
      'last_active_at': lastActiveAt,
      'completed_cases': completedCases,
      'synced_at': DateTime.now().toIso8601String(),
    };
  }

  /// Create from database map.
  factory VolunteerModel.fromDbMap(Map<String, dynamic> map) {
    return VolunteerModel(
      id: map['id'] as int,
      formattedId: map['formatted_id'] as String,
      phoneNumber: map['phone_number'] as String,
      name: map['name'] as String?,
      camp: map['camp'] as String,
      skillType: map['skill_type'] as String,
      zones: (map['zones'] as String).split(','),
      availability: map['availability'] as String,
      preferredLanguage: map['preferred_language'] as String,
      registeredAt: map['registered_at'] as String,
      lastActiveAt: map['last_active_at'] as String?,
      completedCases: map['completed_cases'] as int? ?? 0,
    );
  }
}
