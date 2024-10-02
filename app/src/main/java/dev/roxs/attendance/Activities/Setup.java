package dev.roxs.attendance.Activities;



import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.roxs.attendance.Helper.ClassifierInterface;
import dev.roxs.attendance.Helper.FingerPrint;
import dev.roxs.attendance.Helper.SharedpreferenceHelper;
import dev.roxs.attendance.R;


public class Setup extends AppCompatActivity {

    private EditText vReg_no, vName;
    private ProgressBar vSetupProgress;
    private TextView vFinishSetup;
    private Button button;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference reference;
    private FingerPrint fp;
    private SharedpreferenceHelper sp;
    private RelativeLayout vContainer;
    private PinView vPin,pinView;
    private ProgressBar vPageProgress;



    private void handlePin(DocumentSnapshot documentSnapshot){
        String sPinView = Objects.requireNonNull(pinView.getText()).toString();
        if(!sPinView.isEmpty()){
            if(Objects.requireNonNull(documentSnapshot.get("pin")).toString().equals(sPinView)){
// Retrieve the 'extra' field and map it to ClassifierInterface.Recognition
//                ClassifierInterface.Recognition recognitionData = documentSnapshot.toObject(ClassifierInterface.Recognition.class);
//                assert recognitionData != null;
//                Log.d("EXTRA", "handlePin: "+documentSnapshot.getId()+" "++" "+" "+documentSnapshot.get("extra"));
                sp.addData(documentSnapshot.getId(), (String) documentSnapshot.get("registerNo"), (String) documentSnapshot.get("name"), (List<Double>) documentSnapshot.get("extra"));
                Log.d("Firebase data", "handlePin: "+sp.getStoredEmbeddings());
                startActivity(new Intent(getApplicationContext(), IDPage.class));
                finish();
            }else{
                Toast.makeText(Setup.this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                pinView.setError("Invalid PIN");
                pinView.setText("");
                pinView.setLineColor(getColor(R.color.red));
            }
        }else{
            Toast.makeText(Setup.this, "PIN is required", Toast.LENGTH_SHORT).show();
            pinView.setError("PIN is required");
            pinView.setLineColor(getColor(R.color.red));
        }
    }

    private String CURRENT_VERSION;


    @Override
    protected void onStart() {
        super.onStart();
        sp = new SharedpreferenceHelper(this);
        vPageProgress = findViewById(R.id.page_progress);
        vPageProgress.setVisibility(View.VISIBLE);
        vContainer = findViewById(R.id.setup_container);
        vContainer.setVisibility(View.INVISIBLE);
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            CURRENT_VERSION = packageInfo.versionName; // Retrieve the version name
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            CURRENT_VERSION = "Unknown"; // Fallback in case of an error
        }


        checkAppVersion(); // Call the version check before proceeding
    }

    private void checkAppVersion() {
        // Assuming the version is stored under a collection "app_info" and document "version"
        db.collection("app_info").document("version").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String latestVersion = task.getResult().getString("latest_version");
                Log.d("UT Update", "checkAppVersion: "+latestVersion+"---"+CURRENT_VERSION);
                if (latestVersion != null && !latestVersion.equals(CURRENT_VERSION)) {
                    // If version mismatch, go to the update screen
                    startActivity(new Intent(Setup.this, UpdateScreenActivity.class));
                    finish(); // Close the current activity
                } else {
                    // Proceed with your original logic if versions match
                    proceedWithSetup();
                }
            } else {
                // Handle errors like no internet connection
                Log.d("Version Check", "Failed to retrieve version info");
                Toast.makeText(Setup.this, "Unable to check for updates", Toast.LENGTH_SHORT).show();
                proceedWithSetup(); // Optionally proceed even if there's a failure
            }
        });
    }

    private void proceedWithSetup() {
        // Your existing onStart logic to continue with the setup
        if(sp.isDataAvailable()){
            startActivity(new Intent(getApplicationContext(), IDPage.class));
            finish();
        } else {
            fp = new FingerPrint(Setup.this);
            fp.isFingerprintAvailable(db, fp.getFingerPrint(), (networkError,isAvailable, documentSnapshot) -> {
                if (!networkError) {
                    if (isAvailable) {
                        handlePinPrompt(documentSnapshot);
                    } else {
                        vPageProgress.setVisibility(View.INVISIBLE);
                        vContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handlePinPrompt(DocumentSnapshot documentSnapshot) {
        Log.d("Firebase Data", "onStart: "+documentSnapshot.getString("name"));
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.pin_get_layout,null);
        alertDialog.setView(view);
        pinView = view.findViewById(R.id.pinInput);
        button = view.findViewById(R.id.enter);
        button.setOnClickListener(v -> handlePin(documentSnapshot));
        alertDialog.setCancelable(false);
        AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        pinView.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                handlePin(documentSnapshot);
            }
            return false;
        });
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        sp = new SharedpreferenceHelper(this);
        fp =new FingerPrint(Setup.this);
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
                                Intent intent = new Intent(getApplicationContext(), FaceCapture.class);
                                intent.putExtra("name",sName);
                                intent.putExtra("registerNo", sReg_no);
                                intent.putExtra("pin",sPin);
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