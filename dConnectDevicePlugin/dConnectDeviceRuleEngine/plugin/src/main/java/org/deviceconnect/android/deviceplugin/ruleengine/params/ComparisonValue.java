/*
 ComparisonValue.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.params;

import org.deviceconnect.android.deviceplugin.ruleengine.utils.ComparisonUtil;

/**
 * ComparisonValueクラス.
 * @author NTT DOCOMO, INC.
 */
public class ComparisonValue {
    /** 配列アクセス用定数. */
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    /** 配列サイズ. */
    private static final int SIZE = 2;
    /** データ型. */
    private String mDataType;
    /** intデータ. */
    private int[] mDataInt = new int[SIZE];
    /** longデータ. */
    private long[] mDataLong = new long[SIZE];
    /** floatデータ. */
    private float[] mDataFloat = new float[SIZE];
    /** doubleデータ. */
    private double[] mDataDouble = new double[SIZE];
    /** stringデータ. */
    private String[] mDataString = new String[SIZE];
    /** booleanデータ. */
    private Boolean[] mDataBoolean = new Boolean[SIZE];

    public ComparisonValue(final String dataType) {
        if (ComparisonUtil.checkDataType(dataType)) {
            mDataType = dataType;
        }
    }

    public String getDataType() {
        return mDataType;
    }

    public Integer getDataInt(final int index) {
        if (index == FIRST || index == SECOND) {
            return mDataInt[index];
        } else {
            return null;
        }
    }

    public Long getDataLong(final int index) {
        if (index == FIRST || index == SECOND) {
            return mDataLong[index];
        } else {
            return null;
        }
    }

    public Float getDataFloat(final int index) {
        if (index == FIRST || index == SECOND) {
            return mDataFloat[index];
        } else {
            return null;
        }
    }

    public Double getDataDouble(final int index) {
        if (index == FIRST || index == SECOND) {
            return mDataDouble[index];
        } else {
            return null;
        }
    }

    public String getDataString(final int index) {
        if (index == FIRST || index == SECOND) {
            return mDataString[index];
        } else {
            return null;
        }
    }

    public Boolean getDataBoolean(final int index) {
        if (index == FIRST || index == SECOND) {
            return mDataBoolean[index];
        } else {
            return null;
        }
    }

    public void setDataType(String dataType) {
        if (ComparisonUtil.checkDataType(dataType)) {
            mDataType = dataType;
        }
    }

    public boolean setDataInt(final int index, int dataInt) {
        if (index == FIRST || index == SECOND) {
            mDataInt[index] = dataInt;
            return true;
        } else {
            return false;
        }
    }

    public boolean setDataLong(final int index, long dataLong) {
        if (index == FIRST || index == SECOND) {
            mDataLong[index] = dataLong;
            return true;
        } else {
            return false;
        }
    }

    public boolean setDataFloat(final int index, float dataFloat) {
        if (index == FIRST || index == SECOND) {
            mDataFloat[index] = dataFloat;
            return true;
        } else {
            return false;
        }
    }

    public boolean setDataDouble(final int index, double dataDouble) {
        if (index == FIRST || index == SECOND) {
            mDataDouble[index] = dataDouble;
            return true;
        } else {
            return false;
        }
    }

    public boolean setDataString(final int index, String dataString) {
        if (index == FIRST || index == SECOND) {
            mDataString[index] = dataString;
            return true;
        } else {
            return false;
        }
    }

    public boolean setDataBooleam(final int index, Boolean dataBoolean) {
        if (index == FIRST || index == SECOND) {
            mDataBoolean[index] = dataBoolean;
            return true;
        } else {
            return false;
        }
    }
}
