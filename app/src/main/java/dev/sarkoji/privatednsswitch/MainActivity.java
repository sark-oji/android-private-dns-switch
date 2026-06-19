package dev.sarkoji.privatednsswitch;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private TextView statusView;
    private TextView permissionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name));
        setContentView(createContentView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private View createContentView() {
        int padding = dp(24);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWidth());

        TextView description = new TextView(this);
        description.setText(R.string.app_description);
        description.setTextSize(16);
        description.setPadding(0, dp(12), 0, dp(20));
        root.addView(description, matchWidth());

        statusView = new TextView(this);
        statusView.setTextSize(18);
        statusView.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(statusView, matchWidth());

        permissionView = new TextView(this);
        permissionView.setTextSize(14);
        permissionView.setPadding(0, dp(8), 0, dp(20));
        root.addView(permissionView, matchWidth());

        Button openSettingsButton = new Button(this);
        openSettingsButton.setText(R.string.open_private_dns_settings);
        openSettingsButton.setAllCaps(false);
        openSettingsButton.setOnClickListener(view -> PrivateDnsController.openPrivateDnsSettings(this));
        root.addView(openSettingsButton, matchWidthWithTopMargin(0));

        Button refreshButton = new Button(this);
        refreshButton.setText(R.string.refresh_status);
        refreshButton.setAllCaps(false);
        refreshButton.setOnClickListener(view -> refreshStatus());
        root.addView(refreshButton, matchWidthWithTopMargin(8));

        TextView note = new TextView(this);
        note.setText(R.string.app_note);
        note.setTextSize(14);
        note.setPadding(0, dp(20), 0, 0);
        root.addView(note, matchWidth());

        return scrollView;
    }

    private void refreshStatus() {
        PrivateDnsController.Status status = PrivateDnsController.readStatus(this);
        statusView.setText(getString(
                status.enabled ? R.string.status_enabled : R.string.status_disabled,
                status.modeLabel,
                status.hostnameLabel));

        if (hasSecureSettingsPermission()) {
            permissionView.setText(R.string.permission_granted);
        } else {
            permissionView.setText(getString(R.string.permission_missing, grantCommand()));
        }
    }

    private boolean hasSecureSettingsPermission() {
        return checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private String grantCommand() {
        return "adb shell pm grant " + getPackageName()
                + " android.permission.WRITE_SECURE_SETTINGS";
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWidthWithTopMargin(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWidth();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
