package receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import Drivers.MapDriverBookingActivity;
import Providers.AuthProvider;
import Providers.ClientBookingProvider;
import Providers.GeoFireProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private GeoFireProvider mGeoFireProvider;
    private AuthProvider mAuthProvider;


    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider = new AuthProvider();
        mGeoFireProvider= new GeoFireProvider("active_drivers");
        mGeoFireProvider.removeLocation(mAuthProvider.getId());

        String idClient= intent.getExtras().getString("idClient");
        mClientBookingProvider= new ClientBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "accept");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient",idClient);

        context.startActivity(intent1);
    }
}
