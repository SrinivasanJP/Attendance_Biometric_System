package dev.roxs.attendance.Helper;

import android.os.Build;


public class FingerPrint {

    public FingerPrint() {
    }
    public void getFingerPrint(){

        String deviceHardwareConfig = Build.BOARD + "_" +
                Build.BRAND + "_" +
                Build.DEVICE + "_" +
                Build.HARDWARE + "_" +
                Build.MANUFACTURER + "_" +
                Build.MODEL + "_" +
                Build.PRODUCT + "_" ;

    }

}
