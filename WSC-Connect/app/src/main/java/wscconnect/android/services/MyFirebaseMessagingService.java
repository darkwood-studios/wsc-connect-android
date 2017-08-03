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

import wscconnect.android.MainApplication;
import wscconnect.android.Utils;
import wscconnect.android.models.NotificationModel;

/**
 * Created by chris on 25.04.17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String GCM_SENDER_ID = "771743622546";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map<String, String> data = message.getData();

        // only accept messages from the correct sender
        if (from.equals(GCM_SENDER_ID)) {
            String action = data.get("action");

            if (message.getNotification() != null) {
                handleSimpleNotification(message.getNotification());
                return;
            }

            // push message from wsc-connect.com dashboard including the appID
            if (data.get("extraNotification") != null) {
                String appID = data.get("extraNotification");
                ((MainApplication) getApplication()).executeOnNewPushMessageListener(appID);
                return;
            }

            if (action == null) {
                return;
            }

            switch (action) {
                case "notification":
                    handleNotification(data);
                    break;
            }
        }

    }

    private void handleSimpleNotification(RemoteMessage.Notification notification) {
        Utils.showSimpleNotification(this, notification.getTitle(), notification.getBody());
    }

    private void handleNotification(Map data) {
        String message = (String) data.get("message");
        String logo = (String) data.get("logo");
        String link = (String) data.get("link");
        String appName = (String) data.get("appName");
        String appID = (String) data.get("appID");
        int time = Integer.valueOf((String) data.get("time"));
        boolean confirmed = Boolean.valueOf((String) data.get("confirmed"));

        NotificationModel notification = new NotificationModel();
        notification.setMessage(message);
        notification.setLogo(logo);
        notification.setLink(link);
        notification.setTime(time);
        notification.setConfirmed(confirmed);

        createNotification(appID, appName, notification);
    }

    private void createNotification(final String appID, final String appName, final NotificationModel notification) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(notification.getLogo())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                Utils.showDataNotification(MyFirebaseMessagingService.this, appID, appName, resource, notification);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                Utils.showDataNotification(MyFirebaseMessagingService.this, appID, appName, null, notification);
                            }
                        });
            }
        });
    }
}
