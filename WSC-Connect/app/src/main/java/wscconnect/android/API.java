package wscconnect.android;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import wscconnect.android.models.AppModel;
import wscconnect.android.models.LoginModel;
import wscconnect.android.models.MessageModel;
import wscconnect.android.models.NotificationModel;

/**
 * Created by chris on 18.07.17.
 */

public interface API {
    String ENDPOINT = "https://api.wsc-connect.com/";
    //String ENDPOINT = "http://192.168.0.20:1337/";

    @GET("apps")
    Call<List<AppModel>> getApps();

    @GET("notifications/{app}")
    Call<List<NotificationModel>> getNotifications(@Path("app") String app);

    @GET("messages/{app}")
    Call<List<MessageModel>> getMessages(@Path("app") String app);

    @POST("login/{app}")
    Call<ResponseBody> login(@Path("app") String app, @Body LoginModel body);

    @POST("logout/{app}")
    Call<ResponseBody> logout(@Path("app") String app);

    @POST("token")
    Call<ResponseBody> getAccessToken();
}
