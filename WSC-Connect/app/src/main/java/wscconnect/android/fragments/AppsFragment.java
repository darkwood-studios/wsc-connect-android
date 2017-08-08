package wscconnect.android.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.adapters.AppAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.models.AppModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppsFragment extends Fragment {
    private RecyclerView recyclerView;
    private MainActivity activity;
    private List<AppModel> appList;
    private List<AppModel> originalAppList;
    private SwipeRefreshLayout swipeRefreshView;
    private AppAdapter appAdapter;
    private ProgressBar loadingView;
    private TextView emptyView;

    public AppsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        activity = (MainActivity) getActivity();
        appList = new ArrayList<>();
        originalAppList = new ArrayList<>();
        appAdapter = new AppAdapter(activity, appList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(appAdapter);

        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadApps();
            }
        });

        loadingView.setVisibility(View.VISIBLE);
        loadApps();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.apps_search, menu);

        MenuItem menuSearch = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.apps_search));
        EditText searchTextView = searchView.findViewById(R.id.search_src_text);
        searchTextView.setTextColor(Color.WHITE);
        searchTextView.setHintTextColor(ContextCompat.getColor(activity, android.R.color.white));

        MenuItemCompat.setOnActionExpandListener(menuSearch, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                performSearch(null);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s != null && s.length() >= 1) {
                    performSearch(s);
                }

                return false;
            }
        });
    }

    private void performSearch(String query) {
        appList.clear();
        appList.addAll(originalAppList);
        appAdapter.notifyDataSetChanged();

        if (!appList.isEmpty()) {
            if (query != null) {
                Iterator<AppModel> i = appList.iterator();
                query = query.toLowerCase();
                while (i.hasNext()) {
                    AppModel app = i.next();

                    if (!app.getName().toLowerCase().contains(query) && !app.getUrl().toLowerCase().contains(query)) {
                        i.remove();
                    }
                }
                appAdapter.notifyDataSetChanged();

                setEmptyView();
            } else {
                appList.clear();
                appList.addAll(originalAppList);
                appAdapter.notifyDataSetChanged();
            }
        }
    }

    private void loadApps() {
        activity.getAPI().getApps().enqueue(new RetroCallback<List<AppModel>>(activity) {
            @Override
            public void onResponse(Call<List<AppModel>> call, Response<List<AppModel>> response) {
                super.onResponse(call, response);

                loadingView.setVisibility(View.GONE);
                swipeRefreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    appList.clear();
                    appList.addAll(response.body());
                    appAdapter.notifyDataSetChanged();
                    originalAppList.clear();
                    originalAppList.addAll(response.body());
                } else if (response.code() != 501 && response.code() != 502) {
                    Toast.makeText(activity, R.string.error_general, Toast.LENGTH_SHORT).show();
                }

                setEmptyView();
            }

            @Override
            public void onFailure(Call<List<AppModel>> call, Throwable t) {
                super.onFailure(call, t);

                Log.i(MainActivity.TAG, t.getMessage());

                swipeRefreshView.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    private void setEmptyView() {
        if (appList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);

        recyclerView = view.findViewById(R.id.fragment_apps_list);
        swipeRefreshView = view.findViewById(R.id.fragment_apps_refresh);
        loadingView = view.findViewById(R.id.fragment_apps_loading);
        emptyView = view.findViewById(R.id.fragment_apps_empty);

        return view;
    }

    public void updateAdapter() {
        loadApps();
    }
}
