package wscconnect.android.adapters;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

        builder.setTitle(activity.getString(R.string.dialog_login_title, app.getUrl()));
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        final EditText usernameView = (EditText) dialogView.findViewById(R.id.dialog_login_username);
        final EditText passwordView = (EditText) dialogView.findViewById(R.id.dialog_login_password);
        final Button submitView = (Button) dialogView.findViewById(R.id.dialog_login_submit);
        final Button thirdPartySubmitView = (Button) dialogView.findViewById(R.id.dialog_login_third_party_submit);
        final ImageView passwordVisibleView = (ImageView) dialogView.findViewById(R.id.dialog_login_password_visible);
        TextView thirdPartyInfoView = (TextView) dialogView.findViewById(R.id.dialog_login_third_party_info);

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
        activity.getAPI().login(app.getAppID(), new LoginModel(username, password, FirebaseInstanceId.getInstance().getToken(), thirdParty)).enqueue(new RetroCallback<ResponseBody>(activity) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);
                Log.i(MainActivity.TAG, "onResponse " + response.code());

                Utils.hideProgressView(loadingButton, progressBar);
                disableButton.setEnabled(true);

                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        String accessToken = obj.getString("accessToken");
                        String refreshToken = obj.getString("refreshToken");

                        Utils.saveAccessToken(activity, app.getAppID(), accessToken);
                        Utils.saveRefreshToken(activity, app.getAppID(), refreshToken);

                        notifyItemChanged(position);
                        dialog.dismiss();

                        activity.setNotificationAppID(app.getAppID());
                        activity.updateMyAppsFragment();
                        activity.setActiveMenuItem(R.id.navigation_my_apps);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(activity, R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, R.string.login_failed_global, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(MainActivity.TAG, "onFailure " + t.getMessage());
                Utils.hideProgressView(loadingButton, progressBar);
                disableButton.setEnabled(true);
                Toast.makeText(activity, R.string.login_failed_global, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, url, loggedIn, unreadNotifications;
        public ImageView logo;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.list_app_name);
            url = (TextView) view.findViewById(R.id.list_app_url);
            loggedIn = (TextView) view.findViewById(R.id.list_app_loggedIn);
            unreadNotifications = (TextView) view.findViewById(R.id.list_app_unread_notifications);
            logo = (ImageView) view.findViewById(R.id.list_app_logo);

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
