package dev.roxs.attendance.Activities;


import androidx.annotation.NonNull;
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
import android.widget.TextView;


import java.io.File;

import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.HelperActivities.PermissionInstruction;
import dev.roxs.attendance.R;

public class IDPage extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(getApplicationContext(), PermissionInstruction.class);
                i.putExtra("case","CAMERA");
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idpage);
        //hooks
        ImageView profileImage = findViewById(R.id.profileImage);
        TextView vName = findViewById(R.id.idName);
        TextView vReg_no = findViewById(R.id.idRegNo);

        SharedpreferenceHelper sp = new SharedpreferenceHelper(this);
        vName.setText(sp.getName());
        vReg_no.setText(sp.getRegNo());


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