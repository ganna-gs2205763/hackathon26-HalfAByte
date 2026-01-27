import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'core/database/database_helper.dart';

/// SafeBirth Connect - SMS-first maternal support coordination app.
/// 
/// This app is designed for NGO coordinators and volunteers to manage
/// maternal healthcare support in crisis settings.
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize local database
  await DatabaseHelper.instance.database;
  
  runApp(
    const ProviderScope(
      child: SafeBirthApp(),
    ),
  );
}
