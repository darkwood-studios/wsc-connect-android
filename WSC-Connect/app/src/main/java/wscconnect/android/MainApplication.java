package wscconnect.android;

import android.app.Application;

import wscconnect.android.listeners.OnNewPushMessageListener;

/**
 * Created by Chris on 01.06.2016.
 */
public class MainApplication extends Application {
    private OnNewPushMessageListener onNewPushMessageListener;

    public void setOnNewPushMessageListener(OnNewPushMessageListener onNewPushMessageListener) {
        this.onNewPushMessageListener = onNewPushMessageListener;
    }

    public void executeOnNewPushMessageListener(String appID) {
        if (onNewPushMessageListener != null) {
            onNewPushMessageListener.onNewPushMessage(appID);
        }
    }
}
