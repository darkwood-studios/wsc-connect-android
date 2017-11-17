package wscconnect.android.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.adapters.AppAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.AppModel;
import wscconnect.android.models.LoginModel;

import static wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment.USER_AGENT;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppsFragment extends Fragment implements OnBackPressedListener {
    private RecyclerView recyclerView;
    private MainActivity activity;
    private List<AppModel> appList;
    private List<AppModel> originalAppList;
    private SwipeRefreshLayout swipeRefreshView;
    private AppAdapter appAdapter;
    private ProgressBar loadingView;
    private TextView emptyView;
    private Menu menu;
    private ImageView detailsLogo;
    private LinearLayout detailsContainer;
    private AppModel detailApp;
    private boolean webviewFinishedLoading;
    private static final int WEBVIEW_TIMEOUT = 8000;
    private EditText usernameView;
    private EditText passwordView;
    private Button submitView;
    private Button thirdPartySubmitView;
    private ImageView passwordVisibleView;
    private TextView thirdPartyInfoView;
    private LinearLayout detailsContainerLoggedIn;
    private LinearLayout detailsContainerLoggedOut;
    private Button showAccountButton;
    private Button switchAccountButton;
    private Button logoutAccountButton;
    private TextView loggedInAs;

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
        appAdapter = new AppAdapter(activity, this, appList);

        activity.setOnBackPressedListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(activity, 3);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_name:
                sortListByName();
                appAdapter.notifyDataSetChanged();
                this.menu.findItem(R.id.action_sort_name).setVisible(false);
                this.menu.findItem(R.id.action_sort_users).setVisible(true);
                break;
            case R.id.action_sort_users:
                Collections.sort(appList, new Comparator<AppModel>() {
                    @Override
                    public int compare(AppModel a1, AppModel a2) {
                        return a2.getUserCount() - a1.getUserCount();
                    }
                });
                appAdapter.notifyDataSetChanged();
                this.menu.findItem(R.id.action_sort_users).setVisible(false);
                this.menu.findItem(R.id.action_sort_name).setVisible(true);
                break;
            case android.R.id.home:
                switchToDetailView(false, false, null);
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortListByName() {
        sortListByName(null);
    }

    private void sortListByName(List<AppModel> list) {
        Collections.sort((list == null) ? appList : list, new Comparator<AppModel>() {
            @Override
            public int compare(AppModel a1, AppModel a2) {
                String app1Name = a1.getName().replaceAll("[^a-zA-Z]", "").toLowerCase();
                String app2Name = a2.getName().replaceAll("[^a-zA-Z]", "").toLowerCase();

                return app1Name.compareToIgnoreCase(app2Name);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.apps_fragment, menu);

        this.menu = menu;

        if (detailApp != null) {
            setupToolbar(true, detailApp);
        }

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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // set list to default list, in case a search was performed
        if (hidden) {
            performSearch(null);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            activity.setOnBackPressedListener(this);
        }
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
                for (AppModel app : originalAppList) {
                    if (app.isVisible()) {
                        appList.add(app);
                    }
                }
                appAdapter.notifyDataSetChanged();
            }
        }
    }

    private void loadApps() {
        Utils.getAPI(activity).getApps().enqueue(new RetroCallback<List<AppModel>>(activity) {
            @Override
            public void onResponse(Call<List<AppModel>> call, Response<List<AppModel>> response) {
                super.onResponse(call, response);

                loadingView.setVisibility(View.GONE);
                swipeRefreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    appList.clear();
                    for (AppModel app : response.body()) {
                        if (app.isVisible()) {
                            appList.add(app);
                        }
                    }
                    sortListByName();
                    appAdapter.notifyDataSetChanged();
                    originalAppList.clear();
                    originalAppList.addAll(response.body());
                    sortListByName(originalAppList);

                    // update actionbar
                    updateSubtitle();
                } else if (response.code() != 501 && response.code() != 502) {
                    RetroCallback.showRequestError(activity);
                }

                setEmptyView();
            }

            @Override
            public void onFailure(Call<List<AppModel>> call, Throwable t) {
                super.onFailure(call, t);

                swipeRefreshView.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    public void updateSubtitle() {
        if (activity != null && activity.getCurrentFragment() instanceof AppsFragment && appList != null && appList.size() > 0 && isAdded()) {
            int users = 0;

            for (AppModel app : appList) {
                users += app.getUserCount();
            }
            activity.getSupportActionBar().setSubtitle(getResources().getQuantityString(R.plurals.fragment_apps_subtitle_apps, appList.size(), appList.size()) + ", " + getResources().getQuantityString(R.plurals.fragment_apps_subtitle_users, users, users));
        }
    }

    private void setEmptyView() {
        if (appList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    public void switchToDetailView(boolean detail, boolean forceLogin, final AppModel app) {
        detailApp = app;

        if (detail) {
            swipeRefreshView.setVisibility(View.GONE);
            detailsContainer.setVisibility(View.VISIBLE);

            if (menu != null) {
                MenuItem actionSearch = menu.findItem(R.id.action_search);
                if (actionSearch != null) {
                    actionSearch.collapseActionView();
                }
            }

            setupToolbar(true, app);
            GlideApp.with(activity).load(app.getLogo()).into(detailsLogo);

            if (app.isLoggedIn(activity) && !forceLogin) {
                loggedInAs.setText(getString(R.string.fragment_apps_details_logged_in_as, Utils.getAccessToken(activity, app.getAppID()).getUsername()
));
                showAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.setNotificationAppID(app.getAppID());
                        activity.setActiveMenuItem(R.id.navigation_my_apps);
                    }
                });

                switchAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switchToDetailView(true, true, app);
                    }
                });

                logoutAccountButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAccountButton.setEnabled(false);
                        switchAccountButton.setEnabled(false);
                        final ProgressBar progressBar = Utils.showProgressView(activity, logoutAccountButton, android.R.attr.progressBarStyle);
                        String token = Utils.getAccessTokenString(activity, app.getAppID());
                        Utils.getAPI(activity, token).logout(app.getAppID()).enqueue(new RetroCallback<ResponseBody>(activity) {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                super.onResponse(call, response);

                                showAccountButton.setEnabled(true);
                                switchAccountButton.setEnabled(true);
                                Utils.hideProgressView(logoutAccountButton, progressBar);

                                // we ignore errors and just log the user out
                                Utils.logout(activity, app.getAppID());
                                activity.updateMyAppsFragment();
                                switchToDetailView(false, false, null);
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                super.onFailure(call, t);

                                showAccountButton.setEnabled(true);
                                switchAccountButton.setEnabled(true);
                                Utils.hideProgressView(logoutAccountButton, progressBar);
                            }
                        });
                    }
                });
                detailsContainerLoggedOut.setVisibility(View.GONE);
                detailsContainerLoggedIn.setVisibility(View.VISIBLE);
            } else {
                thirdPartyInfoView.setText(activity.getString(R.string.dialog_login_third_party_info, app.getName()));
                passwordVisibleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (passwordView.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                            passwordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            Glide.with(activity).load(R.drawable.ic_visibility_black_36dp).into(passwordVisibleView);
                        } else {
                            passwordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            GlideApp.with(activity).load(R.drawable.ic_visibility_off_black_36dp).into(passwordVisibleView);
                        }

                        passwordView.setSelection(passwordView.length());
                    }
                });

                GlideApp.with(activity).load(R.drawable.ic_visibility_black_36dp).into(passwordVisibleView);

                thirdPartySubmitView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        login(app, usernameView, passwordView, submitView, thirdPartySubmitView, true);
                    }
                });

                submitView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        login(app, usernameView, passwordView, submitView, thirdPartySubmitView, false);
                    }
                });

                detailsContainerLoggedIn.setVisibility(View.GONE);
                detailsContainerLoggedOut.setVisibility(View.VISIBLE);
            }
        } else {
            detailsContainer.setVisibility(View.GONE);
            swipeRefreshView.setVisibility(View.VISIBLE);
            setupToolbar(false, null);
            updateSubtitle();
            Utils.hideKeyboard(activity);

            // reset edittext values
            usernameView.setText("");
            usernameView.setError(null);
            passwordView.setText("");
            passwordView.setError(null);
        }
    }

    private void setupToolbar(boolean detail, AppModel app) {
        if (detail) {
            activity.getSupportActionBar().setTitle(app.getName());
            activity.getSupportActionBar().setSubtitle(app.getUrl());
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (menu != null) {
                MenuItem actionSort = menu.findItem(R.id.action_sort);
                if (actionSort != null) {
                    actionSort.setVisible(false);
                }
                MenuItem actionSearch = menu.findItem(R.id.action_search);
                if (actionSearch != null) {
                    actionSearch.setVisible(false);
                }
            }
        } else {
            activity.getSupportActionBar().setTitle(R.string.app_name);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (menu != null) {
                MenuItem actionSort = menu.findItem(R.id.action_sort);
                if (actionSort != null) {
                    actionSort.setVisible(true);
                }
                MenuItem actionSearch = menu.findItem(R.id.action_search);
                if (actionSearch != null) {
                    actionSearch.setVisible(true);
                }
            }
        }
    }

    public void login(final AppModel app, EditText usernameView, EditText passwordView, final Button submitView, Button thirdPartySubmitView, boolean thirdParty) {
        final String username = usernameView.getText().toString().trim();
        final String password = passwordView.getText().toString().trim();

        usernameView.setError(null);
        passwordView.setError(null);

        if (username.isEmpty()) {
            usernameView.setError(activity.getString(R.string.required));
            return;
        }

        if (password.isEmpty()) {
            passwordView.setError(activity.getString(R.string.required));
            return;
        }

        if (!Utils.hasInternetConnection(activity)) {
            Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return;
        }

        if (FirebaseInstanceId.getInstance().getToken() == null) {
            Toast.makeText(activity, R.string.firebase_token_required, Toast.LENGTH_LONG).show();
            return;
        }

        final Button loadingButton = (thirdParty) ? thirdPartySubmitView : submitView;
        final Button disableButton = (thirdParty) ? submitView : thirdPartySubmitView;
        final ProgressBar progressBar = Utils.showProgressView(activity, loadingButton, android.R.attr.progressBarStyle);
        disableButton.setEnabled(false);

        Log.i(MainActivity.TAG, "starting login");
        final LoginModel loginModel = new LoginModel();
        loginModel.setUsername(username);
        loginModel.setPassword(password);
        loginModel.setFirebaseToken(FirebaseInstanceId.getInstance().getToken());
        loginModel.setThirdPartyLogin(thirdParty);
        loginModel.setDevice(Build.MODEL);

        Utils.getAPI(activity).login(app.getAppID(), loginModel).enqueue(new RetroCallback<ResponseBody>(activity) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);

                Log.i(MainActivity.TAG, "onResponse " + response.code());
                    Log.i(MainActivity.TAG, "onResponse " + response.raw().message());

                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        final String accessToken = obj.getString("accessToken");
                        final String refreshToken = obj.getString("refreshToken");
                        final String wscConnectToken = obj.getString("wscConnectToken");

                        // no auto login on thirdparty
                        if (loginModel.isThirdPartyLogin()) {
                            saveLogin(accessToken, refreshToken);
                        } else {
                            final WebView webview = new WebView(activity);
                            final WebSettings webSettings = webview.getSettings();
                            webSettings.setUserAgentString(USER_AGENT);
                            final String postData = "type=loginCookie&username=" + URLEncoder.encode(loginModel.getUsername(), "UTF-8") + "&password=" + URLEncoder.encode(loginModel.getPassword(), "UTF-8") + "&wscConnectToken=" + URLEncoder.encode(wscConnectToken, "UTF-8");

                            webview.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);

                                    hideLoading();
                                    saveLogin(accessToken, refreshToken);
                                }

                                @Override
                                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                    //Handle the error
                                    super.onReceivedError(view, errorCode, description, failingUrl);

                                    // also redirect on error, user will just not be logged in
                                    hideLoading();
                                    saveLogin(accessToken, refreshToken);
                                }

                                @Override
                                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                                    super.onReceivedError(view, request, error);

                                    // also redirect on error, user will just not be logged in
                                    hideLoading();
                                    saveLogin(accessToken, refreshToken);
                                }
                            });
                            webview.postUrl(app.getApiUrl(), postData.getBytes());

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // in case the webview login is not finished after the timeout, cancel it manually
                                    if (!webviewFinishedLoading) {
                                        if (webview != null) {
                                            webview.stopLoading();
                                        }
                                        webviewFinishedLoading = true;
                                        saveLogin(accessToken, refreshToken);
                                    }
                                }
                            }, WEBVIEW_TIMEOUT);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    Utils.hideProgressView(loadingButton, progressBar);
                    disableButton.setEnabled(true);
                    Toast.makeText(activity, R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    hideLoading();
                    Toast.makeText(activity, R.string.login_failed_global, Toast.LENGTH_SHORT).show();
                }
            }

            private void saveLogin(String accessToken, String refreshToken) {
                Utils.saveAccessToken(activity, app.getAppID(), accessToken);
                Utils.saveRefreshToken(activity, app.getAppID(), refreshToken);

                activity.setNotificationAppID(app.getAppID());
                activity.updateMyAppsFragment();

                webviewFinishedLoading = true;

                Utils.hideKeyboard(activity);

                Intent appDetail = new Intent(activity, AppActivity.class);
                appDetail.putExtra(AccessTokenModel.EXTRA, Utils.getAccessToken(activity, app.getAppID()));

                switchToDetailView(false, false, null);
                activity.startActivity(appDetail);
            }

            private void hideLoading() {
                Utils.hideProgressView(loadingButton, progressBar);
                disableButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideLoading();
                Toast.makeText(activity, R.string.login_failed_global, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if (detailApp != null) {
            switchToDetailView(false, false, null);
            return true;
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);

        recyclerView = view.findViewById(R.id.fragment_apps_list);
        swipeRefreshView = view.findViewById(R.id.fragment_apps_refresh);
        loadingView = view.findViewById(R.id.fragment_apps_loading);
        emptyView = view.findViewById(R.id.fragment_apps_empty);
        detailsContainer = view.findViewById(R.id.fragment_apps_details);
        detailsLogo = view.findViewById(R.id.fragment_apps_details_logo);
        usernameView = view.findViewById(R.id.fragment_apps_details_username);
        passwordView = view.findViewById(R.id.fragment_apps_details_password);
        submitView = view.findViewById(R.id.fragment_apps_details_login_submit);
        thirdPartySubmitView = view.findViewById(R.id.fragment_apps_details_login_third_party_submit);
        passwordVisibleView = view.findViewById(R.id.fragment_apps_details_password_visible);
        thirdPartyInfoView = view.findViewById(R.id.fragment_apps_details_login_third_party_info);
        detailsContainerLoggedOut = view.findViewById(R.id.fragment_apps_details_root);
        detailsContainerLoggedIn = view.findViewById(R.id.fragment_apps_details_root_logged_in);
        showAccountButton = view.findViewById(R.id.fragment_apps_details_logged_in_show);
        switchAccountButton = view.findViewById(R.id.fragment_apps_details_logged_in_switch);
        logoutAccountButton = view.findViewById(R.id.fragment_apps_details_logged_in_logout);
        loggedInAs = view.findViewById(R.id.fragment_apps_details_logged_in_as);

        return view;
    }

    public void updateAdapter() {
        loadApps();
    }
}
