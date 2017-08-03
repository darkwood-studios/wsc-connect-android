package wscconnect.android.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.fragments.myApps.AppOptionsFragment;
import wscconnect.android.models.NotificationModel;

/**
 * Created by chris on 18.07.17.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private final AppOptionsFragment fragment;
    private MainActivity activity;
    private List<NotificationModel> notificationList;

    public NotificationAdapter(MainActivity activity, List<NotificationModel> notificationList, AppOptionsFragment fragment) {
        this.activity = activity;
        this.notificationList = notificationList;
        this.fragment = fragment;
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
        GlideApp.with(activity).load(notification.getLogo()).error(R.drawable.ic_person_black_50dp).circleCrop().into(holder.avatar);

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
            message = (TextView) view.findViewById(R.id.list_notification_message);
            time = (TextView) view.findViewById(R.id.list_notification_time);
            avatar = (ImageView) view.findViewById(R.id.list_notification_avatar);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NotificationModel n = notificationList.get(getAdapterPosition());
                    fragment.showOption(AppOptionsFragment.OPTION_TYPE_WEBVIEW, n.getLink());
                }
            });
        }
    }
}
