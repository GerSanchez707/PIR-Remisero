package Providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import models.Token;

public class TokenProvider {

    private DatabaseReference mDatabase;

    public TokenProvider() {
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(String idUser) {
        if (idUser == null) return;
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            Token token = new Token(instanceIdResult.getToken());
            mDatabase.child(idUser).setValue(token);
        });
    }

    public DatabaseReference getToken(String idUser) {
        return mDatabase.child(idUser);
    }
}

