package wscconnect.android.models;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import wscconnect.android.Utils;

/**
 * Created by chris on 18.07.17.
 */

public class AppModel {
    @SerializedName("_id")
    private String appID;
    private String name;
    private String url;
    private String apiUrl;
    private String logo;
    @SerializedName("_users")
    private int userCount;
    private boolean visible = true;

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isLoggedIn(Context context) {
        return (Utils.getAccessTokenString(context, appID) != null);
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
