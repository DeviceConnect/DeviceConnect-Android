/*
 Operation.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.params;

import java.io.Serializable;

/**
 * Operationクラス.
 * @author NTT DOCOMO, INC.
 */
public class Operation implements Serializable {
    /** Index. */
    private long mIndex = 0L;
    /** Rest. */
    private String mRest = null;
    /** Action. */
    private String mAction = null;
    /** Parameter. */
    private String mParameter = null;
    /** delayOccurrence. */
    private String mDelayOccurrence = SKIP;

    /** delayOccurrence param - skip */
    public static final String SKIP = "skip";
    /** delayOccurrence param - stack */
    public static final String STACK = "stack";

    /**
     * Get index.
     * @return Index.
     */
    public long getIndex() {
        return mIndex;
    }

    /**
     * Ger rest.
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
     * Get parameer.
     * @return Parameter.
     */
    public String getParameter() {
        return mParameter;
    }

    /**
     * Get delay occurrence.
     * @return Delay occurrence.
     */
    public String getDelayOccurrence() {
        return mDelayOccurrence;
    }

    /**
     * Set index.
     * @param index Index.
     */
    public void setIndex(final long index) {
        mIndex = index;
    }

    /**
     * Set rest.
     * @param rest rest.
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
     * Set delay occurrence
     * @param delayOccurrence Delay occurrence.
     */
    public void setDelayOccurrence(String delayOccurrence) {
        mDelayOccurrence = delayOccurrence;
    }

    public boolean equals(final Object obj) {
        Operation op = (Operation) obj;
        long index = op.getIndex();
        return mIndex == index;
    }
}
