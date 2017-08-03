package wscconnect.android.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import wscconnect.android.Utils;

/**
 * Created by chris on 25.04.17.
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
