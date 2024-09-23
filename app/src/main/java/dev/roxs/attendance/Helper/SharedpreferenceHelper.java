package dev.roxs.attendance.Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SharedpreferenceHelper {
    private static final String FILE_NAME = "userdata";
    private static final String FINGERPRINT = "fingerprint";
    private static final String NAME = "name";
    private static final String REG_NO = "regNo";
    private static final String STORED_EMBEDDINGS = "emb";
    private final SharedPreferences sharedPreferences;

    public SharedpreferenceHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    // Add data including embedding
    public void addData(String fingerprint, String reg_no, String name, List<Double> emb) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FINGERPRINT, fingerprint);
        editor.putString(REG_NO, reg_no);
        editor.putString(NAME, name);
        // Convert List<Float> to a string before storing
        editor.putString(STORED_EMBEDDINGS, listToString(emb));
        editor.apply();
    }
    public void addData(String fingerprint, String reg_no, String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FINGERPRINT, fingerprint);
        editor.putString(REG_NO, reg_no);
        editor.putString(NAME, name);
        editor.apply();
    }

    // Get stored embeddings as a List<Float>
    public List<Float> getStoredEmbeddings() {
        String embString = sharedPreferences.getString(STORED_EMBEDDINGS, "");
        return stringToList(embString);
    }
    public void addEmbeddings(List<Float> emb){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(STORED_EMBEDDINGS,FlistToString(emb));
    }

    public String getFingerprint() {
        return sharedPreferences.getString(FINGERPRINT, "");
    }

    public String getName() {
        return sharedPreferences.getString(NAME, "");
    }

    public String getRegNo() {
        return sharedPreferences.getString(REG_NO, "");
    }

    public boolean isDataAvailable() {
        return sharedPreferences.contains(FINGERPRINT) && sharedPreferences.contains(NAME) && sharedPreferences.contains(REG_NO);
    }

    // Helper function to convert List<Float> to a comma-separated String
    private String listToString(List<Double> list) {
        StringBuilder sb = new StringBuilder();
        for (Double f : list) {
            if (sb.length() > 0) {
                sb.append(","); // separate values with a comma
            }
            sb.append(String.valueOf(f.floatValue()));
        }
        return sb.toString();
    }
    private String FlistToString(List<Float> list) {
        StringBuilder sb = new StringBuilder();
        for (Float f : list) {
            if (sb.length() > 0) {
                sb.append(","); // separate values with a comma
            }
            sb.append(f.toString());
        }
        return sb.toString();
    }

    // Helper function to convert a comma-separated String back to List<Float>
    private List<Float> stringToList(String str) {
        List<Float> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        while (tokenizer.hasMoreTokens()) {
            list.add(Float.parseFloat(tokenizer.nextToken()));
        }
        return list;
    }
}
