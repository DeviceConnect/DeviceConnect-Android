package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.util.Range;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties を使用して、データを保存するためのユーテリティクラス.
 */
public final class PropertyUtil {
    private Properties mProperties;

    public PropertyUtil() {
        mProperties = new Properties();
    }

    public void load(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            mProperties.load(fis);
        }
    }

    public void save(File file) throws IOException {
        try (FileOutputStream f = new FileOutputStream(file);
             BufferedOutputStream b = new BufferedOutputStream(f)) {
            mProperties.store(b, "setting data.");
        }
    }

    public void put(String key, int value) {
        mProperties.put(key, String.valueOf(value));
    }

    public void put(String key, float value) {
        mProperties.put(key, String.valueOf(value));
    }

    public void put(String key, boolean value) {
        mProperties.put(key, String.valueOf(value));
    }

    public void put(String widthKey, String heightKey, HostMediaRecorder.Size size) {
        mProperties.put(widthKey, size.getWidth());
        mProperties.put(heightKey, size.getHeight());
    }

    public void put(String lowerKey, String upperKey, Range<Integer> range) {
        mProperties.put(lowerKey, range.getLower());
        mProperties.put(upperKey, range.getUpper());
    }

    public int getInteger(String key, int defaultValue) {
        String value = mProperties.getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        String value = mProperties.getProperty(key, String.valueOf(defaultValue));
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = mProperties.getProperty(key, String.valueOf(defaultValue));
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public HostMediaRecorder.Size getSize(String widthKey, String heightKey) {
        String w = mProperties.getProperty(widthKey, null);
        String h = mProperties.getProperty(heightKey, null);
        if (w != null && h != null) {
            try {
                return new HostMediaRecorder.Size(Integer.parseInt(w), Integer.parseInt(h));
            } catch (Exception e) {
                // ignore.
            }
        }
        return null;
    }

    public Range<Integer> getRange(String lowerKey, String upperKey) {
        String w = mProperties.getProperty(lowerKey, null);
        String h = mProperties.getProperty(upperKey, null);
        if (w != null && h != null) {
            try {
                return new Range<>(Integer.parseInt(w), Integer.parseInt(h));
            } catch (Exception e) {
                // ignore.
            }
        }
        return null;
    }

    public void close() {
        mProperties.clear();
    }
}
