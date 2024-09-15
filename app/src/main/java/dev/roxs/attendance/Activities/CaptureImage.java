package dev.roxs.attendance.Activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import dev.roxs.attendance.Helper.FaceRecognitionHelper;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;

public class CaptureImage extends AppCompatActivity implements FaceRecognitionHelper.FaceRecognitionCallback {

    private Intent preIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        preIntent = getIntent();
        PreviewView previewView = findViewById(R.id.previewView);
        TextView textProgress = findViewById(R.id.textProgress);
        TextView textNote = findViewById(R.id.textNote);
        SharedpreferenceHelper sp = new SharedpreferenceHelper(getApplicationContext());

        FaceRecognitionHelper faceRecognitionHelper = new FaceRecognitionHelper(CaptureImage.this, sp.getStoredEmbeddings(), this);
        faceRecognitionHelper.cameraBind(getApplicationContext(), previewView, textNote, textProgress);
    }

    @Override
    public void onDistanceCalculated(float distance) {
        Intent sendData = new Intent(getApplicationContext(), SendAttendanceData.class);
        sendData.putExtra("sessionID",preIntent.getStringExtra("sessionID"));
        sendData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendData);
        finish();
    }
}
