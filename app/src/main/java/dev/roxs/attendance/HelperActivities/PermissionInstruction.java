package dev.roxs.attendance.HelperActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import dev.roxs.attendance.R;

public class PermissionInstruction extends AppCompatActivity {

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_instruction);
        Button vOpenSettings = findViewById(R.id.openSettings);
        vOpenSettings.setOnClickListener(v -> openAppSettings());
    }
}