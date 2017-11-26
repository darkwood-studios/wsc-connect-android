package wscconnect.android.models;

import android.content.Context;
import android.text.format.DateUtils;

import wscconnect.android.R;

/**
 * Created by chris on 18.07.17.
 */

public class ThreadModel {
    private int threadID;
    private String topic;
    private boolean isNew;
    private boolean isClosed;
    private int lastPostTime;
    private String lastPostUsername;
    private String lastPostAvatar;
    private int replies;

    public CharSequence getRelativeTime(Context context) {
        if (lastPostTime == 0) {
            return "";
        }

        long timeInMillis = (long) lastPostTime * 1000;

        // less than a minute ago
        if (System.currentTimeMillis() - timeInMillis <= 60000) {
            return context.getString(R.string.just_now);
        }

        return DateUtils.getRelativeTimeSpanString(timeInMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    public int getThreadID() {
        return threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public int getLastPostTime() {
        return lastPostTime;
    }

    public void setLastPostTime(int lastPostTime) {
        this.lastPostTime = lastPostTime;
    }

    public String getLastPostUsername() {
        return lastPostUsername;
    }

    public void setLastPostUsername(String lastPostUsername) {
        this.lastPostUsername = lastPostUsername;
    }

    public int getReplies() {
        return replies;
    }

    public void setReplies(int replies) {
        this.replies = replies;
    }

    public String getLastPostAvatar() {
        return lastPostAvatar;
    }

    public void setLastPostAvatar(String lastPostAvatar) {
        this.lastPostAvatar = lastPostAvatar;
    }
}
