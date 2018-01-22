package wscconnect.android.services;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import wscconnect.android.Utils;
import wscconnect.android.models.AccessTokenModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String GCM_SENDER_ID = "771743622546";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map<String, String> data = message.getData();

        // only accept messages from the correct sender
        if (from != null && from.equals(GCM_SENDER_ID)) {
            String action = data.get("action");

            if (action == null) {
                return;
            }

            String appID = data.get("appID");
            AccessTokenModel token = Utils.getAccessToken(this, appID);

            // we are logged out in the app, but still available on the sever. Just ignore this one.
            if (token == null) {
                return;
            }

            switch (action) {
                case "notification":
                    handleNotification(data);
                    break;
                case "message":
                    handleMessage(data);
                    break;
            }
        }

    }

    private void handleMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String appLogo = data.get("appLogo");
        String appID = data.get("appID");
        int eventID = Integer.parseInt((data.get("eventID") == null) ? "0" : data.get("eventID"));
        String eventName = (data.get("eventName") == null) ? "" : data.get("eventName");

        String tag = appID + "message";

        createNotification(tag, (int) System.currentTimeMillis(), appID, "message", title, message, eventName, eventID, appLogo);
    }

    private void handleNotification(Map<String, String> data) {
        String message = data.get("message");
        String logo = data.get("logo");
        String appName = data.get("appName");
        String appID = data.get("appID");
        String eventHash = (data.get("eventHash") == null) ? "" : data.get("eventHash");
        int eventID = Integer.parseInt((data.get("eventID") == null) ? "0" : data.get("eventID"));
        String eventName = (data.get("eventName") == null) ? "" : data.get("eventName");
        int authorID = Integer.parseInt((data.get("authorID") == null) ? "0" : data.get("authorID"));

        // create app/event unique tag
        String tag = appID + eventHash;

        createNotification(tag, authorID, appID, "notification", appName, message, eventName, eventID, logo);
    }

    private void createNotification(final String notificationTag, final int notificationID, final String appID, final String optionType, final String title, final String message, final String eventName, final int eventID, final String largeIcon) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(largeIcon)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                Utils.showDataNotification(MyFirebaseMessagingService.this, notificationTag, notificationID, appID, optionType, title, message, eventName, eventID, resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                Utils.showDataNotification(MyFirebaseMessagingService.this, notificationTag, notificationID, appID, optionType, title, message, eventName, eventID, null);
                            }
                        });
            }
        });
    }
}
