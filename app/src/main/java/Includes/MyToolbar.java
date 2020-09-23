package Includes;

import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.PIR.pir.R;

public class MyToolbar {

    public static void show(AppCompatActivity activity, String title, boolean upButton) {
        activity.setSupportActionBar(activity.findViewById(R.id.toolbar));
        activity.getSupportActionBar().setTitle("title");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }
}
