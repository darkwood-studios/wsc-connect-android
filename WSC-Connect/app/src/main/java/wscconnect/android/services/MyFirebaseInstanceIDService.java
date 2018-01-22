package wscconnect.android.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import wscconnect.android.Utils;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        if (refreshedToken != null) {
            sendTokenToServer(refreshedToken);
        }
    }

    private void sendTokenToServer(final String firebaseToken) {
        // force user to login again
        Utils.removeAllAccessTokens(this);
    }
}
