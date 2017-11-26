package wscconnect.android;

import android.view.View;
import android.widget.TextView;

/**
 * Created by chris on 20.11.17.
 */

public class HeaderViewHolder extends ViewHolder {
    public TextView title, subtitle;

    public HeaderViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.list_header_title);
        subtitle = view.findViewById(R.id.list_header_subtitle);
    }
}