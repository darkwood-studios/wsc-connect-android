package wscconnect.android.models;

import android.content.Context;
import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import wscconnect.android.R;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class MessageModel {
    private String message;
    private String title;
    private String logo;
    @SerializedName("createdAt")
    private long time;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public CharSequence getRelativeTime(Context context) {
        if (time == 0) {
            return "";
        }

        // less than a minute ago
        if (System.currentTimeMillis() - time <= 60000) {
            return context.getString(R.string.just_now);
        }

        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
