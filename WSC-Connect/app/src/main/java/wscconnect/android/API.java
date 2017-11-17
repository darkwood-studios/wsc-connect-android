package wscconnect.android;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import wscconnect.android.models.AppModel;
import wscconnect.android.models.ConversationMessageModel;
import wscconnect.android.models.ConversationModel;
import wscconnect.android.models.LoginModel;
import wscconnect.android.models.MessageModel;
import wscconnect.android.models.NotificationModel;

/**
 * Created by chris on 18.07.17.
 */

public interface API {
    String ENDPOINT = "https://api.wsc-connect.com/";
    //String ENDPOINT = "http://192.168.2.114:1337/";

    @GET("apps")
    Call<List<AppModel>> getApps();

    @GET("messages/{app}")
    Call<List<MessageModel>> getMessages(@Path("app") String app);

    @POST("login/{app}")
    Call<ResponseBody> login(@Path("app") String app, @Body LoginModel body);

    @POST("logout/{app}")
    Call<ResponseBody> logout(@Path("app") String app);

    @POST("token")
    Call<ResponseBody> getAccessToken();

    @Multipart
    @POST("index.php?wsc-connect-api")
    Call<List<NotificationModel>> getNotifications(@Part("type") RequestBody type);

    @Deprecated
    @GET("notifications/{app}")
    Call<List<NotificationModel>> getNotifications(@Path("app") String app);

    @Multipart
    @POST("index.php?wsc-connect-api")
    Call<List<ConversationModel>> getConversations(@Part("type") RequestBody type, @Part("limit") RequestBody limit, @Part("offset") RequestBody offset);

    @Multipart
    @POST("index.php?wsc-connect-api")
    Call<List<ConversationMessageModel>> getConversationMessages(@Part("type") RequestBody type, @Part("id") RequestBody conversationID, @Part("limit") RequestBody limit, @Part("offset") RequestBody offset);

    @Multipart
    @POST("index.php?wsc-connect-api")
    Call<ConversationMessageModel> getConversationMessage(@Part("type") RequestBody type, @Part("id") RequestBody messageID);

    @Multipart
    @POST("index.php?wsc-connect-api")
    Call<ConversationMessageModel> addConversationMessage(@Part("type") RequestBody type, @Part("id") RequestBody conversationID, @Part("message") RequestBody message);
}
