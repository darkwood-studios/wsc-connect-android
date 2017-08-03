package wscconnect.android.models;

/**
 * Created by chris on 18.07.17.
 */

public class LoginModel {
    private String username;
    private String password;
    private String firebaseToken;
    private boolean thirdPartyLogin;

    public LoginModel(String username, String password, String firebaseToken, boolean thirdPartyLogin) {
        this.username = username;
        this.password = password;
        this.firebaseToken = firebaseToken;
        this.thirdPartyLogin = thirdPartyLogin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public boolean isThirdPartyLogin() {
        return thirdPartyLogin;
    }

    public void setThirdPartyLogin(boolean thirdPartyLogin) {
        this.thirdPartyLogin = thirdPartyLogin;
    }
}
