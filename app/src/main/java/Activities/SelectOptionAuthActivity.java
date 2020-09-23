package Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.PIR.pir.R;

import Clients.RegisterActivity;
import Drivers.RegisterDriverActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.PIR.pir.R.id.btnGoToLogin;
/*import static com.PIR.pir.R2.id.btnRegister; */


public class SelectOptionAuthActivity extends AppCompatActivity {

    private SharedPreferences mPref;

    private Button mButtonGoToLogin;
    private Button mButtonGoToRegister;
    private CircleImageView mcircleImageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Seleccionar opción");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mcircleImageBack= findViewById(R.id.circleImageBack);

         /* video actual */
        mButtonGoToLogin = findViewById(btnGoToLogin);
        mButtonGoToRegister = findViewById(R.id.btnGoToRegister);
        mButtonGoToLogin.setOnClickListener(view -> goToLogin());
        mButtonGoToRegister.setOnClickListener(v -> goToRegister());
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        mcircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void goToLogin() {
        Intent intent = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(intent);
    }
    public void goToRegister() {
        String typeUser = mPref.getString( "user","");
        if (typeUser.equals("client")) {
        Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
        else {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterDriverActivity.class);
            startActivity(intent);
        }
    }

}





