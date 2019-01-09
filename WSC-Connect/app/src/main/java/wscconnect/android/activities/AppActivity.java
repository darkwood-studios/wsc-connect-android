package wscconnect.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;

import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.fragments.myApps.appOptions.AppConversationsFragment;
import wscconnect.android.fragments.myApps.appOptions.AppForumFragment;
import wscconnect.android.fragments.myApps.appOptions.AppMessagesFragment;
import wscconnect.android.fragments.myApps.appOptions.AppNotificationsFragment;
import wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.listeners.OnFragmentUpdateListener;
import wscconnect.android.models.AccessTokenModel;

import static wscconnect.android.activities.MainActivity.EXTRA_OPTION_TYPE;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AppActivity extends AppCompatActivity {
    public final static String FRAGMENT_FORUM = "forum";
    public final static String FRAGMENT_WEBVIEW = "webview";
    public final static String FRAGMENT_NOTIFICATIONS = "notifications";
    public final static String FRAGMENT_CONVERSATIONS = "conversations";
    public final static String FRAGMENT_MESSAGES = "messages";
    public static final String EXTRA_EVENT_NAME = "eventName";
    public static final String EXTRA_EVENT_ID = "eventID";
    public final static String EXTRA_FORCE_LOAD = "forceLoad";
    private AccessTokenModel token;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private AppFragmentAdapter fragmentAdapter;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        AccessTokenModel newToken = intent.getParcelableExtra(AccessTokenModel.EXTRA);
        String optionType = intent.getStringExtra(EXTRA_OPTION_TYPE);
        String eventName = intent.getStringExtra(EXTRA_EVENT_NAME);
        int eventID = intent.getIntExtra(EXTRA_EVENT_ID, 0);

        if (token.getAppID().equals(newToken.getAppID())) {
            determineCurrentPage(optionType, eventName, eventID);
        } else {
            // different app, reload activity
            Intent appIntent = new Intent(this, AppActivity.class);
            appIntent.putExtra(AccessTokenModel.EXTRA, newToken);
            appIntent.putExtra(AppActivity.EXTRA_EVENT_NAME, eventName);
            appIntent.putExtra(AppActivity.EXTRA_EVENT_ID, eventID);
            startActivity(appIntent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        Intent intent = getIntent();
        token = intent.getParcelableExtra(AccessTokenModel.EXTRA);
        final String optionType = intent.getStringExtra(EXTRA_OPTION_TYPE);
        final String eventName = intent.getStringExtra(EXTRA_EVENT_NAME);
        final int eventID = intent.getIntExtra(EXTRA_EVENT_ID, 0);

        if (token == null) {
            finish();
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(token.getAppName());

        String username = token.getUsername();
        if (username == null || username.isEmpty()) {
            username = Utils.getUsername(this, token.getAppID());
        }
        getSupportActionBar().setSubtitle(getString(R.string.fragment_my_apps_username, username));

        viewPager = findViewById(R.id.activity_app_pager);
        tabLayout = findViewById(R.id.activity_app_tabs);

        // check if token has to be refreshed
        JWT jwt = new JWT(token.getToken());
        if (jwt.isExpired(0)) {
            tabLayout.setVisibility(View.GONE);
            Utils.showLoadingOverlay(this, true);
            Utils.refreshAccessToken(this, token.getAppID(), new SimpleCallback() {
                @Override
                public void onReady(boolean success) {
                    token = Utils.getAccessToken(AppActivity.this, token.getAppID());
                    Utils.showLoadingOverlay(AppActivity.this, false);
                    if (token != null) {
                        setup(optionType, eventName, eventID);
                    } else {
                        Toast.makeText(AppActivity.this, getString(R.string.activity_app_token_null), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(AppActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            setup(optionType, eventName, eventID);
        }
    }

    private void setup(String optionType, String eventName, int eventID) {
        tabLayout.setVisibility(View.VISIBLE);
        fragmentAdapter = new AppFragmentAdapter(getSupportFragmentManager(), this, token);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(5);
        tabLayout.setupWithViewPager(viewPager, true);
        setCustomTabView();
        determineCurrentPage(optionType, eventName, eventID);
    }

    private void determineCurrentPage(String optionType, String eventName, int eventID) {
        if (optionType != null) {
            switch (optionType) {
                case "message":
                    setCurrentPage(FRAGMENT_MESSAGES, null);
                    break;
                case "notification":
                    Bundle b = new Bundle();
                    b.putInt(EXTRA_EVENT_ID, eventID);
                    b.putString(EXTRA_EVENT_NAME, eventName);
                    b.putBoolean(EXTRA_FORCE_LOAD, true);

                    switch (eventName) {
                        case "conversation":
                        case "conversationMessage":
                            setCurrentPage(FRAGMENT_CONVERSATIONS, b);
                            break;

                        default:
                            setCurrentPage(FRAGMENT_NOTIFICATIONS, b);
                    }

                    break;
            }
        }
    }

    public void setCustomTabView() {
        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(null);
            tab.setCustomView(fragmentAdapter.getTabView(i));
        }
    }

    public void setCurrentPage(String tabName, Bundle bundle) {
        int position = token.getAppTabs().indexOf(tabName);
        if (position != -1 && fragmentAdapter != null) {
            Fragment f = fragmentAdapter.getFragmentAtPosition(position);
            if (bundle != null && f instanceof OnFragmentUpdateListener) {
                ((OnFragmentUpdateListener) f).onUpdate(bundle);
            }

            viewPager.setCurrentItem(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentAdapter == null) {
            super.onBackPressed();
        } else {
            Fragment currentFragment = fragmentAdapter.getFragmentAtPosition(viewPager.getCurrentItem());

            if (currentFragment != null && currentFragment instanceof OnBackPressedListener) {
                if (!((OnBackPressedListener) currentFragment).onBackPressed()) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    public static class AppFragmentAdapter extends FragmentPagerAdapter {
        Context context;
        AccessTokenModel token;
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public AppFragmentAdapter(FragmentManager fm, Context context, AccessTokenModel token) {
            super(fm);
            this.context = context;
            this.token = token;
        }

        @Override
        public int getCount() {
            return token.getAppTabs().size();
        }

        public Fragment getFragmentAtPosition(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            String tabName = token.getAppTabs().get(position);
            Fragment f = null;

            // do not use Class.newInstance because of https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#newInstance()
            switch (tabName) {
                case FRAGMENT_FORUM:
                    f = new AppForumFragment();
                    break;
                case FRAGMENT_WEBVIEW:
                    f = new AppWebviewFragment();
                    break;
                case FRAGMENT_NOTIFICATIONS:
                    f = new AppNotificationsFragment();
                    break;
                case FRAGMENT_CONVERSATIONS:
                    f = new AppConversationsFragment();
                    break;
                case FRAGMENT_MESSAGES:
                    f = new AppMessagesFragment();
                    break;
            }

            Bundle b = new Bundle();
            b.putParcelable(AccessTokenModel.EXTRA, token);
            f.setArguments(b);
            registeredFragments.put(position, f);
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String tabName = token.getAppTabs().get(position);
            return context.getString(context.getResources().getIdentifier("activity_app_tab_" + tabName, "string", context.getPackageName()));
        }

        public View getTabView(int position) {
            LayoutInflater inflater = LayoutInflater.from(context);

            View v = inflater.inflate(R.layout.app_activity_tab, null);
            TextView title = v.findViewById(R.id.app_activity_tab_title);
            TextView unreadView = v.findViewById(R.id.app_activity_tab_unread);
            title.setText(getPageTitle(position));

            String tabName = token.getAppTabs().get(position);
            int unread = 0;

            switch (tabName) {
                case FRAGMENT_NOTIFICATIONS:
                    unread = Utils.getUnreadNotifications(context, token.getAppID());
                    break;
                case FRAGMENT_CONVERSATIONS:
                    unread = Utils.getUnreadConversations(context, token.getAppID());
                    break;
            }

            if (unread > 0) {
                unreadView.setText(String.valueOf(unread));
                unreadView.setVisibility(View.VISIBLE);
            } else {
                unreadView.setVisibility(View.GONE);
            }

            return v;
        }
    }
}
