package wscconnect.android.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.auth0.android.jwt.JWT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static wscconnect.android.activities.AppActivity.FRAGMENT_CONVERSATIONS;
import static wscconnect.android.activities.AppActivity.FRAGMENT_FORUM;
import static wscconnect.android.activities.AppActivity.FRAGMENT_MESSAGES;
import static wscconnect.android.activities.AppActivity.FRAGMENT_NOTIFICATIONS;
import static wscconnect.android.activities.AppActivity.FRAGMENT_WEBVIEW;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AccessTokenModel implements Parcelable {
    public static final String EXTRA = "extraAccessTokenModel";
    public static final Creator<AccessTokenModel> CREATOR = new Creator<AccessTokenModel>() {
        @Override
        public AccessTokenModel createFromParcel(Parcel in) {
            return new AccessTokenModel(in);
        }

        @Override
        public AccessTokenModel[] newArray(int size) {
            return new AccessTokenModel[size];
        }
    };
    private final long uniqueID;
    private int userID;
    private String username;
    private String appID;
    private String appName;
    private String avatar;
    private String token;
    private String appUrl;
    private String appLogo;
    private String appApiUrl;
    private List<String> appTabs = new ArrayList<>();

    private String secretToken;
    private transient List<String> orderedTabs;

    protected AccessTokenModel(Parcel in) {
        userID = in.readInt();
        username = in.readString();
        appID = in.readString();
        appName = in.readString();
        avatar = in.readString();
        token = in.readString();
        appUrl = in.readString();
        appLogo = in.readString();
        uniqueID = in.readLong();
        appApiUrl = in.readString();
        in.readList(appTabs, null);
        secretToken = in.readString();
    }

    public AccessTokenModel() {
        Random r = new Random();
        uniqueID = r.nextLong();
    }

    @SuppressWarnings("deprecation")
    public static AccessTokenModel fromJWT(JWT jwt) {
        AccessTokenModel token = new AccessTokenModel();

        token.setUsername(jwt.getClaim("username").asString());
        token.setUserID(jwt.getClaim("userID").asInt());
        token.setAppID(jwt.getClaim("appID").asString());
        token.setAvatar(jwt.getClaim("avatar").asString());
        token.setAppName(jwt.getClaim("appName").asString());
        token.setToken(jwt.toString());
        token.setAppUrl(jwt.getClaim("appUrl").asString());
        token.setAppLogo(jwt.getClaim("appLogo").asString());
        token.setAppApiUrl(jwt.getClaim("appApiUrl").asString());
        token.setAppTabs(jwt.getClaim("appTabs").asList(String.class));
        token.setSecretToken(jwt.getClaim("secretToken").asString());

        return token;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    @Deprecated
    public String getUsername() {
        return username;
    }

    @Deprecated
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAvatar() {
        // glide can't properly handle .svg
        if (avatar.endsWith(".svg")) {
            return "";
        }

        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(userID);
        parcel.writeString(username);
        parcel.writeString(appID);
        parcel.writeString(appName);
        parcel.writeString(avatar);
        parcel.writeString(token);
        parcel.writeString(appUrl);
        parcel.writeString(appLogo);
        parcel.writeLong(uniqueID);
        parcel.writeString(appApiUrl);
        parcel.writeList(appTabs);
        parcel.writeString(secretToken);
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getAppLogo() {
        return appLogo;
    }

    public void setAppLogo(String appLogo) {
        this.appLogo = appLogo;
    }

    public String getAppApiUrl() {
        return appApiUrl;
    }

    public void setAppApiUrl(String appApiUrl) {
        this.appApiUrl = appApiUrl;
    }

    public List<String> getAppTabs() {
        // order of the tabs should be: forum|webview, notifications, conversations, messages
        if (orderedTabs == null) {
            orderedTabs = new ArrayList<>();

            if (appTabs.contains(FRAGMENT_FORUM)) {
                orderedTabs.add(FRAGMENT_FORUM);
            }

            if (appTabs.contains(FRAGMENT_WEBVIEW)) {
                orderedTabs.add(FRAGMENT_WEBVIEW);
            }

            if (appTabs.contains(FRAGMENT_NOTIFICATIONS)) {
                orderedTabs.add(FRAGMENT_NOTIFICATIONS);
            }

            if (appTabs.contains(FRAGMENT_CONVERSATIONS)) {
                orderedTabs.add(FRAGMENT_CONVERSATIONS);
            }

            if (appTabs.contains(FRAGMENT_MESSAGES)) {
                orderedTabs.add(FRAGMENT_MESSAGES);
            }
        }

        return orderedTabs;
    }

    public void setAppTabs(List<String> appTabs) {
        this.appTabs = appTabs;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }
}
