/*
 RequestParam.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

/**
 * Representation of Request Parameter specification.
 *
 * @author NTT DOCOMO, INC.
 */
class RequestParam {

    private final String mName;
    private final Type mType;
    private final boolean mIsOption;
    private final Range mRange;

    RequestParam(final String name, final Type type, final boolean isOption, final Range range) {
        mName = name;
        mType = type;
        mIsOption = isOption;
        mRange = range;
    }

    boolean check(final Intent request, final Intent response) {
        Object obj = request.getExtras().get(mName);
        if (obj == null) {
            if (mIsOption) {
                return true;
            } else {
                MessageUtils.setInvalidRequestParameterError(response, mName + " is null.");
                return false;
            }
        }
        if (!checkParamType(obj, response)) {
            return false;
        }
        if (mRange != null) {
            switch (mType) {
                case INT:
                    if (!mRange.check(parseInt(obj))) {
                        MessageUtils.setInvalidRequestParameterError(response, mRange.getErrorMessage(mName));
                        return false;
                    }
                    break;
                case DOUBLE:
                    if (!mRange.check(parseDouble(obj))) {
                        MessageUtils.setInvalidRequestParameterError(response, mRange.getErrorMessage(mName));
                        return false;
                    }
                    break;
                case STRING:
                    if (!mRange.check((String) obj)) {
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
            case STRING:
                if (value instanceof String) {
                    return true;
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

    private double parseDouble(Object obj) {
        if (obj instanceof Double) {
            return (Double) obj;
        }
        if (obj instanceof String) {
            return Double.parseDouble((String) obj);
        }
        throw new NumberFormatException();
    }

    enum Type {

        INT,

        DOUBLE,

        STRING

    }

    static class Range {

        boolean check(final int value) {
            return false;
        }

        boolean check(final double value) {
            return false;
        }

        boolean check(final String value) {
            return false;
        }

        String getErrorMessage(final String paramName) {
            return paramName + " is invalid range.";
        }

    }

}
