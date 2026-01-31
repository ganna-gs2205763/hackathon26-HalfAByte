import 'package:freezed_annotation/freezed_annotation.dart';

part 'case_model.freezed.dart';
part 'case_model.g.dart';

/// Model representing a help request case.
/// Maps to the backend CaseDto.
@freezed
class CaseModel with _$CaseModel {
  const factory CaseModel({
    required String caseId,
    required String zone,
    required String requestType,
    required String status,
    String? riskLevel,
    String? dueDate,
    required String createdAt,
    String? acceptedAt,
    String? closedAt,
    String? volunteerName,
    String? volunteerPhone,
    String? motherPhone,
    String? motherName,
    String? notes,
  }) = _CaseModel;

  const CaseModel._();

  factory CaseModel.fromJson(Map<String, dynamic> json) =>
      _$CaseModelFromJson(json);

  /// Check if this is an emergency case.
  bool get isEmergency => requestType.toUpperCase() == 'EMERGENCY';

  /// Check if case is pending (not yet accepted).
  bool get isPending => status.toUpperCase() == 'PENDING';

  /// Check if case has been accepted.
  bool get isAccepted => status.toUpperCase() == 'ACCEPTED';

  /// Check if case is in progress.
  bool get isInProgress => status.toUpperCase() == 'IN_PROGRESS';

  /// Check if case is completed.
  bool get isCompleted => status.toUpperCase() == 'COMPLETED';

  /// Check if case is cancelled.
  bool get isCancelled => status.toUpperCase() == 'CANCELLED';

  /// Check if case is active (not completed or cancelled).
  bool get isActive => !isCompleted && !isCancelled;

  /// Get a user-friendly status display string.
  String get statusDisplay {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 'Pending';
      case 'ACCEPTED':
        return 'Accepted';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'COMPLETED':
        return 'Completed';
      case 'CANCELLED':
        return 'Cancelled';
      default:
        return status;
    }
  }

  /// Convert to database map for offline storage.
  Map<String, dynamic> toDbMap() {
    return {
      'case_id': caseId,
      'zone': zone,
      'request_type': requestType,
      'status': status,
      'risk_level': riskLevel,
      'due_date': dueDate,
      'created_at': createdAt,
      'accepted_at': acceptedAt,
      'closed_at': closedAt,
      'volunteer_name': volunteerName,
      'volunteer_phone': volunteerPhone,
      'mother_phone': motherPhone,
      'mother_name': motherName,
      'notes': notes,
      'synced_at': DateTime.now().toIso8601String(),
    };
  }

  /// Create from database map.
  factory CaseModel.fromDbMap(Map<String, dynamic> map) {
    return CaseModel(
      caseId: map['case_id'] as String,
      zone: map['zone'] as String,
      requestType: map['request_type'] as String,
      status: map['status'] as String,
      riskLevel: map['risk_level'] as String?,
      dueDate: map['due_date'] as String?,
      createdAt: map['created_at'] as String,
      acceptedAt: map['accepted_at'] as String?,
      closedAt: map['closed_at'] as String?,
      volunteerName: map['volunteer_name'] as String?,
      volunteerPhone: map['volunteer_phone'] as String?,
      motherPhone: map['mother_phone'] as String?,
      motherName: map['mother_name'] as String?,
      notes: map['notes'] as String?,
    );
  }
}
