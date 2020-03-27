package org.deviceconnect.android.libmedia.streaming.sdp.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.deviceconnect.android.libmedia.streaming.sdp.Attribute;

public class FormatAttribute extends Attribute {
    private Integer mFormat;
    private Map<String,String> mParameters = new LinkedHashMap<>();

    public FormatAttribute(Integer format) {
        mFormat = format;
    }

    public FormatAttribute(String line) {
        int indexOf = line.indexOf(" ");
        if (indexOf == -1) {
            mFormat = Integer.parseInt(line.trim());
        } else {
            String formatStr = line.substring(0, indexOf).trim();
            String parameterStr = line.substring(indexOf + 1).trim();

            mFormat = Integer.parseInt(formatStr);

            String[] attrs = parameterStr.split(";");
            for (String attr : attrs) {
                int i = attr.trim().indexOf("=");
                if (i != -1) {
                    String key = attr.trim().substring(0, i);
                    String value = attr.trim().substring(i + 1);
                    mParameters.put(key, value);
                }
            }
        }
    }

    public void setFormat(Integer format) {
        mFormat = format;
    }

    public Integer getFormat() {
        return mFormat;
    }

    public void addParameter(String key, String value) {
        mParameters.put(key, value);
    }

    public Map<String, String> getParameters() {
        return mParameters;
    }

    @Override
    public String getField() {
        return "fmtp";
    }

    @Override
    public String getValue() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String,String> entry : mParameters.entrySet()) {
            builder.append(entry.getKey());
            String value = entry.getValue();
            if (value != null) {
                builder.append("=");
                builder.append(value);
            }
            builder.append(";");
        }
        return mFormat + " " + builder.toString();
    }
}
