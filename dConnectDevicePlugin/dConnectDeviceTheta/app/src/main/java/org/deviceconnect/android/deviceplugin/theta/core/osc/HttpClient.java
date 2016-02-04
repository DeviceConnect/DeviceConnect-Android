package org.deviceconnect.android.deviceplugin.theta.core.osc;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    public HttpClient() {
        mOkHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(60, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
    }

    public HttpResponse execute(final HttpRequest request) throws IOException {
        switch (request.getMethod()) {
            case GET:
                return get(request);
            case POST:
                return post(request);
            default:
                throw new IllegalStateException();
        }
    }

    private HttpResponse get(final HttpRequest request) throws IOException {
        Request call = new Request.Builder().url(request.getUri()).build();
        Response response = mOkHttpClient.newCall(call).execute();
        return new HttpResponse(response);
    }

    private HttpResponse post(final HttpRequest request) throws IOException {
        RequestBody reqBody = RequestBody.create(JSON, request.getBody());
        Request call = new Request.Builder()
            .url(request.getUri())
            .post(reqBody)
            .build();

        Response response =  mOkHttpClient.newCall(call).execute();
        return new HttpResponse(response);
    }

}
