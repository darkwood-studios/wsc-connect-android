package wscconnect.android.models;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class LoginModel {
    private String username;
    private String password;
    private String firebaseToken;
    private boolean thirdPartyLogin;
    private String device;

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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
