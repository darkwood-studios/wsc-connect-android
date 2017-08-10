package wscconnect.android.fragments.myApps.appOptions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.adapters.AppOptionAdapter;
import wscconnect.android.adapters.NotificationAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.fragments.myApps.AppOptionsFragment;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.NotificationModel;

import static android.view.View.GONE;

/**
 * Created by chris on 18.07.17.
 */

public class AppNotificationsFragment extends Fragment {
    private MainActivity activity;
    private AccessTokenModel token;
    private RecyclerView recyclerView;
    private List<NotificationModel> notificationList;
    private NotificationAdapter notificationAdapter;
    private LinearLayout loadingView;
    private TextView loadingTextView;
    private TextView emptyView;
    private Call<List<NotificationModel>> apiCall;
    private boolean loadData;

    public AppNotificationsFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        token = getArguments().getParcelable(AccessTokenModel.EXTRA);
        loadData = getArguments().getBoolean(AppOptionAdapter.EXTRA_LOAD_DATA, true);

        activity = (MainActivity) getActivity();
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(activity, notificationList, token, (AppOptionsFragment) getParentFragment());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(notificationAdapter);

        if (loadData) {
            loadingTextView.setText(getString(R.string.fragment_app_notifications_loading_info, token.getAppName()));
            loadNotifications(null);
        }
    }

    public void setToken(AccessTokenModel token) {
        this.token = token;
    }

    public void loadNotifications(final SimpleCallback callback) {
        token = Utils.getAccessToken(activity, token.getAppID());
        if (token == null) {
            setEmptyView();
            return;
        }
        Log.i("noqwe", "loadingNotificaions " + token.getAppName());

        if (callback == null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        final AccessTokenModel finalToken = token;

        // cancel previous api calls
        if (apiCall != null) {
            apiCall.cancel();
        }

        apiCall = activity.getAPI(token.getToken()).getNotifications(token.getAppID());
        apiCall.enqueue(new RetroCallback<List<NotificationModel>>(activity) {
            @Override
            public void onResponse(Call<List<NotificationModel>> call, Response<List<NotificationModel>> response) {
                super.onResponse(call, response);
                Log.i("noqwe", "onResponse " + token.getAppName() + " code: " + response.code());

                if (response.isSuccessful()) {
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    notificationList.clear();
                    notificationList.addAll(response.body());
                    notificationAdapter.notifyDataSetChanged();

                    int oldUnreadNotifications = Utils.getUnreadNotifications(activity, token.getAppID());
                    int newUnreadNotifications = 0;
                    for (NotificationModel notification : notificationList) {
                        if (!notification.isConfirmed()) {
                            newUnreadNotifications++;
                        }
                    }

                    if (oldUnreadNotifications != newUnreadNotifications) {
                        Utils.saveUnreadNotifications(activity, token.getAppID(), newUnreadNotifications);
                        activity.updateAppsFragment();
                        AppOptionsFragment parentFragment = (AppOptionsFragment) getParentFragment();
                        parentFragment.setCustomTabView();

                        RecyclerView parentRecyclerView = parentFragment.getRecyclerView();
                        if (parentRecyclerView != null) {
                            AppOptionAdapter.MyViewHolder notificationView = (AppOptionAdapter.MyViewHolder) parentRecyclerView.findViewHolderForAdapterPosition(parentFragment.getPositionInRecyclerView(AppOptionsFragment.OPTION_TYPE_NOTIFICATIONS));
                            if (notificationView != null) {
                                notificationView.setUnreadNotifications();
                            }
                        }
                    }

                    setEmptyView();
                    callCallback(callback, true);
                } else if (response.code() == 401) {
                    Utils.refreshAccessToken(activity, finalToken.getAppID(), new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (success) {
                                loadNotifications(callback);
                            } else {
                                callCallback(callback, false);
                                // TODO show info to refresh manually
                            }
                        }
                    });
                } else if (response.code() == 404) {
                    // app has not been found in the database, remove
                    Utils.logout(activity, token.getAppID());
                    activity.updateAllFragments();
                    Toast.makeText(activity, activity.getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();
                } else {
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    RetroCallback.showRequestError(activity);
                    callCallback(callback, false);
                }
            }

            @Override
            public void onFailure(Call<List<NotificationModel>> call, Throwable t) {
                super.onFailure(call, t);

                loadingView.setVisibility(GONE);
                recyclerView.setVisibility(View.VISIBLE);

                callCallback(callback, false);
            }
        });
    }

    private void callCallback(SimpleCallback callback, boolean success) {
        if (callback != null) {
            callback.onReady(success);
        }
    }

    private void setEmptyView() {
        if (notificationList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_app_notifications, container, false);

        recyclerView = view.findViewById(R.id.fragment_app_notification_list);
        loadingView = view.findViewById(R.id.fragment_app_notification_loading);
        loadingTextView = view.findViewById(R.id.fragment_app_notification_loading_info);
        emptyView = view.findViewById(R.id.fragment_app_notification_empty);

        return view;
    }
}
