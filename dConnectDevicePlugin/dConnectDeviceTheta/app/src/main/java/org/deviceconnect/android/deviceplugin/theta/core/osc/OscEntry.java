package org.deviceconnect.android.deviceplugin.theta.core.osc;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OscEntry {

    private String mName;

    private String mUri;

    private long mSize;

    private String mDateTime;

    private int mWidth;

    private int mHeight;

    private OscEntry() {
    }

    public static List<OscEntry> parseList(final JSONArray entries, final boolean isDetail) throws JSONException {
        List<OscEntry> result = new ArrayList<OscEntry>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            result.add(OscEntry.parse(entry, true));
        }
        return result;
    }

    public static OscEntry parse(final JSONObject entry, final boolean isDetail) throws JSONException{
        OscEntry result = new OscEntry();
        result.mName = entry.getString("name");
        if (entry.isNull("uri")) {
            result.mUri = entry.getString("fileUrl");
        } else {
            result.mUri = entry.getString("uri");
        }
        result.mSize = Long.parseLong(entry.getString("size"));

        if (isDetail) {
            result.mWidth = entry.getInt("width");
            result.mHeight = entry.getInt("height");
            result.mDateTime = entry.getString("dateTimeZone");
        } else {
            result.mDateTime = entry.getString("dateTime");
        }
        return result;
    }

    public String getName() {
        return mName;
    }

    public String getUri() {
        return mUri;
    }

    public long getSize() {
        return mSize;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getDateTime() {
        return mDateTime;
    }
}
