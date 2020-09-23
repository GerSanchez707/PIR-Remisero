package Providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import models.ClientBooking;
import models.HistoryBooking;

public class HistoryBookingProvider {

    private DatabaseReference mDatabase;

    public HistoryBookingProvider()
    {
        mDatabase= FirebaseDatabase.getInstance().getReference().child(("HistoryBooking"));
    }
    public Task<Void> create  (HistoryBooking historyBooking)
    {
        return mDatabase.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    public Task<Void> updateCalificationClient(String idHistoryBooking , float calificationClient){
        Map<String, Object> map= new HashMap<>();
        map.put("calificationclient",calificationClient);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public Task<Void> updateCalificationDriver(String idHistoryBooking , float calificationDriver){
        Map<String, Object> map= new HashMap<>();
        map.put("calificationDriver",calificationDriver);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public DatabaseReference getHistoryBooking(String idHistoryBooking)
    {
        return mDatabase.child(idHistoryBooking);
    }

}
