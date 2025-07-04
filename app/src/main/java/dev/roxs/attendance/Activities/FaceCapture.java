package dev.roxs.attendance.Activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.View;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dev.roxs.attendance.Helper.ClassifierInterface;
import dev.roxs.attendance.Helper.FingerPrint;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;

public class FaceCapture extends AppCompatActivity {

    FaceDetector detector;
    private DocumentReference reference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FingerPrint fp;
    private SharedpreferenceHelper sp;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageView face_preview;
    Interpreter tfLite;
    TextView textNote;
    CameraSelector cameraSelector;
    RelativeLayout finishSetup;
    private ProgressBar vSetupProgress;
    RelativeLayout vRetakeBtn;
//    ProgressBar circularProgressBar;

    boolean start=true,flipX=false;
    int cam_face=CameraSelector.LENS_FACING_FRONT;

    int[] intValues;
    int inputSize=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeedings;
    List<Float> flattenEmbeddings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE=192; //Output size of model
    ProcessCameraProvider cameraProvider;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    String name, regNo, pin;
    Bitmap scaled;
    Bitmap sendImage;


    String modelFile="mobile_face_net.tflite"; //model name

//    private HashMap<String, ClassifierInterface.Recognition> registered = new HashMap<>();
    private ClassifierInterface.Recognition recognitionData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_capture);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        regNo = intent.getStringExtra("registerNo");
        pin = intent.getStringExtra("pin");
        fp =new FingerPrint(FaceCapture.this);
        reference = db.collection("users").document(fp.getFingerPrint());
        sp = new SharedpreferenceHelper(this);


        Log.d("INTENT LOGS", "onCreate: Intent data: "+name+" "+regNo+" "+pin);



        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        face_preview =findViewById(R.id.previewImage);
        finishSetup = findViewById(R.id.finishSetup);
        vSetupProgress =findViewById(R.id.setupprogressBar);
        vRetakeBtn = findViewById(R.id.retake);
        textNote = findViewById(R.id.textNote);

        finishSetup.setVisibility(View.INVISIBLE);
        vRetakeBtn.setVisibility(View.INVISIBLE);
        face_preview.setVisibility(View.INVISIBLE);
        vRetakeBtn.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = true;
//                startCamera();
            }
        }));

        finishSetup.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> user = new HashMap<>();
//                user.put("extra", flattenEmbeddings);

                // Get an instance of FirebaseStorage and reference to the storage location
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference imagesRef = storageRef.child("initImageCapture").child(sp.getRegNo());
                // Convert the Bitmap to a byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();
// Upload the byte array to Firebase Storage
                UploadTask uploadTask = imagesRef.putBytes(imageData);
// Handle upload success and failure
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Upload success, you can get the download URL here
                    imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        user.put("initImageURL", downloadUrl);
                        reference.set(user, SetOptions.merge()).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                if(!flattenEmbeddings.isEmpty())
                                    sp.addEmbeddings(flattenEmbeddings);
                                else
                                    Toast.makeText(FaceCapture.this, "FlattenEmbeddings empty", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), IDPage.class);
                                intent.putExtra("name",name);
                                intent.putExtra("registerNo", regNo);
                                intent.putExtra("pin",pin);
                                startActivity(intent);
                                finish();
                            }else{
                                Log.d("UT error", "onCreate: "+task.getException());
                                Toast.makeText(getApplicationContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                                vSetupProgress.setVisibility(View.INVISIBLE);
                                finishSetup.setVisibility(View.VISIBLE);
                            }
                        });

                        // Use the downloadUrl as needed
                    });
                }).addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("Firebase", "Image upload failed", e);
                });

//                reference.set(user, SetOptions.merge()).addOnCompleteListener(task -> {
//                    if(task.isSuccessful()){
//                        if(!flattenEmbeddings.isEmpty())
//                            sp.addEmbeddings(flattenEmbeddings);
//                        else
//                            Toast.makeText(FaceCapture.this, "FlattenEmbeddings empty", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(getApplicationContext(), IDPage.class);
//                        intent.putExtra("name",name);
//                        intent.putExtra("registerNo", regNo);
//                        intent.putExtra("pin",pin);
//                        startActivity(intent);
//                        finish();
//                    }else{
//                        Log.d("UT error", "onCreate: "+task.getException());
//                        Toast.makeText(getApplicationContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
//                        vSetupProgress.setVisibility(View.INVISIBLE);
//                        finishSetup.setVisibility(View.VISIBLE);
//                    }
//                });
            }
        }));

        //Load model
        try {
            tfLite=new Interpreter(loadModelFile(FaceCapture.this,modelFile));
            Log.d("MODEL", "onCreate: Model loaded successfully");
        } catch (IOException e) {
            Log.d("MODEL", "onCreate: Model load unsuccessfully");
            e.printStackTrace();
        }
        //Initialize Face Detector
//        FaceDetectorOptions highAccuracyOpts =
//                new FaceDetectorOptions.Builder()
//                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                        .build();
//        detector = (FaceDetector) FaceDetection.getClient(highAccuracyOpts);
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  // Accurate mode
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)  // Detect facial landmarks (eyes, mouth, etc.)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)    // Detect face contours
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  // Smile and eye open/close detection
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);


        cameraBind();



    }
    private void vibrateDevice(int amplitude ) {
        // Get the system's Vibrator service
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Check if the device has a vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 500 milliseconds (0.5 seconds)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // Use VibrationEffect for devices running Android Oreo (API 26) or higher
                VibrationEffect effect = VibrationEffect.createOneShot(500, amplitude);
                vibrator.vibrate(effect);
            } else {
                // For devices running below Android Oreo, use the deprecated vibrate method
                vibrator.vibrate(500); // Vibrate for 500 milliseconds
            }
        }
    }
    private void addFace() {
        start = false;
//        stopCamera();
        vRetakeBtn.setVisibility(View.VISIBLE);
        finishSetup.setVisibility(View.VISIBLE);
        vibrateDevice(VibrationEffect.EFFECT_TICK);
        recognitionData = new ClassifierInterface.Recognition( -1f);
        flattenEmbeddings = recognitionData.flatten2DArray(embeedings);
        recognitionData.setExtra(recognitionData.flatten2DArray(embeedings));
        Toast.makeText(this, recognitionData.toString(), Toast.LENGTH_SHORT).show();
        Log.d("Firebase data:", "addFace: "+recognitionData.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Bind camera and preview view
    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        previewView=findViewById(R.id.previewView);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CAM Bind ", "cameraBind: "+ e.toString(),e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // Unbind to stop the camera
        }
    }
    private void startCamera(){
        if (cameraProvider != null) {
            // Define the Preview
            Preview preview = new Preview.Builder().build();

            // Define the CameraSelector - choose back camera by default
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            // Bind the preview to the previewView
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            // Bind the camera provider with preview and camera selector
            cameraProvider.unbindAll();

            try {
                // Bind the lifecycle to the camera provider
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class) @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                InputImage image = null;


                @SuppressLint("UnsafeExperimentalUsageError")
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

                Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                }
                
                //Process acquired image to detect faces
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                if(faces.size()!=0) {
                                                    sendImage = toBitmap(mediaImage);
                                                    Face face = faces.get(0); //Get first face from detected faces
                                                    Bitmap frame_bmp = toBitmap(mediaImage);
                                                    int rot = imageProxy.getImageInfo().getRotationDegrees();
                                                    //Adjust orientation of Face
                                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);
                                                    //Get bounding box of face
                                                    RectF boundingBox = new RectF(face.getBoundingBox());
                                                    //Crop out bounding box from whole Bitmap(image)
                                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                                                    if(flipX)
                                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                                    //Scale the acquired Face to 112*112 which is required input for model
                                                    scaled = getResizedBitmap(cropped_face, 112, 112);
                                                    if(start) {
                                                        face_preview.setVisibility(View.VISIBLE);
                                                        textNote.setVisibility(View.INVISIBLE);
                                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
                                                    }
                                                }
                                                else
                                                {
                                                    face_preview.setVisibility(View.INVISIBLE);
                                                    textNote.setVisibility(View.VISIBLE);
                                                }

                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        })
                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<List<Face>> task) {

                                        imageProxy.close(); //v.important to acquire next frame for analysis
                                    }
                                });


            }
        });


        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);


    }

    public void recognizeImage(final Bitmap bitmap) {

        // set Face to Preview
        face_preview.setImageBitmap(bitmap);

        //Create ByteBuffer to store normalized image

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model



        float distance_local = Float.MAX_VALUE;
        String id = "0";
        String label = "?";
        Log.d("Embeedings", "recognizeImage: "+outputMap.toString());
        addFace();
//        Toast.makeText(getApplicationContext(), "Embeedings got", Toast.LENGTH_SHORT).show();

        //Compare new face with saved Faces.
//        if (registered.size() > 0) {
//            final List<Pair<String, Float>> nearest = findNearest(embeedings[0]);//Find 2 closest matching face
//
//            if (nearest.get(0) != null) {
//
//                final String name = nearest.get(0).first; //get name and distance of closest matching face
//                // label = name;
//                distance_local = nearest.get(0).second;
//                if(distance_local<distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
//                    textNote.setText("Nearest: "+name +"\nDist: "+ String.format("%.3f",distance_local)+"\n2nd Nearest: "+nearest.get(1).first +"\nDist: "+ String.format("%.3f",nearest.get(1).second));
//                else
//                    textNote.setText("Unknown "+"\nDist: "+String.format("%.3f",distance_local)+"\nNearest: "+name +"\nDist: "+ String.format("%.3f",distance_local)+"\n2nd Nearest: "+nearest.get(1).first +"\nDist: "+ String.format("%.3f",nearest.get(1).second));
//
////                    System.out.println("nearest: " + name + " - distance: " + distance_local);
//
//
//            }
//        }
//

//            final int numDetectionsOutput = 1;
//            final ArrayList<ClassifierInterface.Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
//            ClassifierInterface.Recognition rec = new ClassifierInterface.Recognition(
//                    id,
//                    label,
//                    distance);
//
//            recognitions.add( rec );

    }

    //Compare Faces by distance between face embeddings
//    private List<Pair<String, Float>> findNearest(float[] emb) {
//        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
//        Pair<String, Float> ret = null; //to get closest match
//        Pair<String, Float> prev_ret = null; //to get second closest match
//        for (Map.Entry<String, ClassifierInterface.Recognition> entry : registered.entrySet())
//        {
//
//            final String name = entry.getKey();
//            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];
//
//            float distance = 0;
//            for (int i = 0; i < emb.length; i++) {
//                float diff = emb[i] - knownEmb[i];
//                distance += diff*diff;
//            }
//            distance = (float) Math.sqrt(distance);
//            if (ret == null || distance < ret.second) {
//                prev_ret=ret;
//                ret = new Pair<>(name, distance);
//            }
//        }
//        if(prev_ret==null) prev_ret=ret;
//        neighbour_list.add(ret);
//        neighbour_list.add(prev_ret);
//
//        return neighbour_list;
//
//    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width*height;
        int uvSize = width*height/4;

        byte[] nv21 = new byte[ySize + uvSize*2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos<ySize; pos+=width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte)~savePixel);
                if (uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row=0; row<height/2; row++) {
            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    private Bitmap toBitmap(Image image) {

        byte[] nv21=YUV_420_888toNV21(image);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }



}