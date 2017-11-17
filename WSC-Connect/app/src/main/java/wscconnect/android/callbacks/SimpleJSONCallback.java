package wscconnect.android.callbacks;

import org.json.JSONObject;

/**
 * Created by chris on 18.07.17.
 */

public interface SimpleJSONCallback {
    void onReady(JSONObject json, boolean success);
}
