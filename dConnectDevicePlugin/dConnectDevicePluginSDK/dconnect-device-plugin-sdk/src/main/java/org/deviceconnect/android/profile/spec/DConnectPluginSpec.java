package org.deviceconnect.android.profile.spec;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DConnectPluginSpec {

    private static final int BUFFER_SIZE = 1024;

    private final Map<String, DConnectProfileSpec> mProfileSpecs
        = new ConcurrentHashMap<String, DConnectProfileSpec>();

    public void addProfileSpec(final String profileName, final InputStream in) throws IOException, JSONException {
        JSONObject json = new JSONObject(loadFile(in));
        DConnectProfileSpec profileSpec = DConnectProfileSpec.fromJson(json);
        mProfileSpecs.put(profileName, profileSpec);
    }

    private static String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            return new String(baos.toByteArray());
        } finally {
            in.close();
        }
    }

    public DConnectProfileSpec findProfileSpec(final String profileName) {
        return mProfileSpecs.get(profileName);
    }

}
