package org.deviceconnect.android.deviceplugin.theta.core.osc;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class OscClient {

    private static final String HOST = "192.168.1.1:80";
    private static final HttpRequest.Method GET = HttpRequest.Method.GET;
    private static final HttpRequest.Method POST = HttpRequest.Method.POST;

    private static final String PATH_INFO = "/osc/info";
    private static final String PATH_STATE = "/osc/state";
    private static final String PATH_COMMANDS_EXECUTE = "/osc/commands/execute";

    private static final String RES_PARAM_STATE = "state";
    private static final String RES_PARAM_RESULTS = "results";
    private static final String RES_PARAM_ENTRIES = "entries";

    private final HttpClient mHttpClient;

    public OscClient() {
        mHttpClient = new HttpClient();
    }

    public OscState state() throws IOException, JSONException {
        HttpRequest request = new HttpRequest(POST, HOST, PATH_STATE);
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

    public OscSession startSession() throws IOException, JSONException {
        HttpResponse response = executeCommand("camera.startSession", new JSONObject());
        JSONObject json = response.getJSON();
        JSONObject results = json.getJSONObject(RES_PARAM_RESULTS);
        return OscSession.parse(results);
    }

    public void closeSession(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        executeCommand("camera.closeSession", params);
    }

    public String takePicture(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        HttpResponse response = executeCommand("camera.takePicture", params);
        JSONObject json = response.getJSON();
        JSONObject results = json.getJSONObject(RES_PARAM_RESULTS);
        return results.getString("fileUri");
    }

    public void startCapture(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        executeCommand("camera._startCapture", params);
    }

    public void stopCapture(final String sessionId) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("sessionId", sessionId);

        executeCommand("camera._stopCapture", params);
    }

    public void delete(final String fileUri) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);

        executeCommand("camera.delete", params);
    }

    public OscCommand.Result getImage(final String fileUri, final boolean isThumbnail) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);
        params.put("_type", isThumbnail ? "thumb" : "full");

        HttpResponse response = executeCommand("camera.getImage", params);
        return OscCommand.Result.parse(response);
    }

    public OscCommand.Result getVideo(final String fileUri, final boolean isThumbnail) throws IOException, JSONException {
        JSONObject params = new JSONObject();
        params.put("fileUri", fileUri);
        params.put("_type", isThumbnail ? "thumb" : "full");

        HttpResponse response = executeCommand("camera._getVideo", params);
        return OscCommand.Result.parse(response);
    }

    private HttpResponse executeCommand(final String name, final JSONObject params) throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("parameters", params);

            HttpRequest request = new HttpRequest(POST, HOST, PATH_COMMANDS_EXECUTE);
            request.setBody(body.toString());
            HttpResponse response = mHttpClient.execute(request);
            return response;
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

}
