package Clients;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.PIR.pir.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import Providers.DriverProvider;
import Providers.HistoryBookingProvider;
import de.hdodenhof.circleimageview.CircleImageView;
import models.HistoryBooking;

public class HistoryBookingDetailClientActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mcircleImageBack;


    private String mExtraId;
    private HistoryBookingProvider mHistoryBookingProvider;
    private DriverProvider mDriverProvider;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_client);

        mTextViewName= findViewById(R.id.textViewNameBookingDetail);
        mTextViewOrigin= findViewById(R.id.textViewOriginHistoryBookingDetail);
        mTextViewDestination= findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewCalification= findViewById(R.id.textViewCalificationHistoryBookingDetail);
        mRatingBarCalification= findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage= findViewById(R.id.circleImageHistoryBookingDetail);
        mcircleImageBack= findViewById(R.id.circleImageBack);


        mDriverProvider= new DriverProvider();
        mExtraId= getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider= new HistoryBookingProvider();
        getHistoryBooking();


        mcircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    HistoryBooking historyBooking= dataSnapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewCalification.setText("Tu Calificacion:" +historyBooking.getCalificationDriver());

                    if(dataSnapshot.hasChild("calificationClient")) {
                        mRatingBarCalification.setRating((float) historyBooking.getCalificationClient());
                    }
                    mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                String name= dataSnapshot.child("name").getValue().toString();
                                mTextViewName.setText(name.toUpperCase());
                                if(dataSnapshot.hasChild("image")) {
                                    String image= dataSnapshot.child("name").getValue().toString();
                                    Picasso.with(HistoryBookingDetailClientActivity.this).load(image).into(mCircleImage);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
