package dev.roxs.attendance.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.roxs.attendance.R;

public class QRReader extends AppCompatActivity {
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrreader);
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        // Load OpenCV
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCV not loaded", Toast.LENGTH_SHORT).show();
        }
    }
    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(QRReader.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(QRReader.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "Barcode reader stopped", Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(() -> {
                        Toast.makeText(QRReader.this, barcodes.valueAt(0).displayValue, Toast.LENGTH_SHORT).show();
                        // Process QR code detection
                        Barcode qrCode = barcodes.valueAt(0);
                        Toast.makeText(QRReader.this, qrCode.displayValue, Toast.LENGTH_SHORT).show();
                        Log.d("TESTING", "receiveDetections: running");
                        // Convert to OpenCV Mat
                        Mat qrCodeImage = convertFrameToMat(qrCode);
                        double qrCodeAngle = getQRCodeAngle(qrCodeImage);
                        double personAngle = calculatePersonAngleRelativeToQRCode(qrCodeAngle);
                        Log.d("ANGLE", "QR Code Angle: " + qrCodeAngle);
                        Log.d("PERSON_ANGLE", "Person Angle Relative to QR Code: " + personAngle);

//                        Intent captureImage = new Intent(QRReader.this, CaptureImage.class);
//                        captureImage.putExtra("sessionID", barcodes.valueAt(0).displayValue);
//                        startActivity(captureImage);
//                        finish();

                    });
                }
            }
        });
    }
    private Mat convertFrameToMat(Barcode qrCode) {
        // Get the bounding box of the detected QR code
        android.graphics.Rect boundingBox = qrCode.getBoundingBox();

        // Validate bounding box dimensions
        if (boundingBox == null || boundingBox.width() <= 0 || boundingBox.height() <= 0) {
            Log.e("convertFrameToMat", "Invalid bounding box dimensions");
            return new Mat(); // Return an empty Mat
        }

        // Get the camera frame as a Bitmap
        Bitmap frameBitmap = getCameraFrameBitmap();

        // Ensure the Bitmap is not null and has valid dimensions
        if (frameBitmap == null || frameBitmap.getWidth() <= 0 || frameBitmap.getHeight() <= 0) {
            Log.e("convertFrameToMat", "Invalid Bitmap dimensions");
            return new Mat(); // Return an empty Mat
        }

        // Ensure the bounding box is within the bounds of the Bitmap
        int x = Math.max(0, boundingBox.left);
        int y = Math.max(0, boundingBox.top);
        int width = Math.min(boundingBox.width(), frameBitmap.getWidth() - x);
        int height = Math.min(boundingBox.height(), frameBitmap.getHeight() - y);

        // Create a cropped Bitmap of the QR code region
        Bitmap qrCodeBitmap = Bitmap.createBitmap(frameBitmap, x, y, width, height);

        // Convert the Bitmap to OpenCV Mat
        Mat qrCodeMat = new Mat();
        org.opencv.android.Utils.bitmapToMat(qrCodeBitmap, qrCodeMat);

        return qrCodeMat;
    }


    private double getQRCodeAngle(Mat qrCodeImage) {
        if (qrCodeImage.empty()) {
            Log.e("getQRCodeAngle", "Input Mat is empty");
            return 0;
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(qrCodeImage, gray, Imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(blurred, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 11, 2);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            Log.e("getQRCodeAngle", "No contours found");
            return 0;
        }

        MatOfPoint largestContour = contours.get(0);
        double maxArea = Imgproc.contourArea(largestContour);
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }

        MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
        RotatedRect rotatedRect = Imgproc.minAreaRect(contour2f);

        double angle = rotatedRect.angle;
        if (rotatedRect.size.width < rotatedRect.size.height) {
            angle = 90 + angle;
        }

        // Normalize the angle
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    private Bitmap getCameraFrameBitmap() {
        // Get the SurfaceView where the camera preview is being displayed
        SurfaceView cameraPreview = surfaceView; // Assuming surfaceView is your SurfaceView instance

        // Get the Surface from the SurfaceView
        Surface surface = cameraPreview.getHolder().getSurface();

        // Create a Bitmap with the same size as the SurfaceView
        Bitmap bitmap = Bitmap.createBitmap(cameraPreview.getWidth(), cameraPreview.getHeight(), Bitmap.Config.ARGB_8888);

        // Use a Canvas to draw the SurfaceView onto the Bitmap
        Canvas canvas = new Canvas(bitmap);
        if (surface != null && surface.isValid()) {
            try {
                cameraPreview.draw(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("QRReader", "Surface is null or not valid");
        }

        return bitmap;
    }
    private double calculatePersonAngleRelativeToQRCode(double qrCodeAngle) {
        // Normalize the angle to be in the range [0, 360)
        double normalizedQRCodeAngle = (qrCodeAngle + 360) % 360;
        // Assuming the ideal QR code angle is 0 degrees, the person’s angle is the same as the QR code angle
        // Adjust this if necessary depending on how your system is set up
        return normalizedQRCodeAngle;
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
