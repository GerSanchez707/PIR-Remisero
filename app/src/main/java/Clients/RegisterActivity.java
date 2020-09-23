package Clients;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.PIR.pir.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import Drivers.MapDriverActivity;
import Drivers.RegisterDriverActivity;
import Includes.MyToolbar;
import Providers.AuthProvider;
import Providers.ClientProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;
import dmax.dialog.SpotsDialog;
import models.Client;

public class RegisterActivity extends AppCompatActivity {


    @BindView(R.id.tietNombreCompleto)
    TextInputEditText tietNombreCompleto;
    @BindView(R.id.tilNombreCompleto)
    TextInputLayout tilNombreCompleto;
    @BindView(R.id.tietEmail)
    TextInputEditText tietEmail;
    @BindView(R.id.tilEmail)
    TextInputLayout tilEmail;
    @BindView(R.id.tietPassword)
    TextInputEditText tietPassword;
    @BindView(R.id.tilPassword)
    TextInputLayout tilPassword;
    @BindView(R.id.btnRegistrarCuenta)
    Button btnRegistrarCuenta;
    private String mTypeUser;

    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        SharedPreferences mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        mTypeUser = mPref.getString("user", "");

        mDialog = new SpotsDialog.Builder().setContext(this).setMessage("Completando registro...").setCancelable(false).build();

        tietNombreCompleto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!tietNombreCompleto.getText().toString().isEmpty())
                    tilNombreCompleto.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        tietEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!tietEmail.getText().toString().isEmpty())
                    tietEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*
        tietEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Utils.validate(Objects.requireNonNull(tietEmail.getText()).toString().trim())) {
                    tilEmail.setError("Debe ingresar un email válido");
                } else {
                    tilEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


         */
        tietPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(tietPassword.getText()).toString().length() >= 6) {
                    tilPassword.setError(null);
                } else {
                    tilPassword.setError("Debe tener al menos 6 caracteres");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnRegistrarCuenta.setOnClickListener(v -> {
            if (tilEmail.getError() == null && tilPassword.getError() == null) {
                validateInputs(Objects.requireNonNull(tietNombreCompleto.getText()).toString().trim(),
                        Objects.requireNonNull(tietEmail.getText()).toString().trim(),
                        Objects.requireNonNull(tietPassword.getText()).toString().trim());
            }
        });
    }

    private void validateInputs(String nombre, String email, String pass) {
        if (nombre.isEmpty()) {
            tilNombreCompleto.setError("Este campo es obligatorio");
            return;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Este campo es obligatorio");
            return;
        }

        if (pass.isEmpty()) {
            tilPassword.setError("Este campo es obligatorio");
            return;
        }

        mDialog.show();
        register(nombre,email,pass);

    }

    private void register(String nombre, String email, String pass) {
        mAuthProvider.register(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                String id = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                create(new Client(id,nombre,email));

            } else {
                Toast.makeText(RegisterActivity.this, "Ocurrio un error al registrarse, vuelva a intentarlo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void create(Client client){
        mClientProvider.create(client).addOnCompleteListener(task -> {
            mDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Se registro el usuario correctamente", Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {
                Toast.makeText(RegisterActivity.this, "Ocurrio un error al registrarse, vuelva a intentarlo", Toast.LENGTH_LONG).show();
            }
        });
    }

}

  /*
    void saveUser(String id, String name, String email) {
        String selectedUser = mPref.getString("user", "");
        User user= new User();
        user.setEmail(email);
        user.setName(name);
            if(selectedUser.equals("driver")) {
                mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(RegisterActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if(selectedUser.equals("client"))
            {
                mDatabase.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(RegisterActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        */


