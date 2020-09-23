package Providers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import utils.CompressorBitmapImage;

public class ImageProvider {

    private StorageReference mStorage;

    public ImageProvider(String ref){
       mStorage= FirebaseStorage.getInstance().getReference().child(ref);
    }
    public UploadTask saveImage(Context context, File image, String idUser){
        byte[] imageByte= CompressorBitmapImage.getImage(context, image.getPath(), 500 ,500);
        StorageReference storage= mStorage.child(idUser + ".jpg");
        mStorage= storage;
        UploadTask uploadTask= storage.putBytes(imageByte);
        return uploadTask;
    }
    public StorageReference getStorage()
    {
        return mStorage;
    }
}
