package org.deviceconnect.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * resourcesのファイルを読み込むクラス。
 */
public final class FileLoader {

    public static String readString(String file) {
        byte[] data = readFile(file);
        if (data != null) {
            return new String(data);
        }
        throw new RuntimeException("Not found a " + file);
    }

    public static byte[] readFile(String file) {
        InputStream input = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            input = FileLoader.class.getClassLoader().getResourceAsStream(file);
            byte[] buffer = new byte[1024];
            int size;
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }

            return output.toByteArray();

        } catch (Exception e) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
