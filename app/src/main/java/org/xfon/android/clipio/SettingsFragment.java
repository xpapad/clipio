package org.xfon.android.clipio;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by xenofon on 6/15/15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}