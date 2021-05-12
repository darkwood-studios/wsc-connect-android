package wscconnect.android.listeners;

import android.os.Bundle;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public interface OnFragmentUpdateListener {
    /**
     * Is called, when a fragment should be updated from the outside
     */
    void onUpdate(Bundle bundle);
}
