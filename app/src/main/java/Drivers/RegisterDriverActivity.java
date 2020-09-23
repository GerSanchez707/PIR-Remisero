package Drivers;

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
import android.widget.Spinner;
import android.widget.Toast;

import com.PIR.pir.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import Clients.RegisterActivity;
import Includes.MyToolbar;
import Providers.AuthProvider;
import Providers.ClientProvider;
import Providers.DriverProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;
import dmax.dialog.SpotsDialog;
import models.Client;
import models.Driver;

public class RegisterDriverActivity extends AppCompatActivity {

    @BindView(R.id.tietNombreCompleto)
    TextInputEditText tietNombreCompleto;
    @BindView(R.id.tilNombreCompleto)
    TextInputLayout tilNombreCompleto;
    @BindView(R.id.tietEmail)
    TextInputEditText tietEmail;
    @BindView(R.id.tilEmail)
    TextInputLayout tilEmail;
    @BindView(R.id.tietMarcaAuto)
    TextInputEditText tietMarcaAuto;
    @BindView(R.id.tilMarcaAuto)
    TextInputLayout tilMarcaAuto;

    @BindView(R.id.tietPatenteAuto)
    TextInputEditText tietPatenteAuto;
    @BindView(R.id.tilPatenteAuto)
    TextInputLayout tilPatenteAuto;

   private  Spinner SpinnerRemisera;

    private Spinner mSpinner;

    @BindView(R.id.tietPassword)
    TextInputEditText tietPassword;
    @BindView(R.id.tilPassword)
    TextInputLayout tilPassword;
    @BindView(R.id.btnRegistrarCuenta)
    Button btnRegistrarCuenta;

    private AuthProvider mAuthProvider;
    private DriverProvider mDriverProvider;

    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        ButterKnife.bind(this);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        mDialog = new SpotsDialog.Builder().setContext(this).setMessage("Completando registro...").setCancelable(false).build();

        tietNombreCompleto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!tietNombreCompleto.getText().toString().trim().isEmpty())
                    tietNombreCompleto.setError(null);
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
                if(!tietEmail.getText().toString().trim().isEmpty())
                    tietEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        /*tietEmail.addTextChangedListener(new TextWatcher() {
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
        });   */

        tietMarcaAuto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!tietMarcaAuto.getText().toString().trim().isEmpty())
                    tietMarcaAuto.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tietPatenteAuto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!tietPatenteAuto.getText().toString().trim().isEmpty())
                    tietPatenteAuto.setError(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SpinnerRemisera= findViewById(R.id.spinnerremisera);

        mSpinner = findViewById(R.id.spinner);




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
            if (tilEmail.getError() == null && tilPassword.getError() == null && mSpinner.getSelectedItemPosition() == 1 ) {
                validateInputs(Objects.requireNonNull(tietNombreCompleto.getText()).toString().trim(),
                        Objects.requireNonNull(tietEmail.getText()).toString().trim(),
                        Objects.requireNonNull(tietPassword.getText()).toString().trim(),
                        Objects.requireNonNull(tietMarcaAuto.getText()).toString().trim(),
                        Objects.requireNonNull(tietPatenteAuto.getText()).toString().trim(),
                        Objects.requireNonNull(SpinnerRemisera.getSelectedItem()).toString().trim(),
                        "si");

            }
            if (tilEmail.getError() == null && tilPassword.getError() == null && mSpinner.getSelectedItemPosition() == 2) {
                validateInputs(Objects.requireNonNull(tietNombreCompleto.getText()).toString().trim(),
                        Objects.requireNonNull(tietEmail.getText()).toString().trim(),
                        Objects.requireNonNull(tietPassword.getText()).toString().trim(),
                        Objects.requireNonNull(tietMarcaAuto.getText()).toString().trim(),
                        Objects.requireNonNull(tietPatenteAuto.getText()).toString().trim(),
                        Objects.requireNonNull(SpinnerRemisera.getSelectedItem()).toString().trim(),
                        "no");

            }

        });
    }

    private void validateInputs(String nombre, String email, String pass,String marca, String patente, String remisera, String AptoparanoVidentes) {
        if (nombre.isEmpty()) {
            tilNombreCompleto.setError("Este campo es obligatorio");
            return;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Este campo es obligatorio");
            return;
        }

        if(marca.isEmpty()){
            tilMarcaAuto.setError("Este campo es obligatorio");
            return;
        }

        if(patente.isEmpty()){
            tilPatenteAuto.setError("Este campo es obligatorio");
            return;
        }
        if(remisera.isEmpty()){
            tilPatenteAuto.setError("Este campo es obligatorio");
            return;
        }
        if(AptoparanoVidentes.isEmpty()){
            tilPatenteAuto.setError("Este campo es obligatorio");
            return;
        }

        if (pass.isEmpty()) {
            tilPassword.setError("Este campo es obligatorio");
            return;
        }

        mDialog.show();
        register(nombre, email, pass,marca,patente,remisera,AptoparanoVidentes);

    }

    private void register(String nombre, String email, String pass, String marca, String patente, String remisera, String Aptoparanovidentes) {
        mAuthProvider.register(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                String id = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                create(new Driver(id,nombre,email,marca,patente.toUpperCase(),remisera,Aptoparanovidentes));

            } else {
                Toast.makeText(RegisterDriverActivity.this, "Ocurrio un error al registrarse, vuelva a intentarlo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void create(Driver driver) {
        mDriverProvider.create(driver).addOnCompleteListener(task -> {
            mDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(RegisterDriverActivity.this, "Se registro el usuario correctamente", Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {
                Toast.makeText(RegisterDriverActivity.this, "Ocurrio un error al registrarse, vuelva a intentarlo", Toast.LENGTH_LONG).show();
            }
        });
    }



}
