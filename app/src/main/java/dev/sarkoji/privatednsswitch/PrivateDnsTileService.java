package dev.sarkoji.privatednsswitch;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public final class PrivateDnsTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (isLocked()) {
            unlockAndRun(this::togglePrivateDns);
        } else {
            togglePrivateDns();
        }
    }

    private void togglePrivateDns() {
        PrivateDnsController.ToggleResult result = PrivateDnsController.toggle(this);
        updateTile();

        if (result.success) {
            return;
        }

        if (result.permissionMissing) {
            openMainActivity();
            return;
        }

        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (!PrivateDnsController.hasSecureSettingsPermission(this)) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(getString(R.string.tile_label));
            setSubtitle(tile, getString(R.string.tile_subtitle_permission_missing));
            tile.updateTile();
            return;
        }

        PrivateDnsController.Status status = PrivateDnsController.readStatus(this);
        tile.setState(status.enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getString(R.string.tile_label));
        setSubtitle(tile, status.enabled ? status.modeLabel : getString(R.string.mode_off));
        tile.updateTile();
    }

    private void setSubtitle(Tile tile, CharSequence subtitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.setSubtitle(subtitle);
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            startActivityAndCollapse(pendingIntent);
        } else {
            startActivityAndCollapse(intent);
        }
    }
}
