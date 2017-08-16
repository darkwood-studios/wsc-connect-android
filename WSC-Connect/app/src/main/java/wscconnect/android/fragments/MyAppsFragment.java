package wscconnect.android.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.fragments.myApps.AppOptionsFragment;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.AppOptionModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAppsFragment extends Fragment implements OnBackPressedListener {
    private MainActivity activity;
    private ViewPager viewPager;
    private AppOptionsFragmentPager pagerAdapter;
    private TabLayout tabLayout;
    private ArrayList<AccessTokenModel> tokenList;
    private LinearLayout emptyView;
    private Button loginView;
    private SparseArray<AppOptionsFragment> optionFragments;
    private String appIDToSelect;
    private HashMap<String, String> optionTypeToSelect = new HashMap<>();

    public MyAppsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (MainActivity) getActivity();
        activity.setOnBackPressedListener(this);
        optionFragments = new SparseArray<>();
        tokenList = new ArrayList<>();

        tokenList.addAll(Utils.getAllAccessTokens(activity));

        pagerAdapter = new AppOptionsFragmentPager(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        // TODO this allows 50 opened tabs - otherwise we got a fragment exception. Fix later, cause it consumes too much memory
        viewPager.setOffscreenPageLimit(25);
        tabLayout.setupWithViewPager(viewPager, true);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                resetCurrentApp();
            }
        });

        setCustomTabView();

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setActiveMenuItem(R.id.navigation_apps);
            }
        });

        setEmptyView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (appIDToSelect != null && tokenList != null) {
            Log.i(MainActivity.TAG, "onActivityCreated optionTypeToSelect.get(appIDToSelect) " + optionTypeToSelect.get(appIDToSelect));
            selectApp(appIDToSelect, optionTypeToSelect.get(appIDToSelect));
        }
    }

    public void setCustomTabView() {
        Log.i("tabfailure", "setCustomTabView tabLayout.getTabCount() " + tabLayout.getTabCount());
        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(null);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
    }

    public void selectApp(String appID, String optionType) {
        Log.i(MainActivity.TAG, "selectApp");
        if (tokenList != null) {
            int position = -1;

            for (int i = 0; i < tokenList.size(); i++) {
                if (tokenList.get(i).getAppID().equals(appID)) {
                    position = i;
                    break;
                }
            }

            if (position != -1) {
                viewPager.setCurrentItem(position);
                pagerAdapter.notifyDataSetChanged();
                setCustomTabView();

                if (optionType != null) {
                    AppOptionsFragment fragment = optionFragments.get(position);
                    if (fragment != null) {
                        Log.i(MainActivity.TAG, "selectApp position " + position);
                        fragment.showOption(optionType);
                    } else {
                        optionTypeToSelect.put(appID, optionType);
                    }
                }
            }
        } else {
            Log.i(MainActivity.TAG, "selectApp else");
            appIDToSelect = appID;
            optionTypeToSelect.put(appID, optionType);
        }
    }

    public void updateAdapter() {
        Log.i("tabfailure", "MyAppsFragment updateAdapter");
        tokenList.clear();
        tokenList.addAll(Utils.getAllAccessTokens(activity));
        pagerAdapter.notifyDataSetChanged();
        setEmptyView();
    }

    private void setEmptyView() {
        if (tokenList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_apps, container, false);
        viewPager = view.findViewById(R.id.fragment_my_apps_pager);
        tabLayout = view.findViewById(R.id.fragment_my_apps_tabs);
        emptyView = view.findViewById(R.id.fragment_my_apps_empty);
        loginView = view.findViewById(R.id.fragment_my_apps_login);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        AppOptionsFragment fragment = optionFragments.get(viewPager.getCurrentItem());
        if (fragment != null) {
            return fragment.onBackPressed();
        }

        return false;
    }

    public void resetCurrentApp() {
        Log.i("tabfailure", "MyAppsFragment resetCurrentApp");
        AppOptionsFragment fragment = optionFragments.get(viewPager.getCurrentItem());
        if (fragment != null) {
            fragment.resetViews();
        }
    }

    private class AppOptionsFragmentPager extends FragmentPagerAdapter {
        private HashMap<Integer, AppOptionsFragment> fragments = new HashMap<>();

        public AppOptionsFragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tokenList.get(position).getAppName();
        }

        @Override
        public long getItemId(int position) {
            return tokenList.get(position).getUniqueID();
        }

        @Override
        public Fragment getItem(int position) {
           // AppOptionsFragment fragment = fragments.get(position);

           // if (fragment == null) {
                AppOptionsFragment fragment = new AppOptionsFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(AccessTokenModel.EXTRA, tokenList.get(position));
                bundle.putString(AppOptionModel.TYPE, optionTypeToSelect.get(tokenList.get(position).getAppID()));
                // reset after creating
                optionTypeToSelect.remove(tokenList.get(position).getAppID());
                fragment.setArguments(bundle);
                optionFragments.append(position, fragment);
                //fragments.put(position, fragment);
           // }

            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            AppOptionsFragment fragment = (AppOptionsFragment) object;

            // check if fragment still exists or should be removed
            boolean removeFragment = true;

            int index = 0;
            for (int i = 0; i < tokenList.size(); i++) {
                AccessTokenModel token = tokenList.get(i);

                if (token.getAppID().equals(fragment.token.getAppID())) {
                    index = i;
                    removeFragment = false;

                    //only update fragment when userID differs from the old one
                    if (token.getUserID() != fragment.token.getUserID()) {
                        fragment.onUpdate(token);
                    } else {
                        fragment.hideWebview();
                    }

                    break;
                }
            }

            if (removeFragment) {
                for (int i = optionFragments.size() - 1; i >= 0; i--) {
                    int key = optionFragments.keyAt(i);
                    AppOptionsFragment result = optionFragments.get(key);
                    if (result.equals(fragment)) {
                        optionFragments.remove(key);
                      //  fragments.remove(index);
                        break;
                    }
                }

                return POSITION_NONE;
            } else {
                return index;
            }
        }

        public View getTabView(int position) {
            View v = activity.getLayoutInflater().inflate(R.layout.my_apps_tab, null);
            TextView title = v.findViewById(R.id.my_apps_tabs_title);
            TextView unreadNotificationsView = v.findViewById(R.id.my_apps_tabs_unread_notifications);
            title.setText(tokenList.get(position).getAppName());

            int unreadNotifications = Utils.getUnreadNotifications(activity, tokenList.get(position).getAppID());
            Log.i("tabfailure", "getTabView unreadNotifications" + unreadNotifications);

            if (unreadNotifications > 0) {
                unreadNotificationsView.setText(String.valueOf(unreadNotifications));
                unreadNotificationsView.setVisibility(View.VISIBLE);
            } else {
                unreadNotificationsView.setVisibility(View.GONE);
            }
            return v;
        }

        @Override
        public int getCount() {
            return tokenList.size();
        }
    }
}
