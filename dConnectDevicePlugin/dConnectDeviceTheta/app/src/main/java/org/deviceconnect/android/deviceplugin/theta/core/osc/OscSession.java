package org.deviceconnect.android.deviceplugin.theta.core.osc;


import org.json.JSONException;
import org.json.JSONObject;

public class OscSession {

    private static final String PARAM_SESSION_ID = "sessionId";

    private static final String PARAM_TIMEOUT = "timeout";

    private String mSessionId;

    private long mTimeout;

    private OscSession() {
    }

    public static OscSession parse(final JSONObject session) throws JSONException {
        OscSession result = new OscSession();
        result.mSessionId = session.getString(PARAM_SESSION_ID);
        result.mTimeout = session.getInt(PARAM_TIMEOUT);
        return result;
    }

    public String getId() {
        return mSessionId;
    }

    public long getTimeout() {
        return mTimeout;
    }
}
