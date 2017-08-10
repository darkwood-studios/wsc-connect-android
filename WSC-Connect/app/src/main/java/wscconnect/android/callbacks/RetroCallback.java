package wscconnect.android.callbacks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wscconnect.android.R;
import wscconnect.android.Utils;

/**
 * Created by chris on 30.07.17.
 */

public class RetroCallback<T> implements Callback<T> {
    private static Toast toast;
    private static AlertDialog dialog;
    private Context context;

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
    public void onResponse(Call<T> call, Response<T> response) {
        // cancel error toast, if visible
        if (toast != null) {
            toast.cancel();
        }

        switch (response.code()) {
            // app version too old
            case 501:
                if (dialog == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.error_app_version_title);
                    builder.setMessage(R.string.error_app_version_message);
                   // builder.setCancelable(false);
                    builder.setPositiveButton(R.string.error_app_version_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=wscconnect.android"));
                            context.startActivity(intent);
                        }
                    });
                    dialog = builder.create();
                }

                if (!dialog.isShowing()) {
                    dialog.show();
                }
                break;
            // api down
            case 502:
                Toast.makeText(context, context.getString(R.string.error_request_api_not_available), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (!call.isCanceled()) {
            showRequestError(context);
        }
    }
}
