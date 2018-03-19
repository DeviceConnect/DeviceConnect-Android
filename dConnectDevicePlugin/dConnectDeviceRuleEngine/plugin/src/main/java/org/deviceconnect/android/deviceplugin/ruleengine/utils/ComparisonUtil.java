/*
 ComparisonUtil.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.utils;

import org.deviceconnect.android.deviceplugin.ruleengine.params.ComparisonValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ComparisonUtilクラス.
 * @author NTT DOCOMO, INC.
 */
public class ComparisonUtil {
    /** 未満(英字). */
    private static final String LESS_THAN = "LessThan";
    /** 未満(1バイト文字). */
    private static final String LESS_THAN_MARK_1 = "<";
    /** 未満(2バイト文字). */
    private static final String LESS_THAN_MARK_2 = "＜";

    /** 以下(英字). */
    private static final String BELOW = "Below";
    /** 以下(1バイト文字). */
    private static final String BELOW_MARK_1 = "<=";
    /** 以下(2バイト文字). */
    private static final String BELOW_MARK_2 = "≦";

    /** 超過(英字). */
    private static final String EXCESS = "Excess";
    /** 超過(1バイト文字). */
    private static final String EXCESS_MARK_1 = ">";
    /** 超過(2バイト文字). */
    private static final String EXCESS_MARK_2 = "＞";

    /** 以上(英字). */
    private static final String NOT_LESS_THAN = "NotLessThan";
    /** 以上(1バイト文字). */
    private static final String NOT_LESS_THAN_MARK_1 = ">=";
    /** 以上(2バイト文字). */
    private static final String NOT_LESS_THAN_MARK_2 = "≧";

    /** 等しい(英字). */
    private static final String EQUAL = "Equal";
    /** 等しい(1バイト文字). */
    private static final String EQUAL_MARK_1 = "=";
    /** 等しい(2バイト文字). */
    private static final String EQUAL_MARK_2 = "＝";

    /** 等しくない(英字). */
    private static final String NOT_EQUAL = "NotEqual";
    /** 等しくない(1バイト文字). */
    private static final String NOT_EQUAL_MARK_1 = "!=";
    /** 等しくない(2バイト文字). */
    private static final String NOT_EQUAL_MARK_2 = "≠";

    /** 指定範囲感(英字). */
    private static final String BETWEEN = "Between";
    /** 指定範囲感(1バイト文字). */
    private static final String BETWEEN_MARK_1 = "~";
    /** 指定範囲感(2バイト文字). */
    private static final String BETWEEN_MARK_2 = "〜";

    /** 型指定 int型 */
    public static final String TYPE_INT = "TypeInt";
    /** 型指定 long型 */
    public static final String TYPE_LONG = "TypeLong";
    /** 型指定 float型 */
    public static final String TYPE_FLOAT = "TypeFloat";
    /** 型指定 double型 */
    public static final String TYPE_DOUBLE = "TypeDouble";
    /** 型指定 String型 */
    public static final String TYPE_STRING = "TypeString";
    /** 型指定 Boolean型 */
    public static final String TYPE_BOOLEAN = "TypeBoolean";
    /** 型指定 日付時間 */
    public static final String TYPE_DATE_TIME = "TypeDateTime";
    /** 型指定 日付 */
    public static final String TYPE_DATE = "TypeDate";
    /** 型指定 時間 */
    public static final String TYPE_TIME = "TypeTime";
    /** 型指定 JSONパラメータ - int型 */
    public static final String TYPE_JSON_INT = "TypeJsonInt";
    /** 型指定 JSONパラメータ - float型 */
    public static final String TYPE_JSON_FLOAT = "TypeJsonFloat";
    /** 型指定 JSONパラメータ - double型 */
    public static final String TYPE_JSON_DOUBLE = "TypeJsonDouble";
    /** 型指定 JSONパラメータ - String型 */
    public static final String TYPE_JSON_STRING = "TypeJsonString";
    /** 型指定 JSONパラメータ - Boolean型 */
    public static final String TYPE_JSON_BOOLEAN = "TypeJsonBoolean";
    /** 型指定 JSONパラメータ - 日付時間 */
    public static final String TYPE_JSON_DATE_TIME = "TypeJsonDateTime";

    /** 判定応答 成立. */
    public static final int RES_TRUE = 1;
    /** 判定応答 不成立. */
    public static final int RES_FALSE = 0;
    /** 判定応答 エラー. */
    public static final int RES_ERROR = -1;

    /**
     * 比較子用文字列チェック.
     * @param comparison 比較子.
     * @return true(比較子合致) / false(不一致).
     */
    public static boolean checkComparison(final String comparison) {
        switch (comparison) {
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
                return true;
            default:
                return false;
        }
    }

    /**
     * データ比較処理.
     * @param left 左辺値格納ComparisonValue構造体.
     * @param comparison 比較条件.
     * @param right 右辺値格納ComparisonValue構造体.
     * @return 1(比較条件成立) / 0(比較条件不成立) / -1(エラー).
     */
    public static int judgeComparison(final ComparisonValue left, final String comparison, final ComparisonValue right) {
        // 比較値のデータ型同一判定
        String dataType = left.getDataType().substring(4);
        if (dataType.contains("Json")) {
            dataType = dataType.substring(4);
        }
        if (!(right.getDataType().contains(dataType))) {
            // 型不一致
            return RES_ERROR;
        }

        // 比較処理
        boolean judge;
        switch (left.getDataType()) {
            case ComparisonUtil.TYPE_INT:
            case ComparisonUtil.TYPE_JSON_INT:
                if (right.getDataInt(ComparisonValue.SECOND) == null) {
                    judge = judgeComparison(left.getDataInt(ComparisonValue.FIRST), comparison, right.getDataInt(ComparisonValue.FIRST), 0);
                } else {
                    judge = judgeComparison(left.getDataInt(ComparisonValue.FIRST), comparison, right.getDataInt(ComparisonValue.FIRST), right.getDataInt(ComparisonValue.SECOND));
                }
                break;
            case ComparisonUtil.TYPE_LONG:
                if (right.getDataLong(ComparisonValue.SECOND) == null) {
                    judge = judgeComparison(left.getDataLong(ComparisonValue.FIRST), comparison, right.getDataLong(ComparisonValue.FIRST), 0);
                } else {
                    judge = judgeComparison(left.getDataLong(ComparisonValue.FIRST), comparison, right.getDataLong(ComparisonValue.FIRST), right.getDataLong(ComparisonValue.SECOND));
                }
                break;
            case ComparisonUtil.TYPE_FLOAT:
            case ComparisonUtil.TYPE_JSON_FLOAT:
                if (right.getDataFloat(ComparisonValue.SECOND) == null) {
                    judge = judgeComparison(left.getDataFloat(ComparisonValue.FIRST), comparison, right.getDataFloat(ComparisonValue.FIRST), 0);
                } else {
                    judge = judgeComparison(left.getDataFloat(ComparisonValue.FIRST), comparison, right.getDataFloat(ComparisonValue.FIRST), right.getDataFloat(ComparisonValue.SECOND));
                }
                break;
            case ComparisonUtil.TYPE_DOUBLE:
            case ComparisonUtil.TYPE_JSON_DOUBLE:
                if (right.getDataDouble(ComparisonValue.SECOND) == null) {
                    judge = judgeComparison(left.getDataDouble(ComparisonValue.FIRST), comparison, right.getDataDouble(ComparisonValue.FIRST), 0);
                } else {
                    judge = judgeComparison(left.getDataDouble(ComparisonValue.FIRST), comparison, right.getDataDouble(ComparisonValue.FIRST), right.getDataDouble(ComparisonValue.SECOND));
                }
                break;
            case ComparisonUtil.TYPE_STRING:
            case ComparisonUtil.TYPE_JSON_STRING:
                try {
                    judge = judgeComparison(left.getDataString(ComparisonValue.FIRST), comparison, right.getDataString(ComparisonValue.FIRST));
                } catch (Exception e) {
                    e.printStackTrace();
                    return RES_ERROR;
                }
                break;
            case ComparisonUtil.TYPE_BOOLEAN:
            case ComparisonUtil.TYPE_JSON_BOOLEAN:
                try {
                    judge = judgeComparison(left.getDataBoolean(ComparisonValue.FIRST), comparison, right.getDataBoolean(ComparisonValue.FIRST));
                } catch (Exception e) {
                    e.printStackTrace();
                    return RES_ERROR;
                }
                break;
            case ComparisonUtil.TYPE_DATE:
            case ComparisonUtil.TYPE_TIME:
            case ComparisonUtil.TYPE_DATE_TIME:
            case ComparisonUtil.TYPE_JSON_DATE_TIME:
                Calendar leftDataTime;
                String[] params;
                if (left.getDataString(ComparisonValue.FIRST).contains("DateTime")) {
                    // 現在時刻取得
                    leftDataTime = Calendar.getInstance();
                } else {
                    // 日付日時変換
                    leftDataTime = ComparisonUtil.setCalendar(left.getDataType(), left.getDataString(ComparisonValue.FIRST));
                    if (leftDataTime == null) {
                        return RES_ERROR;
                    }
                }
                params = right.getDataString(ComparisonValue.FIRST).split(",");
                if (params.length == 1) {
                    // 日付日時変換
                    Calendar rightDateTime = ComparisonUtil.setCalendar(right.getDataType(), params[0]);
                    if (rightDateTime == null) {
                        return RES_ERROR;
                    }
                    judge = judgeComparison(leftDataTime, comparison, rightDateTime);
                } else {
                    List<Calendar> rightDateTimes = new ArrayList<>(2);
                    for (int i = 0; i < 2; i++) {
                        Calendar dateTime = ComparisonUtil.setCalendar(right.getDataType(), params[i]);
                        if (dateTime == null) {
                            return RES_ERROR;
                        }
                        rightDateTimes.add(i, dateTime);
                    }
                    judge = judgeComparison(leftDataTime, comparison, rightDateTimes.get(0), rightDateTimes.get(1));
                }
                break;
            default:
                return RES_ERROR;
        }
        if (judge) {
            return RES_TRUE;
        } else {
            return RES_FALSE;
        }
    }

    /**
     * 日付型設定.
     * @param dataType データタイプ.
     * @param params 日付日時パラメータ.
     * @return Carendarオブジェクト.
     */
    private static Calendar setCalendar(final String dataType, final String params) {
        Calendar dataTime = Calendar.getInstance();
        switch (dataType) {
            case ComparisonUtil.TYPE_DATE:
                // 日付
                String[] dateParam = params.split("[\\-]+");
                if (dateParam.length > 3) {
                    return null;
                }
                dataTime.set(Calendar.YEAR, Integer.valueOf(dateParam[0]));
                dataTime.set(Calendar.MONTH, Integer.valueOf(dateParam[1]));
                dataTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateParam[2]));
                dataTime.set(Calendar.HOUR_OF_DAY, 0);
                dataTime.set(Calendar.MINUTE, 0);
                dataTime.set(Calendar.SECOND, 0);
                dataTime.set(Calendar.MILLISECOND, 0);
                break;
            case ComparisonUtil.TYPE_TIME:
                // 時間
                String[] timeParam = params.split("[:.+\\-]+");
                if (timeParam.length > 4) {
                    return null;
                }
                dataTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeParam[0]));
                dataTime.set(Calendar.MINUTE, Integer.valueOf(timeParam[1]));
                dataTime.set(Calendar.SECOND, Integer.valueOf(timeParam[2]));
                if (timeParam.length >= 4) {
                    dataTime.set(Calendar.MILLISECOND, Integer.valueOf(timeParam[3]));
                } else {
                    dataTime.set(Calendar.MILLISECOND, 0);
                }
                break;
            case ComparisonUtil.TYPE_DATE_TIME:
            case ComparisonUtil.TYPE_JSON_DATE_TIME:
                dataTime.clear();
                // 日付 + 時間
                String[] dateTimeParam = params.split("[\\-T:.+]+");
                if (dateTimeParam.length <= 3) {
                    return null;
                }
                dataTime.set(Calendar.YEAR, Integer.valueOf(dateTimeParam[0]));
                dataTime.set(Calendar.MONTH, Integer.valueOf(dateTimeParam[1]));
                dataTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateTimeParam[2]));
                dataTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(dateTimeParam[3]));
                dataTime.set(Calendar.MINUTE, Integer.valueOf(dateTimeParam[4]));
                dataTime.set(Calendar.SECOND, Integer.valueOf(dateTimeParam[5]));
                if (dateTimeParam.length >= 6) {
                    dataTime.set(Calendar.MILLISECOND, Integer.valueOf(dateTimeParam[6]));
                } else {
                    dataTime.set(Calendar.MILLISECOND, 0);
                }
                break;
            default:
                return null;
        }
        return dataTime;
    }

    /**
     * int型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right1 右辺値１.
     * @param right2 右辺値２(Between指定時のみ有効).
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final int left, final String comparison, final int right1, final int right2) {
        switch (comparison) {
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
                return (left < right1);
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
                return (left <= right1);
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
                return (left > right1);
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
                return (left >= right1);
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left == right1);
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return (left != right1);
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
                return (left >= right1 && left <= right2);
            default:
                return false;
        }
    }

    /**
     * long型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right1 右辺１.
     * @param right2 右辺２(Between指定時のみ有効).
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final long left, final String comparison, final long right1, final long right2) {
        switch (comparison) {
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
                return (left < right1);
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
                return (left <= right1);
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
                return (left > right1);
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
                return (left >= right1);
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left == right1);
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return (left != right1);
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
                return (left >= right1 && left <= right2);
            default:
                return false;
        }
    }

    /**
     * double型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right1 右辺１.
     * @param right2 右辺２(Between指定時のみ有効).
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final double left, final String comparison, final double right1, final double right2) {
        switch (comparison) {
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
                return (left < right1);
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
                return (left <= right1);
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
                return (left > right1);
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
                return (left >= right1);
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left == right1);
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return (left != right1);
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
                return (left >= right1 && left <= right2);
            default:
                return false;
        }
    }

    /**
     * float型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right1 右辺１.
     * @param right2 右辺２(Between指定時のみ有効).
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final float left, final String comparison, final float right1, final float right2) {
        switch (comparison) {
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
                return (left < right1);
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
                return (left <= right1);
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
                return (left > right1);
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
                return (left >= right1);
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left == right1);
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return (left != right1);
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
                return (left >= right1 && left <= right2);
            default:
                return false;
        }
    }

    /**
     * String型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right 右辺値.
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final String left, final String comparison, final String right) throws Exception {
        switch (comparison) {
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left.contains(right));
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return !(left.contains(right));
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
            default:
                throw new Exception("illegal call");
        }
    }

    /**
     * Boolean型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right 右辺値.
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final Boolean left, final String comparison, final Boolean right) throws Exception {
        switch (comparison) {
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left == right);
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return !(left == right);
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
            default:
                throw new Exception("illegal call");
        }
    }

    /**
     * 日付型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right 右辺値.
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final Calendar left, final String comparison, final Calendar right) {
        return judgeComparison(left, comparison, right, null);
    }

    /**
     * 日付型データ比較処理
     * @param left 左辺値.
     * @param comparison 比較条件.
     * @param right1 右辺値.
     * @param right2 右辺値(Between指定時のみ有効).
     * @return true(比較条件成立) / false(比較条件不成立).
     */
    private static boolean judgeComparison(final Calendar left, final String comparison, final Calendar right1, final Calendar right2) {
        switch (comparison) {
            case LESS_THAN:
            case LESS_THAN_MARK_1:
            case LESS_THAN_MARK_2:
                return (left.compareTo(right1) > 0);
            case BELOW:
            case BELOW_MARK_1:
            case BELOW_MARK_2:
                return (left.compareTo(right1) >= 0);
            case EXCESS:
            case EXCESS_MARK_1:
            case EXCESS_MARK_2:
                return (left.compareTo(right1) < 0);
            case NOT_LESS_THAN:
            case NOT_LESS_THAN_MARK_1:
            case NOT_LESS_THAN_MARK_2:
                return (left.compareTo(right1) <= 0);
            case EQUAL:
            case EQUAL_MARK_1:
            case EQUAL_MARK_2:
                return (left.compareTo(right1) == 0);
            case NOT_EQUAL:
            case NOT_EQUAL_MARK_1:
            case NOT_EQUAL_MARK_2:
                return (left.compareTo(right1) != 0);
            case BETWEEN:
            case BETWEEN_MARK_1:
            case BETWEEN_MARK_2:
                return (left.compareTo(right1) >= 0 && left.compareTo(right2) <= 0);
            default:
                return false;
        }
    }

    /**
     * データタイプ用文字列チェック.
     * @param dataType 比較子.
     * @return true(一致) / false(不一致).
     */
    public static boolean checkDataType(final String dataType) {
        switch (dataType) {
            case TYPE_INT:
            case TYPE_LONG:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
            case TYPE_STRING:
            case TYPE_BOOLEAN:
            case TYPE_DATE_TIME:
            case TYPE_DATE:
            case TYPE_TIME:
            case TYPE_JSON_INT:
            case TYPE_JSON_FLOAT:
            case TYPE_JSON_DOUBLE:
            case TYPE_JSON_STRING:
            case TYPE_JSON_BOOLEAN:
            case TYPE_JSON_DATE_TIME:
                return true;
            default:
                return false;
        }
    }
}
