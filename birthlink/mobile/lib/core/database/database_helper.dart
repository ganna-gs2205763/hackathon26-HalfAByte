import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'tables.dart';

/// SQLite database helper for offline data persistence.
class DatabaseHelper {
  static final DatabaseHelper instance = DatabaseHelper._();
  static Database? _database;

  DatabaseHelper._();

  /// Get the database instance, creating it if necessary.
  Future<Database> get database async {
    _database ??= await _initDatabase();
    return _database!;
  }

  /// Initialize the database.
  Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, 'safebirth_connect.db');

    return openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
      onUpgrade: _onUpgrade,
    );
  }

  /// Create tables on first run.
  Future<void> _onCreate(Database db, int version) async {
    await db.execute(Tables.createCasesTable);
    await db.execute(Tables.createVolunteersTable);
    await db.execute(Tables.createSyncQueueTable);
  }

  /// Handle database upgrades.
  Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    // Add migration logic here when schema changes
  }

  /// Close the database.
  Future<void> close() async {
    final db = _database;
    if (db != null) {
      await db.close();
      _database = null;
    }
  }

  /// Clear all data (for testing/logout).
  Future<void> clearAll() async {
    final db = await database;
    await db.delete(Tables.cases);
    await db.delete(Tables.volunteers);
    await db.delete(Tables.syncQueue);
  }
}
