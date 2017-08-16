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
    public static final String EXTRA_LOAD_DATA = "extraLoadData";
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

        if (!option.getType().equals(AppOptionsFragment.OPTION_TYPE_WEBVIEW)) {
            holder.refresh.setVisibility(View.VISIBLE);
        } else {
            holder.refresh.setVisibility(View.GONE);
        }

        holder.frameView.setVisibility(View.GONE);
        holder.title.setText(option.getTitle());
        if (option.getIconUrl() != null) {
            GlideApp.with(activity).load(option.getIconUrl()).error(option.getIcon()).circleCrop().into(holder.icon);
        } else {
            holder.icon.setImageResource(option.getIcon());
        }
        holder.more.setImageResource(option.getMoreIcon());

        if (option.getType().equals(AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS)) {
            int unreadNotifications = Utils.getUnreadNotifications(activity, token.getAppID());
            if (unreadNotifications > 0) {
                holder.unreadNotifications.setText(String.valueOf(unreadNotifications));
                holder.unreadNotifications.setVisibility(View.VISIBLE);
            } else {
                holder.unreadNotifications.setVisibility(View.GONE);
            }
        }
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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView more, icon, refresh;
        public RelativeLayout listView;
        public TextView title, unreadNotifications;
        public FrameLayout frameView;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.list_app_option_title);
            unreadNotifications = view.findViewById(R.id.list_app_option_unread_notifications);
            icon = view.findViewById(R.id.list_app_option_icon);
            more = view.findViewById(R.id.list_app_option_more);
            refresh = view.findViewById(R.id.list_app_option_refresh);
            frameView = view.findViewById(R.id.list_app_option_frame);
            listView = view.findViewById(R.id.list_app_option_list);

            refresh.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.list_app_option_refresh:
                    refreshClicked();
                    break;
                case R.id.list_app_option:
                    rowClicked();
                    break;
            }
        }

        public void setUnreadNotifications() {
            AppOptionModel option = optionsList.get(getAdapterPosition());
            if (token != null && option != null && option.getType().equals(AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS)) {
                int notifications = Utils.getUnreadNotifications(activity, token.getAppID());
                if (notifications > 0) {
                    unreadNotifications.setText(String.valueOf(notifications));
                    unreadNotifications.setVisibility(View.VISIBLE);
                } else {
                    unreadNotifications.setVisibility(View.GONE);
                }
            }
        }

        public void refreshClicked() {
            AppOptionModel option = optionsList.get(getAdapterPosition());
            String tag = option.getType();
            FragmentManager fManager = fragment.getChildFragmentManager();
            final boolean refreshVisible = (refresh.getVisibility() == View.VISIBLE);
            ProgressBar progressBar = null;

            switch (option.getType()) {
                case AppOptionsFragment.OPTION_TYPE_MESSAGES:
                    AppMessagesFragment messagesFragment = (AppMessagesFragment) fManager.findFragmentByTag(tag);
                    if (messagesFragment == null) {
                        loadDropdownFragment(false);
                        messagesFragment = (AppMessagesFragment) fManager.findFragmentByTag(tag);
                    }

                    if (refreshVisible) {
                        progressBar = Utils.showProgressView(activity, refresh, android.R.attr.progressBarStyleSmall);
                    }
                    final ProgressBar finalProgressBar = progressBar;
                    messagesFragment.loadMessages(new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            Utils.hideProgressView(refresh, finalProgressBar);
                        }
                    });
                    break;
                case AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS:
                    AppNotificationsFragment notificationsFragment = (AppNotificationsFragment) fManager.findFragmentByTag(tag);
                    if (notificationsFragment == null) {
                        loadDropdownFragment(false);
                        notificationsFragment = (AppNotificationsFragment) fManager.findFragmentByTag(tag);
                    }

                    if (refreshVisible) {
                        progressBar = Utils.showProgressView(activity, refresh, android.R.attr.progressBarStyleSmall);
                    }
                    final ProgressBar finalProgressBar1 = progressBar;
                    notificationsFragment.loadNotifications(new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            Utils.hideProgressView(refresh, finalProgressBar1);
                        }
                    });
                    break;
            }
        }

        public void loadDropdownFragment(boolean loadData) {
            Log.i("logaAdrqw", "1");
            String optionType = optionsList.get(getAdapterPosition()).getType();

            int randomId = View.generateViewId();
            FragmentManager fManager = fragment.getChildFragmentManager();
            frameView.setId(randomId);
            Log.i("logaAdrqw", "2");

            Fragment newFragment = fManager.findFragmentByTag(optionType);
            Log.i("logaAdrqw", "3");
            if (newFragment != null) {
                if (optionType.equals(AppOptionsFragment.OPTION_TYPE_MESSAGES)) {
                    Log.i("logaAdrqw", "4");
                    ((AppMessagesFragment) newFragment).setToken(token);
                } else {
                    Log.i("logaAdrqw", "5");
                    ((AppNotificationsFragment) newFragment).setToken(token);
                }
                Log.i("logaAdrqw", "6");
                fManager.beginTransaction().show(newFragment).commitNow();
            } else {
                if (optionType.equals(AppOptionsFragment.OPTION_TYPE_MESSAGES)) {
                    Log.i("logaAdrqw", "7");
                    newFragment = new AppMessagesFragment();
                } else {
                    Log.i("logaAdrqw", "8");
                    newFragment = new AppNotificationsFragment();
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(AccessTokenModel.EXTRA, token);
                bundle.putBoolean(EXTRA_LOAD_DATA, loadData);
                newFragment.setArguments(bundle);
                Log.i("logaAdrqw", "9 frameView.getId() " + frameView.getId());
                fManager.beginTransaction().replace(frameView.getId(), newFragment, optionType).commitNow();
                Log.i("logaAdrqw", "10");
            }
            Log.i("logaAdrqw", "11");
        }

        private void rowClicked() {
            AppOptionModel option = optionsList.get(getAdapterPosition());

            switch (option.getType()) {
                case AppOptionsFragment.OPTION_TYPE_WEBVIEW:
                    // webview is opened fullsize
                    fragment.showOption(AppOptionsFragment.OPTION_TYPE_WEBVIEW, null);
                    break;
                case AppOptionsFragment.OPTION_TYPE_MESSAGES:
                case AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS:
                    if (frameView.getVisibility() != View.VISIBLE) {
                        more.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        frameView.setVisibility(View.VISIBLE);
                        if (refresh.getVisibility() != View.GONE) {
                            loadDropdownFragment(true);
                        }
                    } else {
                        frameView.setVisibility(View.GONE);
                        more.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    }
                    break;
            }
        }
    }
}