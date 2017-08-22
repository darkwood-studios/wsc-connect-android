package wscconnect.android.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wscconnect.android.API;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.fragments.AppsFragment;
import wscconnect.android.fragments.MyAppsFragment;
import wscconnect.android.listeners.OnBackPressedListener;

import static wscconnect.android.Utils.getAllAccessTokens;


public class MainActivity extends AppCompatActivity {
    public static boolean IS_VISIBLE = true;
    public final static String TAG = "WSC-Connect";
    public final static String EXTRA_NOTIFICATION = "extraNotification";
    public final static String EXTRA_OPTION_TYPE = "extraOptionType";
    private Fragment currentFragment;
    private API api;
    private BottomNavigationView navigation;
    private String appsFragmentTag;
    private String myAppsFragmentTag;
    private FragmentManager fManager;
    private String notificationAppID;
    private OnBackPressedListener onBackPressedListener;
    private String notificationOptionType;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_apps:
                    if (!(currentFragment instanceof AppsFragment)) {
                        currentFragment = changeFragment(0);
                        ((AppsFragment) currentFragment).updateSubtitle();
                    }
                    return true;
                case R.id.navigation_my_apps:
                    if (!(currentFragment instanceof MyAppsFragment)) {
                        Log.i(TAG, "OnNavigationItemSelectedListener !(currentFragment instanceof MyAppsFragment))");
                        currentFragment = changeFragment(1);
                    } else if (notificationAppID != null) {
                        Log.i(TAG, "OnNavigationItemSelectedListener else if notificationOptionType " + notificationOptionType);
                        ((MyAppsFragment) currentFragment).selectApp(notificationAppID, notificationOptionType);
                        notificationOptionType = null;
                    } else {
                        Log.i(TAG, "OnNavigationItemSelectedListener else");
                        ((MyAppsFragment) currentFragment).resetCurrentApp();
                    }
                    return true;
            }
            return false;
        }

    };

    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null) {
            if (!onBackPressedListener.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void updateAppsFragment() {
        AppsFragment fragment = (AppsFragment) fManager.findFragmentByTag(appsFragmentTag);
        if (fragment != null) {
            fragment.updateAdapter();
        }
    }

    public void updateMyAppsFragment() {
        MyAppsFragment fragment = (MyAppsFragment) fManager.findFragmentByTag(myAppsFragmentTag);
        if (fragment != null) {
            fragment.updateAdapter();
        }
    }

    public void updateAllFragments() {
        updateAppsFragment();
        updateMyAppsFragment();
    }

    public void setNotificationAppID(String notificationAppID) {
        this.notificationAppID = notificationAppID;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        IS_VISIBLE = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        IS_VISIBLE = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                showAboutDialog();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.action_about);
        builder.setMessage(R.string.dialog_about_text);
        builder.setPositiveButton(R.string.close, null);

        AlertDialog dialog = builder.show();

        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setOnBackPressedListener(OnBackPressedListener callback) {
        this.onBackPressedListener = callback;
    }

    private Fragment changeFragment(int position) {
        Fragment newFragment = null;

        switch (position) {
            case 0:
                newFragment = fManager.findFragmentByTag(appsFragmentTag);
                if (newFragment != null) {
                    fManager.beginTransaction().show(newFragment).commit();
                } else {
                    newFragment = new AppsFragment();
                    fManager.beginTransaction().add(R.id.content, newFragment, appsFragmentTag).commit();
                }

                hideFragments(myAppsFragmentTag);
                getSupportActionBar().setTitle(R.string.app_name);
                break;
            case 1:
                newFragment = fManager.findFragmentByTag(myAppsFragmentTag);
                if (newFragment != null) {
                    fManager.beginTransaction().show(newFragment).commitNow();
                    if (notificationAppID != null) {
                        Log.i(TAG, "changeFragment newFragment != null ");
                        ((MyAppsFragment) newFragment).selectApp(notificationAppID, notificationOptionType);
                        notificationOptionType = null;
                    }
                } else {
                    newFragment = new MyAppsFragment();
                    fManager.beginTransaction().add(R.id.content, newFragment, myAppsFragmentTag).commitNow();
                    if (notificationAppID != null) {
                        Log.i(TAG, "changeFragment newFragment === null ");
                        ((MyAppsFragment) newFragment).selectApp(notificationAppID, notificationOptionType);
                    }
                }

                hideFragments(appsFragmentTag);
                getSupportActionBar().setTitle(R.string.title_my_apps);
                getSupportActionBar().setSubtitle(null);
                break;
        }

        return newFragment;
    }

    private void hideFragments(String... tags) {
        for (String tag : tags) {
            Fragment f = fManager.findFragmentByTag(tag);
            if (f != null) {
                fManager.beginTransaction().hide(f).commit();
            }
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

        notificationAppID = newIntent.getStringExtra(EXTRA_NOTIFICATION);
        notificationOptionType = newIntent.getStringExtra(EXTRA_OPTION_TYPE);

        if (notificationAppID != null) {
            navigation.setSelectedItemId(R.id.navigation_my_apps);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCrashlyrics();
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fManager = getSupportFragmentManager();
        appsFragmentTag = AppsFragment.class.getSimpleName();
        myAppsFragmentTag = MyAppsFragment.class.getSimpleName();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        notificationAppID = getIntent().getStringExtra(EXTRA_NOTIFICATION);
        notificationOptionType = getIntent().getStringExtra(EXTRA_OPTION_TYPE);

        if (notificationAppID != null) {
            navigation.setSelectedItemId(R.id.navigation_my_apps);
        } else {
            if (!getAllAccessTokens(this).isEmpty()) {
                navigation.setSelectedItemId(R.id.navigation_my_apps);
            } else {
                navigation.setSelectedItemId(R.id.navigation_apps);
            }
        }

        /* TODO
        ((MainApplication) getApplication()).setOnNewPushMessageListener(new OnNewPushMessageListener() {
            @Override
            public void onNewPushMessage(final String appID) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notificationAppID = appID;
                        notificationOptionType = AppOptionsFragment.OPTION_TYPE_MESSAGES;
                        navigation.setSelectedItemId(R.id.navigation_my_apps);
                    }
                });
            }
        });*/
    }

    private void setupCrashlyrics() {
        Fabric.with(this, new Crashlytics());

        if (FirebaseInstanceId.getInstance().getToken() != null) {
            Crashlytics.setUserIdentifier(FirebaseInstanceId.getInstance().getToken());
        }
    }

    public void setActiveMenuItem(int id) {
        navigation.setSelectedItemId(id);
    }

    public API getAPI() {
        return getAPI(null);
    }

    public API getAPI(final String token) {
        if (api == null || token != null) {
            int timeout = 10;

            final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.writeTimeout(timeout, TimeUnit.SECONDS);
            clientBuilder.connectTimeout(timeout, TimeUnit.SECONDS);
            clientBuilder.readTimeout(timeout, TimeUnit.SECONDS);

            Interceptor offlineResponseCacheInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    if (!Utils.hasInternetConnection(MainActivity.this)) {
                        request = request.newBuilder()
                                .header("Cache-Control",
                                        "public, only-if-cached, max-stale=" + 2419200)
                                .build();
                    }
                    return chain.proceed(request);
                }
            };

            clientBuilder.addInterceptor(offlineResponseCacheInterceptor);
            clientBuilder.cache(new Cache(new File(getCacheDir(),
                    "APICache"), 50 * 1024 * 1024));

            if (token != null) {
                clientBuilder.addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }
                });
            }

            // add current app version
            int versionCode;
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
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
                    .baseUrl(API.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                    .build();
            api = retrofit.create(API.class);
        }

        return api;
    }
}
