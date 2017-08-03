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
    private String logo;

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
}
