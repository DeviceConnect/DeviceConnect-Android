/*
 ErrorStatus.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.params;

/**
 * ErrorStatusクラス.
 * @author NTT DOCOMO, INC.
 */
public class ErrorStatus {
    /** 正常動作. */
    public static String STATUS_NORMAL = "normal";
    /** データ無し. */
    public static String NO_DATA = "no_data";

    /** エラーステータス. */
    private String mStatus = NO_DATA;
    /** エラー発生日時. */
    private String mTimestamp = NO_DATA;

    /**
     * エラーステータス取得.
     * @return エラーステータス.
     */
    public String getStatus() {
        return mStatus;
    }

    /**
     * エラー発生日時取得.
     * @return エラー発生日時.
     */
    public String getTimestamp() {
        return mTimestamp;
    }

    /**
     * エラーステータス設定.
     * @param status エラーステータス.
     */
    public void setStatus(String status) {
        mStatus = status;
    }

    /**
     * エラー発生日時設定.
     * @param timestamp エラー発生日時.
     */
    public void setTimestamp(String timestamp) {
        mTimestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorStatus)) return false;

        ErrorStatus that = (ErrorStatus) o;

        return mStatus.equals(that.mStatus) && mTimestamp.equals(that.mTimestamp);
    }

    @Override
    public int hashCode() {
        int result = mStatus.hashCode();
        result = 31 * result + mTimestamp.hashCode();
        return result;
    }
}
