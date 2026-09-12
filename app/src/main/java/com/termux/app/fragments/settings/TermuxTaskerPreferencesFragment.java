package com.termux.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.termux.R;
import com.termux.shared.termux.settings.preferences.TermuxTaskerAppSharedPreferences;

@Keep
public class TermuxTaskerPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(new TermuxTaskerPreferencesDataStore(context));

        setPreferencesFromResource(R.xml.termux_tasker_preferences, rootKey);
    }

}

class TermuxTaskerPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final TermuxTaskerAppSharedPreferences mPreferences;

    TermuxTaskerPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = TermuxTaskerAppSharedPreferences.build(context, true);
    }

}
