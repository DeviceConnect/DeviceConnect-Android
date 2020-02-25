package org.deviceconnect.android.libmedia.streaming.sdp.attribute;

import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;

public class RtpMapAttribute extends Attribute {

    private Integer mFormat;
    private String mEncodingName;
    private Integer mRate;
    private String mParameters;

    public RtpMapAttribute(Integer format, String encodingName, Integer rate) {
        mFormat = format;
        mEncodingName = encodingName;
        mRate = rate;
    }


    public RtpMapAttribute(Integer format, String encodingName, Integer rate, Integer channel) {
        mFormat = format;
        mEncodingName = encodingName;
        mRate = rate;
        mParameters = String.valueOf(channel);
    }

    public RtpMapAttribute(Integer format, String encodingName, Integer rate, String parameters) {
        mFormat = format;
        mEncodingName = encodingName;
        mRate = rate;
        mParameters = parameters;
    }

    public RtpMapAttribute(String line) {
        String[] params = line.split(" ");
        mFormat = Integer.parseInt(params[0]);

        String[] params2 = params[1].split("/");
        mEncodingName = params2[0];
        mRate = Integer.parseInt(params2[1]);

        if (params2.length > 2) {
            mParameters = params[2];
        }
    }

    public Integer getFormat() {
        return mFormat;
    }

    public void setFormat(Integer format) {
        mFormat = format;
    }

    public String getEncodingName() {
        return mEncodingName;
    }

    public void setEncodingName(String encodingName) {
        mEncodingName = encodingName;
    }

    public Integer getRate() {
        return mRate;
    }

    public void setRate(Integer rate) {
        mRate = rate;
    }

    public String getParameters() {
        return mParameters;
    }

    public void setParameters(String parameters) {
        mParameters = parameters;
    }

    @Override
    public String getField() {
        return "rtpmap";
    }

    @Override
    public String getValue() {
        return mFormat + " " + mEncodingName + "/" + mRate + (mParameters!=null ? "/" + mParameters : "");
    }
}
