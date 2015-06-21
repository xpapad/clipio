package org.xfon.android.clipio;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by xenofon on 6/15/15.
 */
public class SettingsActivity extends Activity {

    public static final String KEY_PREF_CLEANUP_DELETES_TODAY = "pref_cleanup_today";
    public static final String KEY_PREF_START_AT_BOOT = "pref_start_boot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
