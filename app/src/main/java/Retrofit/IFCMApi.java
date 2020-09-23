package Retrofit;

import models.FCMBody;
import models.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers( {
            "Content-Type:application/json",
            "Authorization:key=AAAAYPwF0hc:APA91bHTX-3aN0I0hGGN_Hh1KzfBzTLp5pnF4WecxZme4QuZh4bWMs8bxXJBkSvCNTxNc5FQL7pNVkHGmCNDsRfd14D8Gsr9DQOP2YGUr26C_H0sqm7Z76ANeusXdNG9zCoKBOD6tL9T"
    }

    )
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
