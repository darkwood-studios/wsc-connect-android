package wscconnect.android.callbacks;

import android.content.Context;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.Utils;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class RetroCallback<T> implements Callback<T> {
    private static Toast toast;
    private final Context context;

    public RetroCallback(Context context) {
        this.context = context;
    }

    public static void showRequestError(Context context) {
        // cancel previous toast to prevent multiple messages
        if (toast != null) {
            toast.cancel();
        }

        String m = context.getString(R.string.error_general);

        if (!Utils.hasInternetConnection(context)) {
            m = context.getString(R.string.no_internet_connection);
        }

        toast = Toast.makeText(context, m, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
        // cancel error toast, if visible
        if (toast != null) {
            toast.cancel();
        }

        // api down
        if (response.code() == 502) {
            Toast.makeText(context, context.getString(R.string.error_request_api_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<T> call, @NotNull Throwable t) {
        if (!call.isCanceled()) {
            showRequestError(context);
        }
    }
}
