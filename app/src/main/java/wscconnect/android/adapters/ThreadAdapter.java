package wscconnect.android.adapters;

import android.graphics.Typeface;
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
import wscconnect.android.models.AccessTokenModel;
import wscconnect.android.models.BoardModel;
import wscconnect.android.models.ThreadModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class ThreadAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_THREAD = 2;
    private final AccessTokenModel token;
    private final AppForumFragment fragment;
    private BoardModel board;
    private AppActivity activity;
    private List<ThreadModel> threadList;

    public ThreadAdapter(AppActivity activity, AppForumFragment fragment, List<ThreadModel> threadList, AccessTokenModel token) {
        this.activity = activity;
        this.fragment = fragment;
        this.threadList = threadList;
        this.token = token;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder v = null;

        switch (viewType) {
            case TYPE_HEADER:
                v = new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_header, parent, false));
                break;
            case TYPE_THREAD:
                v = new ThreadViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_thread, parent, false));
                break;
        }

        return v;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_THREAD;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                if (board != null) {
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                    headerViewHolder.title.setText(board.getTitle());
                    if (!board.getDescription().isEmpty()) {
                        headerViewHolder.subtitle.setText(board.getDescription());
                        headerViewHolder.subtitle.setVisibility(View.VISIBLE);
                    } else {
                        headerViewHolder.subtitle.setVisibility(View.GONE);
                    }
                }
                break;
            case TYPE_THREAD:
                ThreadViewHolder threadViewHolder = (ThreadViewHolder) holder;
                ThreadModel thread = threadList.get(getActualPosition(position));

                threadViewHolder.title.setText(thread.getTopic());
                threadViewHolder.subtitle.setText(thread.getLastPostUsername() + " - " + thread.getRelativeTime(activity));
                threadViewHolder.replies.setText(String.valueOf(thread.getReplies()));

                if (thread.isNew()) {
                    threadViewHolder.title.setTypeface(null, Typeface.BOLD);
                } else {
                    threadViewHolder.title.setTypeface(null, Typeface.NORMAL);
                }

                GlideApp.with(activity).load(thread.getLastPostAvatar()).circleCrop().into(threadViewHolder.avatar);
                break;
        }
    }

    private int getActualPosition(int position) {
        return position - 1;
    }

    @Override
    public int getItemCount() {
        return threadList.size() + 1;
    }

    public void setBoard(BoardModel board) {
        this.board = board;
    }

    public class ThreadViewHolder extends ViewHolder {
        public TextView title, subtitle, replies;
        public ImageView avatar;

        public ThreadViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.list_thread_title);
            subtitle = view.findViewById(R.id.list_thread_subtitle);
            replies = view.findViewById(R.id.list_thread_replies);
            avatar = view.findViewById(R.id.list_thread_avatar);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadThread();
                }
            });
        }

        private void loadThread() {
            int position = getActualPosition(getAdapterPosition());

            // RecyclerView.NO_POSITION is returned, if notifyDataSetChanged() has been called just now
            if (position != RecyclerView.NO_POSITION) {
                ThreadModel thread = threadList.get(position);
                fragment.getPosts(thread, AppForumFragment.LIMIT, 0);
            } else {
                // wait a short time and try again.
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        loadThread();
                    }
                }, 200);
            }
        }
    }
}
