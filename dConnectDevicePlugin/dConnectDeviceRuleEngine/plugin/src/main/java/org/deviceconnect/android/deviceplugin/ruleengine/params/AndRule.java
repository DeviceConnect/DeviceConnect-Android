/*
 AndRule.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.params;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AndRuleクラス.
 * @author NTT DOCOMO, INC.
 */
public class AndRule implements Serializable {
    /** AND rule service ID. */
    private List<String> mAndRuleServiceId = new ArrayList<>();
    /** Judgement time. */
    private long mJudgementTime = 0L;
    /** Judgement time unit. */
    private String mJudgementTimeUnit = null;


    /**
     * Get AND rule service ID.
     * @return array of AND rule service ID.
     */
    public List<String> getAndRuleServiceId() {
        return mAndRuleServiceId;
    }

    /**
     * Get judgement time.
     * @return Judgement time.
     */
    public long getJudgementTime() {
        return mJudgementTime;
    }

    /**
     * Get judgement time unit.
     * @return Judgement time unit.
     */
    public String getJudgementTimeUnit() {
        return mJudgementTimeUnit;
    }

    /**
     * Set AND rule service ID.
     * @param andRuleServiceId Array of AND rule service ID.
     */
    public void setAndRuleServiceId(final List<String> andRuleServiceId) {
        mAndRuleServiceId = andRuleServiceId;
    }

    /**
     * Set judgement time.
     * @param judgementTime Judgement time.
     */
    public void setJudgementTime(final long judgementTime) {
        mJudgementTime = judgementTime;
    }

    /**
     * Set judgement time unit.
     * @param judgementTimeUnit Judgement time unit.
     */
    public void setJudgementTimeUnit(final String judgementTimeUnit) {
        mJudgementTimeUnit = judgementTimeUnit;
    }
}
