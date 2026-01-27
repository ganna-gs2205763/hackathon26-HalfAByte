import 'package:flutter/material.dart';

/// Application color palette.
/// Designed for accessibility and clear visual hierarchy.
/// Primary color is Sage Green (#81B29A) as per design specification.
class AppColors {
  AppColors._();

  // Primary brand colors - Sage green theme
  static const Color primary = Color(0xFF81B29A);      // Sage green - calm/nature
  static const Color primaryLight = Color(0xFFB2D4C4);
  static const Color primaryDark = Color(0xFF5C8F74);

  // Secondary colors - Sandy orange for contrast
  static const Color secondary = Color(0xFFF4A261);    // Sandy orange
  static const Color secondaryLight = Color(0xFFF7C59F);
  static const Color secondaryDark = Color(0xFFE07B3C);

  // Status colors
  static const Color emergency = Color(0xFFE63946);    // Red - urgent
  static const Color emergencyLight = Color(0xFFFF6B6B);
  static const Color warning = Color(0xFFF4A261);      // Orange - attention
  static const Color success = Color(0xFF2A9D8F);      // Teal - success
  static const Color info = Color(0xFF457B9D);         // Blue - info

  // Request status colors
  static const Color statusPending = Color(0xFFFFA000);
  static const Color statusAccepted = Color(0xFF1976D2);
  static const Color statusInProgress = Color(0xFF7B1FA2);
  static const Color statusCompleted = Color(0xFF388E3C);
  static const Color statusCancelled = Color(0xFF757575);

  // Risk level colors
  static const Color riskLow = Color(0xFF4CAF50);
  static const Color riskMedium = Color(0xFFFFA000);
  static const Color riskHigh = Color(0xFFD32F2F);

  // Volunteer skill colors
  static const Color skillMidwife = Color(0xFF7B1FA2);
  static const Color skillNurse = Color(0xFF1976D2);
  static const Color skillTrained = Color(0xFF388E3C);
  static const Color skillCommunity = Color(0xFF757575);

  // Neutral colors
  static const Color background = Color(0xFFF5F5F5);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color textPrimary = Color(0xFF212121);
  static const Color textSecondary = Color(0xFF757575);
  static const Color divider = Color(0xFFBDBDBD);

  // Dark theme variants
  static const Color backgroundDark = Color(0xFF121212);
  static const Color surfaceDark = Color(0xFF1E1E1E);
  static const Color textPrimaryDark = Color(0xFFE0E0E0);
  static const Color textSecondaryDark = Color(0xFF9E9E9E);
}
