package Providers;

import Retrofit.IFCMApi;
import Retrofit.RetrofitClient;
import models.FCMBody;
import models.FCMResponse;
import retrofit2.Call;

public class NotificationProvider {
    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
