// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'dashboard_stats_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

DashboardStatsModel _$DashboardStatsModelFromJson(Map<String, dynamic> json) {
  return _DashboardStatsModel.fromJson(json);
}

/// @nodoc
mixin _$DashboardStatsModel {
  int get totalMothers => throw _privateConstructorUsedError;
  int get totalVolunteers => throw _privateConstructorUsedError;
  int get activeVolunteers => throw _privateConstructorUsedError;
  int get pendingRequests => throw _privateConstructorUsedError;
  int get activeRequests => throw _privateConstructorUsedError;
  int get completedToday => throw _privateConstructorUsedError;
  Map<String, int> get mothersByZone => throw _privateConstructorUsedError;
  Map<String, int> get requestsByStatus => throw _privateConstructorUsedError;
  Map<String, int> get volunteersBySkill => throw _privateConstructorUsedError;
  List<DueDateCluster> get upcomingDueDates =>
      throw _privateConstructorUsedError;

  /// Serializes this DashboardStatsModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of DashboardStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DashboardStatsModelCopyWith<DashboardStatsModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DashboardStatsModelCopyWith<$Res> {
  factory $DashboardStatsModelCopyWith(
          DashboardStatsModel value, $Res Function(DashboardStatsModel) then) =
      _$DashboardStatsModelCopyWithImpl<$Res, DashboardStatsModel>;
  @useResult
  $Res call(
      {int totalMothers,
      int totalVolunteers,
      int activeVolunteers,
      int pendingRequests,
      int activeRequests,
      int completedToday,
      Map<String, int> mothersByZone,
      Map<String, int> requestsByStatus,
      Map<String, int> volunteersBySkill,
      List<DueDateCluster> upcomingDueDates});
}

/// @nodoc
class _$DashboardStatsModelCopyWithImpl<$Res, $Val extends DashboardStatsModel>
    implements $DashboardStatsModelCopyWith<$Res> {
  _$DashboardStatsModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DashboardStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? totalMothers = null,
    Object? totalVolunteers = null,
    Object? activeVolunteers = null,
    Object? pendingRequests = null,
    Object? activeRequests = null,
    Object? completedToday = null,
    Object? mothersByZone = null,
    Object? requestsByStatus = null,
    Object? volunteersBySkill = null,
    Object? upcomingDueDates = null,
  }) {
    return _then(_value.copyWith(
      totalMothers: null == totalMothers
          ? _value.totalMothers
          : totalMothers // ignore: cast_nullable_to_non_nullable
              as int,
      totalVolunteers: null == totalVolunteers
          ? _value.totalVolunteers
          : totalVolunteers // ignore: cast_nullable_to_non_nullable
              as int,
      activeVolunteers: null == activeVolunteers
          ? _value.activeVolunteers
          : activeVolunteers // ignore: cast_nullable_to_non_nullable
              as int,
      pendingRequests: null == pendingRequests
          ? _value.pendingRequests
          : pendingRequests // ignore: cast_nullable_to_non_nullable
              as int,
      activeRequests: null == activeRequests
          ? _value.activeRequests
          : activeRequests // ignore: cast_nullable_to_non_nullable
              as int,
      completedToday: null == completedToday
          ? _value.completedToday
          : completedToday // ignore: cast_nullable_to_non_nullable
              as int,
      mothersByZone: null == mothersByZone
          ? _value.mothersByZone
          : mothersByZone // ignore: cast_nullable_to_non_nullable
              as Map<String, int>,
      requestsByStatus: null == requestsByStatus
          ? _value.requestsByStatus
          : requestsByStatus // ignore: cast_nullable_to_non_nullable
              as Map<String, int>,
      volunteersBySkill: null == volunteersBySkill
          ? _value.volunteersBySkill
          : volunteersBySkill // ignore: cast_nullable_to_non_nullable
              as Map<String, int>,
      upcomingDueDates: null == upcomingDueDates
          ? _value.upcomingDueDates
          : upcomingDueDates // ignore: cast_nullable_to_non_nullable
              as List<DueDateCluster>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DashboardStatsModelImplCopyWith<$Res>
    implements $DashboardStatsModelCopyWith<$Res> {
  factory _$$DashboardStatsModelImplCopyWith(_$DashboardStatsModelImpl value,
          $Res Function(_$DashboardStatsModelImpl) then) =
      __$$DashboardStatsModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int totalMothers,
      int totalVolunteers,
      int activeVolunteers,
      int pendingRequests,
      int activeRequests,
      int completedToday,
      Map<String, int> mothersByZone,
      Map<String, int> requestsByStatus,
      Map<String, int> volunteersBySkill,
      List<DueDateCluster> upcomingDueDates});
}

/// @nodoc
class __$$DashboardStatsModelImplCopyWithImpl<$Res>
    extends _$DashboardStatsModelCopyWithImpl<$Res, _$DashboardStatsModelImpl>
    implements _$$DashboardStatsModelImplCopyWith<$Res> {
  __$$DashboardStatsModelImplCopyWithImpl(_$DashboardStatsModelImpl _value,
      $Res Function(_$DashboardStatsModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of DashboardStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? totalMothers = null,
    Object? totalVolunteers = null,
    Object? activeVolunteers = null,
    Object? pendingRequests = null,
    Object? activeRequests = null,
    Object? completedToday = null,
    Object? mothersByZone = null,
    Object? requestsByStatus = null,
    Object? volunteersBySkill = null,
    Object? upcomingDueDates = null,
  }) {
    return _then(_$DashboardStatsModelImpl(
      totalMothers: null == totalMothers
          ? _value.totalMothers
          : totalMothers // ignore: cast_nullable_to_non_nullable
              as int,
      totalVolunteers: null == totalVolunteers
          ? _value.totalVolunteers
          : totalVolunteers // ignore: cast_nullable_to_non_nullable
              as int,
      activeVolunteers: null == activeVolunteers
          ? _value.activeVolunteers
          : activeVolunteers // ignore: cast_nullable_to_non_nullable
              as int,
      pendingRequests: null == pendingRequests
          ? _value.pendingRequests
          : pendingRequests // ignore: cast_nullable_to_non_nullable
              as int,
      activeRequests: null == activeRequests
          ? _value.activeRequests
          : activeRequests // ignore: cast_nullable_to_non_nullable
              as int,
      completedToday: null == completedToday
          ? _value.completedToday
          : completedToday // ignore: cast_nullable_to_non_nullable
              as int,
      mothersByZone: null == mothersByZone
          ? _value._mothersByZone
          : mothersByZone // ignore: cast_nullable_to_non_nullable
              as Map<String, int>,
      requestsByStatus: null == requestsByStatus
          ? _value._requestsByStatus
          : requestsByStatus // ignore: cast_nullable_to_non_nullable
              as Map<String, int>,
      volunteersBySkill: null == volunteersBySkill
          ? _value._volunteersBySkill
          : volunteersBySkill // ignore: cast_nullable_to_non_nullable
              as Map<String, int>,
      upcomingDueDates: null == upcomingDueDates
          ? _value._upcomingDueDates
          : upcomingDueDates // ignore: cast_nullable_to_non_nullable
              as List<DueDateCluster>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$DashboardStatsModelImpl extends _DashboardStatsModel {
  const _$DashboardStatsModelImpl(
      {required this.totalMothers,
      required this.totalVolunteers,
      required this.activeVolunteers,
      required this.pendingRequests,
      required this.activeRequests,
      required this.completedToday,
      final Map<String, int> mothersByZone = const {},
      final Map<String, int> requestsByStatus = const {},
      final Map<String, int> volunteersBySkill = const {},
      final List<DueDateCluster> upcomingDueDates = const []})
      : _mothersByZone = mothersByZone,
        _requestsByStatus = requestsByStatus,
        _volunteersBySkill = volunteersBySkill,
        _upcomingDueDates = upcomingDueDates,
        super._();

  factory _$DashboardStatsModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$DashboardStatsModelImplFromJson(json);

  @override
  final int totalMothers;
  @override
  final int totalVolunteers;
  @override
  final int activeVolunteers;
  @override
  final int pendingRequests;
  @override
  final int activeRequests;
  @override
  final int completedToday;
  final Map<String, int> _mothersByZone;
  @override
  @JsonKey()
  Map<String, int> get mothersByZone {
    if (_mothersByZone is EqualUnmodifiableMapView) return _mothersByZone;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_mothersByZone);
  }

  final Map<String, int> _requestsByStatus;
  @override
  @JsonKey()
  Map<String, int> get requestsByStatus {
    if (_requestsByStatus is EqualUnmodifiableMapView) return _requestsByStatus;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_requestsByStatus);
  }

  final Map<String, int> _volunteersBySkill;
  @override
  @JsonKey()
  Map<String, int> get volunteersBySkill {
    if (_volunteersBySkill is EqualUnmodifiableMapView)
      return _volunteersBySkill;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_volunteersBySkill);
  }

  final List<DueDateCluster> _upcomingDueDates;
  @override
  @JsonKey()
  List<DueDateCluster> get upcomingDueDates {
    if (_upcomingDueDates is EqualUnmodifiableListView)
      return _upcomingDueDates;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_upcomingDueDates);
  }

  @override
  String toString() {
    return 'DashboardStatsModel(totalMothers: $totalMothers, totalVolunteers: $totalVolunteers, activeVolunteers: $activeVolunteers, pendingRequests: $pendingRequests, activeRequests: $activeRequests, completedToday: $completedToday, mothersByZone: $mothersByZone, requestsByStatus: $requestsByStatus, volunteersBySkill: $volunteersBySkill, upcomingDueDates: $upcomingDueDates)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DashboardStatsModelImpl &&
            (identical(other.totalMothers, totalMothers) ||
                other.totalMothers == totalMothers) &&
            (identical(other.totalVolunteers, totalVolunteers) ||
                other.totalVolunteers == totalVolunteers) &&
            (identical(other.activeVolunteers, activeVolunteers) ||
                other.activeVolunteers == activeVolunteers) &&
            (identical(other.pendingRequests, pendingRequests) ||
                other.pendingRequests == pendingRequests) &&
            (identical(other.activeRequests, activeRequests) ||
                other.activeRequests == activeRequests) &&
            (identical(other.completedToday, completedToday) ||
                other.completedToday == completedToday) &&
            const DeepCollectionEquality()
                .equals(other._mothersByZone, _mothersByZone) &&
            const DeepCollectionEquality()
                .equals(other._requestsByStatus, _requestsByStatus) &&
            const DeepCollectionEquality()
                .equals(other._volunteersBySkill, _volunteersBySkill) &&
            const DeepCollectionEquality()
                .equals(other._upcomingDueDates, _upcomingDueDates));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      totalMothers,
      totalVolunteers,
      activeVolunteers,
      pendingRequests,
      activeRequests,
      completedToday,
      const DeepCollectionEquality().hash(_mothersByZone),
      const DeepCollectionEquality().hash(_requestsByStatus),
      const DeepCollectionEquality().hash(_volunteersBySkill),
      const DeepCollectionEquality().hash(_upcomingDueDates));

  /// Create a copy of DashboardStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DashboardStatsModelImplCopyWith<_$DashboardStatsModelImpl> get copyWith =>
      __$$DashboardStatsModelImplCopyWithImpl<_$DashboardStatsModelImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$DashboardStatsModelImplToJson(
      this,
    );
  }
}

abstract class _DashboardStatsModel extends DashboardStatsModel {
  const factory _DashboardStatsModel(
      {required final int totalMothers,
      required final int totalVolunteers,
      required final int activeVolunteers,
      required final int pendingRequests,
      required final int activeRequests,
      required final int completedToday,
      final Map<String, int> mothersByZone,
      final Map<String, int> requestsByStatus,
      final Map<String, int> volunteersBySkill,
      final List<DueDateCluster> upcomingDueDates}) = _$DashboardStatsModelImpl;
  const _DashboardStatsModel._() : super._();

  factory _DashboardStatsModel.fromJson(Map<String, dynamic> json) =
      _$DashboardStatsModelImpl.fromJson;

  @override
  int get totalMothers;
  @override
  int get totalVolunteers;
  @override
  int get activeVolunteers;
  @override
  int get pendingRequests;
  @override
  int get activeRequests;
  @override
  int get completedToday;
  @override
  Map<String, int> get mothersByZone;
  @override
  Map<String, int> get requestsByStatus;
  @override
  Map<String, int> get volunteersBySkill;
  @override
  List<DueDateCluster> get upcomingDueDates;

  /// Create a copy of DashboardStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DashboardStatsModelImplCopyWith<_$DashboardStatsModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

DueDateCluster _$DueDateClusterFromJson(Map<String, dynamic> json) {
  return _DueDateCluster.fromJson(json);
}

/// @nodoc
mixin _$DueDateCluster {
  String get date => throw _privateConstructorUsedError;
  int get count => throw _privateConstructorUsedError;

  /// Serializes this DueDateCluster to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of DueDateCluster
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DueDateClusterCopyWith<DueDateCluster> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DueDateClusterCopyWith<$Res> {
  factory $DueDateClusterCopyWith(
          DueDateCluster value, $Res Function(DueDateCluster) then) =
      _$DueDateClusterCopyWithImpl<$Res, DueDateCluster>;
  @useResult
  $Res call({String date, int count});
}

/// @nodoc
class _$DueDateClusterCopyWithImpl<$Res, $Val extends DueDateCluster>
    implements $DueDateClusterCopyWith<$Res> {
  _$DueDateClusterCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DueDateCluster
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? date = null,
    Object? count = null,
  }) {
    return _then(_value.copyWith(
      date: null == date
          ? _value.date
          : date // ignore: cast_nullable_to_non_nullable
              as String,
      count: null == count
          ? _value.count
          : count // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DueDateClusterImplCopyWith<$Res>
    implements $DueDateClusterCopyWith<$Res> {
  factory _$$DueDateClusterImplCopyWith(_$DueDateClusterImpl value,
          $Res Function(_$DueDateClusterImpl) then) =
      __$$DueDateClusterImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String date, int count});
}

/// @nodoc
class __$$DueDateClusterImplCopyWithImpl<$Res>
    extends _$DueDateClusterCopyWithImpl<$Res, _$DueDateClusterImpl>
    implements _$$DueDateClusterImplCopyWith<$Res> {
  __$$DueDateClusterImplCopyWithImpl(
      _$DueDateClusterImpl _value, $Res Function(_$DueDateClusterImpl) _then)
      : super(_value, _then);

  /// Create a copy of DueDateCluster
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? date = null,
    Object? count = null,
  }) {
    return _then(_$DueDateClusterImpl(
      date: null == date
          ? _value.date
          : date // ignore: cast_nullable_to_non_nullable
              as String,
      count: null == count
          ? _value.count
          : count // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$DueDateClusterImpl implements _DueDateCluster {
  const _$DueDateClusterImpl({required this.date, required this.count});

  factory _$DueDateClusterImpl.fromJson(Map<String, dynamic> json) =>
      _$$DueDateClusterImplFromJson(json);

  @override
  final String date;
  @override
  final int count;

  @override
  String toString() {
    return 'DueDateCluster(date: $date, count: $count)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DueDateClusterImpl &&
            (identical(other.date, date) || other.date == date) &&
            (identical(other.count, count) || other.count == count));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, date, count);

  /// Create a copy of DueDateCluster
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DueDateClusterImplCopyWith<_$DueDateClusterImpl> get copyWith =>
      __$$DueDateClusterImplCopyWithImpl<_$DueDateClusterImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$DueDateClusterImplToJson(
      this,
    );
  }
}

abstract class _DueDateCluster implements DueDateCluster {
  const factory _DueDateCluster(
      {required final String date,
      required final int count}) = _$DueDateClusterImpl;

  factory _DueDateCluster.fromJson(Map<String, dynamic> json) =
      _$DueDateClusterImpl.fromJson;

  @override
  String get date;
  @override
  int get count;

  /// Create a copy of DueDateCluster
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DueDateClusterImplCopyWith<_$DueDateClusterImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

ZoneStatsModel _$ZoneStatsModelFromJson(Map<String, dynamic> json) {
  return _ZoneStatsModel.fromJson(json);
}

/// @nodoc
mixin _$ZoneStatsModel {
  String get zone => throw _privateConstructorUsedError;
  int get motherCount => throw _privateConstructorUsedError;
  int get volunteerCount => throw _privateConstructorUsedError;
  int get pendingCases => throw _privateConstructorUsedError;
  int get activeCases => throw _privateConstructorUsedError;

  /// Serializes this ZoneStatsModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ZoneStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ZoneStatsModelCopyWith<ZoneStatsModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ZoneStatsModelCopyWith<$Res> {
  factory $ZoneStatsModelCopyWith(
          ZoneStatsModel value, $Res Function(ZoneStatsModel) then) =
      _$ZoneStatsModelCopyWithImpl<$Res, ZoneStatsModel>;
  @useResult
  $Res call(
      {String zone,
      int motherCount,
      int volunteerCount,
      int pendingCases,
      int activeCases});
}

/// @nodoc
class _$ZoneStatsModelCopyWithImpl<$Res, $Val extends ZoneStatsModel>
    implements $ZoneStatsModelCopyWith<$Res> {
  _$ZoneStatsModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ZoneStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? zone = null,
    Object? motherCount = null,
    Object? volunteerCount = null,
    Object? pendingCases = null,
    Object? activeCases = null,
  }) {
    return _then(_value.copyWith(
      zone: null == zone
          ? _value.zone
          : zone // ignore: cast_nullable_to_non_nullable
              as String,
      motherCount: null == motherCount
          ? _value.motherCount
          : motherCount // ignore: cast_nullable_to_non_nullable
              as int,
      volunteerCount: null == volunteerCount
          ? _value.volunteerCount
          : volunteerCount // ignore: cast_nullable_to_non_nullable
              as int,
      pendingCases: null == pendingCases
          ? _value.pendingCases
          : pendingCases // ignore: cast_nullable_to_non_nullable
              as int,
      activeCases: null == activeCases
          ? _value.activeCases
          : activeCases // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ZoneStatsModelImplCopyWith<$Res>
    implements $ZoneStatsModelCopyWith<$Res> {
  factory _$$ZoneStatsModelImplCopyWith(_$ZoneStatsModelImpl value,
          $Res Function(_$ZoneStatsModelImpl) then) =
      __$$ZoneStatsModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String zone,
      int motherCount,
      int volunteerCount,
      int pendingCases,
      int activeCases});
}

/// @nodoc
class __$$ZoneStatsModelImplCopyWithImpl<$Res>
    extends _$ZoneStatsModelCopyWithImpl<$Res, _$ZoneStatsModelImpl>
    implements _$$ZoneStatsModelImplCopyWith<$Res> {
  __$$ZoneStatsModelImplCopyWithImpl(
      _$ZoneStatsModelImpl _value, $Res Function(_$ZoneStatsModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of ZoneStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? zone = null,
    Object? motherCount = null,
    Object? volunteerCount = null,
    Object? pendingCases = null,
    Object? activeCases = null,
  }) {
    return _then(_$ZoneStatsModelImpl(
      zone: null == zone
          ? _value.zone
          : zone // ignore: cast_nullable_to_non_nullable
              as String,
      motherCount: null == motherCount
          ? _value.motherCount
          : motherCount // ignore: cast_nullable_to_non_nullable
              as int,
      volunteerCount: null == volunteerCount
          ? _value.volunteerCount
          : volunteerCount // ignore: cast_nullable_to_non_nullable
              as int,
      pendingCases: null == pendingCases
          ? _value.pendingCases
          : pendingCases // ignore: cast_nullable_to_non_nullable
              as int,
      activeCases: null == activeCases
          ? _value.activeCases
          : activeCases // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ZoneStatsModelImpl extends _ZoneStatsModel {
  const _$ZoneStatsModelImpl(
      {required this.zone,
      required this.motherCount,
      required this.volunteerCount,
      required this.pendingCases,
      required this.activeCases})
      : super._();

  factory _$ZoneStatsModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$ZoneStatsModelImplFromJson(json);

  @override
  final String zone;
  @override
  final int motherCount;
  @override
  final int volunteerCount;
  @override
  final int pendingCases;
  @override
  final int activeCases;

  @override
  String toString() {
    return 'ZoneStatsModel(zone: $zone, motherCount: $motherCount, volunteerCount: $volunteerCount, pendingCases: $pendingCases, activeCases: $activeCases)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ZoneStatsModelImpl &&
            (identical(other.zone, zone) || other.zone == zone) &&
            (identical(other.motherCount, motherCount) ||
                other.motherCount == motherCount) &&
            (identical(other.volunteerCount, volunteerCount) ||
                other.volunteerCount == volunteerCount) &&
            (identical(other.pendingCases, pendingCases) ||
                other.pendingCases == pendingCases) &&
            (identical(other.activeCases, activeCases) ||
                other.activeCases == activeCases));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, zone, motherCount,
      volunteerCount, pendingCases, activeCases);

  /// Create a copy of ZoneStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ZoneStatsModelImplCopyWith<_$ZoneStatsModelImpl> get copyWith =>
      __$$ZoneStatsModelImplCopyWithImpl<_$ZoneStatsModelImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ZoneStatsModelImplToJson(
      this,
    );
  }
}

abstract class _ZoneStatsModel extends ZoneStatsModel {
  const factory _ZoneStatsModel(
      {required final String zone,
      required final int motherCount,
      required final int volunteerCount,
      required final int pendingCases,
      required final int activeCases}) = _$ZoneStatsModelImpl;
  const _ZoneStatsModel._() : super._();

  factory _ZoneStatsModel.fromJson(Map<String, dynamic> json) =
      _$ZoneStatsModelImpl.fromJson;

  @override
  String get zone;
  @override
  int get motherCount;
  @override
  int get volunteerCount;
  @override
  int get pendingCases;
  @override
  int get activeCases;

  /// Create a copy of ZoneStatsModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ZoneStatsModelImplCopyWith<_$ZoneStatsModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
