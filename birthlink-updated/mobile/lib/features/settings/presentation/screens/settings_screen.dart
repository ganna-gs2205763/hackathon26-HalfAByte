import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_colors.dart';
import '../../../../core/localization/app_localizations.dart';
import '../../../../core/providers/role_provider.dart';
import '../../../../shared/providers/locale_provider.dart';
import '../../../../shared/providers/phone_provider.dart';
import '../providers/volunteer_provider.dart';

/// Settings screen for language, notifications, and profile.
class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locale = ref.watch(localeProvider);
    final l10n = context.l10n;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.translate('settings_title')),
      ),
      body: ListView(
        children: [
          // Volunteer Profile Section
          _buildVolunteerSection(context, ref, l10n),
          const Divider(),

          // Role Section
          _buildRoleSection(context, ref, l10n),
          const Divider(),

          // Language Section
          _buildSectionHeader(context, l10n.translate('settings_language')),
          _buildLanguageTile(context, ref, locale),
          const Divider(),

          // Notifications Section
          _buildSectionHeader(context, l10n.translate('settings_notifications')),
          SwitchListTile(
            secondary: const Icon(Icons.notifications_outlined),
            title: Text(l10n.translate('settings_notifications')),
            subtitle: const Text('Receive alerts for new cases'),
            value: true,
            onChanged: (value) {
              // TODO: Implement notifications toggle
            },
          ),
          const Divider(),

          // About Section
          _buildSectionHeader(context, l10n.translate('settings_about')),
          ListTile(
            leading: const Icon(Icons.info_outline),
            title: const Text('BirthLink'),
            subtitle: const Text('Version 1.0.0'),
          ),
          ListTile(
            leading: const Icon(Icons.code),
            title: const Text('Open Source Licenses'),
            onTap: () {
              showLicensePage(
                context: context,
                applicationName: 'BirthLink',
                applicationVersion: '1.0.0',
              );
            },
          ),
          const Divider(),

          // Logout
          ListTile(
            leading: const Icon(Icons.logout, color: AppColors.emergency),
            title: Text(
              l10n.translate('settings_logout'),
              style: const TextStyle(color: AppColors.emergency),
            ),
            onTap: () => _showLogoutDialog(context, ref, l10n),
          ),

          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleSmall?.copyWith(
              color: AppColors.primary,
              fontWeight: FontWeight.bold,
            ),
      ),
    );
  }

  Widget _buildVolunteerSection(
      BuildContext context, WidgetRef ref, AppLocalizations l10n) {
    final volunteerAsync = ref.watch(volunteerProfileNotifierProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionHeader(context, l10n.translate('volunteer_profile')),
        volunteerAsync.when(
          loading: () => const ListTile(
            leading: CircleAvatar(
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
            title: Text('Loading...'),
          ),
          error: (error, stack) => ListTile(
            leading: const CircleAvatar(
              backgroundColor: AppColors.emergency,
              child: Icon(Icons.error_outline, color: Colors.white),
            ),
            title: Text(l10n.translate('error')),
            subtitle: Text(_getErrorMessage(l10n, error)),
            trailing: IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: () =>
                  ref.read(volunteerProfileNotifierProvider.notifier).refresh(),
            ),
          ),
          data: (volunteer) {
            if (volunteer == null) {
              return _buildLoginPrompt(context, ref, l10n);
            }

            return Column(
              children: [
                // Volunteer Info
                ListTile(
                  leading: CircleAvatar(
                    backgroundColor: AppColors.primary.withValues(alpha: 0.2),
                    child: Text(
                      volunteer.displayName.isNotEmpty
                          ? volunteer.displayName[0].toUpperCase()
                          : '?',
                      style: const TextStyle(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  title: Text(volunteer.displayName),
                  subtitle: Text(
                    '${volunteer.formattedId} • ${volunteer.skillTypeDisplay}',
                  ),
                ),

                // Availability Toggle
                _buildAvailabilityTile(context, ref, l10n, volunteer.availability),

                // Zones
                ListTile(
                  leading: const Icon(Icons.location_on_outlined),
                  title: Text(l10n.translate('zone')),
                  subtitle: Text(volunteer.zonesDisplay),
                ),

                // Completed Cases
                ListTile(
                  leading: const Icon(Icons.check_circle_outline),
                  title: Text(l10n.translate('completed')),
                  subtitle: Text('${volunteer.completedCases} cases'),
                ),
              ],
            );
          },
        ),
      ],
    );
  }

  Widget _buildAvailabilityTile(
    BuildContext context,
    WidgetRef ref,
    AppLocalizations l10n,
    String availability,
  ) {
    final isAvailable = availability.toUpperCase() == 'AVAILABLE';

    return SwitchListTile(
      secondary: Icon(
        isAvailable ? Icons.check_circle : Icons.pause_circle_outline,
        color: isAvailable ? AppColors.success : AppColors.warning,
      ),
      title: Text(l10n.translate('update_availability')),
      subtitle: Text(
        isAvailable
            ? l10n.translate('available')
            : l10n.translate('busy'),
        style: TextStyle(
          color: isAvailable ? AppColors.success : AppColors.warning,
          fontWeight: FontWeight.bold,
        ),
      ),
      value: isAvailable,
      activeColor: AppColors.success,
      onChanged: (value) async {
        try {
          await ref
              .read(volunteerProfileNotifierProvider.notifier)
              .updateAvailability(value ? 'AVAILABLE' : 'BUSY');
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(
                  value
                      ? l10n.translate('available')
                      : l10n.translate('busy'),
                ),
                backgroundColor: AppColors.success,
              ),
            );
          }
        } catch (e) {
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(_getErrorMessage(l10n, e)),
                backgroundColor: AppColors.emergency,
              ),
            );
          }
        }
      },
    );
  }

  Widget _buildLoginPrompt(
      BuildContext context, WidgetRef ref, AppLocalizations l10n) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              const Icon(
                Icons.person_outline,
                size: 48,
                color: AppColors.textSecondary,
              ),
              const SizedBox(height: 12),
              Text(
                'Enter your phone number to sync your profile',
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppColors.textSecondary,
                    ),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => _showPhoneNumberDialog(context, ref, l10n),
                child: const Text('Set Phone Number'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildRoleSection(
      BuildContext context, WidgetRef ref, AppLocalizations l10n) {
    final role = ref.watch(roleProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionHeader(context, l10n.translate('settings_role')),
        ListTile(
          leading: Icon(
            role == UserRole.ngo
                ? Icons.admin_panel_settings
                : Icons.volunteer_activism,
            color: role == UserRole.ngo ? AppColors.info : AppColors.primary,
          ),
          title: Text(l10n.translate('settings_role')),
          subtitle: Text(
            role == UserRole.ngo
                ? l10n.translate('role_ngo')
                : l10n.translate('role_volunteer'),
            style: TextStyle(
              color: role == UserRole.ngo ? AppColors.info : AppColors.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
          trailing: Switch(
            value: role == UserRole.ngo,
            activeColor: AppColors.info,
            onChanged: (isNgo) {
              ref.read(roleProvider.notifier).setRole(
                    isNgo ? UserRole.ngo : UserRole.volunteer,
                  );
            },
          ),
          onTap: () => _showRoleDialog(context, ref, role, l10n),
        ),
      ],
    );
  }

  Widget _buildLanguageTile(
      BuildContext context, WidgetRef ref, Locale locale) {
    final l10n = context.l10n;

    return ListTile(
      leading: const Icon(Icons.language),
      title: Text(l10n.translate('settings_language')),
      subtitle: Text(locale.languageCode == 'en' ? 'English' : 'العربية'),
      trailing: Switch(
        value: locale.languageCode == 'ar',
        activeColor: AppColors.primary,
        onChanged: (isArabic) {
          ref.read(localeProvider.notifier).setLocale(
                Locale(isArabic ? 'ar' : 'en'),
              );
        },
      ),
      onTap: () => _showLanguageDialog(context, ref, locale),
    );
  }

  void _showLanguageDialog(
      BuildContext context, WidgetRef ref, Locale currentLocale) {
    final l10n = context.l10n;

    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text(l10n.translate('settings_language')),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: Radio<String>(
                value: 'en',
                groupValue: currentLocale.languageCode,
                onChanged: (value) {
                  ref.read(localeProvider.notifier).setLocale(const Locale('en'));
                  Navigator.pop(dialogContext);
                },
              ),
              title: const Text('English'),
              onTap: () {
                ref.read(localeProvider.notifier).setLocale(const Locale('en'));
                Navigator.pop(dialogContext);
              },
            ),
            ListTile(
              leading: Radio<String>(
                value: 'ar',
                groupValue: currentLocale.languageCode,
                onChanged: (value) {
                  ref.read(localeProvider.notifier).setLocale(const Locale('ar'));
                  Navigator.pop(dialogContext);
                },
              ),
              title: const Text('العربية'),
              onTap: () {
                ref.read(localeProvider.notifier).setLocale(const Locale('ar'));
                Navigator.pop(dialogContext);
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: Text(l10n.translate('cancel')),
          ),
        ],
      ),
    );
  }

  void _showRoleDialog(
      BuildContext context, WidgetRef ref, UserRole currentRole, AppLocalizations l10n) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text(l10n.translate('settings_role')),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: Radio<UserRole>(
                value: UserRole.ngo,
                groupValue: currentRole,
                onChanged: (value) {
                  ref.read(roleProvider.notifier).setRole(UserRole.ngo);
                  Navigator.pop(dialogContext);
                },
              ),
              title: Text(l10n.translate('role_ngo')),
              subtitle: Text(l10n.translate('role_ngo_description')),
              onTap: () {
                ref.read(roleProvider.notifier).setRole(UserRole.ngo);
                Navigator.pop(dialogContext);
              },
            ),
            ListTile(
              leading: Radio<UserRole>(
                value: UserRole.volunteer,
                groupValue: currentRole,
                onChanged: (value) {
                  ref.read(roleProvider.notifier).setRole(UserRole.volunteer);
                  Navigator.pop(dialogContext);
                },
              ),
              title: Text(l10n.translate('role_volunteer')),
              subtitle: Text(l10n.translate('role_volunteer_description')),
              onTap: () {
                ref.read(roleProvider.notifier).setRole(UserRole.volunteer);
                Navigator.pop(dialogContext);
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: Text(l10n.translate('cancel')),
          ),
        ],
      ),
    );
  }

  void _showPhoneNumberDialog(
      BuildContext context, WidgetRef ref, AppLocalizations l10n) {
    final controller = TextEditingController();

    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Enter Phone Number'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(
            labelText: 'Phone Number',
            hintText: '+1234567890',
            prefixIcon: Icon(Icons.phone),
          ),
          keyboardType: TextInputType.phone,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: Text(l10n.translate('cancel')),
          ),
          ElevatedButton(
            onPressed: () {
              final phone = controller.text.trim();
              if (phone.isNotEmpty) {
                ref.read(phoneNumberProvider.notifier).setPhoneNumber(phone);
                Navigator.pop(dialogContext);
                ref.invalidate(volunteerProfileNotifierProvider);
              }
            },
            child: Text(l10n.translate('save')),
          ),
        ],
      ),
    );
  }

  void _showLogoutDialog(
      BuildContext context, WidgetRef ref, AppLocalizations l10n) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text(l10n.translate('settings_logout')),
        content: Text('${l10n.translate('settings_logout')}?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: Text(l10n.translate('cancel')),
          ),
          TextButton(
            onPressed: () {
              ref.read(phoneNumberProvider.notifier).clearPhoneNumber();
              ref.invalidate(volunteerProfileNotifierProvider);
              Navigator.pop(dialogContext);
            },
            child: Text(
              l10n.translate('settings_logout'),
              style: const TextStyle(color: AppColors.emergency),
            ),
          ),
        ],
      ),
    );
  }

  String _getErrorMessage(AppLocalizations l10n, Object error) {
    final errorStr = error.toString().toLowerCase();
    if (errorStr.contains('unauthorized')) {
      return 'Please set your phone number';
    }
    if (errorStr.contains('network') || errorStr.contains('socket')) {
      return l10n.translate('error_network');
    }
    if (errorStr.contains('timeout')) {
      return l10n.translate('error_timeout');
    }
    return l10n.translate('error_unknown');
  }
}
