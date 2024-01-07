package dev.roxs.attendance.Activities;



import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
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
    private PinView vPin;

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
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                            LayoutInflater inflater = this.getLayoutInflater();
                            View view = inflater.inflate(R.layout.pin_get_layout,null);
                            alertDialog.setView(view);
                            PinView pinView = view.findViewById(R.id.pinInput);
                            Button button = view.findViewById(R.id.enter);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            alertDialog.create().show();
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
        vPin = findViewById(R.id.pinInput);
        vPin.setAnimationEnable(true);
        vPin.setPasswordHidden(true);
        vSetupProgress = findViewById(R.id.setupprogressBar);
        vFinishSetup = findViewById(R.id.finishSetupText);
        RelativeLayout finishSetupBtn = findViewById(R.id.finishSetupBtn);

        //Finish setup button click
        finishSetupBtn.setOnClickListener(view -> {
            vFinishSetup.setVisibility(View.INVISIBLE);
            vSetupProgress.setVisibility(View.VISIBLE);
            String sReg_no = vReg_no.getText().toString();
            String sName = vName.getText().toString();
            String sPin = Objects.requireNonNull(vPin.getText()).toString();
            //document reference
            reference = db.collection("users").document(fp.getFingerPrint());

            if(!sReg_no.isEmpty()){
                if(!sName.isEmpty()){
                    if(!sPin.isEmpty()){
                        Map<String, String> user = new HashMap<>();
                        user.put("name", sName);
                        user.put("registerNo", sReg_no);
                        user.put("pin",sPin);
                        reference.set(user).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                sp.addData(fp.getFingerPrint(),sReg_no, sName);
                                Intent intent = new Intent(getApplicationContext(), IDPage.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Log.d("UT error", "onCreate: "+task.getException());
                                Toast.makeText(Setup.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                                vSetupProgress.setVisibility(View.INVISIBLE);
                                vFinishSetup.setVisibility(View.VISIBLE);
                            }
                        });
                    }else{
                        vPin.setError("pin is required");
                    }
                }else{
                    vName.setError("Name is required");
                }
            }else{
                vReg_no.setError("Register number is required");
            }
            vSetupProgress.setVisibility(View.INVISIBLE);
            vFinishSetup.setVisibility(View.VISIBLE);
        });

    }

}