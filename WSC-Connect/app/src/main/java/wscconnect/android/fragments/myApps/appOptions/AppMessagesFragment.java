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
import wscconnect.android.adapters.MessageAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.callbacks.SimpleCallback;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.MessageModel;

import static android.view.View.GONE;

/**
 * Created by chris on 18.07.17.
 */

public class AppMessagesFragment extends Fragment {
    public AccessTokenModel token;
    private MainActivity activity;
    private RecyclerView recyclerView;
    private List<MessageModel> messageList;
    private MessageAdapter messageAdapter;
    private LinearLayout loadingView;
    private TextView loadingTextView;
    private TextView emptyView;
    private boolean loading;

    public AppMessagesFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        token = getArguments().getParcelable(AccessTokenModel.EXTRA);

        activity = (MainActivity) getActivity();
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(activity, messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        loadingTextView.setText(getString(R.string.fragment_app_messagess_loading_info, token.getAppName()));
        Log.i(MainActivity.TAG, "AppMessagesFragment onActivityCreated loadMessages()");
        loadMessages(null);
    }

    public void setToken(AccessTokenModel token) {
        this.token = token;
    }

    public void loadMessages(final SimpleCallback callback) {
        token = Utils.getAccessToken(activity, token.getAppID());
        if (token == null) {
            Log.i(MainActivity.TAG, "AppMessagesFragment token is null");
            setEmptyView();
            return;
        }

        if (loading) {
            callCallback(callback, false);
            return;
        }

        loading = true;

        if (callback == null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Log.i(MainActivity.TAG, "AppMessagesFragment loading messages");

        final AccessTokenModel finalToken = token;
        activity.getAPI(token.getToken()).getMessages(token.getAppID()).enqueue(new RetroCallback<List<MessageModel>>(activity) {
            @Override
            public void onResponse(Call<List<MessageModel>> call, Response<List<MessageModel>> response) {
                super.onResponse(call, response);

                loading = false;

                if (response.isSuccessful()) {
                    Log.i(MainActivity.TAG, "AppMessagesFragment success loading");
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    messageList.clear();
                    messageList.addAll(response.body());
                    messageAdapter.notifyDataSetChanged();

                    setEmptyView();
                    callCallback(callback, true);
                } else if (response.code() == 401) {
                    Utils.refreshAccessToken(activity, finalToken.getAppID(), new SimpleCallback() {
                        @Override
                        public void onReady(boolean success) {
                            if (success) {
                                loadMessages(callback);
                            } else {
                                // TODO show info to refresh manually
                            }
                        }
                    });
                } else if (response.code() == 404) {
                    // app has not been found in the database, remove
                    Utils.removeAccessTokenString(activity, token.getAppID());
                    activity.updateAllFragments();
                    Toast.makeText(activity, activity.getString(R.string.fragment_app_notifications_app_removed, token.getAppName()), Toast.LENGTH_LONG).show();
                } else {
                    loadingView.setVisibility(GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    Toast.makeText(activity, R.string.error_general, Toast.LENGTH_SHORT).show();
                    callCallback(callback, false);
                }
            }

            @Override
            public void onFailure(Call<List<MessageModel>> call, Throwable t) {
                super.onFailure(call, t);

                loading = false;

                loadingView.setVisibility(GONE);
                recyclerView.setVisibility(View.VISIBLE);

                Toast.makeText(activity, R.string.error_general, Toast.LENGTH_SHORT).show();
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
        if (messageList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_app_messages, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_app_messages_list);
        loadingView = (LinearLayout) view.findViewById(R.id.fragment_app_messages_loading);
        loadingTextView = (TextView) view.findViewById(R.id.fragment_app_messages_loading_info);
        emptyView = (TextView) view.findViewById(R.id.fragment_app_messages_empty);

        return view;
    }
}
