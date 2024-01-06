package dev.roxs.attendance.Activities;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Objects;

import dev.roxs.attendance.Helper.FingerPrint;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;


public class Setup extends AppCompatActivity {

    private EditText vReg_no, vName;
    private ProgressBar vSetupProgress;
    private TextView vFinishSetup;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference reference;
    private FingerPrint fp;
    private SharedpreferenceHelper sp;
    private RelativeLayout vContainer;

    @Override
    protected void onStart() {
        super.onStart();
        sp = new SharedpreferenceHelper(this);
        vContainer = findViewById(R.id.setup_container);
        vContainer.setVisibility(View.INVISIBLE);
        if(sp.isDataAvailable()){
            startActivity(new Intent(getApplicationContext(), IDPage.class));
            finish();
        }else{
                fp = new FingerPrint(Setup.this);
                fp.isFingerprintAvailable(db, fp.getFingerPrint(), (networkError,isAvailable, documentSnapshot) -> {
                    if(!networkError){
                        if (isAvailable) {
                            //TODO: add PIN verification here
                            sp.addData(documentSnapshot.getId(), Objects.requireNonNull(documentSnapshot.get("registerNo")).toString(), Objects.requireNonNull(documentSnapshot.get("name")).toString());
                            startActivity(new Intent(getApplicationContext(), IDPage.class));
                            finish();
                        } else {
                            Toast.makeText(Setup.this, "Fingerprint not available", Toast.LENGTH_SHORT).show();
                            vContainer.setVisibility(View.VISIBLE);
                        }
                        }else{
                            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
                        }
                });
                
        }

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
            //document reference
            reference = db.collection("users").document(fp.getFingerPrint());

            Map<String, String> user = new HashMap<>();
            user.put("name", sName);
            user.put("registerNo", sReg_no);
            reference.set(user).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    sp.addData(fp.getFingerPrint(),sReg_no, sName);
                    Intent intent = new Intent(getApplicationContext(), IDPage.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(Setup.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                    vSetupProgress.setVisibility(View.INVISIBLE);
                    vFinishSetup.setVisibility(View.VISIBLE);
                }
            });
        });

    }

}