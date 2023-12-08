package dev.roxs.attendance.Activities;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import dev.roxs.attendance.Helper.FingerPrint;
import dev.roxs.attendance.R;


public class Setup extends AppCompatActivity {

    private EditText vReg_no, vName;
    private ProgressBar vSetupProgress;
    private TextView vFinishSetup;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference reference;
    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        //hooks
        vReg_no = findViewById(R.id.reg_no);
        vName = findViewById(R.id.name);
        vSetupProgress = findViewById(R.id.setupprogressBar);
        vFinishSetup = findViewById(R.id.finishSetupText);
        RelativeLayout finishSetupBtn = findViewById(R.id.finishSetupBtn);
        
       
        

        //Finish setup button click
        finishSetupBtn.setOnClickListener(view -> {
            vFinishSetup.setVisibility(View.INVISIBLE);
            vSetupProgress.setVisibility(View.VISIBLE);
            String sReg_no = vReg_no.getText().toString();
            String sName = vName.getText().toString();
            FingerPrint fp = new FingerPrint(Setup.this);
            
            //document reference
            reference = db.collection("users").document(sReg_no);

            Map<String, String> user = new HashMap<>();
            user.put("name", sName);
            user.put("fingerprint",fp.getFingerPrint());
            reference.set(user).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    startActivity(new Intent(getApplicationContext(), IDPage.class));
                }else{
                    Toast.makeText(Setup.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                    vSetupProgress.setVisibility(View.INVISIBLE);
                    vFinishSetup.setVisibility(View.VISIBLE);
                }
            });
           
            Log.d("UT", fp.getFingerPrint());
        });

    }
}