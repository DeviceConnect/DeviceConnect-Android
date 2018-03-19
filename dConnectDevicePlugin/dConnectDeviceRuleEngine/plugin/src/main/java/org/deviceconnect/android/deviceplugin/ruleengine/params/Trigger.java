/*
 Trigger.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.params;

import org.deviceconnect.android.deviceplugin.ruleengine.utils.TimerUtil;

import java.io.Serializable;

/**
 * Triggerクラス.
 * @author NTT DOCOMO, INC.
 */
public class Trigger implements Serializable {
    /** Interval. */
    private long mInterval = 0L;
    /** Interval unit. */
    private String mIntervalUnit = null;
    /** Reference time. */
    private String mReferenceTime = TimerUtil.REF_NO_SETTING;
    /** Rest. */
    private String mRest = null;
    /** Action. */
    private String mAction = null;
    /** Parameter. */
    private String mParameter = null;
    /** Comparision left. */
    private String mComparisionLeft = null;
    /** Comparision left data type. */
    private String mComparisionLeftDataType = null;
    /** Comparision. */
    private String mComparision = null;
    /** Comparision right. */
    private String mComparisionRight = null;
    /** Comparision right data type. */
    private String mComparisionRightDataType = null;

    /**
     * Get interval.
     * @return Interval.
     */
    public long getInterval() {
        return mInterval;
    }

    /**
     * Get interval unit.
     * @return Interval unit.
     */
    public String getIntervalUnit() {
        return mIntervalUnit;
    }

    /**
     * Get reference time.
     * @return Reference time.
     */
    public String getReferenceTime() {
        return mReferenceTime;
    }
    /**
     * Get rest.
     * @return Rest.
     */
    public String getRest() {
        return mRest;
    }

    /**
     * Get action.
     * @return Action.
     */
    public String getAction() {
        return mAction;
    }

    /**
     * Get parameter.
     * @return Parameter.
     */
    public String getParameter() {
        return mParameter;
    }

    /**
     * Get coparision left data type.
     * @return Coparision left data type.
     */
    public String getComparisionLeftDataType() {
        return mComparisionLeftDataType;
    }

    /**
     * Get coparision left.
     * @return Coparision left.
     */
    public String getComparisionLeft() {
        return mComparisionLeft;
    }

    /**
     * Get coparision.
     * @return Coparision.
     */
    public String getComparision() {
        return mComparision;
    }

    /**
     * Get coparision right data type.
     * @return Coparision right data type.
     */
    public String getComparisionRightDataType() {
        return mComparisionRightDataType;
    }

    /**
     * Get coparision right.
     * @return Coparision right.
     */
    public String getComparisionRight() {
        return mComparisionRight;
    }

    /**
     * Set interval.
     * @param interval Execute interval.
     */
    public void setInterval(final long interval) {
        mInterval = interval;
    }

    /**
     * Set interval unit.
     * @param intervalUnit Interval unit.
     */
    public void setIntervalUnit(final String intervalUnit) {
        mIntervalUnit = intervalUnit;
    }

    /**
     * Get reference time.
     * @param referenceTime Reference time.
     */
    public void setReferenceTime(final String referenceTime) {
        mReferenceTime = referenceTime;
    }

    /**
     * Set rest.
     * @param rest Rest.
     */
    public void setRest(final String rest) {
        mRest = rest;
    }

    /**
     * Set action.
     * @param action Action.
     */
    public void setAction(final String action) {
        mAction = action;
    }

    /**
     * Set parameter.
     * @param parameter Parameter.
     */
    public void setParameter(final String parameter) {
        mParameter = parameter;
    }

    /**
     * Set comparision left.
     * @param comparisionLeft Comparision left.
     */
    public void setComparisionLeft(final String comparisionLeft) {
        mComparisionLeft = comparisionLeft;
    }

    /**
     * Set comparision left data type.
     * @param comparisionLeftDataType Comparision left data type.
     */
    public void setComparisionLeftDataType(final String comparisionLeftDataType) {
        mComparisionLeftDataType = comparisionLeftDataType;
    }

    /**
     * Set comparision.
     * @param comparision Comparision.
     */
    public void setComparision(final String comparision) {
        mComparision = comparision;
    }

    /**
     * Set comparision right.
     * @param comparisionRight Comparision right.
     */
    public void setComparisionRight(final String comparisionRight) {
        mComparisionRight = comparisionRight;
    }

    /**
     * Set comparision right data type.
     * @param comparisionRightDatatype Comparision right data type.
     */
    public void setComparisionRightDataType(final String comparisionRightDatatype) {
        mComparisionRightDataType = comparisionRightDatatype;
    }
}
