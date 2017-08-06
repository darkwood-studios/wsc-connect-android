package wscconnect.android;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.auth0.android.jwt.JWT;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.models.AccessTokenModel;

import static wscconnect.android.activities.MainActivity.EXTRA_NOTIFICATION;
import static wscconnect.android.activities.MainActivity.EXTRA_OPTION_TYPE;

/**
 * Created by chris on 18.07.17.
 */

public class Utils {
    public final static String SHARED_PREF_KEY = "wsc-connect";

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static void hideProgressView(View view, ProgressBar bar) {
        hideProgressView(view, bar, true);
    }

    public static void hideProgressView(View view, ProgressBar bar, boolean makeViewVisible) {
        if (bar != null) {
            ViewGroup vg = (ViewGroup) bar.getParent();
            vg.removeView(bar);
        }

        if (makeViewVisible) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static ProgressBar showProgressView(Context context, View view, int style) {
        ProgressBar progressBar = new ProgressBar(context, null, style);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setLayoutParams(view.getLayoutParams());

        view.setVisibility(View.GONE);
        ViewGroup vg = (ViewGroup) view.getParent();
        int index = vg.indexOfChild(view);
        vg.addView(progressBar, index);

        return progressBar;
    }


    public static void logout(Context context, String appID) {
        Utils.saveUnreadNotifications(context, appID, 0);
        Utils.removeAccessTokenString(context, appID);
    }

    public static void saveAccessToken(Context context, String appID, String accessToken) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        prefs.edit().putString("accessToken-" + appID, accessToken).apply();
    }

    public static void saveRefreshToken(Context context, String appID, String accessToken) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        prefs.edit().putString("refreshToken-" + appID, accessToken).apply();
    }

    public static void saveUnreadNotifications(Context context, String appID, int count) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        prefs.edit().putInt("unreadNotifications-" + appID, count).apply();
    }

    public static int getUnreadNotifications(Context context, String appID) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return prefs.getInt("unreadNotifications-" + appID, 0);
    }

    public static String getAccessTokenString(Context context, String appID) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return prefs.getString("accessToken-" + appID, null);
    }

    public static void removeAccessTokenString(Context context, String appID) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        prefs.edit().remove("accessToken-" + appID).apply();
    }

    public static String getRefreshTokenString(Context context, String appID) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return prefs.getString("refreshToken-" + appID, null);
    }

    public static AccessTokenModel getAccessToken(Context context, String appID) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        String token = prefs.getString("accessToken-" + appID, null);

        if (token == null) {
            return null;
        }

        JWT jwt = new JWT(token);
        return AccessTokenModel.fromJWT(jwt);
    }

    public static ArrayList<AccessTokenModel> getAllAccessTokens(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);

        ArrayList<AccessTokenModel> tokens = new ArrayList<>();

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("accessToken-")) {
                JWT jwt = new JWT(entry.getValue().toString());
                tokens.add(AccessTokenModel.fromJWT(jwt));
            }
        }

        Collections.sort(tokens, new Comparator<AccessTokenModel>() {
            @Override
            public int compare(AccessTokenModel t1, AccessTokenModel t2) {
                return t1.getAppName().compareToIgnoreCase(t2.getAppName());
            }
        });

        return tokens;
    }

    public static void removeAllAccessTokens(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("accessToken-")) {
                prefs.edit().remove(entry.getKey()).apply();
            }
        }
    }

    public static void refreshAccessToken(final MainActivity activity, final String appID, final SimpleCallback callback) {
        String refreshToken = Utils.getRefreshTokenString(activity, appID);

        activity.getAPI(refreshToken).getAccessToken().enqueue(new RetroCallback<ResponseBody>(activity) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);

                if (response.isSuccessful()) {
                    JSONObject obj;
                    try {
                        obj = new JSONObject(response.body().string());
                        String accessToken = obj.getString("accessToken");
                        saveAccessToken(activity, appID, accessToken);
                        callback.onReady(true);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        callback.onReady(false);
                    }
                } else {
                    callback.onReady(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);

                callback.onReady(false);
            }
        });
    }

    public static void showDataNotification(Context context, String tag, int id, String appID, String optionType, String title, String message, Bitmap largeIcon) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);

        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.putExtra(EXTRA_NOTIFICATION, appID);
        if (optionType != null) {
            intent.putExtra(EXTRA_OPTION_TYPE, optionType);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentTitle(title);
        message = Utils.fromHtml(message).toString();
        notificationBuilder.setContentText(message);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        mNotificationManager.notify(tag, id, notificationBuilder.build());
    }

    public static void showFullScreenPhotoDialog(final Activity activity, String photoURL) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_NoTitleBar_Fullscreen);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_photo, null);

        ImageView photo = (ImageView) dialogView.findViewById(R.id.dialog_photo_photo);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.show();

        GlideApp.with(activity).load(photoURL).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                dialog.dismiss();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(photo);

        dialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private static boolean isLollipop() {
        return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }
}
