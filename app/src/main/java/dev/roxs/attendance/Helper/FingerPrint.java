package dev.roxs.attendance.Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

public class FingerPrint {
    private final Context mContext;
    public FingerPrint(Context context) {
        mContext = context;
    }
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
        return androidId+"_"+deviceHardwareConfig;
    }

}
