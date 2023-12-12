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
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;


import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import dev.roxs.attendance.Helper.ImageUtils;
import dev.roxs.attendance.R;

public class CaptureImage extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

//        camera_open_id = findViewById(R.id.camera_button);
//        click_image_id = findViewById(R.id.click_image);
//
//        // Camera_open button is for open the camera and add the setOnClickListener in this button
//        camera_open_id.setOnClickListener(v -> {
//            // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
//            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // Start the activity with camera_intent, and request pic id
//            startActivityForResult(camera_intent, pic_id);
//        });
//    }
//
//    // This method will help to retrieve the image
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // Match the request 'pic id with requestCode
//        if (requestCode == pic_id) {
//            // BitMap is data structure of image file which store the image in memory
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            // Set the image in imageview for display
//            click_image_id.setImageBitmap(photo);
//        }
//    }
        previewView = findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);

        // Capture image without displaying shutter UI
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @OptIn(markerClass = ExperimentalGetImage.class) @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                // Convert ImageProxy to Bitmap
                Image mediaImage = image.getImage();
                if (mediaImage != null) {
                    Bitmap imageBitmap = ImageUtils.imageProxyToBitmap(mediaImage);
                    // Release the ImageProxy
                    image.close();

                    // Save the Bitmap to internal storage
                    saveToInternalStorage(imageBitmap);
                }
            }

            private void saveToInternalStorage(Bitmap bitmapImage) {
                File privateDir = getApplicationContext().getFilesDir();
                File imageFile = new File(privateDir, "captured_image.jpg");

                try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    startActivity(new Intent(getApplicationContext(), IDPage.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Handle capture errors
            }
        });
    }
}
