/*
 RequestParam.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;


import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

/**
 * Representation of Request Parameter.
 *
 * @author NTT DOCOMO, INC.
 */
class RequestParam {

    private final String mName;
    private final Type mType;
    private final Range mRange;

    RequestParam(final String name, final Type type, final Range range) {
        mName = name;
        mType = type;
        mRange = range;
    }

    boolean check(final Intent request, final Intent response) {
        Object obj = request.getExtras().get(mName);
        if (obj == null) {
            return true;
        }
        if (!checkParamType(obj, response)) {
            return false;
        }
        if (mRange != null) {
            switch (mType) {
                case INT:
                    if (!mRange.checkInt(parseInt(obj))) {
                        MessageUtils.setInvalidRequestParameterError(response, mRange.getErrorMessage(mName));
                        return false;
                    }
                    break;
                case DOUBLE:
                    if (!mRange.checkDouble(parseDouble(obj))) {
                        MessageUtils.setInvalidRequestParameterError(response, mRange.getErrorMessage(mName));
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private boolean checkParamType(final Object value, final Intent response) {
        switch (mType) {
            case INT:
                try {
                    parseInt(value);
                    return true;
                } catch (NumberFormatException e) {
                    // Nothing to do.
                }
                break;
            case DOUBLE:
                try {
                    parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    // Nothing to do.
                }
                break;
            default:
                // Unknown type of request parameter.
        }
        MessageUtils.setInvalidRequestParameterError(response, mName + " is invalid type.");
        return false;
    }

    private int parseInt(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        }
        throw new NumberFormatException();
    }

    private int parseDouble(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        }
        throw new NumberFormatException();
    }

    enum Type {

        INT,

        DOUBLE

    }

    static class Range {

        boolean checkInt(final int value) {
            return false;
        }

        boolean checkDouble(final double value) {
            return false;
        }

        String getErrorMessage(final String paramName) {
            return paramName + " is invalid range.";
        }

    }

}
