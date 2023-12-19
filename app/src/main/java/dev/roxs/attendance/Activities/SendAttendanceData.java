package dev.roxs.attendance.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_attendance_data);
        SharedpreferenceHelper sp = new SharedpreferenceHelper(this);
        FingerPrint fp = new FingerPrint(this);
        //get session ID
        String sessionID = getIntent().getStringExtra("sessionID");
        //get fingerprint
        String fingerPrint = fp.getFingerPrint();
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
                    String downloadUrl = uri.toString();
                    assert sessionID != null;
                    sessionReference = db.collection("Attendance").document(sessionID);
                    Map<String, Object> attendanceData = new HashMap<>();
                    attendanceData.put(fingerPrint, downloadUrl);
                    sessionReference.set(attendanceData, SetOptions.merge()).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(), IDPage.class));
                            finish();
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