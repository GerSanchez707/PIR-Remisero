package Drivers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.PIR.pir.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Clients.DetailRequestActivity;
import Clients.RequestDriverActivity;
import Providers.AuthProvider;
import Providers.ClientBookingProvider;
import Providers.ClientProvider;
import Providers.DriverProvider;
import Providers.GeoFireProvider;
import Providers.GoogleApiProvider;
import Providers.NotificationProvider;
import Providers.TokenProvider;
import models.ClientBooking;
import models.FCMBody;
import models.FCMResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.DecodePoints;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeoFireProvider mGeoFireProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private final static int LOCATION_REQUEST_CODE  =1;
    private final static int SETTINGS_REQUEST_CODE  =2;

    private Marker mMarker;
    private LatLng mCurrentLatlng;
    private TokenProvider mTokenProvider;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;

    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;


    private  String mExtraclientId;
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;
    private DriverProvider mDriverProvider;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private boolean mFirstTime = true;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;

    private boolean mIsCloseToClient=false;

    private NotificationProvider nNotificationProvider;

    private ImageView mimageviewBooking;


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mCurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_sed_n_48))
                    );

                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));

                    updateLocation();

                    if(mFirstTime){
                        mFirstTime=false;
                        getClientBooking();
                    }
                }

            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);
        mAuthProvider= new AuthProvider();
        mGeoFireProvider = new  GeoFireProvider("drivers_working");
        mTokenProvider= new TokenProvider();

        mFusedLocation= LocationServices.getFusedLocationProviderClient(this);

        mMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);


        mTextViewClientBooking = findViewById(R.id.textViewclientBooking);
        mTextViewEmailClientBooking= findViewById(R.id.textViewEmailclientBooking);
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginclientBooking);
        mTextViewDestinationClientBooking= findViewById(R.id.textViewDestinationclientBooking);

        mClientProvider = new ClientProvider();
        mClientBookingProvider= new ClientBookingProvider();
        mExtraclientId = getIntent().getStringExtra("idClient");
        mButtonStartBooking= findViewById(R.id.btnStartBooking);
        mButtonFinishBooking= findViewById(R.id.btnFinishBooking);

        //mButtonStartBooking.setEnabled(false);

        mGoogleApiProvider= new GoogleApiProvider(MapDriverBookingActivity.this);

        nNotificationProvider= new NotificationProvider();

        mimageviewBooking= findViewById(R.id.imageViewClientBooking);
        getClient();

        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCloseToClient) {
                    startBooking();
                }
                else{
                    Toast.makeText(MapDriverBookingActivity.this,"Debes estar mas cerca del punto de inicio de viaje", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishBooking();
            }
        });


    }

    private void finishBooking() {
        mClientBookingProvider.updateStatus(mExtraclientId, "finish");
        mClientBookingProvider.updateIdHistoryBooking(mExtraclientId);
        sendNotification("Viaje finalizado");
        if(mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
        mGeoFireProvider.removeLocation(mAuthProvider.getId());
        Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClientActivity.class);
        intent.putExtra("idClient",mExtraclientId);
        startActivity(intent);
        finish();
    }

    private void startBooking() {
        mClientBookingProvider.updateStatus(mExtraclientId, "start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE); //revisar
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue_)));
        drawRoute(mDestinationLatLng);
        sendNotification("Viaje Iniciado");
    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng)
    {
        double distance=0;
        Location clientLocation= new Location("");
        Location driverLocation= new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driverLocation);
        return distance;

    }


    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraclientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String destination =dataSnapshot.child("destination").getValue().toString();
                    String origin =dataSnapshot.child("origin").getValue().toString();
                    double destinationLat= Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng= Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat= Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng= Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng= new LatLng(originLat,originLng);
                    mDestinationLatLng= new LatLng(destinationLat,destinationLng);
                    mTextViewOriginClientBooking.setText("retirar cliente en: " + origin);
                    mTextViewDestinationClientBooking.setText("destino: " + destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red_)));
                    drawRoute(mOriginLatLng);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void drawRoute(LatLng latLng)
    {
        mGoogleApiProvider.getDirections(mCurrentLatlng, mOriginLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route= jsonArray.getJSONObject(0);
                    JSONObject poliylines = route.getJSONObject("overview_polyline");
                    String points = poliylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions= new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);
                    JSONArray legs= route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance= leg.getJSONObject("distance");
                    JSONObject duration= leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText=  duration.getString("text");
                }catch (Exception e) {
                    Log.d("Error","Error encontrando" + e.getMessage());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


    private void getClient() {
        mClientProvider.getClient(mExtraclientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                  String email=dataSnapshot.child("email").getValue().toString();
                  String name=dataSnapshot.child("email").getValue().toString();
                  String image= "";
                  if(dataSnapshot.hasChild("image"))
                  {
                      image = dataSnapshot.child("image").getValue().toString();
                      Picasso.with(MapDriverBookingActivity.this).load(image).into(mimageviewBooking);
                  }
                    mTextViewClientBooking.setText(name);
                  mTextViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation() {
        if(mAuthProvider.existSession() && mCurrentLatlng != null) {
            mGeoFireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatlng);
            if (!mIsCloseToClient) {
                if (mOriginLatLng != null && mCurrentLatlng != null) {
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatlng); //metros
                    if (distance <= 200) {
                        //mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca del punto de inicio de viaje", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);

        mLocationRequest= new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==LOCATION_REQUEST_CODE)
        {
            if(grantResults.length >0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback , Looper.myLooper() );
                    mMap.setMyLocationEnabled(true);
                }
                else {
                    checkLocationPermissions();
                }
            }
            else{
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived())  {
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();

    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private  void disconnect(){

        if(mFusedLocation != null)
        {

            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if(mAuthProvider.existSession()) {
                mGeoFireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else{
            Toast.makeText(this,"No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }
    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }


    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        }
    }
    private void sendNotification(final String status) {
        mTokenProvider.getToken(mExtraclientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String token= dataSnapshot.child("token").getValue().toString();
                    Map<String,String> map = new HashMap<>();
                    map.put("title","ESTADO DE TU VIAJE" );
                    map.put("body",
                            "Tu estado de viaje es:" + status
                    );
                    FCMBody fcmBody=new FCMBody(token,"high","4500s" ,map);
                    nNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() != 1) {
                                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT);

                                }
                            }
                            else{
                                Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT);
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
                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion porque el " +
                            "remisero no tiene un token de sesion", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //*nNotificationProvider.sendNotification();
    }


}
