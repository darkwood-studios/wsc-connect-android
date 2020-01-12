package wscconnect.android.adapters;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import wscconnect.android.R;
import wscconnect.android.models.MessageModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private Activity activity;
    private List<MessageModel> messageList;

    public MessageAdapter(Activity activity, List<MessageModel> messageList) {
        this.activity = activity;
        this.messageList = messageList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_message, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        MessageModel message = messageList.get(position);

        holder.title.setText(message.getTitle());
        holder.message.setText(message.getMessage());
        holder.time.setText(message.getRelativeTime(activity));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, message, time;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.list_message_title);
            message = view.findViewById(R.id.list_message_message);
            time = view.findViewById(R.id.list_message_time);
        }
    }
}
