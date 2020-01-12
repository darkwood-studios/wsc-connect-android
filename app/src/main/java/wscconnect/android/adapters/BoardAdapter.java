package wscconnect.android.adapters;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.HeaderViewHolder;
import wscconnect.android.R;
import wscconnect.android.ViewHolder;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.fragments.myApps.appOptions.AppForumFragment;
import wscconnect.android.fragments.myApps.appOptions.AppWebviewFragment;
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.BoardModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class BoardAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_BOARD = 2;
    private final AccessTokenModel token;
    private final AppForumFragment fragment;
    private BoardModel category;
    private AppActivity activity;
    private List<BoardModel> boardList;

    public BoardAdapter(AppActivity activity, AppForumFragment fragment, List<BoardModel> boardList, AccessTokenModel token) {
        this.activity = activity;
        this.fragment = fragment;
        this.boardList = boardList;
        this.token = token;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder v = null;

        if (category != null) {
            switch (viewType) {
                case TYPE_HEADER:
                    v = new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_header, parent, false));
                    break;
                case TYPE_BOARD:
                    v = new BoardViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_board, parent, false));
                    break;
            }
        } else {
            v = new BoardViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_board, parent, false));
        }

        return v;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 && category != null) {
            return TYPE_HEADER;
        } else {
            return TYPE_BOARD;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (category != null) {
            switch (holder.getItemViewType()) {
                case TYPE_HEADER:
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                    headerViewHolder.title.setText(category.getTitle());
                    if (!category.getDescription().isEmpty()) {
                        headerViewHolder.subtitle.setText(category.getDescription());
                        headerViewHolder.subtitle.setVisibility(View.VISIBLE);
                    } else {
                        headerViewHolder.subtitle.setVisibility(View.GONE);
                    }
                    break;
                case TYPE_BOARD:
                    onBindBoardViewHolder(holder, position);
                    break;
            }
        } else {
            onBindBoardViewHolder(holder, position);
        }
    }

    private void onBindBoardViewHolder(ViewHolder holder, int position) {
        BoardViewHolder headerViewHolder = (BoardViewHolder) holder;
        BoardModel board = boardList.get(getActualPosition(position));

        int padding = activity.getResources().getDimensionPixelSize(R.dimen.layout_padding);

        if (board.getDepth() > 1) {
            headerViewHolder.itemView.setPadding(padding * board.getFixedDepth(), padding, padding, padding);
        } else {
            headerViewHolder.itemView.setPadding(padding, padding, padding, padding);
        }

        if (board.getUnreadThreads() > 0) {
            headerViewHolder.unreadThreads.setText(String.valueOf(board.getUnreadThreads()));
            headerViewHolder.unreadThreads.setVisibility(View.VISIBLE);
        } else {
            headerViewHolder.unreadThreads.setVisibility(View.GONE);
        }

        headerViewHolder.title.setText(board.getTitle());
        GlideApp.with(activity).load(board.getIcon()).into(headerViewHolder.icon);
    }

    private int getActualPosition(int position) {
        if (category != null) {
            position -= 1;
        }

        return position;
    }

    @Override
    public int getItemCount() {
        int count = boardList.size();

        if (category != null) {
            count += 1;
        }

        return count;
    }

    public void setCategory(BoardModel category) {
        this.category = category;
    }

    public class BoardViewHolder extends ViewHolder {
        public TextView title, unreadThreads;
        public ImageView icon;

        public BoardViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.list_board_title);
            unreadThreads = view.findViewById(R.id.list_board_unread_threads);
            icon = view.findViewById(R.id.list_board_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadBoard();
                }
            });
        }

        private void loadBoard() {
            int position = getActualPosition(getAdapterPosition());

            // RecyclerView.NO_POSITION is returned, if notifyDataSetChanged() has been called just now
            if (position != RecyclerView.NO_POSITION) {
                BoardModel board = boardList.get(position);
                if (board.isLink()) {
                    if (token.getAppTabs().contains(AppActivity.FRAGMENT_WEBVIEW)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(AppWebviewFragment.URL, board.getLink());
                        activity.setCurrentPage(AppActivity.FRAGMENT_WEBVIEW, bundle);
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(board.getLink()));
                        activity.startActivity(browserIntent);
                    }
                } else if (board.isBoard()) {
                    fragment.getThreads(board, AppForumFragment.LIMIT, 0);
                } else {
                    fragment.getCategoryBoards(board);
                }
            } else {
                // wait a short time and try again.
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        loadBoard();
                    }
                }, 200);
            }
        }
    }
}
