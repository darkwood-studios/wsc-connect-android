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

import java.util.ArrayList;

import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.fragments.myApps.AppOptionsFragment;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.models.AccessTokenModel;

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
    private String optionTypeToSelect;

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
        viewPager.setOffscreenPageLimit(3);
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

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setActiveMenuItem(R.id.navigation_apps);
            }
        });

        setEmptyView();

        if (appIDToSelect != null && tokenList != null) {
            selectApp(appIDToSelect, optionTypeToSelect);
        }
    }

    public void selectApp(String appID, String optionType) {
        if (tokenList != null) {
            Log.i(MainActivity.TAG, "tokenList != null");
            int position = -1;

            for (int i = 0; i < tokenList.size(); i++) {
                if (tokenList.get(i).getAppID().equals(appID)) {
                    position = i;
                    break;
                }
            }
            Log.i(MainActivity.TAG, "position: " + position);

            if (position != -1) {
                viewPager.setCurrentItem(position);
                pagerAdapter.notifyDataSetChanged();

                if (optionType != null) {
                    AppOptionsFragment fragment = optionFragments.get(position);

                    switch (optionType) {
                        case AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS:
                            fragment.showOption(AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS);
                            break;
                    }
                }
            }
        } else {
            appIDToSelect = appID;
            optionTypeToSelect = optionType;
        }
    }

    public void updateAdapter() {
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
        viewPager = (ViewPager) view.findViewById(R.id.fragment_my_apps_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.fragment_my_apps_tabs);
        emptyView = (LinearLayout) view.findViewById(R.id.fragment_my_apps_empty);
        loginView = (Button) view.findViewById(R.id.fragment_my_apps_login);

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
        AppOptionsFragment fragment = optionFragments.get(viewPager.getCurrentItem());
        fragment.resetViews();
    }

    private class AppOptionsFragmentPager extends FragmentPagerAdapter {
        //HashMap<Integer, AppOptionsFragment> fragments = new HashMap<>();

        public AppOptionsFragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tokenList.get(position).getAppName();
        }

        @Override
        public long getItemId(int position) {
            Log.i(MainActivity.TAG, "Pager getItemId " + tokenList.get(position).getUniqueID());
            return tokenList.get(position).getUniqueID();
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(MainActivity.TAG, "Pager getItem position " + position);
            Log.i(MainActivity.TAG, "Pager getItem appID " + tokenList.get(position).getAppID() + " " + tokenList.get(position).getUsername());
            for (AccessTokenModel token : tokenList) {
                Log.i(MainActivity.TAG, "Pager getItem tokenList entry " + token.getAppID() + " " + token.getUserID());

            }

            //AppOptionsFragment fragment = fragments.get(position);
            //if (fragment == null) {
            AppOptionsFragment fragment = new AppOptionsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(AccessTokenModel.EXTRA, tokenList.get(position));
            fragment.setArguments(bundle);
            optionFragments.append(position, fragment);
            //fragments.put(position, fragment);
            //}

            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            AppOptionsFragment fragment = (AppOptionsFragment) object;

            // check if fragment still exists or should be removed
            boolean removeFragment = true;
            Log.i(MainActivity.TAG, "getItemPosition fragment.token: " + fragment.token.getAppID() + " " + fragment.token.getUsername());

            int index = 0;
            for (int i = 0; i < tokenList.size(); i++) {
                AccessTokenModel token = tokenList.get(i);

                Log.i(MainActivity.TAG, "getItemPosition token: " + token.getAppID() + " " + token.getUsername());
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
                    if (result == object) {
                        optionFragments.remove(key);
                    }
                }

                return POSITION_NONE;
            } else {
                return index;
            }

            //return POSITION_NONE;

            /*if (object != null && object instanceof AppOptionsFragment) {
                AppOptionsFragment fragment = (AppOptionsFragment) object;

                int index = tokenList.indexOf(fragment.token);
                Log.i(MainActivity.TAG, "MyAppsFragment index = " + index);

                if (index != -1) {
                    //fragment.update();
                    return POSITION_UNCHANGED;
                } else {
                    return POSITION_NONE;
                }
            } else {
                return super.getItemPosition(object);
            }*/
        }

        @Override
        public int getCount() {
            return tokenList.size();
        }
    }
}
