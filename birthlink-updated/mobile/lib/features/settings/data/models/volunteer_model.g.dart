// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'volunteer_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$VolunteerModelImpl _$$VolunteerModelImplFromJson(Map<String, dynamic> json) =>
    _$VolunteerModelImpl(
      id: (json['id'] as num).toInt(),
      formattedId: json['formattedId'] as String,
      phoneNumber: json['phoneNumber'] as String,
      name: json['name'] as String?,
      camp: json['camp'] as String,
      skillType: json['skillType'] as String,
      zones: (json['zones'] as List<dynamic>).map((e) => e as String).toList(),
      availability: json['availability'] as String,
      preferredLanguage: json['preferredLanguage'] as String,
      registeredAt: json['registeredAt'] as String,
      lastActiveAt: json['lastActiveAt'] as String?,
      completedCases: (json['completedCases'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$VolunteerModelImplToJson(
        _$VolunteerModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'formattedId': instance.formattedId,
      'phoneNumber': instance.phoneNumber,
      'name': instance.name,
      'camp': instance.camp,
      'skillType': instance.skillType,
      'zones': instance.zones,
      'availability': instance.availability,
      'preferredLanguage': instance.preferredLanguage,
      'registeredAt': instance.registeredAt,
      'lastActiveAt': instance.lastActiveAt,
      'completedCases': instance.completedCases,
    };
