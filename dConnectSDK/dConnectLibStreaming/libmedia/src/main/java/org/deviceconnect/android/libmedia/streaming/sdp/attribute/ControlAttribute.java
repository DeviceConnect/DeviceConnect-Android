package org.deviceconnect.android.libmedia.streaming.sdp.attribute;

import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;

/**
 *
 *
 * 20.3.  SDP Extension Syntax
 *
 * @see <a href="https://tools.ietf.org/html/rfc7826">RFC7826</a>
 */
public class ControlAttribute extends Attribute {

    /**
     * trackID=xxx
     * or
     * streamID=xxx
     */
    private String mID;

    public ControlAttribute() {
    }

    public ControlAttribute(String id) {
        mID = id;
    }

    public String getID() {
        return mID;
    }

    public void setID(String ID) {
        mID = ID;
    }

    @Override
    public String getField() {
        return "control";
    }

    @Override
    public String getValue() {
        return mID;
    }
}
