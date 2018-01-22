package wscconnect.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import wscconnect.android.R;
import wscconnect.android.fragments.SettingsFragment;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_settings_content, new SettingsFragment())
                .commit();
    }
}
