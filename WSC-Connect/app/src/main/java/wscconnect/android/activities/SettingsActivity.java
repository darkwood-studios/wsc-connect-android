package wscconnect.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import wscconnect.android.R;
import wscconnect.android.fragments.SettingsFragment;

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
