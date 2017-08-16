package wscconnect.android;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import wscconnect.android.listeners.OnNewPushMessageListener;

/**
 * Created by Chris on 01.06.2016.
 */
public class MainApplication extends Application {
    private OnNewPushMessageListener onNewPushMessageListener;

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public void setOnNewPushMessageListener(OnNewPushMessageListener onNewPushMessageListener) {
        this.onNewPushMessageListener = onNewPushMessageListener;
    }

    public void executeOnNewPushMessageListener(String appID) {
        if (onNewPushMessageListener != null) {
            onNewPushMessageListener.onNewPushMessage(appID);
        }
    }
}
