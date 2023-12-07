package dev.roxs.attendance.Helper;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final String ENCODING = "UTF-8";

    public static String encrypt(String input, String key) {
        try {
            // Create the cipher instance
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashedKey = sha.digest(key.getBytes(ENCODING));
            byte[] validKey = Arrays.copyOf(hashedKey, 16);
            // Generate SecretKeySpec from the given key
            SecretKeySpec secretKeySpec = new SecretKeySpec(validKey, AES_ALGORITHM);

            // Initialize Cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            // Generate Initialization Vector (IV)
            byte[] iv = cipher.getIV();
            IvParameterSpec ivParams = new IvParameterSpec(iv);

            // Perform encryption
            byte[] encrypted = cipher.doFinal(input.getBytes(ENCODING));

            // Combine IV and encrypted data into a single string (for transport/storage)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // Convert the encrypted data to a Base64-encoded string for safe transport
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("EncryptionError", "Encryption error: " + e.getMessage());
        }
        return null;
    }
}

