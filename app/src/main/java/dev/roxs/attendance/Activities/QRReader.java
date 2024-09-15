package dev.roxs.attendance.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import dev.roxs.attendance.R;

public class QRReader extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    // Sensor-related fields


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrreader);

        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
    }


    private void initialiseDetectorsAndSources() {
        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(QRReader.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(QRReader.this,
                                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    Log.e("Permission", "surfaceCreated: camera permission ",e );
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Log.d("UT QR started", "release: qr started");
            }

            @SuppressLint({"ResourceAsColor", "SetTextI18n"})
            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(() -> {
                        // Get the bounding box of the QR code
                        Rect boundingBox = barcodes.valueAt(0).getBoundingBox();

                        // Calculate the angle based on the bounding box
                        double angle = calculateAngle(boundingBox);
                        txtBarcodeValue.setText("QR Code Angle: " + (int)angle + "°");

                        if (isAngleAllowed(angle)) {
                            Toast.makeText(QRReader.this, barcodes.valueAt(0).displayValue, Toast.LENGTH_SHORT).show();
                            Intent captureImage = new Intent(QRReader.this, CaptureImage.class);
                            captureImage.putExtra("sessionID", barcodes.valueAt(0).displayValue);
                            startActivity(captureImage);
                            finish();
                        }else{
                            txtBarcodeValue.setText("QR Code Angle: " + (int)angle + "°"+" is not allowed try in different angle");

                        }
                    });
                }else{
                    txtBarcodeValue.post(()-> txtBarcodeValue.setText("No QR Code detected"));
                }
            }
        });
    }
    // Calculate the angle of the QR code in 3D space
    private double calculateAngle(Rect boundingBox) {
        // Length (l) is the width of the bounding box
        int l = boundingBox.width();

        // Breadth (b) is the height of the bounding box
        int b = boundingBox.height();

        // Calculate the angle based on the aspect ratio
        double aspectRatio = (double) l / (double) b;

        // Assuming that the QR code is square, the aspect ratio at 0° would be 1.
        // As the phone tilts, the aspect ratio changes.
        // You can use this ratio to infer the tilt angle.

        return Math.toDegrees(Math.atan(aspectRatio));
    }

    // Function to check if the angle is within the allowed range
    private boolean isAngleAllowed(double angle) {
        // Block angles between 0-30 degrees and 100-120 degrees for pitch
        return (angle>35);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}
