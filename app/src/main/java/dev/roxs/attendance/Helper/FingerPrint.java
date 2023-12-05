package dev.roxs.attendance.Helper;

import android.os.Build;
import android.util.Log;

public class FingerPrint {

    public FingerPrint() {
    }
    public void getFingerPrint(){

        String data = Build.BOARD + "***" +
                Build.BOOTLOADER + "***" +
                Build.BRAND + "***" +
                Build.DEVICE + "***" +
                Build.DISPLAY + "***" +
                Build.FINGERPRINT + "***" +
                Build.HARDWARE + "***" +
                Build.HOST + "***" +
                Build.ID + "***" +
                Build.MANUFACTURER + "***" +
                Build.MODEL + "***" +
                Build.PRODUCT + "***" +
                Build.getRadioVersion() + "***";

        Log.d("FingerPrint", "getFingerPrint: "+ data);
    }

}
