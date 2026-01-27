// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'case_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$CaseModelImpl _$$CaseModelImplFromJson(Map<String, dynamic> json) =>
    _$CaseModelImpl(
      caseId: json['caseId'] as String,
      zone: json['zone'] as String,
      requestType: json['requestType'] as String,
      status: json['status'] as String,
      riskLevel: json['riskLevel'] as String?,
      dueDate: json['dueDate'] as String?,
      createdAt: json['createdAt'] as String,
      acceptedAt: json['acceptedAt'] as String?,
      closedAt: json['closedAt'] as String?,
      volunteerName: json['volunteerName'] as String?,
      volunteerPhone: json['volunteerPhone'] as String?,
      motherPhone: json['motherPhone'] as String?,
      motherName: json['motherName'] as String?,
      notes: json['notes'] as String?,
    );

Map<String, dynamic> _$$CaseModelImplToJson(_$CaseModelImpl instance) =>
    <String, dynamic>{
      'caseId': instance.caseId,
      'zone': instance.zone,
      'requestType': instance.requestType,
      'status': instance.status,
      'riskLevel': instance.riskLevel,
      'dueDate': instance.dueDate,
      'createdAt': instance.createdAt,
      'acceptedAt': instance.acceptedAt,
      'closedAt': instance.closedAt,
      'volunteerName': instance.volunteerName,
      'volunteerPhone': instance.volunteerPhone,
      'motherPhone': instance.motherPhone,
      'motherName': instance.motherName,
      'notes': instance.notes,
    };
