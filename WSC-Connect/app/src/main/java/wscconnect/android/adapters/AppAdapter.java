package wscconnect.android.adapters;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.AppModel;
import wscconnect.android.models.LoginModel;

import static wscconnect.android.Utils.getAccessToken;
import static wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment.USER_AGENT;

/**
 * Created by chris on 18.07.17.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder> {
    private MainActivity activity;
    private List<AppModel> appList;

    public AppAdapter(MainActivity activity, List<AppModel> appList) {
        this.activity = activity;
        this.appList = appList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_app, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        AppModel app = appList.get(position);

        holder.name.setText(app.getName());
        holder.url.setText(app.getUrl());
        GlideApp.with(activity).load(app.getLogo()).circleCrop().error(R.drawable.ic_apps_black_24dp).into(holder.logo);
        if (app.isLoggedIn(activity)) {
            AccessTokenModel token = getAccessToken(activity, app.getAppID());
            holder.loggedIn.setText(activity.getString(R.string.list_app_loggedIn, token.getUsername()));
            holder.loggedIn.setVisibility(View.VISIBLE);
        } else {
            holder.loggedIn.setVisibility(View.GONE);
        }

        int unreadNotifications = Utils.getUnreadNotifications(activity, app.getAppID());
        if (unreadNotifications > 0) {
            holder.unreadNotifications.setText(String.valueOf(unreadNotifications));
            holder.unreadNotifications.setVisibility(View.VISIBLE);
        } else {
            holder.unreadNotifications.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    private void logout(final AppModel app, final TextView loggedIn, final int position) {
        final ProgressBar progressBar = Utils.showProgressView(activity, loggedIn, android.R.attr.progressBarStyleSmall);
        String token = Utils.getAccessTokenString(activity, app.getAppID());
        activity.getAPI(token).logout(app.getAppID()).enqueue(new RetroCallback<ResponseBody>(activity) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);

                Utils.hideProgressView(loggedIn, progressBar);

                // we ignore errors and just log the user out
                Utils.logout(activity, app.getAppID());
                notifyItemChanged(position);
                activity.updateMyAppsFragment();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);

                Utils.hideProgressView(loggedIn, progressBar);
            }
        });
    }

    private void showLoginDialog(final AppModel app, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_login, null);

        builder.setTitle(activity.getString(R.string.dialog_login_title, app.getName()));
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        final EditText usernameView = dialogView.findViewById(R.id.dialog_login_username);
        final EditText passwordView = dialogView.findViewById(R.id.dialog_login_password);
        final Button submitView = dialogView.findViewById(R.id.dialog_login_submit);
        final Button thirdPartySubmitView = dialogView.findViewById(R.id.dialog_login_third_party_submit);
        final ImageView passwordVisibleView = dialogView.findViewById(R.id.dialog_login_password_visible);
        TextView thirdPartyInfoView = dialogView.findViewById(R.id.dialog_login_third_party_info);

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
                login(app, position, usernameView, passwordView, submitView, thirdPartySubmitView, dialog, true);
            }
        });

        submitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(app, position, usernameView, passwordView, submitView, thirdPartySubmitView, dialog, false);
            }
        });

        dialog.show();
    }

    public void login(final AppModel app, final int position, EditText usernameView, EditText passwordView, final Button submitView, Button thirdPartySubmitView, final Dialog dialog, boolean thirdParty) {
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

        activity.getAPI().login(app.getAppID(), loginModel).enqueue(new RetroCallback<ResponseBody>(activity) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);
                Log.i(MainActivity.TAG, "onResponse " + response.code());

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
                            WebView webview = new WebView(activity);
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

                notifyItemChanged(position);
                dialog.dismiss();

                activity.setNotificationAppID(app.getAppID());
                activity.updateMyAppsFragment();
                activity.setActiveMenuItem(R.id.navigation_my_apps);
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


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, url, loggedIn, unreadNotifications;
        public ImageView logo;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.list_app_name);
            url = view.findViewById(R.id.list_app_url);
            loggedIn = view.findViewById(R.id.list_app_loggedIn);
            unreadNotifications = view.findViewById(R.id.list_app_unread_notifications);
            logo = view.findViewById(R.id.list_app_logo);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AppModel app = appList.get(getAdapterPosition());

                    if (app.isLoggedIn(activity)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setItems(R.array.list_app_dialog_items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        showLoginDialog(app, getAdapterPosition());
                                        break;
                                    case 1:
                                        logout(app, loggedIn, getAdapterPosition());
                                        break;
                                }
                            }
                        });
                        builder.show();
                    } else {
                        showLoginDialog(app, getAdapterPosition());
                    }
                }
            });
        }
    }
}
