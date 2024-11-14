package dev.roxs.attendance.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dev.roxs.attendance.Helper.FingerPrint;
import dev.roxs.attendance.Helper.LocationUtils;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.HelperActivities.PermissionInstruction;
import dev.roxs.attendance.R;

public class SendAttendanceData extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference sessionReference;
    private ProgressBar uploadImageBuffer, locationBuffer;
    private ProgressBar markAttendanceBuffer;
    private ImageView uploadImageDone;
    private ImageView markAttendanceDone, locationDone;
    private RelativeLayout uploadImageBar, locationBar;
    private SharedpreferenceHelper sp;
    private String sessionID, fingerPrint;
    private double latitude, longitude, altitude,angle;

    @SuppressLint({"WrongThread", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_attendance_data);

        //hooks progress bars
        ProgressBar fingerPrintBuffer = findViewById(R.id.fingerPrintBuffer);
        uploadImageBuffer = findViewById(R.id.imageUploadBuffer);
        markAttendanceBuffer = findViewById(R.id.markAttendanceBuffer);
        locationBuffer = findViewById(R.id.locationBuffer);

        //hooks done symbols
        ImageView fingerPrintDone = findViewById(R.id.fingerPrintDone);
        uploadImageDone = findViewById(R.id.imageUploadDone);
        markAttendanceDone = findViewById(R.id.markAttendanceDone);
        locationDone = findViewById(R.id.locationDone);

        //hooks bar
        RelativeLayout fingerPrintBar = findViewById(R.id.fingerPrintBar);
        uploadImageBar = findViewById(R.id.uploadImageBar);
        locationBar = findViewById(R.id.locationBar);

        sp = new SharedpreferenceHelper(this);
        FingerPrint fp = new FingerPrint(this);
        //get session ID
        sessionID = getIntent().getStringExtra("sessionID");

        //get fingerprint
        fingerPrint = fp.getFingerPrint();
        fingerPrintBuffer.setVisibility(View.GONE);
        fingerPrintDone.setVisibility(View.VISIBLE);
        fingerPrintBar.setBackground(getDrawable(R.drawable.process_done));
        getLocation();


    }

    private void getLocation() {

        boolean isLocationEnabled = LocationUtils.isLocationEnabled(this);
        if (isLocationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestNewLocationData();
            }
        } else {
            Toast.makeText(this, "Turn on Location and Mobile Network", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestNewLocationData() {

        LocationRequest.Builder lr = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY);
        lr.setWaitForAccurateLocation(true)
                .setIntervalMillis(10000)
                .setMaxUpdateAgeMillis(20000);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.requestLocationUpdates(lr.build(), mLocationCallback, Looper.myLooper());
    }

    private boolean isSent = false;
    private final LocationCallback mLocationCallback = new LocationCallback() {

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if(!isSent) {
                Location mLastLocation = locationResult.getLastLocation();
                assert mLastLocation != null;
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                altitude = mLastLocation.getAltitude();
                isSent = true;
                locationDone.setVisibility(View.VISIBLE);
                locationBuffer.setVisibility(View.GONE);
                locationBar.setBackground(getDrawable(R.drawable.process_done));
                sendData();
            }
        }
    };

    @SuppressLint("UseCompatLoadingForDrawables")
    private void sendData(){

        //get Captured image
        File privateDir = getApplicationContext().getFilesDir();
        File imageFile = new File(privateDir, "send.jpg");

        if (imageFile.exists()) {
            Bitmap loadedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            ByteArrayOutputStream imageAsByte = new ByteArrayOutputStream();
            loadedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageAsByte);
            byte[] data = imageAsByte.toByteArray();
            // Get an instance of FirebaseStorage and reference to the storage location
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("studentImages").child(sp.getRegNo());

// Upload the byte array to Firebase Storage
            UploadTask uploadTask = imagesRef.putBytes(data);

            uploadTask.addOnFailureListener(exception -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {
                // Handle successful uploads
                // Get a URL to the uploaded content
                imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadImageBuffer.setVisibility(View.GONE);
                    uploadImageDone.setVisibility(View.VISIBLE);
                    uploadImageBar.setBackground(getDrawable(R.drawable.process_done));
                    String downloadUrl = uri.toString()+","+latitude+","+longitude+","+altitude+","+Double.toString(getIntent().getDoubleExtra("angle",0.0));
                    assert sessionID != null;
                    sessionReference = db.collection("Attendance").document(sessionID);
                    Map<String, Object> attendanceData = new HashMap<>();
                    attendanceData.put(fingerPrint, downloadUrl);
                    sessionReference.set(attendanceData, SetOptions.merge()).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            markAttendanceBuffer.setVisibility(View.GONE);
                            markAttendanceDone.setVisibility(View.VISIBLE);
                            // Vibrate on success
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                            }

                            // Delay before starting activity
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(getApplicationContext(), IDPage.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }, 1000); // Delay of 1 second (1000 milliseconds)

                        }else{
                            Log.d("UT", "onCreate: "+task.getException());
                            Toast.makeText(SendAttendanceData.this, "Unexpected error occurs!", Toast.LENGTH_LONG).show();
                        }
                    });

                });
            });

        }else{
            Toast.makeText(this, "Image capture error try again", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Intent i = new Intent(getApplicationContext(), PermissionInstruction.class);
                i.putExtra("case","LOCATION");
                startActivity(i);
                finish();
            }
        }
    }

}