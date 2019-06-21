package wscconnect.android.fragments;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.GlideApp;
import wscconnect.android.KeyUtils;
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
import wscconnect.android.models.LogoutModel;

import static wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment.USER_AGENT;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AppsFragment extends Fragment implements OnBackPressedListener {
    private static final int WEBVIEW_TIMEOUT = 8000;
    private RecyclerView allRecyclerView;
    private MainActivity activity;
    List<AppModel> newestList;
    List<AppModel> topList;
    private List<AppModel> appList;
    private List<AppModel> appListFull;
    private List<AppModel> appListFullCopy;
    private AppAdapter newestListAdapter;
    private AppAdapter topListAdapter;
    private AppAdapter appFullAdapter;
    private AppAdapter appAdapter;
    private ProgressBar topLoadingView;
    private ProgressBar newestLoadingView;
    private ProgressBar allLoadingView;
    private TextView fullListEmpty;
    private Menu menu;
    private ImageView detailsLogo;
    private LinearLayout detailsContainer;
    private AppModel detailApp;
    private boolean webviewFinishedLoading;
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
    private TextView privacy;
    private CheckBox privacyCheckbox;
    private RecyclerView newestRecyclerView;
    private RecyclerView topListRecyclerView;
    private RecyclerView allFullRecyclerView;
    private ScrollView scrollView;
    private Button othersMore;
    private LinearLayout appsContainer;
    private LinearLayout allFullContainer;
    private ArrayList<AppModel> allVisibleApps;
    private boolean isFullAppListVisible = false;

    public AppsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        activity = (MainActivity) getActivity();

        newestList = new ArrayList<>();
        newestListAdapter = new AppAdapter(activity, this, newestList);
        newestRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        newestRecyclerView.setAdapter(newestListAdapter);
        LinearSnapHelper newestLinearSnapHelper = new LinearSnapHelper();
        newestLinearSnapHelper.attachToRecyclerView(newestRecyclerView);

        topList = new ArrayList<>();
        topListAdapter = new AppAdapter(activity, this, topList);
        topListRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        topListRecyclerView.setAdapter(topListAdapter);
        LinearSnapHelper topLinearSnapHelper = new LinearSnapHelper();
        topLinearSnapHelper.attachToRecyclerView(topListRecyclerView);

        appList = new ArrayList<>();
        appListFullCopy = new ArrayList<>();
        appAdapter = new AppAdapter(activity, this, appList);
        allRecyclerView.setLayoutManager(new GridLayoutManager(activity, 2, LinearLayoutManager.HORIZONTAL, false));
        allRecyclerView.setAdapter(appAdapter);

        LinearSnapHelper allLinearSnapHelper = new LinearSnapHelper();
        allLinearSnapHelper.attachToRecyclerView(allRecyclerView);

        appListFull = new ArrayList<>();
        allVisibleApps = new ArrayList<>();
        appFullAdapter = new AppAdapter(activity, this, appListFull, R.layout.list_app_all);
        allFullRecyclerView.setLayoutManager(new GridLayoutManager(activity, 4));
        allFullRecyclerView.setAdapter(appFullAdapter);

        activity.setOnBackPressedListener(this);

        privacy.setMovementMethod(LinkMovementMethod.getInstance());
        privacyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    privacyCheckbox.setError(null);
                }
            }
        });

        othersMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullList(true);
            }
        });

        showProgressBars(true);
        loadApps();
    }

    private void showProgressBars(boolean show) {
        if (show) {
            topLoadingView.setVisibility(View.VISIBLE);
            newestLoadingView.setVisibility(View.VISIBLE);
            allLoadingView.setVisibility(View.VISIBLE);
        } else {
            topLoadingView.setVisibility(View.GONE);
            newestLoadingView.setVisibility(View.GONE);
            allLoadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.apps_fragment, menu);

        this.menu = menu;

        if (detailApp != null) {
            setupToolbar(true, detailApp);
        }

        if (isFullAppListVisible) {
            showFullList(true);
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
        if (!appListFullCopy.isEmpty()) {
            if (query != null) {
                showFullList(true);

                appListFull.clear();
                Iterator<AppModel> i = appListFullCopy.iterator();
                query = query.toLowerCase();
                while (i.hasNext()) {
                    AppModel app = i.next();

                    if (app.getName().toLowerCase().contains(query) || app.getUrl().toLowerCase().contains(query)) {
                        appListFull.add(app);
                    }
                }
                appFullAdapter.notifyDataSetChanged();
                toggleIsEmptyForFullList();
            } else {
                appListFull.clear();
                appListFull.addAll(allVisibleApps);
                appFullAdapter.notifyDataSetChanged();
                toggleIsEmptyForFullList();
            }
        }
    }

    private void toggleIsEmptyForFullList() {
        if (appListFull.isEmpty()) {
            fullListEmpty.setVisibility(View.VISIBLE);
        } else {
            fullListEmpty.setVisibility(View.GONE);
        }
    }

    private void showFullList(boolean show) {
        if (show) {
            isFullAppListVisible = true;
            scrollView.setVisibility(View.GONE);
            allFullContainer.setVisibility(View.VISIBLE);

            // toolbar
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            showSearchIcon(true);
        } else {
            isFullAppListVisible = false;
            allFullContainer.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void loadApps() {
        Utils.getAPI(activity).getMixedApps().enqueue(new RetroCallback<JsonObject>(activity) {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                super.onResponse(call, response);

                showProgressBars(false);

                if (response.isSuccessful()) {
                    newestList.clear();
                    newestList.addAll(AppModel.fromJSONArray(response.body().getAsJsonArray("newest")));
                    newestListAdapter.notifyDataSetChanged();

                    topList.clear();
                    topList.addAll(AppModel.fromJSONArray(response.body().getAsJsonArray("top")));
                    topListAdapter.notifyDataSetChanged();

                    appList.clear();
                    ArrayList<AppModel> allApps = new ArrayList<>(AppModel.fromJSONArray(response.body().getAsJsonArray("all")));
                    for (AppModel app : allApps) {
                        if (app.isVisible()) {
                            allVisibleApps.add(app);
                        }
                    }
                    appList.addAll(allVisibleApps);
                    appAdapter.notifyDataSetChanged();

                    appListFull.clear();
                    appListFull.addAll(allVisibleApps);
                    appListFullCopy.addAll(allApps);
                    appFullAdapter.notifyDataSetChanged();
                    othersMore.setVisibility(View.VISIBLE);
                } else if (response.code() != 501 && response.code() != 502) {
                    RetroCallback.showRequestError(activity);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                super.onFailure(call, t);

                showProgressBars(false);
            }
        });
    }

    public void switchToDetailView(boolean detail, boolean forceLogin, final AppModel app) {
        detailApp = app;

        if (detail) {
            appsContainer.setVisibility(View.GONE);
            detailsContainer.setVisibility(View.VISIBLE);

            if (menu != null) {
                MenuItem actionSearch = menu.findItem(R.id.action_search);
                if (actionSearch != null) {
                    actionSearch.collapseActionView();
                }
            }

            setupToolbar(true, app);
            if (app.isLogoAccessible()) {
                GlideApp.with(activity).load(app.getLogo()).into(detailsLogo);
            } else {
                GlideApp.with(activity).load(R.drawable.logo_off).into(detailsLogo);

            }

            if (app.isLoggedIn(activity) && !forceLogin) {
                String username = Utils.getAccessToken(activity, app.getAppID()).getUsername();
                if (username == null || username.isEmpty()) {
                    username = Utils.getUsername(activity, app.getAppID());
                }

                loggedInAs.setText(getString(R.string.fragment_apps_details_logged_in_as, username));
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

                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(activity, R.string.firebase_token_required, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                final LogoutModel logoutModel = new LogoutModel();
                                logoutModel.setFirebaseToken(task.getResult().getToken());

                                String token = Utils.getAccessTokenString(activity, app.getAppID());
                                Utils.getAPI(activity, token).logout(app.getAppID(), logoutModel).enqueue(new RetroCallback<ResponseBody>(activity) {
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
                        }});
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
            appsContainer.setVisibility(View.VISIBLE);
            setupToolbar(false, null);

            if (isFullAppListVisible) {
                showFullList(true);
            } else {
                showFullList(false);
            }

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
            showSearchIcon(false);
        } else {
            activity.getSupportActionBar().setTitle(R.string.app_name);
            activity.getSupportActionBar().setSubtitle(null);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            showSearchIcon(true);
        }
    }

    private void showSearchIcon(boolean show) {
        if (menu != null) {
            MenuItem actionSearch = menu.findItem(R.id.action_search);
            if (actionSearch != null) {
                actionSearch.setVisible(show);
            }
        }
    }

    public void login(final AppModel app, EditText usernameView, EditText passwordView, final Button submitView, final Button thirdPartySubmitView, final boolean thirdParty) {
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

        if (!privacyCheckbox.isChecked()) {
            privacyCheckbox.setError("");
            return;
        }

        if (!Utils.hasInternetConnection(activity)) {
            Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseInstanceId.getInstance().getInstanceId()
        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                if (!task.isSuccessful()) {
                    Toast.makeText(activity, R.string.firebase_token_required, Toast.LENGTH_LONG).show();
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();

                final Button loadingButton = (thirdParty) ? thirdPartySubmitView : submitView;
                final Button disableButton = (thirdParty) ? submitView : thirdPartySubmitView;
                final ProgressBar progressBar = Utils.showProgressView(activity, loadingButton, android.R.attr.progressBarStyle);
                disableButton.setEnabled(false);

                final KeyPair keyPair = KeyUtils.generateKeyPair();

                final LoginModel loginModel = new LoginModel();
                loginModel.setUsername(username);
                loginModel.setPassword(password);
                loginModel.setFirebaseToken(token);
                loginModel.setThirdPartyLogin(thirdParty);
                loginModel.setDevice(Build.MODEL);
                loginModel.setPublicKey(KeyUtils.getPublicPemKey(keyPair));

                Utils.getAPI(activity).login(app.getAppID(), loginModel).enqueue(new RetroCallback<ResponseBody>(activity) {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        super.onResponse(call, response);

                        if (response.isSuccessful()) {
                            try {
                                JSONObject obj = new JSONObject(response.body().string());
                                final String accessToken = obj.getString("accessToken");
                                final String refreshToken = obj.getString("refreshToken");
                                final String wscConnectToken = obj.getString("wscConnectToken");
                                final String username = obj.optString("username");
                                final String pluginVersion = obj.optString("pluginVersion", "1.0.0");

                                // login successful, save keys
                                KeyUtils.saveKeyPair(app.getAppID(), keyPair, activity);

                                // save plugin version at point of login
                                Utils.saveInstallPluginVersion(app.getAppID(), pluginVersion, activity);

                                // no auto login on thirdparty
                                if (loginModel.isThirdPartyLogin()) {
                                    saveLogin(accessToken, refreshToken, wscConnectToken, username);
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
                                            saveLogin(accessToken, refreshToken, wscConnectToken, username);
                                        }

                                        @Override
                                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                            //Handle the error
                                            super.onReceivedError(view, errorCode, description, failingUrl);

                                            // also redirect on error, user will just not be logged in
                                            hideLoading();
                                            saveLogin(accessToken, refreshToken, wscConnectToken, username);
                                        }

                                        @Override
                                        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                                            super.onReceivedError(view, request, error);

                                            // also redirect on error, user will just not be logged in
                                            hideLoading();
                                            saveLogin(accessToken, refreshToken, wscConnectToken, username);
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
                                                saveLogin(accessToken, refreshToken, wscConnectToken, username);
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

                    private void saveLogin(String accessToken, String refreshToken, String wscConnectToken, String username) {
                        Utils.saveAccessToken(activity, app.getAppID(), accessToken);
                        Utils.saveRefreshToken(activity, app.getAppID(), refreshToken);
                        Utils.saveWscConnectToken(activity, app.getAppID(), wscConnectToken);
                        Utils.saveUsername(activity, app.getAppID(), username);

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
        });
    }

    @Override
    public boolean onBackPressed() {
        if (detailApp != null) {
            switchToDetailView(false, false, null);
            return true;
        } else if (allFullContainer.getVisibility() == View.VISIBLE) {
            showFullList(false);
            return true;
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps, container, false);

        allRecyclerView = view.findViewById(R.id.fragment_apps_all_list);
        newestRecyclerView = view.findViewById(R.id.fragment_apps_newest_list);
        topListRecyclerView = view.findViewById(R.id.fragment_apps_top_list);
        topLoadingView = view.findViewById(R.id.fragment_apps_top_loading);
        newestLoadingView = view.findViewById(R.id.fragment_apps_newest_loading);
        allFullRecyclerView = view.findViewById(R.id.fragment_apps_all_full_list);
        allLoadingView = view.findViewById(R.id.fragment_apps_all_loading);
        fullListEmpty = view.findViewById(R.id.fragment_apps_all_full_list_empty);
        detailsContainer = view.findViewById(R.id.fragment_apps_details);
        appsContainer = view.findViewById(R.id.fragment_apps);
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
        privacy = view.findViewById(R.id.fragment_apps_details_privacy);
        privacyCheckbox = view.findViewById(R.id.fragment_apps_details_privacy_checkbox);
        scrollView = view.findViewById(R.id.fragment_apps_scrollview);
        othersMore = view.findViewById(R.id.fragment_apps_all_more);
        allFullContainer = view.findViewById(R.id.fragment_apps_all_full_list_container);

        return view;
    }

    public void updateAdapter() {
        loadApps();
    }
}
