package wscconnect.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.fragments.AppsFragment;
import wscconnect.android.models.AppModel;

/**
 * Created by chris on 18.07.17.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder> {
    private final AppsFragment fragment;
    private MainActivity activity;
    private List<AppModel> appList;

    public AppAdapter(MainActivity activity, AppsFragment fragment, List<AppModel> appList) {
        this.activity = activity;
        this.appList = appList;
        this.fragment = fragment;
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

        URI uri = null;
        try {
            uri = new URI(app.getUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (uri != null) {
            holder.url.setText(uri.getHost().replace("www.", ""));
        } else {
            holder.url.setText(app.getUrl());
        }

        GlideApp.with(activity).load(app.getLogo()).error(R.drawable.ic_apps_black_24dp).into(holder.logo);
        holder.users.setText(String.valueOf(app.getUserCount()));

        int unreadNotifications = Utils.getUnreadNotifications(activity, app.getAppID());
        if (unreadNotifications > 0) {
            holder.unreadNotifications.setText(String.valueOf(unreadNotifications));
            holder.unreadNotificationsContainer.setVisibility(View.VISIBLE);
        } else {
            holder.unreadNotificationsContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout unreadNotificationsContainer;
        public LinearLayout usersContainer;
        public TextView name, url, users, unreadNotifications;
        public ImageView logo;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.list_app_name);
            url = view.findViewById(R.id.list_app_url);
            users = view.findViewById(R.id.list_app_users);
            usersContainer = view.findViewById(R.id.list_app_users_container);
            unreadNotifications = view.findViewById(R.id.list_app_unread_notifications);
            unreadNotificationsContainer = view.findViewById(R.id.list_app_unread_notifications_container);
            logo = view.findViewById(R.id.list_app_logo);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AppModel app = appList.get(getAdapterPosition());

                    fragment.switchToDetailView(true, false, app);
                }
            });
        }
    }
}
