package org.deviceconnect.android.deviceplugin.theta.core.osc;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

class HttpResponse {

    private final Response mEntity;

    private byte[] mBuffer;

    HttpResponse(final Response entity) {
        mEntity = entity;
    }

    public byte[] getBytes() throws IOException {
        if (mBuffer == null) {
            mBuffer = mEntity.body().bytes();
        }
        return mBuffer;
    }

    public String getString() throws IOException {
        return new String(getBytes(), "UTF-8");
    }

    public JSONObject getJSON() throws IOException, JSONException {
        return new JSONObject(getString());
    }

    public int getStatusCode() {
        return mEntity.code();
    }

    public InputStream getStream() throws IOException {
        return mEntity.body().byteStream();
    }
}
