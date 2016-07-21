package org.deviceconnect.android.profile.spec.parser;


import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.json.JSONException;
import org.json.JSONObject;

public interface DConnectProfileSpecJsonParser {

    DConnectProfileSpec parseJson(JSONObject json) throws JSONException;

}
