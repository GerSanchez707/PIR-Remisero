package Clients;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.PIR.pir.R;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Providers.AuthProvider;
import Providers.ClientBookingProvider;
import Providers.GeoFireProvider;
import Providers.GoogleApiProvider;
import Providers.NotificationProvider;
import Providers.TokenProvider;
import models.ClientBooking;
import models.FCMBody;
import models.FCMResponse;
import models.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.DecodePoints;

public class RequestDriverActivity extends AppCompatActivity {
    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingfor;
    private Button mButtonCancelRequest;
    //obtener conductor mas cercano
    private GeoFireProvider mGeofireProvider;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private LatLng mOriginLatLng;
    private double mRadius= 0.1;
    private boolean mDriverfound=false;
    private String mIdDriverfound ="";
    //almacenara la long y lat del conductor encontrado
    private LatLng mDriverfoundLatLng;
    //post conductor mas cercano
    private NotificationProvider nNotificationProvider;
    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;
    private String mExtraOrigin;
    private  String mExtraDestination;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private ValueEventListener mListener;





    //





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        mAnimation= findViewById(R.id.animation);
        mTextViewLookingfor= findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest= findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();
       //obtner conductor mas cercano
        mExtraOriginLat=getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng=getIntent().getDoubleExtra("origin_lng", 0);
        mOriginLatLng= new LatLng(mExtraOriginLat,mExtraOriginLng);
        mGeofireProvider= new GeoFireProvider("active_drivers");
        //post conductor mas cercano
        mTokenProvider= new TokenProvider();
        nNotificationProvider= new NotificationProvider();
        mClientBookingProvider= new ClientBookingProvider();
        mAuthProvider= new AuthProvider();
        mExtraOrigin= getIntent().getStringExtra("origin");
        mExtraDestination= getIntent().getStringExtra("destination");
        mExtraDestinationLat= getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng= getIntent().getDoubleExtra("destination_lng",0);
        mDestinationLatLng= new LatLng(mExtraDestinationLat,mExtraDestinationLng);
        mGoogleApiProvider= new GoogleApiProvider(RequestDriverActivity.this);


        mButtonCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              cancelRequest();
            }
        });

        //

        getClosesDriver();

    }

    private void cancelRequest() {
        mClientBookingProvider.delete(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                    sendNotificationCancel();
            }
        });
    }

    // Obtner conductor mas cercano en un radio 0.1 km
    private void getClosesDriver(){
        mGeofireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {

            // location almacena la latitud y longitud donde se encuentra el conductor
            //key es la clave del conductor
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // verificamos si no tenemos un conductor encontrado
                if(!mDriverfound)
                {   // encontro conductor
                    mDriverfound= true;
                    mIdDriverfound= key;
                    mDriverfoundLatLng= new LatLng(location.latitude,location.longitude);
                    mTextViewLookingfor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");
                    //post conductor mas cercano

                    //
                    createClientBooking();
                    Log.d("DRIVER","ID: " + mIdDriverfound);

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }
            // cuando finaliza busqueda de conductor en radio de 0.1km ingresa al siguiente metodo...onGeoQueryReady()
            @Override
            public void onGeoQueryReady() {
                //ingresa cuando termina la busqueda de conductor en radio de 0.1 km
                if(!mDriverfound)
                {
                    mRadius= mRadius + 0.1f;
                    // no encontro conductor
                    if (mRadius > 5)
                    {
                        mTextViewLookingfor.setText("NO SE ENCONTRO CONDUCTOR");
                        Toast.makeText(RequestDriverActivity.this,"NO SE ENCONTRO UN CONDUCTOR",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        getClosesDriver();
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    // post post conductor

    private void createClientBooking()
    {
        mGoogleApiProvider.getDirections(mOriginLatLng,mDriverfoundLatLng).enqueue(new Callback<String>() {
        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            try {
                JSONObject jsonObject = new JSONObject(response.body());
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                JSONObject route= jsonArray.getJSONObject(0);
                JSONObject poliylines = route.getJSONObject("overview_polyline");
                String points = poliylines.getString("points");

                JSONArray legs= route.getJSONArray("legs");
                JSONObject leg = legs.getJSONObject(0);
                JSONObject distance= leg.getJSONObject("distance");
                JSONObject duration= leg.getJSONObject("duration");
                String distanceText = distance.getString("text");
                String durationText=  duration.getString("text");
                sendNotification(durationText,distanceText);


            }catch (Exception e) {
                Log.d("Error","Error encontrando" + e.getMessage());
            }
        }

        @Override
        public void onFailure(Call<String> call, Throwable t) {

        }
    });



    }


    //post conductor mas cercano
    //datasnapshot contiene toda la informacion del nodo

    private void sendNotificationCancel()
    {
        mTokenProvider.getToken(mIdDriverfound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String token= dataSnapshot.child("token").getValue().toString();
                    Map<String,String> map = new HashMap<>();
                    map.put("title","VIAJE CANCELADO");
                    map.put("body",
                            "El cliente cancelo la solicitud"
                    );




                    FCMBody fcmBody=new FCMBody(token,"high","4500s", map);
                    nNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    Toast.makeText(RequestDriverActivity.this, "La notificacion se cancelo correctamente", Toast.LENGTH_SHORT);
                                    Intent intent =new Intent(RequestDriverActivity.this,MapClientActivity.class);
                                    startActivity(intent);
                                    finish();
                                    // Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT);
                                } else {
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT);
                                }
                            }
                            else{
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT);
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error","Error" + t.getMessage());
                        }
                    });
                }
                else
                {
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el remisero no tiene un token de sesion", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void sendNotification(final String time,final String km) {
        mTokenProvider.getToken(mIdDriverfound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String token= dataSnapshot.child("token").getValue().toString();
                    Map<String,String> map = new HashMap<>();
                    map.put("title","SOLICITUD DE SERVICIO A" + time + "de tu posicion");
                    map.put("body",
                            "Un cliente esta solicitando un servicio a una distancia de " + km + "\n" +
                                    "Recoger en " + mExtraOrigin +"\n" +
                                    "Destino:" +mExtraDestination
                    );
                    map.put("idClient", mAuthProvider.getId());
                    map.put("origin", mExtraOrigin);
                    map.put("destination", mExtraDestination);
                    map.put("min",time) ;
                    map.put("distance", km);



                    FCMBody fcmBody=new FCMBody(token,"high","4500s", map);
                    nNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    ClientBooking clientBooking= new ClientBooking(
                                            mAuthProvider.getId(),
                                            mIdDriverfound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng

                                    );
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            checkStatusClientBooking();

                                            //Toast.makeText(RequestDriverActivity.this,"La peticion se creo correctamente",Toast.LENGTH_SHORT);
                                        }
                                    });
                                   // Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT);
                                } else {
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT);
                                }
                            }
                            else{
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT);
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error","Error" + t.getMessage());
                        }
                    });
                }
                else
                {
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el remisero no tiene un token de sesion", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //*nNotificationProvider.sendNotification();
    }

    private void checkStatusClientBooking() {
       mListener= mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String status= dataSnapshot.getValue().toString();
                    if(status.equals("accept")) {
                        Intent intent = new Intent(RequestDriverActivity.this,MapClientBookingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(status.equals("cancel")){
                        Toast.makeText(RequestDriverActivity.this,"El conductor no acepto el viaje", Toast.LENGTH_SHORT);
                        Intent intent = new Intent(RequestDriverActivity.this,MapClientActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener!=null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
}
