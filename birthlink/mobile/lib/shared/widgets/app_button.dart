import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';

/// Primary button with loading state support.
/// 
/// Features:
/// - Loading indicator when [isLoading] is true
/// - Optional leading icon
/// - Full width option
/// - Disabled state handling
class AppButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final bool isLoading;
  final bool isFullWidth;
  final IconData? icon;
  final AppButtonStyle style;

  const AppButton({
    super.key,
    required this.text,
    this.onPressed,
    this.isLoading = false,
    this.isFullWidth = false,
    this.icon,
    this.style = AppButtonStyle.primary,
  });

  /// Creates a primary (filled) button.
  const AppButton.primary({
    super.key,
    required this.text,
    this.onPressed,
    this.isLoading = false,
    this.isFullWidth = false,
    this.icon,
  }) : style = AppButtonStyle.primary;

  /// Creates a secondary (outlined) button.
  const AppButton.secondary({
    super.key,
    required this.text,
    this.onPressed,
    this.isLoading = false,
    this.isFullWidth = false,
    this.icon,
  }) : style = AppButtonStyle.secondary;

  /// Creates a text button.
  const AppButton.text({
    super.key,
    required this.text,
    this.onPressed,
    this.isLoading = false,
    this.isFullWidth = false,
    this.icon,
  }) : style = AppButtonStyle.text;

  /// Creates an emergency (danger) button.
  const AppButton.emergency({
    super.key,
    required this.text,
    this.onPressed,
    this.isLoading = false,
    this.isFullWidth = false,
    this.icon,
  }) : style = AppButtonStyle.emergency;

  @override
  Widget build(BuildContext context) {
    final Widget buttonChild = _buildButtonContent(context);

    Widget button;
    switch (style) {
      case AppButtonStyle.primary:
        button = ElevatedButton(
          onPressed: isLoading ? null : onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            minimumSize: isFullWidth ? const Size(double.infinity, 48) : null,
          ),
          child: buttonChild,
        );
        break;

      case AppButtonStyle.secondary:
        button = OutlinedButton(
          onPressed: isLoading ? null : onPressed,
          style: OutlinedButton.styleFrom(
            side: const BorderSide(color: AppColors.primary),
            minimumSize: isFullWidth ? const Size(double.infinity, 48) : null,
          ),
          child: buttonChild,
        );
        break;

      case AppButtonStyle.text:
        button = TextButton(
          onPressed: isLoading ? null : onPressed,
          style: TextButton.styleFrom(
            minimumSize: isFullWidth ? const Size(double.infinity, 48) : null,
          ),
          child: buttonChild,
        );
        break;

      case AppButtonStyle.emergency:
        button = ElevatedButton(
          onPressed: isLoading ? null : onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.emergency,
            foregroundColor: Colors.white,
            minimumSize: isFullWidth ? const Size(double.infinity, 48) : null,
          ),
          child: buttonChild,
        );
        break;
    }

    return button;
  }

  Widget _buildButtonContent(BuildContext context) {
    if (isLoading) {
      return const SizedBox(
        height: 20,
        width: 20,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
        ),
      );
    }

    if (icon != null) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 18),
          const SizedBox(width: 8),
          Text(text),
        ],
      );
    }

    return Text(text);
  }
}

/// Button style variants.
enum AppButtonStyle {
  /// Primary filled button with primary color background.
  primary,
  
  /// Secondary outlined button with primary color border.
  secondary,
  
  /// Text button without background or border.
  text,
  
  /// Emergency/danger button with red background.
  emergency,
}

/// Icon button with loading state support.
class AppIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback? onPressed;
  final bool isLoading;
  final Color? color;
  final String? tooltip;

  const AppIconButton({
    super.key,
    required this.icon,
    this.onPressed,
    this.isLoading = false,
    this.color,
    this.tooltip,
  });

  @override
  Widget build(BuildContext context) {
    final Widget iconWidget = isLoading
        ? SizedBox(
            height: 20,
            width: 20,
            child: CircularProgressIndicator(
              strokeWidth: 2,
              valueColor: AlwaysStoppedAnimation<Color>(
                color ?? Theme.of(context).colorScheme.primary,
              ),
            ),
          )
        : Icon(icon, color: color);

    return IconButton(
      icon: iconWidget,
      onPressed: isLoading ? null : onPressed,
      tooltip: tooltip,
    );
  }
}
