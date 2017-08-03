package wscconnect.android.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.fragments.myApps.AppOptionsFragment;
import wscconnect.android.fragments.myApps.appOptions.AppMessagesFragment;
import wscconnect.android.fragments.myApps.appOptions.AppNotificationsFragment;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.AppOptionModel;

/**
 * Created by chris on 18.07.17.
 */

public class AppOptionAdapter extends RecyclerView.Adapter<AppOptionAdapter.MyViewHolder> {
    private final AppOptionsFragment fragment;
    private final AccessTokenModel token;
    private MainActivity activity;
    private List<AppOptionModel> optionsList;

    public AppOptionAdapter(MainActivity activity, List<AppOptionModel> optionsList, AccessTokenModel token, AppOptionsFragment fragment) {
        this.activity = activity;
        this.optionsList = optionsList;
        this.fragment = fragment;
        this.token = token;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_app_option, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final AppOptionModel option = optionsList.get(position);

        holder.frameView.setVisibility(View.GONE);
        holder.refresh.setVisibility(View.GONE);
        holder.title.setText(option.getTitle());
        if (option.getIconUrl() != null) {
            GlideApp.with(activity).load(option.getIconUrl()).error(option.getIcon()).circleCrop().into(holder.icon);
        } else {
            holder.icon.setBackgroundResource(option.getIcon());
        }
        holder.more.setImageResource(option.getMoreIcon());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = option.getType();
                int randomId = View.generateViewId();
                FragmentManager fManager = fragment.getChildFragmentManager();

                switch (option.getType()) {
                    case AppOptionsFragment.OPTION_TYPE_WEBVIEW:
                        // webview is opened fullsize
                        fragment.showOption(AppOptionsFragment.OPTION_TYPE_WEBVIEW, null);
                        break;
                    case AppOptionsFragment.OPTION_TYPE_MESSAGES:
                    case AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS:
                        if (holder.frameView.getVisibility() != View.VISIBLE) {
                            holder.frameView.setVisibility(View.VISIBLE);
                            holder.refresh.setVisibility(View.VISIBLE);
                            holder.more.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                            holder.frameView.setId(randomId);

                            Fragment newFragment = fManager.findFragmentByTag(tag);
                            if (newFragment != null) {
                                if (option.getType().equals(AppOptionsFragment.OPTION_TYPE_MESSAGES)) {
                                    ((AppMessagesFragment) newFragment).setToken(token);
                                } else {
                                    ((AppNotificationsFragment) newFragment).setToken(token);
                                }
                                fManager.beginTransaction().show(newFragment).commitNow();
                            } else {
                                Log.i(MainActivity.TAG, "AppOptionAdapter newFragment == null");
                                if (option.getType().equals(AppOptionsFragment.OPTION_TYPE_MESSAGES)) {
                                    Log.i(MainActivity.TAG, "AppOptionAdapter new AppMessagesFragment()");
                                    newFragment = new AppMessagesFragment();
                                } else {
                                    newFragment = new AppNotificationsFragment();
                                }
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(AccessTokenModel.EXTRA, token);
                                newFragment.setArguments(bundle);
                                fManager.beginTransaction().replace(holder.frameView.getId(), newFragment, tag).commitNow();
                            }
                        } else {
                            holder.frameView.setVisibility(View.GONE);
                            holder.refresh.setVisibility(View.GONE);
                            holder.more.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        }
                        break;
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }

    public void removeFragments() {
        FragmentManager fManager = fragment.getChildFragmentManager();

        Fragment f = fManager.findFragmentByTag(AppOptionsFragment.OPTION_TYPE_MESSAGES);
        if (f != null) {
            fManager.beginTransaction().remove(f).commitNow();
        }

        f = fManager.findFragmentByTag(AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS);
        if (f != null) {
            fManager.beginTransaction().remove(f).commitNow();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView more;
        private final ImageView refresh;
        public RelativeLayout listView;
        public TextView title;
        public ImageView icon;
        public FrameLayout frameView;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.list_app_option_title);
            icon = (ImageView) view.findViewById(R.id.list_app_option_icon);
            more = (ImageView) view.findViewById(R.id.list_app_option_more);
            refresh = (ImageView) view.findViewById(R.id.list_app_option_refresh);
            frameView = (FrameLayout) view.findViewById(R.id.list_app_option_frame);
            listView = (RelativeLayout) view.findViewById(R.id.list_app_option_list);

            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppOptionModel model = optionsList.get(getAdapterPosition());
                    String tag = model.getType();
                    FragmentManager fManager = fragment.getChildFragmentManager();

                    final ProgressBar progressBar = Utils.showProgressView(activity, refresh, android.R.attr.progressBarStyleSmall);

                    switch (model.getType()) {
                        case AppOptionsFragment.OPTION_TYPE_MESSAGES:
                            Log.i(MainActivity.TAG, "AppOptionAdapter refresh clicked");
                            AppMessagesFragment messagesFragment = (AppMessagesFragment) fManager.findFragmentByTag(tag);
                            if (messagesFragment != null) {
                                messagesFragment.loadMessages(new SimpleCallback() {
                                    @Override
                                    public void onReady(boolean success) {
                                        Utils.hideProgressView(refresh, progressBar, frameView.getVisibility() == View.VISIBLE);
                                    }
                                });
                            }
                            break;
                        case AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS:
                            AppNotificationsFragment notificationsFragment = (AppNotificationsFragment) fManager.findFragmentByTag(tag);
                            if (notificationsFragment != null) {
                                notificationsFragment.loadNotifications(new SimpleCallback() {
                                    @Override
                                    public void onReady(boolean success) {
                                        Utils.hideProgressView(refresh, progressBar, frameView.getVisibility() == View.VISIBLE);
                                    }
                                });
                            }
                            break;
                    }
                }
            });

        }
    }
}