package dev.roxs.attendance.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.roxs.attendance.R;

public class Setup extends AppCompatActivity {
    private EditText vReg_no, vName;
    private ProgressBar vSetupProgress;
    private TextView vFinishSetup;
    private RelativeLayout finishSetupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        vReg_no = findViewById(R.id.reg_no);
        vName = findViewById(R.id.name);
        vSetupProgress = findViewById(R.id.setupprogressBar);
        vSetupProgress.setVisibility(View.INVISIBLE);

        finishSetupBtn = findViewById(R.id.finishSetupBtn);
        finishSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}