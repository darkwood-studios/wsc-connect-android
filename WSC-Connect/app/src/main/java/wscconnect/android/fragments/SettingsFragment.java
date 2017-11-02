package wscconnect.android.fragments;


import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.Fragment;

import wscconnect.android.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ringtone = prefs.getString("pref_notifications_ringtone", null);

        final RingtonePreference ringtonePref = (RingtonePreference) findPreference("pref_notifications_ringtone");

        if (ringtone != null && !ringtone.isEmpty() && !getRingtoneName(ringtone).isEmpty()) {
            ringtonePref.setSummary(getRingtoneName(ringtone));
        }

        ringtonePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String value = (String) o;
                if (!value.isEmpty() && !getRingtoneName(value).isEmpty()) {
                    ringtonePref.setSummary(getRingtoneName(value));
                } else {
                    ringtonePref.setSummary(R.string.pref_notifications_ringtone_summary);
                }

                return true;
            }
        });
    }

    private String getRingtoneName(String value) {
        String name = "";
        Uri ringtoneUri = Uri.parse(value);
        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
        if (ringtone != null) {
            name = ringtone.getTitle(getActivity());
        }

        return name;
    }

}
