package wscconnect.android.models;

import android.content.Context;
import android.text.format.DateUtils;

import wscconnect.android.R;

/**
 * Created by chris on 18.07.17.
 */

public class PostModel {
    private int postID;
    private String message;
    private String username;
    private String avatar;
    private int time;

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
}
