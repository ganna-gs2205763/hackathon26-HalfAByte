// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dashboard_stats_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$DashboardStatsModelImpl _$$DashboardStatsModelImplFromJson(
        Map<String, dynamic> json) =>
    _$DashboardStatsModelImpl(
      totalMothers: (json['totalMothers'] as num).toInt(),
      totalVolunteers: (json['totalVolunteers'] as num).toInt(),
      activeVolunteers: (json['activeVolunteers'] as num).toInt(),
      pendingRequests: (json['pendingRequests'] as num).toInt(),
      activeRequests: (json['activeRequests'] as num).toInt(),
      completedToday: (json['completedToday'] as num).toInt(),
      mothersByZone: (json['mothersByZone'] as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ) ??
          const {},
      requestsByStatus:
          (json['requestsByStatus'] as Map<String, dynamic>?)?.map(
                (k, e) => MapEntry(k, (e as num).toInt()),
              ) ??
              const {},
      volunteersBySkill:
          (json['volunteersBySkill'] as Map<String, dynamic>?)?.map(
                (k, e) => MapEntry(k, (e as num).toInt()),
              ) ??
              const {},
      upcomingDueDates: (json['upcomingDueDates'] as List<dynamic>?)
              ?.map((e) => DueDateCluster.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
    );

Map<String, dynamic> _$$DashboardStatsModelImplToJson(
        _$DashboardStatsModelImpl instance) =>
    <String, dynamic>{
      'totalMothers': instance.totalMothers,
      'totalVolunteers': instance.totalVolunteers,
      'activeVolunteers': instance.activeVolunteers,
      'pendingRequests': instance.pendingRequests,
      'activeRequests': instance.activeRequests,
      'completedToday': instance.completedToday,
      'mothersByZone': instance.mothersByZone,
      'requestsByStatus': instance.requestsByStatus,
      'volunteersBySkill': instance.volunteersBySkill,
      'upcomingDueDates': instance.upcomingDueDates,
    };

_$DueDateClusterImpl _$$DueDateClusterImplFromJson(Map<String, dynamic> json) =>
    _$DueDateClusterImpl(
      date: json['date'] as String,
      count: (json['count'] as num).toInt(),
    );

Map<String, dynamic> _$$DueDateClusterImplToJson(
        _$DueDateClusterImpl instance) =>
    <String, dynamic>{
      'date': instance.date,
      'count': instance.count,
    };

_$ZoneStatsModelImpl _$$ZoneStatsModelImplFromJson(Map<String, dynamic> json) =>
    _$ZoneStatsModelImpl(
      zone: json['zone'] as String,
      motherCount: (json['motherCount'] as num).toInt(),
      volunteerCount: (json['volunteerCount'] as num).toInt(),
      pendingCases: (json['pendingCases'] as num).toInt(),
      activeCases: (json['activeCases'] as num).toInt(),
    );

Map<String, dynamic> _$$ZoneStatsModelImplToJson(
        _$ZoneStatsModelImpl instance) =>
    <String, dynamic>{
      'zone': instance.zone,
      'motherCount': instance.motherCount,
      'volunteerCount': instance.volunteerCount,
      'pendingCases': instance.pendingCases,
      'activeCases': instance.activeCases,
    };
