package wscconnect.android.models;

import android.content.Context;
import android.text.format.DateUtils;

import wscconnect.android.R;
import wscconnect.android.Utils;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class NotificationModel {
    private String message;
    private String link;
    private String logo;
    private String avatar;
    private int time;
    private boolean confirmed;

    public String getMessage() {
        return Utils.fromHtml(message).toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLogo() {
        // glide can't properly handle .svg
        if (logo == null || logo.endsWith(".svg")) {
            return "";
        }

        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public CharSequence getRelativeTime(Context context) {
        if (time == 0) {
            return "";
        }

        long timeInMillis = (long) time * 1000;

        // less than a minute ago
        if (System.currentTimeMillis() - timeInMillis <= 60000) {
            return context.getString(R.string.just_now);
        }

        return DateUtils.getRelativeTimeSpanString(timeInMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    public String getAvatar() {
        // glide can't properly handle .svg
        if (avatar == null || avatar.endsWith(".svg")) {
            return "";
        }

        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
