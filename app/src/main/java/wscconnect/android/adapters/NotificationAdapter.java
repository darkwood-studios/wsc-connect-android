package wscconnect.android.adapters;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.NotificationModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private final AccessTokenModel token;
    private AppActivity activity;
    private List<NotificationModel> notificationList;

    public NotificationAdapter(AppActivity activity, List<NotificationModel> notificationList, AccessTokenModel token) {
        this.activity = activity;
        this.notificationList = notificationList;
        this.token = token;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_notification, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        holder.message.setText(notification.getMessage());
        holder.time.setText(notification.getRelativeTime(activity));
        GlideApp.with(activity).load(notification.getAvatar()).error(R.drawable.ic_person_black_50dp).circleCrop().into(holder.avatar);

        if (!notification.isConfirmed()) {
            holder.message.setTypeface(null, Typeface.BOLD);
        } else {
            holder.message.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message, time;
        public ImageView avatar;

        public MyViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.list_notification_message);
            time = view.findViewById(R.id.list_notification_time);
            avatar = view.findViewById(R.id.list_notification_avatar);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    NotificationModel n = notificationList.get(position);

                    if (!n.isConfirmed()) {
                        n.setConfirmed(true);
                        notifyItemChanged(position);
                        Utils.saveUnreadNotifications(activity, token.getAppID(), Utils.getUnreadNotifications(activity, token.getAppID()) - 1);
                        activity.setCustomTabView();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString(AppWebviewFragment.URL, n.getLink());
                    activity.setCurrentPage(AppActivity.FRAGMENT_WEBVIEW, bundle);
                }
            });
        }
    }
}
