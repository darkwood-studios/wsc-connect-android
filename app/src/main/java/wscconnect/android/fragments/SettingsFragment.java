package wscconnect.android.fragments;


import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import wscconnect.android.R;
import wscconnect.android.Utils;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class SettingsFragment extends PreferenceFragment {
    public static final int PERMISSION_READ_EXTERNAL = 1;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case SettingsFragment.PERMISSION_READ_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0 ||  grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDialog();
                }
            }
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.error_permission_external);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(R.string.error_permission_external_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.show();
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
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showPermissionDialog();
                    } else {
                        // No explanation needed; request the permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    PERMISSION_READ_EXTERNAL);
                        }

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }

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
