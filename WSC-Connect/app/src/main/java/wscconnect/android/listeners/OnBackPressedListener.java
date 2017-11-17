package wscconnect.android.listeners;

/**
 * Created by chris on 28.07.17.
 */

public interface OnBackPressedListener {
    /**
     * Is called, when the back button is pressed
     * @return true, if class can handle a back press, otherwise false
     */
    boolean onBackPressed();
}
