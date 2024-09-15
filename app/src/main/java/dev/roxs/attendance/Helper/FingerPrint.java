package dev.roxs.attendance.Helper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FingerPrint {
    public interface FingerprintAvailabilityListener {
        void onFingerprintAvailability(boolean networkError,boolean isAvailable, QueryDocumentSnapshot documentSnapshot);
    }
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }
        return false;
    }
    public void isFingerprintAvailable(FirebaseFirestore db, String fingerprint, FingerprintAvailabilityListener listener) {

        if(isNetworkAvailable(mContext)) {
            db.collection("users").get().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.getId().equals(fingerprint)) {
                            // Fingerprint found
                            listener.onFingerprintAvailability(false, true, documentSnapshot);
                            return; // Exit the loop and the method
                        }
                    }
                    listener.onFingerprintAvailability(false, false, null);
                } else {
                    listener.onFingerprintAvailability(false, false, null);
                }
            });
        }else{
            listener.onFingerprintAvailability(true,false,null);
        }
    }

    private final Context mContext;
    public FingerPrint(Context context) {
        mContext = context;
    }
    //FINGER PRINT GENERATOR
    public String getFingerPrint(){

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        String deviceHardwareConfig = Build.BOARD + "_" +
                Build.BRAND + "_" +
                Build.DEVICE + "_" +
                Build.HARDWARE + "_" +
                Build.MANUFACTURER + "_" +
                Build.MODEL + "_" +
                Build.PRODUCT + "_" +displayWidth+"x"+displayHeight;
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder sensorDetails = new StringBuilder();
        for (Sensor sensor : sensorList) {
            sensorDetails.append(sensor.getName()).append(sensor.getVendor()).append("_");
        }

        return hashString(androidId+"_"+deviceHardwareConfig+sensorList);
    }
    //ENCRYPTING WITH SHA256
    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Handle the exception appropriately
            return null;
        }
    }

}
