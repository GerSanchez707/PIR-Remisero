package Providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import models.Client;
import models.Driver;

public class DriverProvider {

    DatabaseReference mDatabase;

    public DriverProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
    }

    public Task<Void> create(Driver driver)
    {
        return mDatabase.child(driver.getId()).setValue(driver);
    }

    public  DatabaseReference getDriver( String IdDriver) {
        return mDatabase.child(IdDriver);
    }

    public Task<Void> update(Driver driver)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", driver.getName());
        map.put("image", driver.getImage());
        map.put("vehicleBrand", driver.getVehicleBrand());
        map.put("vehiclePlate", driver.getVehiclePlate());
        map.put("remisera", driver.getRemisera());
        map.put("aptoNV", driver.getAptoNV());

        return mDatabase.child(driver.getId()).updateChildren(map);
    }
 }

