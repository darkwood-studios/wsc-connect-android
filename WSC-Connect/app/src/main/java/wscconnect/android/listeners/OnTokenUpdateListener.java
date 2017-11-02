package wscconnect.android.listeners;

import wscconnect.android.models.AccessTokenModel;

/**
 * Created by chris on 28.07.17.
 */

public interface OnTokenUpdateListener {
    void onUpdate(AccessTokenModel token);
}
