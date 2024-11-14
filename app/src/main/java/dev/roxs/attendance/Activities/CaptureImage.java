package dev.roxs.attendance.Activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import dev.roxs.attendance.Helper.FaceRecognitionHelper;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;

public class CaptureImage extends AppCompatActivity implements FaceRecognitionHelper.FaceRecognitionCallback {

    private Intent preIntent;
    ImageView face_preview;
    RelativeLayout confirmBtn, retakeBtn;
    Boolean startRocognize;

    Bitmap sendImage,storeImage;

    FaceRecognitionHelper faceRecognitionHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        preIntent = getIntent();
        face_preview = findViewById(R.id.previewImage);
        confirmBtn = findViewById(R.id.finishSetup);
        retakeBtn = findViewById(R.id.retake);
        confirmBtn.setVisibility(View.INVISIBLE);
        retakeBtn.setVisibility(View.INVISIBLE);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceRecognitionHelper.saveToInternalStorage(storeImage, sendImage);
                Intent sendData = new Intent(getApplicationContext(), SendAttendanceData.class);
                sendData.putExtra("sessionID",preIntent.getStringExtra("sessionID"));
                sendData.putExtra("angle",preIntent.getDoubleExtra("angle",0.0));
                sendData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(sendData);
                finish();

            }
        });

        retakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceRecognitionHelper.setRecognition(true);
                face_preview.setVisibility(View.INVISIBLE);
                confirmBtn.setVisibility(View.INVISIBLE);
                retakeBtn.setVisibility(View.INVISIBLE);
            }
        });

        PreviewView previewView = findViewById(R.id.previewView);
        TextView textProgress = findViewById(R.id.textProgress);
        TextView textNote = findViewById(R.id.textNote);
        SharedpreferenceHelper sp = new SharedpreferenceHelper(getApplicationContext());

        faceRecognitionHelper = new FaceRecognitionHelper(CaptureImage.this, sp.getStoredEmbeddings(), this);
        faceRecognitionHelper.cameraBind(getApplicationContext(), previewView, textNote, textProgress);
    }

    @Override
    public void onDistanceCalculated(float distance, Bitmap storeImage, Bitmap sendImage) {
        this.sendImage = sendImage;
        this.storeImage = storeImage;
        faceRecognitionHelper.setRecognition(false);

        // Update UI on the main thread
        runOnUiThread(() -> {
            face_preview.setImageBitmap(sendImage);

            face_preview.setVisibility(View.VISIBLE);
            retakeBtn.setVisibility(View.VISIBLE);
            confirmBtn.setVisibility(View.VISIBLE);
        });
    }

}
