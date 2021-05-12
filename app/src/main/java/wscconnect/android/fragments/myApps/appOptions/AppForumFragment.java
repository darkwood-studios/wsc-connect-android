package wscconnect.android.fragments.myApps.appOptions;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.adapters.BoardAdapter;
import wscconnect.android.adapters.PostAdapter;
import wscconnect.android.adapters.ThreadAdapter;
import wscconnect.android.callbacks.RetroCallback;
import wscconnect.android.listeners.OnBackPressedListener;
import wscconnect.android.listeners.OnFragmentUpdateListener;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.BoardModel;
import wscconnect.android.models.PostModel;
import wscconnect.android.models.ThreadModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AppForumFragment extends Fragment implements OnBackPressedListener, OnFragmentUpdateListener {
    public final static int LIMIT = 20;
    private AppActivity activity;
    private AccessTokenModel token;
    private RecyclerView boardListView;
    private List<BoardModel> boardList;
    private BoardAdapter boardAdapter;
    private List<BoardModel> categoryBoardList;
    private BoardAdapter categoryBoardAdapter;
    private List<ThreadModel> threadList;
    private PostAdapter postAdapter;
    private List<PostModel> postList;
    private RecyclerView categoryListView;
    private RecyclerView threadListView;
    private RecyclerView postListView;
    private ThreadAdapter threadAdapter;
    private TextView emptyView;
    private ActiveView activeView;
    private LinearLayout loadingView;
    private TextView loadingTextView;
    private SwipeRefreshLayout refreshView;
    private String host;
    private BoardModel activeBoard;

    public AppForumFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getArguments() != null;
        token = getArguments().getParcelable(AccessTokenModel.EXTRA);
        host = Utils.prepareApiUrl(token.getAppApiUrl());
        activeView = ActiveView.BOARD_LIST;

        activity = (AppActivity) getActivity();
        boardList = new ArrayList<>();
        categoryBoardList = new ArrayList<>();
        threadList = new ArrayList<>();
        postList = new ArrayList<>();
        boardAdapter = new BoardAdapter(activity, this, boardList, token);
        categoryBoardAdapter = new BoardAdapter(activity, this, categoryBoardList, token);
        threadAdapter = new ThreadAdapter(activity, this, threadList);
        postAdapter = new PostAdapter(activity, postList);

        boardListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        boardListView.setAdapter(boardAdapter);

        categoryListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        categoryListView.setAdapter(categoryBoardAdapter);

        threadListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        threadListView.setAdapter(threadAdapter);

        postListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        postListView.setAdapter(postAdapter);

        loadingTextView.setText(getString(R.string.fragment_app_forum_loading_info, token.getAppName()));
        loadingView.setVisibility(View.VISIBLE);
        getBoards();

        refreshView.setOnRefreshListener(this::getBoards);
    }

    @SuppressWarnings("deprecation")
    public void getBoards() {
        Utils.getAPI(activity, host, token.getToken()).getBoards(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getBoards"), null).enqueue(new RetroCallback<List<BoardModel>>(activity) {
            @Override
            public void onResponse(@NotNull Call<List<BoardModel>> call, @NotNull Response<List<BoardModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    boardList.clear();
                    assert response.body() != null;
                    boardList.addAll(response.body());
                    boardAdapter.notifyDataSetChanged();
                    loadingView.setVisibility(View.GONE);
                    if (boardList.isEmpty()) {
                        emptyView.setText(R.string.fragment_app_forum_empty_boards);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BoardModel>> call, @NotNull Throwable t) {
                super.onFailure(call, t);

                refreshView.setRefreshing(false);

                t.printStackTrace();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_app_forum, container, false);

        boardListView = view.findViewById(R.id.fragment_app_forum_list);
        loadingView = view.findViewById(R.id.fragment_app_forum_loading);
        loadingTextView = view.findViewById(R.id.fragment_app_forum_loading_info);
        refreshView = view.findViewById(R.id.fragment_app_forum_refresh);
        categoryListView = view.findViewById(R.id.fragment_app_forum_category_list);
        threadListView = view.findViewById(R.id.fragment_app_forum_thread_list);
        postListView = view.findViewById(R.id.fragment_app_forum_post_list);
        emptyView = view.findViewById(R.id.fragment_app_forum_empty_view);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (threadListVisible() || categoryListVisible()) {
            switchToBoardList();
            return true;
        }

        return false;
    }

    @Override
    public void onUpdate(Bundle bundle) {

    }

    @SuppressWarnings("deprecation")
    public void getPosts(final ThreadModel thread, int limit, int offset) {
        refreshView.setRefreshing(true);

        Utils.getAPI(activity, host, token.getToken()).getPosts(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getPosts"), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(thread.getThreadID())), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(limit)), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(offset))).enqueue(new RetroCallback<List<PostModel>>(activity) {
            @Override
            public void onResponse(@NotNull Call<List<PostModel>> call, @NotNull Response<List<PostModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    postList.clear();
                    assert response.body() != null;
                    postList.addAll(response.body());
                    postAdapter.setThread(thread);
                    postAdapter.notifyDataSetChanged();
                    loadingView.setVisibility(View.GONE);

                    switchToPostList();
                    if (postList.isEmpty()) {
                        emptyView.setText(R.string.fragment_app_forum_empty_posts);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PostModel>> call, @NotNull Throwable t) {
                super.onFailure(call, t);

                refreshView.setRefreshing(false);

                t.printStackTrace();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void getThreads(final BoardModel board, int limit, int offset) {
        refreshView.setRefreshing(true);

        Utils.getAPI(activity, host, token.getToken()).getThreads(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getThreads"), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(board.getBoardID())), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(limit)), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(offset))).enqueue(new RetroCallback<List<ThreadModel>>(activity) {
            @Override
            public void onResponse(@NotNull Call<List<ThreadModel>> call, @NotNull Response<List<ThreadModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    threadList.clear();
                    assert response.body() != null;
                    threadList.addAll(response.body());
                    threadAdapter.setBoard(board);
                    threadAdapter.notifyDataSetChanged();
                    loadingView.setVisibility(View.GONE);

                    switchToThreadList();
                    if (threadList.isEmpty()) {
                        emptyView.setText(R.string.fragment_app_forum_empty_threads);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ThreadModel>> call, @NotNull Throwable t) {
                super.onFailure(call, t);

                refreshView.setRefreshing(false);

                t.printStackTrace();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void getCategoryBoards(final BoardModel board) {
        refreshView.setRefreshing(true);
        activeBoard = board;

        Utils.getAPI(activity, host, token.getToken()).getBoards(Utils.getApiUrlExtension(token.getAppApiUrl()), RequestBody.create(MediaType.parse("text/plain"), "getBoards"), RequestBody.create(MediaType.parse("text/plain"), String.valueOf(board.getBoardID()))).enqueue(new RetroCallback<List<BoardModel>>(activity) {
            @Override
            public void onResponse(@NotNull Call<List<BoardModel>> call, @NotNull Response<List<BoardModel>> response) {
                super.onResponse(call, response);

                refreshView.setRefreshing(false);

                if (response.isSuccessful()) {
                    categoryBoardList.clear();
                    assert response.body() != null;
                    categoryBoardList.addAll(response.body());
                    categoryBoardAdapter.setCategory(activeBoard);
                    categoryBoardAdapter.notifyDataSetChanged();
                    loadingView.setVisibility(View.GONE);
                    switchToCategoryList();

                    if (categoryBoardList.isEmpty()) {
                        emptyView.setText(R.string.fragment_app_forum_empty_boards);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onFailure(Call<List<BoardModel>> call, @NotNull Throwable t) {
                super.onFailure(call, t);

                refreshView.setRefreshing(false);

                t.printStackTrace();
            }
        });
    }

    private boolean categoryListVisible() {
        return categoryListView.getVisibility() == View.VISIBLE;
    }

    private boolean threadListVisible() {
        return threadListView.getVisibility() == View.VISIBLE;
    }

    private void switchToCategoryList() {
        activeView = ActiveView.CATEGORY_LIST;
        postListView.setVisibility(View.GONE);
        threadListView.setVisibility(View.GONE);
        boardListView.setVisibility(View.GONE);
        categoryListView.setVisibility(View.VISIBLE);
    }

    private void switchToBoardList() {
        activeView = ActiveView.BOARD_LIST;
        postListView.setVisibility(View.GONE);
        threadListView.setVisibility(View.GONE);
        categoryListView.setVisibility(View.GONE);
        boardListView.setVisibility(View.VISIBLE);
    }

    private void switchToThreadList() {
        activeView = ActiveView.THREAD_LIST;
        postListView.setVisibility(View.GONE);
        boardListView.setVisibility(View.GONE);
        categoryListView.setVisibility(View.GONE);
        threadListView.setVisibility(View.VISIBLE);
    }

    private void switchToPostList() {
        activeView = ActiveView.POST_LIST;
        boardListView.setVisibility(View.GONE);
        categoryListView.setVisibility(View.GONE);
        threadListView.setVisibility(View.GONE);
        postListView.setVisibility(View.VISIBLE);
    }

    public enum ActiveView {
        BOARD_LIST, CATEGORY_LIST, THREAD_LIST, POST_LIST
    }
}
