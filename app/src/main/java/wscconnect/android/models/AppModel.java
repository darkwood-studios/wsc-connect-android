package wscconnect.android.models;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import wscconnect.android.Utils;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AppModel {
    @SerializedName("_id")
    private String appID;
    private String name;
    private String url;
    private String apiUrl;
    private String logo;
    private boolean visible = true;
    private boolean logoAccessible = true;

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

    public boolean isLogoAccessible() {
        return logoAccessible;
    }

    public void setLogoAccessible(boolean logoAccessible) {
        this.logoAccessible = logoAccessible;
    }

    public static ArrayList<AppModel> fromJSONArray(JsonArray jsonApps) {
        ArrayList<AppModel> apps = new ArrayList<>();

        for (int i = 0; i < jsonApps.size(); i++) {
            apps.add(fromJSONObject(jsonApps.get(i).getAsJsonObject()));
        }

        return apps;
    }

    private static AppModel fromJSONObject(JsonObject jsonApp) {
        AppModel app = new AppModel();
        app.setAppID(jsonApp.get("_id").getAsString());
        app.setName(jsonApp.get("name").getAsString());
        app.setUrl(jsonApp.get("url").getAsString());
        app.setLogo(jsonApp.get("logo").getAsString());
        app.setApiUrl(jsonApp.get("apiUrl").getAsString());

        boolean visible = true;
        if (jsonApp.has("visible")) {
            visible = jsonApp.get("visible").getAsInt() == 1;
        }
        app.setVisible(visible);
        app.setLogoAccessible(jsonApp.get("logoAccessible").getAsInt() == 1);

        return app;
    }
}
