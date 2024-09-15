package dev.roxs.attendance.Helper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import dev.roxs.attendance.R;


public class FaceRecognitionHelper {
    private static final String MODEL_NAME="mobile_face_net.tflite";
    Interpreter tfLite;
    FaceDetector detector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private TextView textNote,textProgress;
    private final Activity activity;
    private final List<Float> storedEmbeddings;
    private Bitmap sendImage;
    private final ClassifierInterface.Recognition recognitionHelper;
    boolean start;
    private int bufferCount = 0;
    
    //recognize data
    int[] intValues;
    private static final int INPUT_SIZE=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeddings;
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    private static final int OUTPUT_SIZE=192; //Output size of model

    private final FaceRecognitionCallback callback; // Add this line
    public interface FaceRecognitionCallback {
        void onDistanceCalculated(float distance);
    }

    public FaceRecognitionHelper(Activity activity, List<Float> storedEmbeddings,FaceRecognitionCallback callback) {
        InitFaceDetector();
        InitInterpreter(activity);
        this.activity = activity;
        this.start = true;
        this.storedEmbeddings = storedEmbeddings;
        this.recognitionHelper = new ClassifierInterface.Recognition();
        this.callback = callback;
        
    }
    private void InitInterpreter(Activity activity){
        try {
            tfLite=new Interpreter(loadModelFile(activity));
            Log.d("MODEL", "onCreate: Model loaded successfully");
        } catch (IOException e) {
            Log.e("MODEL", "onCreate: Model load unsuccessfully",e);
        }
    }
    private void InitFaceDetector(){
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  // Accurate mode
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)  // Detect facial landmarks (eyes, mouth, etc.)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)    // Detect face contours
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  // Smile and eye open/close detection
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(FaceRecognitionHelper.MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

        inputStream.close();  // Close the stream only after the mapping is done
        return mappedByteBuffer;
    }

    public void cameraBind(Context context,PreviewView previewView, TextView textNote, TextView textProgress) {
        this.previewView = previewView;
        this.textNote = textNote;
        this.textProgress =textProgress;
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CAM Bind ", "cameraBind: "+ e);
            }
        }, ContextCompat.getMainExecutor(context));
    }
    public void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
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
                    Log.e("Analyzer", "analyze: ",e );
                }
                InputImage image;


                @SuppressLint("UnsafeExperimentalUsageError")
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

                Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                } else {
                    image = null;
                }

                //Process acquired image to detect faces
                assert image != null;
                detector.process(image)
                    .addOnSuccessListener(
                            faces -> {

                                if(!faces.isEmpty()) {
                                    sendImage = toBitmap(mediaImage);
                                    Face face = faces.get(0); //Get first face from detected faces
                                    Bitmap frame_bmp = toBitmap(mediaImage);
                                    int rot = imageProxy.getImageInfo().getRotationDegrees();
                                    //Adjust orientation of Face
                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false);
                                    //Get bounding box of face
                                    RectF boundingBox = new RectF(face.getBoundingBox());
                                    //Crop out bounding box from whole Bitmap(image)
                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                                    cropped_face = rotateBitmap(cropped_face, 0, true);
                                    //Scale the acquired Face to 112*112 which is required input for model
                                    Bitmap scaled = getResizedBitmap(cropped_face);
                                    if(start){
                                        recognizeImage(scaled);
                                    }
                                }
                                else
                                {
                                    textNote.setText(R.string.face_align);
                                    textNote.setVisibility(View.VISIBLE);
                                    Log.d("Face Detector", "onSuccess: No face detected");
                                }

                            })
                    .addOnFailureListener(
                            e -> Log.e("FaceDetector", "onFailure: face detector ", e))
                    .addOnCompleteListener(task -> {

                        imageProxy.close(); //v.important to acquire next frame for analysis
                    });


            }
        });
        


        cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, imageAnalysis, preview);

    }
    
    @SuppressLint("SetTextI18n")
    private void recognizeImage(final Bitmap bitmap) {

        // set Face to Preview
//        face_preview.setImageBitmap(bitmap);

        //Create ByteBuffer to store normalized image

        ByteBuffer imgData = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[INPUT_SIZE * INPUT_SIZE];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int pixelValue = intValues[i * INPUT_SIZE + j];
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


        embeddings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeddings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
        float distance = calculateDistance();

        if(distance<1){
            textProgress.setText("Distance is "+distance);
            textProgress.setVisibility(View.VISIBLE);
            textNote.setVisibility(View.INVISIBLE);
           stopCamera();
           saveToInternalStorage(sendImage);
            if (callback != null) {
                callback.onDistanceCalculated(distance);
            }
        }else{
            if(bufferCount>100) {
                textNote.setText("Your face does not matches our record");
                textNote.setVisibility(View.VISIBLE);
            }
            else{
                bufferCount++;
            }
        }


    }

    private static Bitmap getResizedBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) 112) / width;
        float scaleHeight = ((float) 112) / height;
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
        Canvas canvas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        canvas.drawBitmap(source, matrix, paint);

        if (!source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, 1.0f);
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

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public float calculateDistance() {
        float[][] storedEmbeddings = this.recognitionHelper.unflattenList(this.storedEmbeddings);
        if (storedEmbeddings[0].length != embeddings[0].length) {
            throw new IllegalArgumentException("Embeddings must have the same length");
        }
        float distance = 0;
        for (int i = 0; i < storedEmbeddings[0].length; i++) {
            double diff = storedEmbeddings[0][i] - embeddings[0][i];
            distance += (float) (diff * diff);
        }
        return (float) Math.sqrt(distance);
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90); // Rotate to align with 0 degrees
            bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
            File privateDir = activity.getApplicationContext().getFilesDir();
            File imageFile = new File(privateDir, "captured_image.jpg");

            try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 8, outputStream);
            } catch (IOException e) {
                Log.e("File output error", "saveToInternalStorage: ",e );
            }
        }
    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll(); // Unbind to stop the camera
        }
    }


}
