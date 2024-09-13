package dev.roxs.attendance.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import dev.roxs.attendance.Helper.FaceRecognitionHelper;
import dev.roxs.attendance.Helper.ImageUtils;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;

public class CaptureImage extends AppCompatActivity implements FaceRecognitionHelper.FaceRecognitionCallback {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private TextView textNote, textProgress;
    private Intent preIntent;
    private FaceRecognitionHelper faceRecognitionHelper;
    private SharedpreferenceHelper sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        preIntent = getIntent();
        previewView = findViewById(R.id.previewView);
        textProgress = findViewById(R.id.textProgress);
        textNote = findViewById(R.id.textNote);
        sp = new SharedpreferenceHelper(getApplicationContext());

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        faceRecognitionHelper = new FaceRecognitionHelper(CaptureImage.this,sp.getStoredEmbeddings(),this);
        faceRecognitionHelper.cameraBind(getApplicationContext(), previewView,textNote,textProgress);
    }

    @Override
    public void onDistanceCalculated(float distance) {
        Toast.makeText(this, "reached here", Toast.LENGTH_SHORT).show();
        Intent sendData = new Intent(getApplicationContext(), SendAttendanceData.class);
        sendData.putExtra("sessionID",preIntent.getStringExtra("sessionID"));
        sendData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendData);
        finish();
    }

//    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
//        Preview preview = new Preview.Builder().build();
//        CameraSelector cameraSelector = new CameraSelector.Builder()
//                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//                .build();
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//        ImageCapture imageCapture = new ImageCapture.Builder()
//                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
//                .build();
//
//        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
//
//        // Capture image without displaying shutter UI
//        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
//            @OptIn(markerClass = ExperimentalGetImage.class) @Override
//            public void onCaptureSuccess(@NonNull ImageProxy image) {
//                // Convert ImageProxy to Bitmap
//                Image mediaImage = image.getImage();
//                if (mediaImage != null) {
//                    Bitmap imageBitmap = ImageUtils.imageProxyToBitmap(mediaImage);
//                    // Release the ImageProxy
//                    image.close();
//                    int rotationDegree = getRotationDegreeFromBitmap(imageBitmap);
//                    Log.d("UT rotation", "onCaptureSuccess: "+rotationDegree);
//                    if (rotationDegree != 0) {
//                        // Rotate the image to align with 0 degrees
//                        Matrix matrix = new Matrix();
//                        matrix.postRotate(-rotationDegree); // Rotate to align with 0 degrees
//
//                        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
//
//                        // Save the rotated image
//                        saveToInternalStorage(rotatedBitmap);
//                    } else {
//                        // If the image is already aligned to 0 degrees, save it without further rotation
//                        saveToInternalStorage(imageBitmap);
//                    }
//                }
//            }
//
//            private void saveToInternalStorage(Bitmap bitmapImage) {
//                // Change the angle as needed
//                File privateDir = getApplicationContext().getFilesDir();
//                File imageFile = new File(privateDir, "captured_image.jpg");
//
//                try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
//                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 8, outputStream);
//                    Intent sendData = new Intent(getApplicationContext(), SendAttendanceData.class);
//                    sendData.putExtra("sessionID",preIntent.getStringExtra("sessionID"));
//                    sendData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(sendData);
//                    finish();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(@NonNull ImageCaptureException exception) {
//                // Handle capture errors
//            }
//        });
//    }
//    private int getRotationDegreeFromBitmap(Bitmap bitmap) {
//        int rotationDegree = 0;
//        if (bitmap.getWidth() > bitmap.getHeight()) {
//            // Landscape orientation, consider it as 90 or 270 degrees
//            rotationDegree = 90;
//        }
//        // Add more logic here based on specific cases, but without metadata, it's a best-guess scenario
//
//        return rotationDegree;
//    }
}
