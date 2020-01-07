package wscconnect.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU Lesser General Public License <http://opensource.org/licenses/lgpl-license.php>
 */
public class KeyUtils {
    private static final String ALGORITHM           = "RSA";
    private static final int    KEYSIZE             = 1024;
    private static final String PUBLICKEY_PREFIX    = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLICKEY_POSTFIX   = "-----END PUBLIC KEY-----";
    private static final String PRIVATEKEY_PREFIX   = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PRIVATEKEY_POSTFIX  = "-----END RSA PRIVATE KEY-----";
    private static final String DECRYPT_ALGORITHM = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEYSIZE);

            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        return null;
    }

    public static void saveKeyPair(String appID, KeyPair keyPair, Context context) {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] publicKeyDER = publicKey.getEncoded();
        byte[] privateKeyDER = privateKey.getEncoded();

        String publicEncoded = Base64.encodeToString(publicKeyDER, Base64.NO_WRAP);
        String privateEncoded = Base64.encodeToString(privateKeyDER, Base64.NO_WRAP);

        SharedPreferences prefs = context.getSharedPreferences(Utils.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        prefs.edit().putString("privateKey-" + appID, privateEncoded).apply();
        prefs.edit().putString("publicKey-" + appID, publicEncoded).apply();
    }

    public static String getPublicPemKey(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();

        byte[] publicKeyDER = publicKey.getEncoded();

        String publicKeyEncoded = Base64.encodeToString(publicKeyDER, Base64.NO_WRAP);

        return PUBLICKEY_PREFIX + "\n" + publicKeyEncoded.replaceAll("(.{64})", "$1\n") + "\n" + PUBLICKEY_POSTFIX;
    }

    public static String getPrivateKey(String appID, Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(Utils.SHARED_PREF_KEY, Context.MODE_PRIVATE);
            String privateKey = prefs.getString("privateKey-" + appID, null);

            if (privateKey == null) {
                return null;
            }

            return privateKey;

            //byte[] pivateKeyBytes = Base64.decode(privateKey, Base64.NO_WRAP);

           // return new String(pivateKeyBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decodeString(String encodedString, String appID, Context context) {
        try {
            String privateKey = getPrivateKey(appID, context);

            if (privateKey == null) {
                return encodedString;
            }

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.NO_WRAP));
            KeyFactory fact = KeyFactory.getInstance(ALGORITHM);
            PrivateKey priv = fact.generatePrivate(keySpec);

            Cipher c = Cipher.getInstance(DECRYPT_ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, priv);
            byte[] decodedBytes = c.doFinal(Base64.decode(encodedString, Base64.DEFAULT));

            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encodedString;
    }
}
