package wscconnect.android.listeners;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public interface OnBackPressedListener {
    /**
     * Is called, when the back button is pressed
     *
     * @return true, if class can handle a back press, otherwise false
     */
    boolean onBackPressed();
}
