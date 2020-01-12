package wscconnect.android.fragments.myApps.appOptions;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.adapters.ConversationAdapter;
import wscconnect.android.adapters.ConversationMessageAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.callbacks.SimpleJSONCallback;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.listeners.OnFragmentUpdateListener;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.ConversationMessageModel;
import wscconnect.android.models.ConversationModel;

import static android.view.View.GONE;
import static wscconnect.android.activities.AppActivity.EXTRA_EVENT_ID;
import static wscconnect.android.activities.AppActivity.EXTRA_EVENT_NAME;
import static wscconnect.android.activities.AppActivity.EXTRA_FORCE_LOAD;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AppConversationsFragment extends Fragment implements OnBackPressedListener, OnFragmentUpdateListener {
    public final static int LIMIT = 20;
    private final static int VISIBLE_THRESHOLD = 3;
    private AppActivity activity;
    private AccessTokenModel token;
    private RecyclerView conversationListView;
    private List<ConversationModel> conversationList;
    private List<ConversationMessageModel> conversationMessageList;
    private ConversationAdapter conversationAdapter;
    private LinearLayout loadingView;
    private TextView loadingTextView;
    private TextView emptyView;
    private Call<List<ConversationModel>> apiCall;
    private SwipeRefreshLayout refreshView;
    private String host;
    private ConversationMessageAdapter conversationMessageAdapter;
    private CoordinatorLayout conversationMessageListContainer;
    private FloatingActionButton conversationMessageFab;
    private RecyclerView conversationMessageListView;
    private ConversationModel activeConversation;
    private boolean conversationsLoading;
    private boolean conversationsAllLoaded;
    private boolean conversationMessagesLoading;
    private boolean conversationMessagesAllLoaded;
    private boolean conversationMessagesShouldLoad;

    public AppConversationsFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        token = getArguments().getParcelable(AccessTokenModel.EXTRA);
        host = Utils.prepareApiUrl(token.getAppApiUrl());

        activity = (AppActivity) getActivity();
        conversationList = new ArrayList<>();
        conversationMessageList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(activity, this, conversationList, token);
        conversationMessageAdapter = new ConversationMessageAdapter(activity, this, conversationMessageList, token);

        final LinearLayoutManager conversationLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        conversationListView.setLayoutManager(conversationLayoutManager);
        conversationListView.setAdapter(conversationAdapter);
        final LinearLayoutManager conversationMessagesLayoutManger = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        conversationMessageListView.setLayoutManager(conversationMessagesLayoutManger);
        conversationMessageListView.setAdapter(conversationMessageAdapter);

        loadingTextView.setText(getString(R.string.fragment_app_conversations_loading_info, token.getAppName()));
        getConversations(LIMIT, 0, null);

        refreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (messageListVisible()) {
                    getConversationMessages(activeConversation, LIMIT, 0, null);
                } else {
                    getConversations(LIMIT, 0, new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            refreshView.setRefreshing(false);
                        }
                    });
                }
            }
        });

        conversationListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem, totalItemCount;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = conversationLayoutManager.getItemCount();
                lastVisibleItem = conversationLayoutManager.findLastVisibleItemPosition();
                if (!conversationsLoading && !conversationsAllLoaded && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                    conversationsLoading = true;
                    refreshView.setRefreshing(true);
                    onLoadMoreConversations();
                }
            }
        });

        conversationMessageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem, totalItemCount;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = conversationMessagesLayoutManger.getItemCount();
                lastVisibleItem = conversationMessagesLayoutManger.findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                    conversationMessageFab.hide();
                    if (conversationMessagesShouldLoad && conversationMessageAdapter.isConversationMessagesAutoLoad() && !conversationMessagesLoading && !conversationMessagesAllLoaded) {
                        conversationMessagesLoading = true;
                        refreshView.setRefreshing(true);
                        onLoadMoreConversationMessages();
                    }
                } else {
                    conversationMessagesShouldLoad = true;
                    conversationMessageFab.show();
                }
            }
        });

        conversationMessageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationMessagesShouldLoad = false;
                conversationMessageListView.scrollToPosition(conversationMessagesLayoutManger.getItemCount() - 1);
            }
        });
    }

    public void refreshViewRefreshing(boolean refreshing) {
        if (refreshView != null) {
            refreshView.setRefreshing(refreshing);
        }
    }

    public void setToken(AccessTokenModel token) {
        this.token = token;
    }

    public void getConversations(final int limit, final int offset, final SimpleCallback callback) {
        if (callback == null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        final AccessTokenModel finalToken = token;

        // cancel previous api calls
        if (apiCall != null) {
            apiCall.cancel();
        }

        apiCall = Utils.getAPI(activity, host, token.getToken()).getConversations(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getConversations"), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(limit)), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(offset)));
        apiCall.enqueue(new RetroCallback<List<ConversationModel>>(activity) {
            @Override
            public void onResponse(Call<List<ConversationModel>> call, Response<List<ConversationModel>> response) {
                super.onResponse(call, response);

                if (response.isSuccessful()) {
                    loadingView.setVisibility(GONE);
                    conversationListView.setVisibility(View.VISIBLE);

                    // only clear list, if no offset parameter is passed
                    if (offset == 0) {
                        conversationList.clear();
                        conversationsAllLoaded = false;
                    }

                    // if we receive less data then we requested, there is no more available
                    if (response.body().size() < limit) {
                        conversationsAllLoaded = true;
                    }

                    conversationList.addAll(response.body());
                    sortConversations();
                    conversationAdapter.notifyDataSetChanged();

                    int oldUnreadConversations = Utils.getUnreadConversations(activity, token.getAppID());
                    int newUnreadConversations = 0;
                    for (ConversationModel conversation : conversationList) {
                        if (conversation.isNew()) {
                            newUnreadConversations++;
                        }
                    }

                    if (oldUnreadConversations != newUnreadConversations) {
                        Utils.saveUnreadConversations(activity, token.getAppID(), newUnreadConversations);
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
                                getConversations(limit, offset, callback);
                            } else {
                                callCallback(callback, false);
                                // TODO show info to refresh manually
                            }
                        }
                    });
                } else if (response.code() == 404) {
                    // app has not been found in the database, remove
                    Utils.logout(activity, token.getAppID());
                    if (isAdded()) {
                        Toast.makeText(activity, getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();
                    }
                } else if (response.code() == 403) {
                    // user has been logged out
                    Utils.logout(activity, token.getAppID());
                    if (isAdded()) {
                        Toast.makeText(activity, getString(R.string.fragment_app_notifications_logged_out, token.getAppName()), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String message = "";

                    try {
                        String error = response.errorBody().string();
                        JSONObject json = new JSONObject(error);
                        message = json.getString("message");
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    loadingView.setVisibility(GONE);
                    conversationListView.setVisibility(View.VISIBLE);

                    if (message.equals("package")) {
                        Toast.makeText(activity, getString(R.string.fragment_app_conversations_package, token.getAppName()), Toast.LENGTH_LONG).show();
                    } else {
                        RetroCallback.showRequestError(activity);
                    }
                    callCallback(callback, false);
                }
            }

            @Override
            public void onFailure(Call<List<ConversationModel>> call, Throwable t) {
                super.onFailure(call, t);

                loadingView.setVisibility(GONE);
                conversationListView.setVisibility(View.VISIBLE);

                callCallback(callback, false);
            }
        });
    }

    private void sortConversations() {
        Collections.sort(conversationList, new Comparator<ConversationModel>() {
            @Override
            public int compare(ConversationModel c1, ConversationModel c2) {
                if (c1.getTime() < c2.getTime())
                    return 1;
                if (c1.getTime() > c2.getTime())
                    return -1;
                return 0;
            }
        });
    }

    public void getConversationMessages(final ConversationModel conversation, final int limit, final int offset, final SimpleCallback callback) {
        if (conversation == null) {
            return;
        }

        activeConversation = conversation;

        Utils.getAPI(activity, host, token.getToken()).getConversationMessages(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getConversationMessages"), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(conversation.getConversationID())), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(limit)), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(offset))).enqueue(new RetroCallback<List<ConversationMessageModel>>(activity) {
            @Override
            public void onResponse(Call<List<ConversationMessageModel>> call, Response<List<ConversationMessageModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    // only clear list, if no offset parameter is passed
                    if (offset == 0) {
                        ((LinearLayoutManager) conversationMessageListView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                        conversationMessageList.clear();
                        conversationMessagesAllLoaded = false;

                        conversationMessageAdapter.setConversation(conversation);
                    }

                    // if we receive less data then we requested, there is no more available
                    if (response.body().size() < limit) {
                        conversationMessageAdapter.setConversationMessagesAutoLoad(true);
                        conversationMessagesAllLoaded = true;
                    }

                    conversationMessageList.addAll(response.body());
                    conversationMessageAdapter.notifyDataSetChanged();

                    showConversationMessages(true);
                    callCallback(callback, true);

                    Utils.saveUnreadConversations(activity, token.getAppID(), Utils.getUnreadConversations(activity, token.getAppID()) - 1);
                    conversation.setNew(false);
                    conversationAdapter.notifyDataSetChanged();
                    activity.setCustomTabView();
                } else if (response.code() == 409) {
                    Utils.refreshAccessToken(activity, token.getAppID(), new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (success) {
                                // refresh token
                                token = Utils.getAccessToken(activity, token.getAppID());
                                getConversationMessages(conversation, limit, offset, callback);
                            } else {
                                callCallback(callback, false);
                                // TODO show info to refresh manually
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ConversationMessageModel>> call, Throwable t) {
                super.onFailure(call, t);

                refreshView.setRefreshing(false);
                callCallback(callback, false);
            }
        });
    }

    private void showConversationMessages(boolean show) {
        if (show) {
            conversationListView.setVisibility(View.GONE);
            conversationMessageListContainer.setVisibility(View.VISIBLE);
        } else {
            activeConversation = null;
            conversationMessageListContainer.setVisibility(View.GONE);
            conversationListView.setVisibility(View.VISIBLE);
        }
    }

    private void callCallback(SimpleCallback callback, boolean success) {
        if (callback != null) {
            callback.onReady(success);
        }
    }

    private void callJSONCallback(SimpleJSONCallback callback, JSONObject json, boolean success) {
        if (callback != null) {
            callback.onReady(json, success);
        }
    }

    private void setEmptyView() {
        if (conversationList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_app_conversations, container, false);

        conversationListView = view.findViewById(R.id.fragment_app_conversations_list);
        conversationMessageListView = view.findViewById(R.id.fragment_app_conversations_message_list);
        conversationMessageListContainer = view.findViewById(R.id.fragment_app_conversations_message_container);
        conversationMessageFab = view.findViewById(R.id.fragment_app_conversations_message_fab);
        loadingView = view.findViewById(R.id.fragment_app_conversations_loading);
        loadingTextView = view.findViewById(R.id.fragment_app_conversations_loading_info);
        emptyView = view.findViewById(R.id.fragment_app_conversations_empty);
        refreshView = view.findViewById(R.id.fragment_app_conversations_refresh);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (messageListVisible()) {
            showConversationMessages(false);
            return true;
        }

        return false;
    }

    private boolean messageListVisible() {
        return conversationMessageListContainer.getVisibility() == View.VISIBLE;
    }

    public void addConversationMessage(final int conversationID, final String message, final SimpleJSONCallback callback) {
        Utils.getAPI(activity, host, token.getToken()).addConversationMessage(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "addConversationMessage"), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(conversationID)), RequestBody.create(MediaType.parse("text/plain"), message)).enqueue(new RetroCallback<ConversationMessageModel>(activity) {
            @Override
            public void onResponse(Call<ConversationMessageModel> call, Response<ConversationMessageModel> response) {
                super.onResponse(call, response);

                if (response.isSuccessful()) {
                    conversationMessageList.add(0, response.body());
                    conversationMessageAdapter.notifyDataSetChanged();

                    // update conversation
                    for (int i = 0; i < conversationList.size(); i++) {
                        ConversationModel conversation = conversationList.get(i);
                        if (conversation.getConversationID() == conversationID) {
                            conversation.setTime(((int) (System.currentTimeMillis() / 1000)));
                            sortConversations();
                            conversationAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    // scroll to top
                    (conversationMessageListView.getLayoutManager()).smoothScrollToPosition(conversationMessageListView, null, 0);

                    callJSONCallback(callback, null, true);
                } else if (response.code() == 409) {
                    Utils.refreshAccessToken(activity, token.getAppID(), new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (success) {
                                // refresh token
                                token = Utils.getAccessToken(activity, token.getAppID());
                                addConversationMessage(conversationID, message, callback);
                            } else {
                                callJSONCallback(callback, null, false);
                                // TODO show info to refresh manually
                            }
                        }
                    });
                } else {
                    try {
                        JSONObject error = new JSONObject(response.errorBody().string());
                        callJSONCallback(callback, error, false);
                    } catch (JSONException | IOException e) {
                        callJSONCallback(callback, null, false);
                    }
                }
            }

            @Override
            public void onFailure(Call<ConversationMessageModel> call, Throwable t) {
                super.onFailure(call, t);

                callJSONCallback(callback, null, false);
            }
        });
    }

    public void onLoadMoreConversations() {
        getConversations(LIMIT, conversationList.size(), new SimpleCallback() {
            @Override
            public void onReady(boolean success) {
                refreshView.setRefreshing(false);
                conversationsLoading = false;
            }
        });
    }

    public void onLoadMoreConversationMessages() {
        onLoadMoreConversationMessages(null);
    }

    public void onLoadMoreConversationMessages(final SimpleCallback callback) {
        getConversationMessages(activeConversation, LIMIT, conversationMessageList.size(), new SimpleCallback() {
            @Override
            public void onReady(boolean success) {
                refreshView.setRefreshing(false);
                conversationMessagesLoading = false;

                if (callback != null) {
                    callback.onReady(success);
                }
            }
        });
    }

    @Override
    public void onUpdate(Bundle bundle) {
        int eventID = bundle.getInt(EXTRA_EVENT_ID);
        int eventname = bundle.getInt(EXTRA_EVENT_NAME);
        boolean forceLoad = bundle.getBoolean(EXTRA_FORCE_LOAD);

        if (forceLoad) {
            refreshView.setRefreshing(true);
            getConversations(LIMIT, 0, new SimpleCallback() {
                @Override
                public void onReady(boolean success) {
                    refreshView.setRefreshing(false);
                }
            });
        }
    }
}
