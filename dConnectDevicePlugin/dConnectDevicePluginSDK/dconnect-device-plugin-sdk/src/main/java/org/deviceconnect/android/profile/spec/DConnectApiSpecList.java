package org.deviceconnect.android.profile.spec;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DConnectApiSpecList implements DConnectApiSpecConstants {

    private final Map<String, Map<Method, DConnectApiSpec>> mAllApiSpecs
        = new HashMap<String, Map<Method, DConnectApiSpec>>();

    public DConnectApiSpecList() {}

    public Map<Method, DConnectApiSpec> findApiSpecs(final String path) {
        synchronized (mAllApiSpecs) {
            String key = path.toLowerCase();
            return mAllApiSpecs.get(key);
        }
    }

    public DConnectApiSpec findApiSpec(final Method method, final String path) {
        synchronized (mAllApiSpecs) {
            Map<Method, DConnectApiSpec> apiSpecs = findApiSpecs(path);
            if (apiSpecs == null) {
                return null;
            }
            return apiSpecs.get(method);
        }
    }

    public DConnectApiSpec findApiSpec(final String method, final String path) {
        return findApiSpec(Method.parse(method), path);
    }

    public void addApiSpecList(final InputStream json, final DConnectApiSpecFilter filter) throws IOException, JSONException {
        String file = loadFile(json);
        JSONArray array = new JSONArray(file);
        for (int i = 0; i < array.length(); i++) {
            JSONObject apiObj = array.getJSONObject(i);
            DConnectApiSpec apiSpec = DConnectApiSpec.fromJson(apiObj);
            if (apiSpec != null && filter != null && filter.filter(apiSpec)) {
                addApiSpec(apiSpec);
            }
        }
    }

    public void addApiSpecList(final InputStream json) throws IOException, JSONException {
        addApiSpecList(json, null);
    }

    private String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024];
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

    private void addApiSpec(final DConnectApiSpec apiSpec) {
        synchronized (mAllApiSpecs) {
            String path = apiSpec.getPath().toLowerCase();
            Map<Method, DConnectApiSpec> apiSpecs = mAllApiSpecs.get(path);
            if (apiSpecs == null) {
                apiSpecs = new HashMap<Method, DConnectApiSpec>();
                mAllApiSpecs.put(path, apiSpecs);
            }
            apiSpecs.put(apiSpec.getMethod(), apiSpec);
        }
    }

}
