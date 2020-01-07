package wscconnect.android;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import wscconnect.android.listeners.OnNewPushMessageListener;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
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
