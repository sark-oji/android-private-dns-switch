package dev.sarkoji.privatednsswitch;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;

public final class PrivateDnsController {
    private static final String SETTINGS_PRIVATE_DNS_MODE = "private_dns_mode";
    private static final String SETTINGS_PRIVATE_DNS_SPECIFIER = "private_dns_specifier";
    private static final String MODE_OFF = "off";
    private static final String MODE_OPPORTUNISTIC = "opportunistic";
    private static final String MODE_HOSTNAME = "hostname";

    private static final String ACTION_PRIVATE_DNS_SETTINGS = "android.settings.PRIVATE_DNS_SETTINGS";
    private static final String PREFS_NAME = "private_dns_state";
    private static final String PREF_LAST_MODE = "last_mode";
    private static final String PREF_LAST_SPECIFIER = "last_specifier";

    private PrivateDnsController() {
    }

    public static Status readStatus(Context context) {
        String mode = readGlobal(context, SETTINGS_PRIVATE_DNS_MODE);
        String specifier = readGlobal(context, SETTINGS_PRIVATE_DNS_SPECIFIER);
        String normalizedMode = normalizeMode(mode);
        boolean enabled = !MODE_OFF.equals(normalizedMode);

        return new Status(
                enabled,
                normalizedMode,
                TextUtils.isEmpty(specifier) ? "" : specifier,
                modeLabel(context, normalizedMode),
                TextUtils.isEmpty(specifier) ? context.getString(R.string.hostname_not_set) : specifier);
    }

    public static ToggleResult toggle(Context context) {
        if (!hasSecureSettingsPermission(context)) {
            return ToggleResult.permissionMissing();
        }

        Status current = readStatus(context);
        try {
            if (current.enabled) {
                rememberEnabledConfiguration(context, current);
                if (writeGlobal(context, SETTINGS_PRIVATE_DNS_MODE, MODE_OFF)) {
                    return ToggleResult.disabled();
                }
                return ToggleResult.failed(context.getString(R.string.toggle_write_failed));
            }

            RestoreConfiguration restoreConfiguration = restoreConfiguration(context);
            if (!TextUtils.isEmpty(restoreConfiguration.specifier)) {
                if (!writeGlobal(
                        context,
                        SETTINGS_PRIVATE_DNS_SPECIFIER,
                        restoreConfiguration.specifier)) {
                    return ToggleResult.failed(context.getString(R.string.toggle_write_failed));
                }
            }
            if (writeGlobal(context, SETTINGS_PRIVATE_DNS_MODE, restoreConfiguration.mode)) {
                return ToggleResult.enabled();
            }
            return ToggleResult.failed(context.getString(R.string.toggle_write_failed));
        } catch (SecurityException exception) {
            return ToggleResult.failed(context.getString(R.string.toggle_security_exception));
        }
    }

    public static boolean hasSecureSettingsPermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void openPrivateDnsSettings(Context context) {
        if (tryOpen(context, new Intent(ACTION_PRIVATE_DNS_SETTINGS))) {
            return;
        }
        if (tryOpen(context, new Intent(Settings.ACTION_WIRELESS_SETTINGS))) {
            return;
        }
        tryOpen(context, new Intent(Settings.ACTION_SETTINGS));
    }

    private static boolean tryOpen(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException exception) {
            return false;
        }
    }

    private static RestoreConfiguration restoreConfiguration(Context context) {
        SharedPreferences prefs = prefs(context);
        String savedMode = normalizeMode(prefs.getString(PREF_LAST_MODE, null));
        String savedSpecifier = prefs.getString(PREF_LAST_SPECIFIER, null);
        String currentSpecifier = readGlobal(context, SETTINGS_PRIVATE_DNS_SPECIFIER);

        if (MODE_HOSTNAME.equals(savedMode) && !TextUtils.isEmpty(savedSpecifier)) {
            return new RestoreConfiguration(MODE_HOSTNAME, savedSpecifier);
        }
        if (!TextUtils.isEmpty(currentSpecifier)) {
            return new RestoreConfiguration(MODE_HOSTNAME, currentSpecifier);
        }
        if (MODE_OPPORTUNISTIC.equals(savedMode)) {
            return new RestoreConfiguration(MODE_OPPORTUNISTIC, "");
        }
        return new RestoreConfiguration(MODE_OPPORTUNISTIC, "");
    }

    private static void rememberEnabledConfiguration(Context context, Status status) {
        if (!status.enabled) {
            return;
        }

        prefs(context)
                .edit()
                .putString(PREF_LAST_MODE, status.mode)
                .putString(PREF_LAST_SPECIFIER, status.specifier)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String readGlobal(Context context, String key) {
        return Settings.Global.getString(context.getContentResolver(), key);
    }

    private static boolean writeGlobal(Context context, String key, String value) {
        return Settings.Global.putString(context.getContentResolver(), key, value);
    }

    private static String normalizeMode(String mode) {
        if (TextUtils.isEmpty(mode)) {
            return MODE_OFF;
        }
        if (MODE_HOSTNAME.equals(mode) || MODE_OPPORTUNISTIC.equals(mode) || MODE_OFF.equals(mode)) {
            return mode;
        }
        return MODE_OFF;
    }

    private static String modeLabel(Context context, String mode) {
        if (MODE_HOSTNAME.equals(mode)) {
            return context.getString(R.string.mode_hostname);
        }
        if (MODE_OPPORTUNISTIC.equals(mode)) {
            return context.getString(R.string.mode_automatic);
        }
        return context.getString(R.string.mode_off);
    }

    public static final class Status {
        public final boolean enabled;
        public final String mode;
        public final String specifier;
        public final String modeLabel;
        public final String hostnameLabel;

        private Status(
                boolean enabled,
                String mode,
                String specifier,
                String modeLabel,
                String hostnameLabel) {
            this.enabled = enabled;
            this.mode = mode;
            this.specifier = specifier;
            this.modeLabel = modeLabel;
            this.hostnameLabel = hostnameLabel;
        }
    }

    public static final class ToggleResult {
        public final boolean success;
        public final boolean permissionMissing;
        public final String message;

        private ToggleResult(boolean success, boolean permissionMissing, String message) {
            this.success = success;
            this.permissionMissing = permissionMissing;
            this.message = message;
        }

        private static ToggleResult enabled() {
            return new ToggleResult(true, false, "");
        }

        private static ToggleResult disabled() {
            return new ToggleResult(true, false, "");
        }

        private static ToggleResult permissionMissing() {
            return new ToggleResult(false, true, "");
        }

        private static ToggleResult failed(String message) {
            return new ToggleResult(false, false, message);
        }
    }

    private static final class RestoreConfiguration {
        private final String mode;
        private final String specifier;

        private RestoreConfiguration(String mode, String specifier) {
            this.mode = mode;
            this.specifier = specifier;
        }
    }
}
