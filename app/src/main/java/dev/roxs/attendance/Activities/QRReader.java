package dev.roxs.attendance.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.SparseArray;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.Calib3d;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dev.roxs.attendance.R;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.KeyPoint;
import org.opencv.features2d.ORB;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
public class QRReader extends AppCompatActivity {
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    // ORB detector
    private ORB orb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrreader);

        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV initialization failed", Toast.LENGTH_SHORT).show();
        } else {
            orb = ORB.create();
        }
    }

    private void initialiseDetectorsAndSources() {
        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
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


            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(() -> {
                        String qrCodeValue = barcodes.valueAt(0).displayValue;
                        Toast.makeText(QRReader.this, qrCodeValue, Toast.LENGTH_SHORT).show();

//                        // Pass the QR code value to the next activity
//                        Intent captureImage = new Intent(QRReader.this, CaptureImage.class);
//                        captureImage.putExtra("sessionID", qrCodeValue);
//                        startActivity(captureImage);

                        // Capture the current frame from SurfaceView
                        Bitmap bitmap = captureFrameFromSurface();
                        if (bitmap != null) {
                            Mat capturedImage = new Mat();
                            Utils.bitmapToMat(bitmap, capturedImage);
                            Imgproc.cvtColor(capturedImage, capturedImage, Imgproc.COLOR_RGBA2GRAY);

                            // Estimate the camera angle
                            estimateCameraAngle(capturedImage, qrCodeValue);
                        } else {
                            Toast.makeText(QRReader.this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                        }

                        finish();
                    });
                }
            }
        });
    }

    private Bitmap captureFrameFromSurface() {
        // Capture a frame from the SurfaceView
        surfaceView.setDrawingCacheEnabled(true);
        surfaceView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(surfaceView.getDrawingCache());
        surfaceView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void estimateCameraAngle(Mat capturedImage, String qrCodeValue) {
        // Load reference image based on QR code value (you should have a predefined reference image)
        Mat refImage = new Mat();

        // Load image from resources
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qr_ref);  // Replace with your actual image resource

        // Convert the Bitmap to Mat
        Utils.bitmapToMat(bitmap, refImage);

        // Convert to grayscale if needed
        Imgproc.cvtColor(refImage, refImage, Imgproc.COLOR_RGBA2GRAY);

        if (refImage.empty()) {
            Toast.makeText(this, "Reference image not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Detect keypoints and descriptors
        MatOfKeyPoint refKeypoints = new MatOfKeyPoint();
        MatOfKeyPoint capturedKeypoints = new MatOfKeyPoint();
        Mat refDescriptors = new Mat();
        Mat capturedDescriptors = new Mat();

        orb.detectAndCompute(refImage, new Mat(), refKeypoints, refDescriptors);
        orb.detectAndCompute(capturedImage, new Mat(), capturedKeypoints, capturedDescriptors);

        // Match descriptors using BFMatcher
        BFMatcher matcher = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMING, true);
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(refDescriptors, capturedDescriptors, matches);

        // Calculate homography matrix
        Mat homography = calculateHomography(refKeypoints, capturedKeypoints, matches);
        if (homography != null) {
            // Decompose homography matrix to get rotation and translation vectors
            Mat rotation = new Mat();
            Mat translation = new Mat();
            Mat normals = new Mat();

            List<Mat> rotations = new ArrayList<>();
            List<Mat> translations = new ArrayList<>();
            List<Mat> normalsList = new ArrayList<>();

            Calib3d.decomposeHomographyMat(homography, new Mat(), rotations, translations, normalsList);

            if (!rotations.isEmpty()) {
                rotation = rotations.get(0);
                translation = translations.get(0);
                normals = normalsList.get(0);

                // Use the rotation and translation matrices for further processing
                Toast.makeText(this, "Camera angle estimated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to estimate camera angle", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to compute homography", Toast.LENGTH_SHORT).show();
        }
    }

    private Mat calculateHomography(MatOfKeyPoint refKeypoints, MatOfKeyPoint capturedKeypoints, MatOfDMatch matches) {
        List<Point> refPoints = new ArrayList<>();
        List<Point> capturedPoints = new ArrayList<>();

        List<org.opencv.core.DMatch> matchesList = matches.toList();
        List<KeyPoint> refKeyPointsList = refKeypoints.toList();
        List<KeyPoint> capturedKeyPointsList = capturedKeypoints.toList();

        for (int i = 0; i < matchesList.size(); i++) {
            refPoints.add(refKeyPointsList.get(matchesList.get(i).queryIdx).pt);
            capturedPoints.add(capturedKeyPointsList.get(matchesList.get(i).trainIdx).pt);
        }

        MatOfPoint2f refMatOfPoint2f = new MatOfPoint2f();
        refMatOfPoint2f.fromList(refPoints);
        MatOfPoint2f capturedMatOfPoint2f = new MatOfPoint2f();
        capturedMatOfPoint2f.fromList(capturedPoints);

        if (refPoints.size() >= 4 && capturedPoints.size() >= 4) {
            return Calib3d.findHomography(refMatOfPoint2f, capturedMatOfPoint2f, Calib3d.RANSAC, 5);
        } else {
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}
