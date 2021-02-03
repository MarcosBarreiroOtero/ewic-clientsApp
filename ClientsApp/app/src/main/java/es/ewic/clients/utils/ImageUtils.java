package es.ewic.clients.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ImageUtils {

    public static Bitmap convert(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        Log.e("IMAGEN", "Byte array tamaño " + outputStream.toByteArray().length);
        Log.e("IMAGEN", "Byte array string tamaño " + new String(outputStream.toByteArray()).length());
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
