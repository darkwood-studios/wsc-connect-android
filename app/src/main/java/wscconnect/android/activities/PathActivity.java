package wscconnect.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static wscconnect.android.activities.MainActivity.EXTRA_NOTIFICATION;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class PathActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();

        // missing parameters
        if (in == null || in.getData() == null) {
            redirectMainActivity();
            return;
        }

        Uri data = in.getData();

        // the URL only looks like wsc-connect.com/apps/
        if (data.getPathSegments().size() != 2) {
            redirectMainActivity();
            return;
        }

        String appID = data.getPathSegments().get(1);

        if (!appID.matches("[0-9a-fA-F]{24}$")) {
            redirectMainActivity();
            return;
        }

        // redirect to app or open login dialog
        redirectMainActivity(appID);
    }

    private void redirectMainActivity() {
        redirectMainActivity(null);
    }

    private void redirectMainActivity(String appID) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        if (appID != null) {
            intent.putExtra(EXTRA_NOTIFICATION, appID);
        }
        startActivity(intent);
        finish();
    }
}
