package Clients;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.PIR.pir.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Drivers.MapDriverBookingActivity;
import Providers.AuthProvider;
import Providers.ClientBookingProvider;
import Providers.DriverProvider;
import Providers.GeoFireProvider;
import Providers.GoogleApiProvider;
import Providers.TokenProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.DecodePoints;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;

    private GeoFireProvider mGeoFireProvider;
    private TokenProvider mTokenProvider;

    private Marker mMarkerDriver;

    private boolean mFirstTime = true;

    private PlacesClient mPlaces;

    private String mOrigin;
    private LatLng mOriginLatLng;

    private String mDestination;
    private LatLng mDestinationLatLng;
    private LatLng mDriverLatLng;
    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;
    private TextView mTextViewStatusBooking;

    private ClientBookingProvider mClientBookingProvider;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private DriverProvider mDriverProvider;

    private ValueEventListener mListener;
    private String mIdDriver;
    private ValueEventListener mListenerStatus;
    private ImageView mimageviewBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);
        mAuthProvider = new AuthProvider();

        mGeoFireProvider = new GeoFireProvider("drivers_working");
        mTokenProvider= new TokenProvider();
        mGoogleApiProvider= new GoogleApiProvider(MapClientBookingActivity.this);
        mClientBookingProvider= new ClientBookingProvider();

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mDriverProvider= new DriverProvider();



        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }


        mTextViewClientBooking = findViewById(R.id.textViewdriverBooking);
        mTextViewEmailClientBooking= findViewById(R.id.textViewEmaildriverBooking);
        mTextViewOriginClientBooking = findViewById(R.id.textViewOrigindriverBooking);
        mTextViewDestinationClientBooking= findViewById(R.id.textViewDestinationdriverBooking);
        mTextViewStatusBooking= findViewById(R.id.textviewStatusBooking);

        mimageviewBooking= findViewById(R.id.imageViewClientBooking);

        getStatus();
        getClientBooking();

    }

    private void getStatus() {
        mListenerStatus =   mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status=dataSnapshot.getValue().toString();
                    mTextViewClientBooking.setText(status);
                    if(status.equals("accept"))
                    {
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }
                    if(status.equals("start"))
                    {
                        mTextViewStatusBooking.setText("Estado: Viaje Iniciado");
                        startBooking();
                    }
                    else if(status.equals("finish")){
                        mTextViewStatusBooking.setText("Estado: Viaje Finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
        finish();

    }

    private void startBooking() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue_)));
        drawRoute(mDestinationLatLng);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener != null)
        {
            mGeoFireProvider.getDriverLocation(mIdDriver).removeEventListener(mListener);
        }
        if(mListenerStatus != null){
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String destination =dataSnapshot.child("destination").getValue().toString();
                    String origin =dataSnapshot.child("origin").getValue().toString();
                    String IdDriver= dataSnapshot.child("idDriver").getValue().toString();

                    mIdDriver=IdDriver;
                    double destinationLat= Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng= Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat= Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng= Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng= new LatLng(originLat,originLng);
                    mDestinationLatLng= new LatLng(destinationLat,destinationLng);
                    mTextViewOriginClientBooking.setText("retirar cliente en: " + origin);
                    mTextViewDestinationClientBooking.setText("destino: " + destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red_)));
                    getDriver(IdDriver);
                    getDriverLocation(IdDriver);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    private void getDriver(String IdDriver)
    {
        mDriverProvider.getDriver(IdDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String name= dataSnapshot.child("name").getValue().toString();
                    String email= dataSnapshot.child("email").getValue().toString();
                    String image= "";
                    if(dataSnapshot.hasChild("image"))
                    {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(MapClientBookingActivity.this).load(image).into(mimageviewBooking);
                    }
                    mTextViewClientBooking.setText(name);
                    mTextViewClientBooking.setText(email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {
         mListener=   mGeoFireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        double lat= Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                        double lng= Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                        mDriverLatLng= new LatLng(lat,lng);
                        if(mMarkerDriver != null){
                           mMarkerDriver.remove();
                       }
                        mMarkerDriver = mMap.addMarker(new MarkerOptions().position(
                                new LatLng(lat,lng)
                        )
                                .title("Tu remisero")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_sed_n_48)));
                             if(mFirstTime){
                                 mFirstTime=false;
                                 mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                         new CameraPosition.Builder()
                                                 .target(mDriverLatLng)
                                                 .zoom(14f)
                                                 .build()
                                 ));
                                 drawRoute(mOriginLatLng);
                             }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void drawRoute(LatLng latLng)
    {
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
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






        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMyLocationEnabled(true);





        }




}
