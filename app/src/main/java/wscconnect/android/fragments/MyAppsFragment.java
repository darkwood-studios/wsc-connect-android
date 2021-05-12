package wscconnect.android.fragments;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.adapters.MyAppsAdapter;
import wscconnect.android.models.AccessTokenModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class MyAppsFragment extends Fragment {
    private MainActivity activity;
    private ArrayList<AccessTokenModel> myApps;
    private LinearLayout emptyView;
    private Button loginView;
    private RecyclerView recyclerView;
    private MyAppsAdapter appAdapter;
    private SwipeRefreshLayout swipeRefresh;

    public MyAppsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (MainActivity) getActivity();
        myApps = new ArrayList<>();

        appAdapter = new MyAppsAdapter(activity, this, myApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(appAdapter);

        updateData();

        loginView.setOnClickListener(view -> activity.setActiveMenuItem(R.id.navigation_apps));

        swipeRefresh.setOnRefreshListener(() -> {
            updateData();
            swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        updateData();
    }

    public void updateData() {
        myApps.clear();
        myApps.addAll(Utils.getAllAccessTokens(activity));

        appAdapter.notifyDataSetChanged();
        setEmptyView();
    }

    private void setEmptyView() {
        if (myApps.isEmpty()) {
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
        emptyView = view.findViewById(R.id.fragment_my_apps_empty);
        loginView = view.findViewById(R.id.fragment_my_apps_login);
        recyclerView = view.findViewById(R.id.fragment_my_apps_list);
        swipeRefresh = view.findViewById(R.id.fragment_my_apps_refresh);

        return view;
    }
}
