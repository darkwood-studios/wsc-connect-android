package wscconnect.android.models;

import android.content.Context;
import android.text.format.DateUtils;

import wscconnect.android.R;

/**
 * Created by chris on 18.07.17.
 */

public class ConversationModel implements APIModel {
    private int conversationID;
    private String title;
    private String logo;
    private boolean unread;
    private String link;
    private String participants;
    private String avatar;
    private long time;

    public int getConversationID() {
        return conversationID;
    }

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
}
