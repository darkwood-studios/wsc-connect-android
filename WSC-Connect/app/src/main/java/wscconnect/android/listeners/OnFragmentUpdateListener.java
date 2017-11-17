package wscconnect.android.listeners;

import android.os.Bundle;

/**
 * Created by chris on 28.07.17.
 */

public interface OnFragmentUpdateListener {
    /**
     * Is called, when a fragment should be updated from the outside
     * @param bundle
     */
    void onUpdate(Bundle bundle);
}
