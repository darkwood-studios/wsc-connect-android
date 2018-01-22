package wscconnect.android;

import android.view.View;
import android.widget.TextView;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class HeaderViewHolder extends ViewHolder {
    public TextView title, subtitle;

    public HeaderViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.list_header_title);
        subtitle = view.findViewById(R.id.list_header_subtitle);
    }
}
