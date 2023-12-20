package dev.roxs.attendance.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;

public class SendAttendanceData extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference sessionReference;
    private ProgressBar uploadImageBuffer;
    private ProgressBar markAttendanceBuffer;
    private ImageView uploadImageDone;
    private ImageView markAttendanceDone;
    private RelativeLayout uploadImageBar;

    @SuppressLint({"WrongThread", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_attendance_data);

        //hooks progress bars
        ProgressBar fingerPrintBuffer = findViewById(R.id.fingerPrintBuffer);
        uploadImageBuffer = findViewById(R.id.imageUploadBuffer);
        markAttendanceBuffer = findViewById(R.id.markAttendanceBuffer);

        //hooks done symbols
        ImageView fingerPrintDone = findViewById(R.id.fingerPrintDone);
        uploadImageDone = findViewById(R.id.imageUploadDone);
        markAttendanceDone = findViewById(R.id.markAttendanceDone);

        //hooks bar
        RelativeLayout fingerPrintBar = findViewById(R.id.fingerPrintBar);
        uploadImageBar = findViewById(R.id.uploadImageBar);

        SharedpreferenceHelper sp = new SharedpreferenceHelper(this);
        FingerPrint fp = new FingerPrint(this);
        //get session ID
        String sessionID = getIntent().getStringExtra("sessionID");
        //get fingerprint
        String fingerPrint = fp.getFingerPrint();
        fingerPrintBuffer.setVisibility(View.GONE);
        fingerPrintDone.setVisibility(View.VISIBLE);
        fingerPrintBar.setBackground(getDrawable(R.drawable.process_done));

        //get Captured image
        File privateDir = getApplicationContext().getFilesDir();
        File imageFile = new File(privateDir, "captured_image.jpg");

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
                    String downloadUrl = uri.toString();
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
                                startActivity(new Intent(getApplicationContext(), IDPage.class));
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
}