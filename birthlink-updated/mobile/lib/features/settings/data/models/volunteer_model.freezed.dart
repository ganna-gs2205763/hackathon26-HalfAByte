// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'volunteer_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

VolunteerModel _$VolunteerModelFromJson(Map<String, dynamic> json) {
  return _VolunteerModel.fromJson(json);
}

/// @nodoc
mixin _$VolunteerModel {
  int get id => throw _privateConstructorUsedError;
  String get formattedId => throw _privateConstructorUsedError;
  String get phoneNumber => throw _privateConstructorUsedError;
  String? get name => throw _privateConstructorUsedError;
  String get camp => throw _privateConstructorUsedError;
  String get skillType => throw _privateConstructorUsedError;
  List<String> get zones => throw _privateConstructorUsedError;
  String get availability => throw _privateConstructorUsedError;
  String get preferredLanguage => throw _privateConstructorUsedError;
  String get registeredAt => throw _privateConstructorUsedError;
  String? get lastActiveAt => throw _privateConstructorUsedError;
  int get completedCases => throw _privateConstructorUsedError;

  /// Serializes this VolunteerModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of VolunteerModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $VolunteerModelCopyWith<VolunteerModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $VolunteerModelCopyWith<$Res> {
  factory $VolunteerModelCopyWith(
          VolunteerModel value, $Res Function(VolunteerModel) then) =
      _$VolunteerModelCopyWithImpl<$Res, VolunteerModel>;
  @useResult
  $Res call(
      {int id,
      String formattedId,
      String phoneNumber,
      String? name,
      String camp,
      String skillType,
      List<String> zones,
      String availability,
      String preferredLanguage,
      String registeredAt,
      String? lastActiveAt,
      int completedCases});
}

/// @nodoc
class _$VolunteerModelCopyWithImpl<$Res, $Val extends VolunteerModel>
    implements $VolunteerModelCopyWith<$Res> {
  _$VolunteerModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of VolunteerModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? formattedId = null,
    Object? phoneNumber = null,
    Object? name = freezed,
    Object? camp = null,
    Object? skillType = null,
    Object? zones = null,
    Object? availability = null,
    Object? preferredLanguage = null,
    Object? registeredAt = null,
    Object? lastActiveAt = freezed,
    Object? completedCases = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      formattedId: null == formattedId
          ? _value.formattedId
          : formattedId // ignore: cast_nullable_to_non_nullable
              as String,
      phoneNumber: null == phoneNumber
          ? _value.phoneNumber
          : phoneNumber // ignore: cast_nullable_to_non_nullable
              as String,
      name: freezed == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String?,
      camp: null == camp
          ? _value.camp
          : camp // ignore: cast_nullable_to_non_nullable
              as String,
      skillType: null == skillType
          ? _value.skillType
          : skillType // ignore: cast_nullable_to_non_nullable
              as String,
      zones: null == zones
          ? _value.zones
          : zones // ignore: cast_nullable_to_non_nullable
              as List<String>,
      availability: null == availability
          ? _value.availability
          : availability // ignore: cast_nullable_to_non_nullable
              as String,
      preferredLanguage: null == preferredLanguage
          ? _value.preferredLanguage
          : preferredLanguage // ignore: cast_nullable_to_non_nullable
              as String,
      registeredAt: null == registeredAt
          ? _value.registeredAt
          : registeredAt // ignore: cast_nullable_to_non_nullable
              as String,
      lastActiveAt: freezed == lastActiveAt
          ? _value.lastActiveAt
          : lastActiveAt // ignore: cast_nullable_to_non_nullable
              as String?,
      completedCases: null == completedCases
          ? _value.completedCases
          : completedCases // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$VolunteerModelImplCopyWith<$Res>
    implements $VolunteerModelCopyWith<$Res> {
  factory _$$VolunteerModelImplCopyWith(_$VolunteerModelImpl value,
          $Res Function(_$VolunteerModelImpl) then) =
      __$$VolunteerModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      String formattedId,
      String phoneNumber,
      String? name,
      String camp,
      String skillType,
      List<String> zones,
      String availability,
      String preferredLanguage,
      String registeredAt,
      String? lastActiveAt,
      int completedCases});
}

/// @nodoc
class __$$VolunteerModelImplCopyWithImpl<$Res>
    extends _$VolunteerModelCopyWithImpl<$Res, _$VolunteerModelImpl>
    implements _$$VolunteerModelImplCopyWith<$Res> {
  __$$VolunteerModelImplCopyWithImpl(
      _$VolunteerModelImpl _value, $Res Function(_$VolunteerModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of VolunteerModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? formattedId = null,
    Object? phoneNumber = null,
    Object? name = freezed,
    Object? camp = null,
    Object? skillType = null,
    Object? zones = null,
    Object? availability = null,
    Object? preferredLanguage = null,
    Object? registeredAt = null,
    Object? lastActiveAt = freezed,
    Object? completedCases = null,
  }) {
    return _then(_$VolunteerModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      formattedId: null == formattedId
          ? _value.formattedId
          : formattedId // ignore: cast_nullable_to_non_nullable
              as String,
      phoneNumber: null == phoneNumber
          ? _value.phoneNumber
          : phoneNumber // ignore: cast_nullable_to_non_nullable
              as String,
      name: freezed == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String?,
      camp: null == camp
          ? _value.camp
          : camp // ignore: cast_nullable_to_non_nullable
              as String,
      skillType: null == skillType
          ? _value.skillType
          : skillType // ignore: cast_nullable_to_non_nullable
              as String,
      zones: null == zones
          ? _value._zones
          : zones // ignore: cast_nullable_to_non_nullable
              as List<String>,
      availability: null == availability
          ? _value.availability
          : availability // ignore: cast_nullable_to_non_nullable
              as String,
      preferredLanguage: null == preferredLanguage
          ? _value.preferredLanguage
          : preferredLanguage // ignore: cast_nullable_to_non_nullable
              as String,
      registeredAt: null == registeredAt
          ? _value.registeredAt
          : registeredAt // ignore: cast_nullable_to_non_nullable
              as String,
      lastActiveAt: freezed == lastActiveAt
          ? _value.lastActiveAt
          : lastActiveAt // ignore: cast_nullable_to_non_nullable
              as String?,
      completedCases: null == completedCases
          ? _value.completedCases
          : completedCases // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$VolunteerModelImpl extends _VolunteerModel {
  const _$VolunteerModelImpl(
      {required this.id,
      required this.formattedId,
      required this.phoneNumber,
      this.name,
      required this.camp,
      required this.skillType,
      required final List<String> zones,
      required this.availability,
      required this.preferredLanguage,
      required this.registeredAt,
      this.lastActiveAt,
      this.completedCases = 0})
      : _zones = zones,
        super._();

  factory _$VolunteerModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$VolunteerModelImplFromJson(json);

  @override
  final int id;
  @override
  final String formattedId;
  @override
  final String phoneNumber;
  @override
  final String? name;
  @override
  final String camp;
  @override
  final String skillType;
  final List<String> _zones;
  @override
  List<String> get zones {
    if (_zones is EqualUnmodifiableListView) return _zones;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_zones);
  }

  @override
  final String availability;
  @override
  final String preferredLanguage;
  @override
  final String registeredAt;
  @override
  final String? lastActiveAt;
  @override
  @JsonKey()
  final int completedCases;

  @override
  String toString() {
    return 'VolunteerModel(id: $id, formattedId: $formattedId, phoneNumber: $phoneNumber, name: $name, camp: $camp, skillType: $skillType, zones: $zones, availability: $availability, preferredLanguage: $preferredLanguage, registeredAt: $registeredAt, lastActiveAt: $lastActiveAt, completedCases: $completedCases)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$VolunteerModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.formattedId, formattedId) ||
                other.formattedId == formattedId) &&
            (identical(other.phoneNumber, phoneNumber) ||
                other.phoneNumber == phoneNumber) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.camp, camp) || other.camp == camp) &&
            (identical(other.skillType, skillType) ||
                other.skillType == skillType) &&
            const DeepCollectionEquality().equals(other._zones, _zones) &&
            (identical(other.availability, availability) ||
                other.availability == availability) &&
            (identical(other.preferredLanguage, preferredLanguage) ||
                other.preferredLanguage == preferredLanguage) &&
            (identical(other.registeredAt, registeredAt) ||
                other.registeredAt == registeredAt) &&
            (identical(other.lastActiveAt, lastActiveAt) ||
                other.lastActiveAt == lastActiveAt) &&
            (identical(other.completedCases, completedCases) ||
                other.completedCases == completedCases));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      formattedId,
      phoneNumber,
      name,
      camp,
      skillType,
      const DeepCollectionEquality().hash(_zones),
      availability,
      preferredLanguage,
      registeredAt,
      lastActiveAt,
      completedCases);

  /// Create a copy of VolunteerModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$VolunteerModelImplCopyWith<_$VolunteerModelImpl> get copyWith =>
      __$$VolunteerModelImplCopyWithImpl<_$VolunteerModelImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$VolunteerModelImplToJson(
      this,
    );
  }
}

abstract class _VolunteerModel extends VolunteerModel {
  const factory _VolunteerModel(
      {required final int id,
      required final String formattedId,
      required final String phoneNumber,
      final String? name,
      required final String camp,
      required final String skillType,
      required final List<String> zones,
      required final String availability,
      required final String preferredLanguage,
      required final String registeredAt,
      final String? lastActiveAt,
      final int completedCases}) = _$VolunteerModelImpl;
  const _VolunteerModel._() : super._();

  factory _VolunteerModel.fromJson(Map<String, dynamic> json) =
      _$VolunteerModelImpl.fromJson;

  @override
  int get id;
  @override
  String get formattedId;
  @override
  String get phoneNumber;
  @override
  String? get name;
  @override
  String get camp;
  @override
  String get skillType;
  @override
  List<String> get zones;
  @override
  String get availability;
  @override
  String get preferredLanguage;
  @override
  String get registeredAt;
  @override
  String? get lastActiveAt;
  @override
  int get completedCases;

  /// Create a copy of VolunteerModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$VolunteerModelImplCopyWith<_$VolunteerModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
