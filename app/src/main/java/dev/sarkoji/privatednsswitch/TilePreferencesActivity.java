package dev.sarkoji.privatednsswitch;

import android.app.Activity;
import android.os.Bundle;

public final class TilePreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrivateDnsController.openPrivateDnsSettings(this);
        finish();
    }
}
