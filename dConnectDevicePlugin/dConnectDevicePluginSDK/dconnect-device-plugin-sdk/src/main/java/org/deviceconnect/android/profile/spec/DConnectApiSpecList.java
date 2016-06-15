package org.deviceconnect.android.profile.spec;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DConnectApiSpecList {

    private final List<DConnectApiSpec> mApiSpecList = new ArrayList<DConnectApiSpec>();

    public DConnectApiSpecList() {}

    public void load(final InputStream in) throws IOException {
        String file = loadFile(in);

        Log.d("AAA", "Loaded JSON: " + file);

        try {
            JSONArray array = new JSONArray(file);
            Log.d("AAA", "Loaded JSON Array: " + array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject apiObj = array.getJSONObject(i);
                String name = apiObj.getString("name");
                String path = apiObj.getString("path");
                String method = apiObj.getString("method");
                String type = apiObj.getString("type");
                DConnectApiSpec apiSpec = new DConnectApiSpec(name, type, method, path);
                if (apiObj.has("requestParams")) {
                    JSONArray requestParams = apiObj.getJSONArray("requestParams");
                    for (int k = 0; k < requestParams.length(); k++) {
                        JSONObject paramObj = requestParams.getJSONObject(k);
                        DConnectRequestParamSpec paramSpec = DConnectRequestParamSpec.fromJson(paramObj);
                        apiSpec.addRequestParam(paramSpec);
                    }
                }
                addApiSpec(apiSpec);
            }
        } catch (JSONException e) {
            throw new IOException(e);
        }
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

    public void addApiSpec(final DConnectApiSpec apiSpec) {
        mApiSpecList.add(apiSpec);
    }

    public DConnectApiSpec findApiSpec(final String method, final String path) {
        for (DConnectApiSpec spec : mApiSpecList) {
            if (spec.getMethod().getName().equals(method) && spec.getPath().equals(path)) {
                return spec;
            }
        }
        return null;
    }

}
