package wscconnect.android.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.auth0.android.jwt.JWT;

import java.util.Random;

/**
 * Created by chris on 18.07.17.
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
    private long uniqueID;
    private int userID;
    private String username;
    private String appID;
    private String appName;
    private String avatar;
    private String token;
    private String appUrl;
    private String appLogo;
    private String appApiUrl;

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
    }

    public AccessTokenModel() {
        Random r = new Random();
        uniqueID = r.nextLong();
    }

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

        return token;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

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

    public long getUniqueID() {
        return uniqueID;
    }

    public String getAppApiUrl() {
        return appApiUrl;
    }

    public void setAppApiUrl(String appApiUrl) {
        this.appApiUrl = appApiUrl;
    }
}
