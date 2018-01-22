package wscconnect.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.auth0.android.jwt.JWT;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment;
import wscconnect.android.models.AccessTokenModel;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static wscconnect.android.activities.MainActivity.EXTRA_OPTION_TYPE;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class Utils {
    public final static String SHARED_PREF_KEY = "wsc-connect";
    private static boolean accessTokenRefreshing;
    private static SimpleCallback onRefreshAccessTokenFinishCallback;

    public static boolean hasInternetConnection(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static void hideKeyboard(Activity activity) {
        try {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (((activity.getCurrentFocus() != null) && (activity.getCurrentFocus().getWindowToken() != null))) {
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showKeyboard(Context context) {
        ((InputMethodManager) (context).getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideProgressView(View view, ProgressBar bar) {
        hideProgressView(view, bar, true);
    }

    public static void hideProgressView(View view, ProgressBar bar, boolean makeViewVisible) {
        if (bar != null) {
            ViewGroup vg = (ViewGroup) bar.getParent();
            if (vg != null) {
                vg.removeView(bar);
            }
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
        Utils.saveUnreadConversations(context, appID, 0);
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

    public static void saveUnreadConversations(Context context, String appID, int count) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        prefs.edit().putInt("unreadConversations-" + appID, count).apply();
    }

    public static int getUnreadConversations(Context context, String appID) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return prefs.getInt("unreadConversations-" + appID, 0);
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
                String app1Name = t1.getAppName().replaceAll("[^a-zA-Z]", "").toLowerCase();
                String app2Name = t2.getAppName().replaceAll("[^a-zA-Z]", "").toLowerCase();

                return app1Name.compareToIgnoreCase(app2Name);
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

    private static void callOnRefreshAccessTokenFinishCallback(boolean success) {
        if (onRefreshAccessTokenFinishCallback != null) {
            onRefreshAccessTokenFinishCallback.onReady(success);
        }
    }

    private static void setOnRefreshAccessTokenFinishCallback(SimpleCallback callback) {
        onRefreshAccessTokenFinishCallback = callback;
    }

    public static void refreshAccessToken(final Activity activity, final String appID, final SimpleCallback callback) {
        String refreshToken = Utils.getRefreshTokenString(activity, appID);

        if (accessTokenRefreshing) {
            setOnRefreshAccessTokenFinishCallback(new SimpleCallback() {
                @Override
                public void onReady(boolean success) {
                    setOnRefreshAccessTokenFinishCallback(null);
                    callback.onReady(success);
                }
            });
            return;
        }

        accessTokenRefreshing = true;

        Utils.getAPI(activity, refreshToken).getAccessToken().enqueue(new RetroCallback<ResponseBody>(activity) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);

                accessTokenRefreshing = false;

                if (response.isSuccessful()) {
                    JSONObject obj;
                    try {
                        obj = new JSONObject(response.body().string());
                        String accessToken = obj.getString("accessToken");

                        saveAccessToken(activity, appID, accessToken);
                        callback.onReady(true);
                        callOnRefreshAccessTokenFinishCallback(true);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        callback.onReady(false);
                        callOnRefreshAccessTokenFinishCallback(false);
                    }
                } else {
                    callback.onReady(false);
                    callOnRefreshAccessTokenFinishCallback(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);

                accessTokenRefreshing = false;
                t.printStackTrace();

                callback.onReady(false);
                callOnRefreshAccessTokenFinishCallback(false);
            }
        });
    }

    @TargetApi(26)
    private static void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                context.getString(R.string.default_notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

    public static void showDataNotification(Context context, String tag, int id, String appID, String optionType, String title, String message, String eventName, int eventID, Bitmap largeIcon) {

        initChannels(context);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);

        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon);
        }

        Intent intent = new Intent(context, AppActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.putExtra(AccessTokenModel.EXTRA, Utils.getAccessToken(context, appID));
        intent.putExtra(AppActivity.EXTRA_EVENT_NAME, eventName);
        intent.putExtra(AppActivity.EXTRA_EVENT_ID, eventID);
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

        // get preferences
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtone = prefs.getString("pref_notifications_ringtone", null);
        boolean vibrate = prefs.getBoolean("pref_notifications_vibration", false);

        if (ringtone != null && !ringtone.isEmpty() && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            notificationBuilder.setSound(Uri.parse(ringtone), AudioManager.STREAM_NOTIFICATION);
        }

        if (vibrate && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            Vibrator v = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            long pattern[] = new long[]{0, 300, 100, 300};

            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                v.vibrate(pattern, -1);
            }
        }

        mNotificationManager.notify(tag, id, notificationBuilder.build());
    }

    public static void showFullScreenPhotoDialog(final Activity activity, String photoURL) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_NoTitleBar_Fullscreen);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_photo, null);

        ImageView photo = dialogView.findViewById(R.id.dialog_photo_photo);

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

    public static API getAPI(Context context) {
        return Utils.getAPI(context, API.ENDPOINT, null);
    }

    public static API getAPI(Context context, final String token) {
        return Utils.getAPI(context, API.ENDPOINT, token);
    }

    public static API getAPI(final Context context, final String url, final String token) {
        int timeout = 10;

        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.writeTimeout(timeout, TimeUnit.SECONDS);
        clientBuilder.connectTimeout(timeout, TimeUnit.SECONDS);
        clientBuilder.readTimeout(timeout, TimeUnit.SECONDS);

        Interceptor offlineResponseCacheInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!Utils.hasInternetConnection(context)) {
                    request = request.newBuilder()
                            .header("Cache-Control",
                                    "public, only-if-cached, max-stale=" + 2419200)
                            .build();
                }
                return chain.proceed(request);
            }
        };

        Interceptor fixUrlInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String url = request.url().toString();
                url = url.replace("%3F", "?");
                url = url.replace("%2F", "/");

                request = request.newBuilder()
                        .url(url)
                        .build();

                return chain.proceed(request);
            }
        };

        clientBuilder.addInterceptor(offlineResponseCacheInterceptor);
        clientBuilder.addInterceptor(fixUrlInterceptor);
        clientBuilder.cache(new Cache(new File(context.getCacheDir(),
                "APICache"), 50 * 1024 * 1024));

        if (token != null) {
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            // necessary, because some Apache webservers discard the auth headers
                            .addHeader("X-Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
            });
        }

        // set user agent
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("User-Agent", AppWebviewFragment.USER_AGENT)
                        .build();
                return chain.proceed(newRequest);
            }
        });

        // add current app version
        int versionCode;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // an error is likely not good. Set versionCode to 1
            versionCode = 1;
        }
        final int finalVersionCode = versionCode;
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("X-App-Version-Code", String.valueOf(finalVersionCode))
                        .build();
                return chain.proceed(newRequest);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .client(clientBuilder.build())
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .build();
        return retrofit.create(API.class);
    }

    public static String getApiUrlExtension(String appApiUrl) {
        if (appApiUrl.contains("index.php/WSCConnectAPI/")) {
            return "index.php/WSCConnectAPI/";
        } else if (appApiUrl.contains("index.php?wsc-connect-api/")) {
            return "index.php?wsc-connect-api/";
        } else if (appApiUrl.contains("wsc-connect-api/")) {
            return "wsc-connect-api/";
        } else if (appApiUrl.contains("index.php?wsc-connect-api")) {
            return "index.php?wsc-connect-api";
        } else if (appApiUrl.contains("wsc-connect-api")) {
            return "wsc-connect-api";
        }

        return "";
    }

    public static String prepareApiUrl(String appApiUrl) {
        appApiUrl = appApiUrl.replace("index.php/WSCConnectAPI/", "");
        appApiUrl = appApiUrl.replace("index.php?wsc-connect-api/", "");
        appApiUrl = appApiUrl.replace("wsc-connect-api/", "");
        appApiUrl = appApiUrl.replace("index.php?wsc-connect-api", "");
        appApiUrl = appApiUrl.replace("wsc-connect-api", "");

        if (!appApiUrl.endsWith("/")) {
            appApiUrl = appApiUrl + "/";
        }

        return appApiUrl;
    }

    public static void setError(Context context, TextView view) {
        setError(context, view, context.getString(R.string.required));
    }

    public static void setError(Context context, TextView view, String error) {
        view.requestFocus();
        view.setError(error);
        ViewParent parent = view.getParent();
        if (parent != null) {
            parent.requestChildFocus(view, view);
        }
    }

    public static void showLoadingOverlay(Activity activity, boolean show) {
        if (activity == null) {
            return;
        }

        Window window = activity.getWindow();

        if (window == null) {
            return;
        }

        ViewGroup rootView = window.getDecorView().findViewById(android.R.id.content);
        LayoutInflater li = LayoutInflater.from(activity);
        View loadingView = li.inflate(R.layout.loading_overlay_view, null);

        View v = rootView.findViewById(R.id.loading_overlay_view);
        if (show) {
            if (v == null) {
                rootView.addView(loadingView);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        } else {
            if (v != null) {
                rootView.findViewById(R.id.loading_overlay_view).setVisibility(View.GONE);
            }
        }
    }

    public static int dpToPx(int dp, Context context) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
