package org.deviceconnect.android.deviceplugin.theta.core.osc;


import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

class HttpResponse {

    private final Response mEntity;

    HttpResponse(final Response entity) {
        mEntity = entity;
    }

    public byte[] getBytes() throws IOException {
        return mEntity.body().bytes();
    }

    public String getString() throws IOException {
        return mEntity.body().string();
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
