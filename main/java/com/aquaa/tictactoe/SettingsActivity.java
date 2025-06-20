package com.aquaa.tictactoe; // Remember to replace with your package name

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // This layout just contains a FrameLayout for the fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Set up AI Difficulty preference
            ListPreference difficultyPreference = findPreference("ai_difficulty");
            if (difficultyPreference != null) {
                difficultyPreference.setEntries(new CharSequence[]{"Easy", "Medium", "Hard"});
                difficultyPreference.setEntryValues(new CharSequence[]{"Easy", "Medium", "Hard"});
                // Set default value if not already set
                if (difficultyPreference.getValue() == null) {
                    difficultyPreference.setValue("Medium");
                }
            }

            // REMOVED: Theme switch and its listener.
            // Theme is now controlled by the system's DayNight mode and app themes.

            // Sound preference
            SwitchPreferenceCompat soundSwitch = findPreference("sound_enabled");
            if (soundSwitch != null) {
                // The GameActivity will pick up changes to this preference on resume
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // No specific action needed here for theme, as it's automatic.
            // GameActivity will reload other preferences like sound onResume.
        }
    }
}
