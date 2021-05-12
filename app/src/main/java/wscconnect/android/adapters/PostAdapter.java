package wscconnect.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.HeaderViewHolder;
import wscconnect.android.R;
import wscconnect.android.Utils;
import wscconnect.android.ViewHolder;
import wscconnect.android.activities.AppActivity;
import wscconnect.android.models.PostModel;
import wscconnect.android.models.ThreadModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class PostAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_POST = 2;
    private static final int TYPE_FORM = 3;
    private ThreadModel thread;
    private final AppActivity activity;
    private final List<PostModel> postList;

    public PostAdapter(AppActivity activity, List<PostModel> postList) {
        this.activity = activity;
        this.postList = postList;
    }

    public void setThread(ThreadModel thread) {
        this.thread = thread;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (getItemCount() == position + 1) {
            return TYPE_FORM;
        } else {
            return TYPE_POST;
        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ViewHolder v = null;

        switch (viewType) {
            case TYPE_HEADER:
                v = new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_header, parent, false));
                break;
            case TYPE_POST:
                v = new PostViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_generic_message, parent, false));
                break;
            case TYPE_FORM:
                v = new FormViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_generic_message_form, parent, false));
                break;
        }

        assert v != null;
        return v;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

                headerViewHolder.title.setText(thread.getTopic());
                headerViewHolder.subtitle.setText(activity.getString(R.string.list_post_header_author, thread.getLastPostUsername()));
                break;
            case TYPE_POST:
                PostViewHolder postViewHolder = (PostViewHolder) holder;
                position = getActualPosition(position);

                PostModel post = postList.get(position);
                postViewHolder.message.setText(Utils.fromHtml(post.getMessage()));
                postViewHolder.time.setText(post.getRelativeTime(activity));
                postViewHolder.username.setText(post.getUsername());
                GlideApp.with(activity).load(post.getAvatar()).error(R.drawable.ic_person_black_50dp).circleCrop().into(postViewHolder.avatar);

                // remove bottom border for last item
                if (position + 1 == postList.size()) {
                    postViewHolder.content.setBackground(null);
                } else {
                    postViewHolder.content.setBackgroundResource(R.drawable.border_bottom);
                }
                break;
            case TYPE_FORM:
                FormViewHolder formViewHolder = (FormViewHolder) holder;
                Utils.setError(formViewHolder.text, null);

                if (thread.isClosed()) {
                    formViewHolder.text.setVisibility(View.GONE);
                    formViewHolder.submit.setText(R.string.list_conversation_message_form_closed);
                } else {
                    formViewHolder.text.setVisibility(View.VISIBLE);
                    formViewHolder.submit.setText(R.string.submit);
                }
                break;
        }
    }

    private int getActualPosition(int position) {
        return position - 1;
    }

    @Override
    public int getItemCount() {
        return postList.size() + 2;
    }

    public class FormViewHolder extends ViewHolder {
        public EditText text;
        public Button submit;

        public FormViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.list_generic_message_form_text);
            submit = view.findViewById(R.id.list_generic_message_form_submit);

            submit.setOnClickListener(view1 -> {
                if (!submit.isEnabled()) {
                    return;
                }

                String separator = System.getProperty("line.separator");
                assert separator != null;
                String message = text.getText().toString().trim().replaceAll(separator, "<br>");

                if (message.isEmpty()) {
                    Utils.setError(activity, text);
                }
            });
        }
    }

    public static class PostViewHolder extends ViewHolder {
        public TextView message, time, username;
        public ImageView avatar;
        public LinearLayout content;

        public PostViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.list_conversation_message_message);
            time = view.findViewById(R.id.list_conversation_message_time);
            username = view.findViewById(R.id.list_conversation_message_username);
            avatar = view.findViewById(R.id.list_conversation_message_avatar);
            content = view.findViewById(R.id.list_conversation_message_content);

            view.setOnLongClickListener(view1 -> true);
        }
    }
}
