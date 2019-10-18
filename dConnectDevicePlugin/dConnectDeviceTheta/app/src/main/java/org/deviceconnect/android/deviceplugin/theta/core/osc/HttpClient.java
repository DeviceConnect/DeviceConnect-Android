package org.deviceconnect.android.deviceplugin.theta.core.osc;


import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient mOkHttpClient;

    public HttpClient(final Credentials credentials) {
        this(credentials, null);
    }

    public HttpClient(final Credentials credentials, final SocketFactory socketFactory) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);
        if (credentials != null) {
            builder.authenticator(new DigestAuthenticator(credentials));
        }
        if (socketFactory != null) {
            builder.socketFactory(socketFactory);
        }
        mOkHttpClient = builder.build();
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
