package dev.roxs.attendance.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;

import dev.roxs.attendance.R;

public class SendAttendanceData extends AppCompatActivity {

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_attendance_data);
        //get session ID
        String sessionID = getIntent().getStringExtra("sessionID");
        //get Captured image
        File privateDir = getApplicationContext().getFilesDir();
        File imageFile = new File(privateDir, "captured_image.jpg");

        if (imageFile.exists()) {
            Bitmap loadedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            ByteArrayOutputStream imageAsByte = new ByteArrayOutputStream();
            loadedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageAsByte);
            byte[] data = imageAsByte.toByteArray();


        }
    }
}