package wscconnect.android.fragments.myApps.appOptions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.adapters.NotificationAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.listeners.OnFragmentUpdateListener;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.NotificationModel;

import static android.view.View.GONE;
import static wscconnect.android.activities.AppActivity.EXTRA_EVENT_ID;
import static wscconnect.android.activities.AppActivity.EXTRA_EVENT_NAME;
import static wscconnect.android.activities.AppActivity.EXTRA_FORCE_LOAD;

/**
 * Created by chris on 18.07.17.
 */

public class AppNotificationsFragment extends Fragment implements OnFragmentUpdateListener {
    private AppActivity activity;
    private AccessTokenModel token;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshView;
    private List<NotificationModel> notificationList;
    private NotificationAdapter notificationAdapter;
    private LinearLayout loadingView;
    private TextView loadingTextView;
    private TextView emptyView;
    private Call<List<NotificationModel>> apiCall;

    public AppNotificationsFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        token = getArguments().getParcelable(AccessTokenModel.EXTRA);

        activity = (AppActivity) getActivity();
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(activity, notificationList, token);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(notificationAdapter);

        loadingTextView.setText(getString(R.string.fragment_app_notifications_loading_info, token.getAppName()));
        Log.i("asduhd", "onActivityCreated AppNotific");

        getNotifications(null);

        refreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshView.setRefreshing(true);
                getNotifications(new SimpleCallback() {
                    @Override
                    public void onReady(boolean success) {
                        refreshView.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void setToken(AccessTokenModel token) {
        this.token = token;
    }

    public void getNotificationsLegacy(final SimpleCallback callback) {
        Log.i("asduhd", "use legacy");

        if (token == null) {
            setEmptyView();
            return;
        }

        if (callback == null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        final AccessTokenModel finalToken = token;

        // cancel previous api calls
        if (apiCall != null) {
            apiCall.cancel();
        }

        apiCall = Utils.getAPI(activity, token.getToken()).getNotifications(token.getAppID());
        apiCall.enqueue(new RetroCallback<List<NotificationModel>>(activity) {
            @Override
            public void onResponse(Call<List<NotificationModel>> call, Response<List<NotificationModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);
                Log.i("asduhd", "lgeacy cold: " + response.code());
                Log.i("asduhd", "call: " + call.request().toString());

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
                        activity.setCustomTabView();
                    }

                    setEmptyView();
                    callCallback(callback, true);
                } else if (response.code() == 401) {
                    Utils.refreshAccessToken(activity, finalToken.getAppID(), new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (success) {
                                // refresh token
                                token = Utils.getAccessToken(activity, token.getAppID());
                                getNotificationsLegacy(callback);
                            } else {
                                callCallback(callback, false);
                                // TODO show info to refresh manually
                            }
                        }
                    });
                } else {
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    callCallback(callback, false);
                }
            }

            @Override
            public void onFailure(Call<List<NotificationModel>> call, Throwable t) {
                Log.i("asduhd", "lgeacy cold: onFailure");

                refreshView.setRefreshing(false);
                loadingView.setVisibility(GONE);
                recyclerView.setVisibility(View.VISIBLE);

                callCallback(callback, false);
            }
        });
    }

    public void getNotifications(final SimpleCallback callback) {
        if (token == null) {
            setEmptyView();
            return;
        }
        Log.i("asduhd", "getNotifications");

        if (callback == null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        final AccessTokenModel finalToken = token;

        // cancel previous api calls
        if (apiCall != null) {
            apiCall.cancel();
        }

        String host = Utils.prepareApiUrl(token.getAppApiUrl());

        apiCall = Utils.getAPI(activity, host, token.getToken()).getNotifications(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getNotifications"));
        apiCall.enqueue(new RetroCallback<List<NotificationModel>>(activity) {
            @Override
            public void onResponse(Call<List<NotificationModel>> call, Response<List<NotificationModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);
                Log.i("asduhd", "getNotifications code: " + response.code());
                Log.i("asduhd", "call: " + call.request().toString());

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
                        activity.setCustomTabView();
                    }

                    setEmptyView();
                    callCallback(callback, true);
                } else if (response.code() == 409) {
                    Utils.refreshAccessToken(activity, finalToken.getAppID(), new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (success) {
                                // refresh token
                                token = Utils.getAccessToken(activity, token.getAppID());
                                getNotifications(callback);
                            } else {
                                callCallback(callback, false);
                                // TODO show info to refresh manually
                            }
                        }
                    });
                } else if (response.code() == 404) {
                    // TODO for legacy plugin versions. Remove in next version
                    getNotificationsLegacy(new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (!success && isAdded()) {
                                Utils.logout(activity, token.getAppID());
                                Toast.makeText(activity, getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    // app has not been found in the database, remove
                    /*Utils.logout(activity, token.getAppID());
                    Toast.makeText(activity, getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();*/
                } else if (response.code() == 403) {
                    // TODO for legacy plugin versions. Remove in next version
                    getNotificationsLegacy(new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (!success && isAdded()) {
                                Utils.logout(activity, token.getAppID());
                                Toast.makeText(activity, getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    /*
                    // user has been logged out
                    Utils.logout(activity, token.getAppID());
                   // activity.updateAllFragments();
                    Toast.makeText(activity, getString(R.string.fragment_app_notifications_logged_out, token.getAppName()), Toast.LENGTH_LONG).show();*/
                } else {
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    RetroCallback.showRequestError(activity);
                    callCallback(callback, false);
                }
            }

            @Override
            public void onFailure(Call<List<NotificationModel>> call, Throwable t) {
                if (t instanceof IllegalStateException) {
                    getNotificationsLegacy(new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (!success && isAdded()) {
                                Utils.logout(activity, token.getAppID());
                                Toast.makeText(activity, getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    super.onFailure(call, t);

                    refreshView.setRefreshing(false);
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    callCallback(callback, false);
                }
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
        refreshView = view.findViewById(R.id.fragment_app_notification_refresh);

        return view;
    }

    @Override
    public void onUpdate(Bundle bundle) {
        int eventID = bundle.getInt(EXTRA_EVENT_ID);
        int eventname = bundle.getInt(EXTRA_EVENT_NAME);
        boolean forceLoad = bundle.getBoolean(EXTRA_FORCE_LOAD);

        if (forceLoad) {
            refreshView.setRefreshing(true);
            getNotifications(new SimpleCallback() {
                @Override
                public void onReady(boolean success) {
                    refreshView.setRefreshing(false);
                }
            });
        }
    }
}
