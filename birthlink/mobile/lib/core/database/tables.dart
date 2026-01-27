/// Database table definitions for offline storage.
class Tables {
  Tables._();

  // Table names
  static const String cases = 'cases';
  static const String volunteers = 'volunteers';
  static const String syncQueue = 'sync_queue';

  /// Cases table for storing help requests.
  static const String createCasesTable = '''
    CREATE TABLE $cases (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      case_id TEXT UNIQUE NOT NULL,
      request_type TEXT NOT NULL,
      status TEXT NOT NULL,
      zone TEXT NOT NULL,
      risk_level TEXT,
      due_date TEXT,
      mother_phone TEXT,
      mother_name TEXT,
      volunteer_phone TEXT,
      volunteer_name TEXT,
      created_at TEXT NOT NULL,
      accepted_at TEXT,
      closed_at TEXT,
      notes TEXT,
      synced_at TEXT
    )
  ''';

  /// Volunteers table for caching volunteer data.
  static const String createVolunteersTable = '''
    CREATE TABLE $volunteers (
      id INTEGER PRIMARY KEY,
      formatted_id TEXT NOT NULL,
      phone_number TEXT UNIQUE NOT NULL,
      name TEXT,
      camp TEXT NOT NULL,
      skill_type TEXT NOT NULL,
      zones TEXT NOT NULL,
      availability TEXT NOT NULL,
      preferred_language TEXT NOT NULL,
      registered_at TEXT NOT NULL,
      last_active_at TEXT,
      completed_cases INTEGER DEFAULT 0,
      synced_at TEXT
    )
  ''';

  /// Sync queue for offline actions.
  static const String createSyncQueueTable = '''
    CREATE TABLE $syncQueue (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      action_type TEXT NOT NULL,
      payload TEXT NOT NULL,
      created_at TEXT NOT NULL,
      attempts INTEGER DEFAULT 0,
      last_attempt_at TEXT,
      error TEXT
    )
  ''';
}
