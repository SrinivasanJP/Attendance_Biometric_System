package dev.roxs.attendance.Helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.nio.ByteBuffer;

public class ImageUtils {
    public static Bitmap imageProxyToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
