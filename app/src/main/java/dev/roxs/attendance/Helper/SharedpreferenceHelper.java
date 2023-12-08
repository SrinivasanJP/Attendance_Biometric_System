package dev.roxs.attendance.Helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedpreferenceHelper {
    private static final String FILE_NAME = "userdata";
    private static final String FINGERPRINT = "fingerprint";
    private static final String NAME = "name";
    private static final String REG_NO = "regNo";
    private final SharedPreferences sharedPreferences;
    public SharedpreferenceHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }
    public void addData(String fingerprint, String reg_no, String name){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FINGERPRINT, fingerprint);
        editor.putString(REG_NO, reg_no);
        editor.putString(NAME, name);
        editor.apply();
    }

    public String getFingerprint(){
        return sharedPreferences.getString(FINGERPRINT,"");
    }
    public String getName(){
        return sharedPreferences.getString(NAME,"");
    }
    public String getRegNo(){
        return sharedPreferences.getString(REG_NO,"");
    }

}
