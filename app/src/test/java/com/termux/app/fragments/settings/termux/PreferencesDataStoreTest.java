package com.termux.app.fragments.settings.termux;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class PreferencesDataStoreTest {
    @Test
    public void separatePageStoresSharePersistedPreferences() {
        TerminalIOPreferencesDataStore first =
            new TerminalIOPreferencesDataStore(RuntimeEnvironment.getApplication());
        TerminalIOPreferencesDataStore second =
            new TerminalIOPreferencesDataStore(RuntimeEnvironment.getApplication());
        assertNotSame(first, second);
        first.putBoolean("soft_keyboard_enabled", false);
        first.putBoolean("soft_keyboard_enabled_only_if_no_hardware", true);
        assertFalse(second.getBoolean("soft_keyboard_enabled", true));
        assertTrue(second.getBoolean("soft_keyboard_enabled_only_if_no_hardware", false));
        second.putBoolean("soft_keyboard_enabled", true);
        assertTrue(first.getBoolean("soft_keyboard_enabled", false));
    }
}
