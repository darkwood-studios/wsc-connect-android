package wscconnect.android.fragments;


import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import wscconnect.android.R;
import wscconnect.android.Utils;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    String channelName = Utils.getNotificationChannel(getActivity());
                    if (channelName != null) {
                        mNotificationManager.deleteNotificationChannel(channelName);
                    }
                    Utils.setNotificationChannel(getActivity(), null);
                }
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
