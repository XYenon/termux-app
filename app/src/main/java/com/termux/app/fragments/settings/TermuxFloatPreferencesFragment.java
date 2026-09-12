package com.termux.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.termux.R;
import com.termux.shared.termux.settings.preferences.TermuxFloatAppSharedPreferences;

@Keep
public class TermuxFloatPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(new TermuxFloatPreferencesDataStore(context));

        setPreferencesFromResource(R.xml.termux_float_preferences, rootKey);
    }

}

class TermuxFloatPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final TermuxFloatAppSharedPreferences mPreferences;

    TermuxFloatPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = TermuxFloatAppSharedPreferences.build(context, true);
    }

}
