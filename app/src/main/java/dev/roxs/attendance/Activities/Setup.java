package dev.roxs.attendance.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import dev.roxs.attendance.Helper.FingerPrint;
import dev.roxs.attendance.R;

public class Setup extends AppCompatActivity {

    private EditText vReg_no, vName;
    private ProgressBar vSetupProgress;
    private TextView vFinishSetup;
    private RelativeLayout finishSetupBtn;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        vReg_no = findViewById(R.id.reg_no);
        vName = findViewById(R.id.name);
        vSetupProgress = findViewById(R.id.setupprogressBar);
        vSetupProgress.setVisibility(View.INVISIBLE);

        finishSetupBtn = findViewById(R.id.finishSetupBtn);
        finishSetupBtn.setOnClickListener(view -> {
            @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.d("Android ID", "onClick: "+ androidId+"---");
            FingerPrint fp = new FingerPrint(Setup.this);
            fp.getFingerPrint();
        });

    }
}