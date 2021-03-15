package org.deviceconnect.android.deviceplugin.uvc.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UVCRegistry {
    private static final String FILE_NAME = "uvc-save.dat";

    private final List<UVC> mUVCList = new ArrayList<>();
    private final Context mContext;

    public UVCRegistry(Context context) {
        mContext = context;
        load();
    }

    public List<UVC> getUVCList() {
        return mUVCList;
    }

    public void addUVC(String deviceId, String name) {
        UVC uvc = new UVC(deviceId, name);
        if (!mUVCList.contains(uvc)) {
            mUVCList.add(uvc);
        }
        save();
    }

    public void removeUVC(String deviceId) {
        UVC removeUVC = null;
        for (UVC uvc : mUVCList) {
            if (uvc.getDeviceId().equalsIgnoreCase(deviceId)) {
                removeUVC = uvc;
                break;
            }
        }
        if (removeUVC != null) {
            mUVCList.remove(removeUVC);
        }
        save();
    }

    private void removeUVC(UVC uvc) {
        mUVCList.remove(uvc);
    }

    private void save() {
        JSONArray array = new JSONArray();
        for (UVC uvc : mUVCList) {
            try {
                array.put(createObject(uvc));
            } catch (Exception e) {
                // ignore.
            }
        }

        String text = array.toString();
        try (FileOutputStream fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(text.getBytes());
        } catch (Exception e) {
            // ignore.
        }
    }

    private void load() {
        int len;
        byte[] buf = new byte[4092];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream ios = mContext.openFileInput(FILE_NAME)) {
            while ((len = ios.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
        } catch (Exception e) {
            // ignore.
        }

        mUVCList.clear();
        try {
            JSONArray array = new JSONArray(new String(baos.toByteArray()));
            for (int i = 0; i < array.length(); i++) {
                mUVCList.add(createUVC(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            // ignore.
        }
    }

    private JSONObject createObject(UVC uvc) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("deviceId", uvc.getDeviceId());
        object.put("name", uvc.getName());
        return object;
    }

    private UVC createUVC(JSONObject object) throws JSONException {
        String deviceId = object.getString("deviceId");
        String name = object.getString("name");
        return new UVC(deviceId, name);
    }

    public static class UVC {
        private final String mDeviceId;
        private final String mName;

        public UVC(String deviceId, String name) {
            mDeviceId = deviceId;
            mName = name;
        }

        public String getDeviceId() {
            return mDeviceId;
        }

        public String getName() {
            return mName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UVC uvc = (UVC) o;
            return Objects.equals(mDeviceId, uvc.mDeviceId) &&
                    Objects.equals(mName, uvc.mName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mDeviceId, mName);
        }
    }
}
