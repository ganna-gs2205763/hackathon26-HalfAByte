/// API endpoint constants.
/// Base URL is configured per environment.
class ApiEndpoints {
  ApiEndpoints._();

  // Base URLs
  // Android emulator uses 10.0.2.2 to access host machine's localhost
  static const String androidEmulatorBaseUrl = 'http://10.0.2.2:8080/api';
  static const String iosSimulatorBaseUrl = 'http://localhost:8080/api';
  static const String devBaseUrl = 'http://10.0.2.2:8080/api';  // Default to Android emulator
  static const String prodBaseUrl = 'https://api.safebirth.org';  // Update for production

  // Dashboard endpoints (paths relative to baseUrl which includes /api)
  static const String dashboardStats = '/dashboard/stats';
  static const String dashboardCases = '/dashboard/cases';
  static const String dashboardVolunteers = '/dashboard/volunteers';
  static const String dashboardZones = '/dashboard/zones';

  // Case endpoints
  static String caseDetails(String caseId) => '/dashboard/cases/$caseId';

  // Volunteer endpoints
  static const String volunteerMe = '/volunteer/me';
  static const String volunteerMyCases = '/volunteer/me/cases';
  static const String volunteerAvailability = '/volunteer/me/availability';

  // SMS endpoints (for simulation/testing)
  static const String smsSimulate = '/sms/simulate';
  static const String smsHealth = '/sms/health';
}
