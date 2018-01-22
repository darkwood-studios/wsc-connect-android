package wscconnect.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;
import wscconnect.android.R;
import wscconnect.android.fragments.AppsFragment;
import wscconnect.android.fragments.MyAppsFragment;
import wscconnect.android.listeners.OnBackPressedListener;

import static wscconnect.android.Utils.getAllAccessTokens;


public class MainActivity extends AppCompatActivity {
    public final static String TAG = "WSC-Connect";
    public final static String EXTRA_NOTIFICATION = "extraNotification";
    public final static String EXTRA_OPTION_TYPE = "extraOptionType";
    public static boolean IS_VISIBLE = true;
    public Toolbar toolbar;
    private Fragment currentFragment;
    private BottomNavigationView navigation;
    private String appsFragmentTag;
    private String myAppsFragmentTag;
    private FragmentManager fManager;
    private String notificationAppID;
    private OnBackPressedListener onBackPressedListener;
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
            fragment.updateData();
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
                    if (!newFragment.isStateSaved()) {
                        fManager.beginTransaction().show(newFragment).commit();
                    }
                } else {
                    newFragment = new AppsFragment();
                    fManager.beginTransaction().add(R.id.activity_main_content, newFragment, appsFragmentTag).commit();
                }

                hideFragments(myAppsFragmentTag);
                getSupportActionBar().setTitle(R.string.app_name);
                break;
            case 1:
                newFragment = fManager.findFragmentByTag(myAppsFragmentTag);
                if (newFragment != null) {
                    if (!newFragment.isStateSaved()) {
                        fManager.beginTransaction().show(newFragment).commitNow();
                        if (notificationAppID != null) {
                            Log.i(TAG, "changeFragment newFragment != null ");
                        }
                    }
                } else {
                    newFragment = new MyAppsFragment();
                    fManager.beginTransaction().add(R.id.activity_main_content, newFragment, myAppsFragmentTag).commitNow();
                    if (notificationAppID != null) {
                        Log.i(TAG, "changeFragment newFragment === null ");
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

        if (notificationAppID != null) {
            navigation.setSelectedItemId(R.id.navigation_my_apps);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCrashlyrics();
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.activity_main_toolbar);

        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fManager = getSupportFragmentManager();
        appsFragmentTag = AppsFragment.class.getSimpleName();
        myAppsFragmentTag = MyAppsFragment.class.getSimpleName();

        navigation = findViewById(R.id.activity_main_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        notificationAppID = getIntent().getStringExtra(EXTRA_NOTIFICATION);

        if (notificationAppID != null) {
            navigation.setSelectedItemId(R.id.navigation_my_apps);
        } else {
            if (!getAllAccessTokens(this).isEmpty()) {
                navigation.setSelectedItemId(R.id.navigation_my_apps);
            } else {
                navigation.setSelectedItemId(R.id.navigation_apps);
            }
        }
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
}
