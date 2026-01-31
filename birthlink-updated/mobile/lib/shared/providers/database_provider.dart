import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sqflite/sqflite.dart';

import '../../core/database/database_helper.dart';

/// Provider for the SQLite database instance.
/// 
/// This is an async provider that initializes the database once
/// and provides access throughout the app for offline support.
final databaseProvider = FutureProvider<Database>((ref) async {
  final db = await DatabaseHelper.instance.database;
  return db;
});

/// Provider to check if database is ready.
final isDatabaseReadyProvider = Provider<bool>((ref) {
  final dbAsync = ref.watch(databaseProvider);
  return dbAsync.hasValue;
});
