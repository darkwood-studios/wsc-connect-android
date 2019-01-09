package wscconnect.android.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.ComparableVersion;
import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.fragments.MyAppsFragment;
import wscconnect.android.models.AccessTokenModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class MyAppsAdapter extends RecyclerView.Adapter<MyAppsAdapter.MyViewHolder> {
    private final MyAppsFragment fragment;
    private MainActivity activity;
    private List<AccessTokenModel> appList;

    public MyAppsAdapter(MainActivity activity, MyAppsFragment fragment, List<AccessTokenModel> appList) {
        this.activity = activity;
        this.appList = appList;
        this.fragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_my_apps, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        AccessTokenModel app = appList.get(position);

        holder.name.setText(app.getAppName());
        String username = app.getUsername();
        if (username == null || username.isEmpty()) {
            username = Utils.getUsername(activity, app.getAppID());
        }
        holder.username.setText(activity.getString(R.string.fragment_my_apps_username, username));

        int unreadNotifications = Utils.getUnreadNotifications(activity, app.getAppID());
        unreadNotifications += Utils.getUnreadConversations(activity, app.getAppID());

        if (unreadNotifications > 0) {
            holder.notifications.setText(String.valueOf(unreadNotifications));
            holder.notifications.setVisibility(View.VISIBLE);
        } else {
            holder.notifications.setVisibility(View.GONE);
        }

        String version = Utils.getInstallPluginVersion(activity, app.getAppID());
        ComparableVersion currentVersion = new ComparableVersion(version);
        ComparableVersion minVersion;

        if (version.startsWith("2.")) {
            minVersion = new ComparableVersion("2.0.10");
        } else {
            minVersion = new ComparableVersion("3.0.11");
        }

        if (currentVersion.compareTo(minVersion) >= 0) {
            holder.secure.setVisibility(View.VISIBLE);
            holder.insecure.setVisibility(View.GONE);
        } else {
            holder.secure.setVisibility(View.GONE);
            holder.insecure.setVisibility(View.VISIBLE);
        }

        GlideApp.with(activity).load(app.getAppLogo()).circleCrop().error(R.drawable.ic_apps_black_50dp).into(holder.logo);

    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, username, notifications;
        public ImageView logo, secure, insecure;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.list_my_apps_name);
            username = view.findViewById(R.id.list_my_apps_username);
            notifications = view.findViewById(R.id.list_my_apps_notifications);
            logo = view.findViewById(R.id.list_my_apps_logo);
            secure = view.findViewById(R.id.list_my_apps_secure);
            insecure = view.findViewById(R.id.list_my_apps_insecure);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AccessTokenModel token = appList.get(getAdapterPosition());
                    Intent appDetail = new Intent(activity, AppActivity.class);
                    appDetail.putExtra(AccessTokenModel.EXTRA, token);
                    activity.startActivity(appDetail);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final AccessTokenModel app = appList.get(getAdapterPosition());
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setItems(R.array.list_my_apps_dialog_items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    final ProgressBar progress = Utils.showProgressView(activity, notifications, android.R.attr.progressBarStyleSmall);
                                    Utils.getAPI(activity, app.getToken()).logout(app.getAppID()).enqueue(new RetroCallback<ResponseBody>(activity) {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            super.onResponse(call, response);

                                            Utils.hideProgressView(notifications, progress, false);

                                            // we ignore errors and just log the user out
                                            Utils.logout(activity, app.getAppID());
                                            fragment.updateData();
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            super.onFailure(call, t);

                                            Utils.hideProgressView(notifications, progress, false);
                                        }
                                    });
                                    break;
                            }
                        }
                    });

                    builder.show();
                    return true;
                }
            });
        }
    }
}
