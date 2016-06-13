package pcdiy.moovedit;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String KEY_PREF_THRESHOLD = "threshold";
	public static final String KEY_PREF_ALGORITHM = "algorithm";
	public static final String KEY_PREF_LOCATE_OBJECT = "locate_object";
	public static final String KEY_PREF_RESOLUTION = "resolution";
	public static final String KEY_PREF_PREVIEW_EFFECT = "preview_effect";

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        //sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onPause() {
        super.onPause();
        //sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PREF_RESOLUTION)) {
            @SuppressWarnings("deprecation")
			Preference resPref = findPreference(key);
            sharedPreferences.contains(KEY_PREF_RESOLUTION);
            if (resPref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) resPref;
                resPref.setSummary(listPref.getEntry());
            }
        }
		
		if (key.equals(KEY_PREF_PREVIEW_EFFECT)) {
            @SuppressWarnings("deprecation")
			Preference prePref = findPreference(key);
            sharedPreferences.contains(KEY_PREF_RESOLUTION);
            if (prePref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) prePref;
                prePref.setSummary(listPref.getEntry());
            }
        }
		
		if (key.equals(KEY_PREF_ALGORITHM)) {
            @SuppressWarnings("deprecation")
			Preference algPref = findPreference(key);
            sharedPreferences.contains(KEY_PREF_RESOLUTION);
            if (algPref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) algPref;
                algPref.setSummary(listPref.getEntry());
            }
        }
	}
}







