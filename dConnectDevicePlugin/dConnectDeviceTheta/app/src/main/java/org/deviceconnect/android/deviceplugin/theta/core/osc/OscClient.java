package org.deviceconnect.android.deviceplugin.theta.core.osc;


import com.burgstaller.okhttp.digest.Credentials;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.net.SocketFactory;

public class OscClient {

    private static final HttpRequest.Method GET = HttpRequest.Method.GET;
    private static final HttpRequest.Method POST = HttpRequest.Method.POST;

    private static final String PATH_INFO = "/osc/info";
    private static final String PATH_STATE = "/osc/state";
    private static final String PATH_COMMANDS_EXECUTE = "/osc/commands/execute";
    private static final String PATH_COMMANDS_STATUS = "/osc/commands/status";

    private static final String REQ_PARAM_ID = "id";

    private static final String RES_PARAM_STATE = "state";
    private static final String RES_PARAM_RESULTS = "results";
    private static final String RES_PARAM_ENTRIES = "entries";

    private final HttpClient mHttpClient;
    private final String mHost;

    public OscClient(final String host, final Credentials credentials, final SocketFactory socketFactory) {
        mHttpClient = new HttpClient(credentials, socketFactory);
        mHost = host;
    }

    public String getHost() {
        return mHost;
    }

    public OscState state() throws IOException, JSONException {
        HttpRequest request = new HttpRequest(POST, getHost(), PATH_STATE);
        HttpResponse response = mHttpClient.execute(request);

        JSONObject json = response.getJSON();
        JSONObject state = json.getJSONObject(RES_PARAM_STATE);
        return OscState.parse(state);
    }

    public OscCommand.Result listAll(final int offset, final int maxLength) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("entryCount", maxLength);
        if (offset > 0) {
            params.put("continuationToken", String.valueOf(offset));
        }

        HttpResponse response = executeCommand("camera._listAll", params);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result listFiles(final int offset, final int maxLength) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileType", "all");
        params.put("entryCount", maxLength);
        if (offset > 0) {
            params.put("startPosition", String.valueOf(offset));
        }

        HttpResponse response = executeCommand("camera.listFiles", params);
        return OscCommand.Result.parse(response);
    }
    // Theta API v2.1以降は必要なくなる
    @Deprecated
    public OscSession startSession() throws IOException, JSONException {
        HttpResponse response = executeCommand("camera.startSession", new JSONObject());
        JSONObject json = response.getJSON();
        JSONObject results = json.getJSONObject(RES_PARAM_RESULTS);
        return OscSession.parse(results);
    }

    // Theta API v2.1以降は必要なくなる
    @Deprecated
    public OscCommand.Result closeSession(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        HttpResponse response = executeCommand("camera.closeSession", params);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result takePicture() throws IOException, JSONException {
        return takePicture(null);
    }
    public OscCommand.Result takePicture(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        if (sessionId != null) {
            params.put("sessionId", sessionId);
        }
        HttpResponse response = executeCommand("camera.takePicture", params);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result startCapture() throws IOException, JSONException {
        HttpResponse response = executeCommand("camera.startCapture", null);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result startCapture(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        HttpResponse response = executeCommand("camera._startCapture", params);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result stopCapture() throws IOException, JSONException {
        HttpResponse response = executeCommand("camera.stopCapture", null);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result stopCapture(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        HttpResponse response = executeCommand("camera._stopCapture", params);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result delete(final String fileUri) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);

        HttpResponse response = executeCommand("camera.delete", params);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result delete(final List<String> fileUrls) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        JSONArray files = new JSONArray();
        for (String file : fileUrls) {
            files.put(file);
        }
        params.put("fileUrls", files);
        HttpResponse response = executeCommand("camera.delete", params);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result getImage(final String fileUri, final boolean isThumbnail) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);
        params.put("_type", isThumbnail ? "thumb" : "full");

        HttpResponse response = executeCommand("camera.getImage", params);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result getImageFromFileURL(final String fileUri, final boolean isThumbnail) throws IOException, JSONException {
        String url = fileUri;
        if (isThumbnail) {
            url = fileUri + "?type=thumb";
        }

        HttpResponse response = getFile(url);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result getVideo(final String fileUri, final boolean isThumbnail) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);
        params.put("_type", isThumbnail ? "thumb" : "full");

        HttpResponse response = executeCommand("camera._getVideo", params);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result getVideoFromFileURL(final String fileUri, final boolean isThumbnail) throws IOException, JSONException {
        String url = fileUri;
        if (isThumbnail) {
            url = fileUri + "?type=thumb";
        }

        HttpResponse response = getFile(url);
        return OscCommand.Result.parse(response);
    }
    public InputStream getLivePreview() throws IOException, JSONException {
        HttpResponse response = executeCommand("camera.getLivePreview", null);
        return response.getStream();
    }
    public InputStream getLivePreview(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        HttpResponse response = executeCommand("camera._getLivePreview", params);
        return response.getStream();
    }

    public OscCommand.Result getMetaData(final String fileUri) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);

        HttpResponse response = executeCommand("camera.getMetadata", params);
        return OscCommand.Result.parse(response);
    }
    public OscCommand.Result getOptions(final JSONArray optionNames) throws IOException, JSONException {
        return getOptions(null, optionNames);
    }
    public OscCommand.Result getOptions(final String sessionId, final JSONArray optionNames) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        if (sessionId != null) {
            params.put("sessionId", sessionId);
        }
        params.put("optionNames", optionNames);

        HttpResponse response = executeCommand("camera.getOptions", params);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result setOptions(final JSONObject options) throws IOException, JSONException {
        return setOptions(null, options);
    }
    public OscCommand.Result setOptions(final String sessionId, final JSONObject options) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        if (sessionId != null) {
            params.put("sessionId", sessionId);
        }
        params.put("options", options);

        HttpResponse response = executeCommand("camera.setOptions", params);
        return OscCommand.Result.parse(response);
    }

    private HttpResponse executeCommand(final String name, final JSONObject params) throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            if (params != null) {
                body.put("parameters", params);
            }
            HttpRequest request = new HttpRequest(POST, getHost(), PATH_COMMANDS_EXECUTE);
            request.setBody(body.toString());
            return mHttpClient.execute(request);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

    private HttpResponse getFile(final String url) throws IOException {
        HttpRequest request = new HttpRequest(GET, url);
        return mHttpClient.execute(request);
    }

    private OscCommand.Result statusCommand(final String commandId) throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.put(REQ_PARAM_ID, commandId);

            HttpRequest request = new HttpRequest(POST, getHost(), PATH_COMMANDS_STATUS);
            request.setBody(body.toString());
            HttpResponse response = mHttpClient.execute(request);
            return OscCommand.Result.parse(response);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

    public OscCommand.Result waitForDone(final String commandId) throws IOException, JSONException, InterruptedException {
        for (;;) {
            OscCommand.Result result = statusCommand(commandId);
            JSONObject json = result.getJSON();
            String state = json.getString(RES_PARAM_STATE);
            if ("done".equals(state)) {
                return result;
            }

            Thread.sleep(200);
        }
    }

}
