package org.xfon.android.clipio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by xenofon on 6/17/15.
 */
public class StartClipioServiceAtBootReceiver extends BroadcastReceiver {
    private static final String TAG = StartClipioServiceAtBootReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received broadcast event");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean startAtBoot = pref.getBoolean(SettingsActivity.KEY_PREF_START_AT_BOOT, false);
            Log.d(TAG, "Start at boot: " + startAtBoot);
            if (startAtBoot) {
                Log.i(TAG, "Starting service");
                Intent serviceIntent = new Intent(context, ClipioClipboardService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
