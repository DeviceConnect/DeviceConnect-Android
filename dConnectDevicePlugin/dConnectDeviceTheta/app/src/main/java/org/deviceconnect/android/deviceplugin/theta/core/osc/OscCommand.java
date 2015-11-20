package org.deviceconnect.android.deviceplugin.theta.core.osc;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class OscCommand {

    private final String mName;

    private final JSONObject mParameters = new JSONObject();

    public OscCommand(final String commandName) {
        mName = commandName;
    }

    public String getName() {
        return mName;
    }

    public JSONObject getParameters() {
        return mParameters;
    }

    public void addParameter(final String name, final String value) throws JSONException {
        mParameters.put(name, value);
    }

    public void addParameter(final String name, final boolean value) throws JSONException {
        mParameters.put(name, value);
    }

    public void addParameter(final String name, final int value) throws JSONException {
        mParameters.put(name, value);
    }

    public void addParameter(final String name, final double value) throws JSONException {
        mParameters.put(name, value);
    }

    public static class Result {

        private final Error mError;

        private final HttpResponse mBody;

        private Result(final Error error, final HttpResponse response) {
            mError = error;
            mBody = response;
        }

        public boolean isSuccess() {
            return mError == null;
        }

        public int getHttpStatusCode() {
            return mBody.getStatusCode();
        }

        public Error getError() {
            return mError;
        }

        public JSONObject getJSON() throws IOException, JSONException {
            if (mBody == null) {
                return null;
            }
            return mBody.getJSON();
        }

        public byte[] getBytes() throws IOException {
            if (mBody == null) {
                return null;
            }
            return mBody.getBytes();
        }

        public InputStream getInputStream() throws IOException {
            if (mBody == null) {
                return null;
            }
            return mBody.getStream();
        }

        public static Result parse(final HttpResponse response) throws IOException, JSONException {
            int status = response.getStatusCode();
            if (status == 200) {
                return new Result(null, response);
            } else {
                JSONObject json = response.getJSON();
                JSONObject error = json.getJSONObject("error");
                String code = error.getString("code");
                Error errorCode = Error.parse(code);
                return new Result(errorCode, response);
            }
        }
    }

    public enum Error {

        UNKNOWN_COMMAND("unknownCommand"),
        DISABLED_COMMAND("disabledCommand"),
        MISSING_PARAMETER("missingParameter"),
        INVALID_PARAMETER_NAME("invalidParameterName"),
        INVALID_SESSION_ID("invalidSessionId"),
        INVALID_PARAMETER_VALUE("invalidParameterValue"),
        CORRUPTED_FILE("corruptedFile"),
        CAMERA_IN_EXCLUSIVE_USE("cameraInExclusiveUse"),
        POWER_OFF_SEQUENCE_RUNNING("powerOffSequenceRunning"),
        INVALID_FILE_FORMAT("invalidFileFormat"),
        SERVICE_UNAVAILABLE("serviceUnavailable"),
        UNEXPECTED("unexpected");

        private final String mCode;

        Error(final String code) {
            mCode = code;
        }

        public String getCode() {
            return mCode;
        }

        public static Error parse(final String code) {
            for (Error e : values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            return null;
        }
    }

}
