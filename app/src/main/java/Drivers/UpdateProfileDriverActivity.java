package Drivers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.PIR.pir.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import Clients.UpdateProfileActivity;
import Includes.MyToolbar;
import Providers.AuthProvider;
import Providers.ClientProvider;
import Providers.DriverProvider;
import Providers.ImageProvider;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Client;
import models.Driver;
import utils.CompressorBitmapImage;
import utils.FileUtil;

public class UpdateProfileDriverActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;
    private TextView mTextBrandVehicle;
    private TextView mTextViewPlate;
    private TextView mTextViewRemisera;
    private TextView mTextViewNoVidente;

    private DriverProvider mDriverProvider;
    private AuthProvider mAuthProvider;

    private File mImageFile;
    private String mImage;

    private final int  GALLERY_REQUEST = 1;

    private ProgressDialog mProgressDialog;

    private String mName;

    private String mVehicleBrand;
    private String mVehiclePlate;
    private String mRemisera;
    private String mNovidente;
    private ImageProvider mimageProvider;

    private CircleImageView mcircleImageBack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_driver);
        mImageViewProfile=findViewById(R.id.imageViewProfile);
        mButtonUpdate= findViewById(R.id.btnUpdateProfile);
        mTextViewName= findViewById(R.id.textInputName);
        mTextBrandVehicle=findViewById(R.id.textInputVehicleBrand);
        mTextViewPlate=findViewById(R.id.textInputVehiclePlate);
        mTextViewRemisera=findViewById(R.id.textRemisera);
        mTextViewNoVidente=findViewById(R.id.textAptoParaNoVidentes);
        mcircleImageBack= findViewById(R.id.circleImageBack);



        mDriverProvider= new DriverProvider();
        mAuthProvider= new AuthProvider();
        mimageProvider= new ImageProvider("driver_images");

        mProgressDialog= new ProgressDialog(this);

        getDriverInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        mcircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("Image/*");
        startActivityForResult(galleryIntent,GALLERY_REQUEST );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK){
            try{
                mImageFile = FileUtil.from(this,data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e){
                Log.d("ERROR","Mensaje" +e.getMessage());
            }

        }
    }

    private  void getDriverInfo()
    {
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String name= dataSnapshot.child("name").getValue().toString();
                    String VehicleBrand= dataSnapshot.child("vehicleBrand").getValue().toString();
                    String VehiclePlate= dataSnapshot.child("vehiclePlate").getValue().toString();
                    String remisera= dataSnapshot.child("remisera").getValue().toString();
                    String aptoNV= dataSnapshot.child("aptoNV").getValue().toString();
                    String image="";
                    if(dataSnapshot.hasChild("image"))
                    {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileDriverActivity.this).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                    mTextBrandVehicle.setText(VehicleBrand);
                    mTextViewPlate.setText(VehiclePlate);
                    mTextViewRemisera.setText(remisera);
                    mTextViewNoVidente.setText(aptoNV);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        mName = mTextViewName.getText().toString();
        mVehicleBrand= mTextBrandVehicle.getText().toString();
        mVehiclePlate= mTextViewPlate.getText().toString();
        mRemisera= mTextViewRemisera.getText().toString();
        mNovidente= mTextViewNoVidente.getText().toString();
        if(!mName.equals("") && mImageFile!= null){
            mProgressDialog.setMessage("Espere un momento..");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();
        }
        else
        {
            Toast.makeText(this,"Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        mimageProvider.saveImage(UpdateProfileDriverActivity.this,mImageFile,mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    mimageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image= uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setName(mName);
                            driver.setId(mAuthProvider.getId());
                            driver.setVehicleBrand(mVehicleBrand);
                            driver.setVehiclePlate(mVehiclePlate);
                            driver.setRemisera(mRemisera);
                            driver.setAptoNV(mNovidente);

                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileDriverActivity.this,"Su informacion se actualizo correctamente" ,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else
                {
                    Toast.makeText(UpdateProfileDriverActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
