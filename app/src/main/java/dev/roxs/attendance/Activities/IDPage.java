package dev.roxs.attendance.Activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

import dev.roxs.attendance.R;

public class IDPage extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idpage);
        //hooks
        ImageView profileImage = findViewById(R.id.profileImage);
        File privateDir = getApplicationContext().getFilesDir();
        File imageFile = new File(privateDir, "captured_image.jpg");

        if (imageFile.exists()) {
            Bitmap loadedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            // Set the loaded bitmap to the profileImage ImageView
            profileImage.setImageBitmap(loadedBitmap);
        }
        if (ContextCompat.checkSelfPermission(IDPage.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(IDPage.this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
        //hooks
        RelativeLayout markAttendanceBtn = findViewById(R.id.markAttendanceBtn);
        markAttendanceBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), QRReader.class)));


    }

}