/*
 DateUtils.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * RFC3339形式のDateデータを生成する機能を提供する。
 *
 *
 * @author NTT DOCOMO, INC.
 */
public class RFC3339DateUtils {

    /**
     * RFC3339の日付フォーマット定義.
     */
    private final static String RFC_3339 = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
    private final static String RFC_3339_OPT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ";

    /**
     * ユーティリティクラスなのでprivate.
     */
    private RFC3339DateUtils() {
    }

    /**
     * RFC3339形式あるいはISO8601形式の文字列からCalendarクラスのオブジェクトを生成する.
     * 日付のみ、時間のみの文字列には対応していない。
     *
     * @param dateString RFC3339形式あるいはISO8601形式の日時文字列.
     * @param locale     日付のLocale
     * @param timezone   日付のTimezone
     * @return Calendarオブジェクト / エラー発生時はnull.
     */
    public static Calendar toCalendar(final String dateString, final Locale locale, final TimeZone timezone) {
        Date converted;
        // TimeZone表記が"Z"の場合
        if (dateString.endsWith("Z")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(RFC_3339, locale);
                format.setTimeZone(timezone);
                converted = new Date(format.parse(dateString).getTime());
            } catch (java.text.ParseException pe) {
                SimpleDateFormat format = new SimpleDateFormat(RFC_3339_OPT, locale);
                format.setTimeZone(timezone);
                format.setLenient(true);
                try {
                    converted = new Date(format.parse(dateString).getTime());
                } catch (ParseException e) {
                    return null;
                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(converted);
            return calendar;
        }

        // TimeZoneが文字列表記されている場合、TimeZone分離
        String dateAddString;
        String timeZone;
        if (dateString.lastIndexOf('+') == -1) {
            try {
                dateAddString = dateString.substring(0, dateString.lastIndexOf('-'));
                timeZone = dateString.substring(dateString.lastIndexOf('-'));
            } catch (Exception e) {
                return null;
            }
        } else {
            try {
                dateAddString = dateString.substring(0, dateString.lastIndexOf('+'));
                timeZone = dateString.substring(dateString.lastIndexOf('+'));
            } catch (Exception e) {
                return null;
            }
        }

        String tmp = dateAddString + timeZone;
        try {
            SimpleDateFormat format = new SimpleDateFormat(RFC_3339, locale);
            converted = new Date(format.parse(tmp).getTime());
        } catch (java.text.ParseException pe) {
            SimpleDateFormat format = new SimpleDateFormat(RFC_3339_OPT, locale);
            format.setLenient(true);
            try {
                converted = new Date(format.parse(tmp).getTime());
            } catch (ParseException e) {
                return null;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(converted);
        return calendar;
    }

    /**
     * DateオブジェクトからCalendarクラスのオブジェクトを生成する.
     * @param date Dateオブジェクト
     * @param locale 設定するDateのLocale
     * @param timezone 設定するDateのTimeZone
     * @return Calendarオブジェクト / エラー発生時はnull.
     */
    public static Calendar toCalendar(final Date date, final Locale locale, final TimeZone timezone) {
        Calendar calendar = Calendar.getInstance(timezone, locale);
        calendar.setTime(date);
        return calendar;
    }

    /**
     * longからCalendarクラスのオブジェクトを生成する.
     * @param dateLong Dateのlong
     * @param locale 設定するDateのLocale
     * @param timezone 設定するDateのTimeZone
     * @return Calendarオブジェクト / エラー発生時はnull.
     */
    public static Calendar toCalendar(final long dateLong, final Locale locale, final TimeZone timezone) {
        Calendar calendar = Calendar.getInstance(timezone, locale);
        calendar.setTimeInMillis(dateLong);
        return calendar;
    }

    /**
     * RFC3339形式あるいはISO8601形式の文字列からCalendarクラスのオブジェクトを生成する.
     * 日付のみ、時間のみの文字列には対応していない。
     *
     * @param dateString RFC3339形式あるいはISO8601形式の日時文字列.
     * @return Calendarオブジェクト / エラー発生時はnull.
     */
    public static Calendar toCalendar(final String dateString) {
        return toCalendar(dateString, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * DateオブジェクトからCalendarクラスのオブジェクトを生成する.
     * @param date Dateオブジェクト
     * @return Calendarオブジェクト / エラー発生時はnull.
     */
    public static Calendar toCalendar(final Date date) {
        return toCalendar(date, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * longからCalendarクラスのオブジェクトを生成する.
     * @param dateLong Dateのlong
     * @return Calendarオブジェクト / エラー発生時はnull.
     */
    public static Calendar toCalendar(final long dateLong) {
        return toCalendar(dateLong, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * RFC3339形式あるいはISO8601形式の文字列からdateクラスのオブジェクトを生成する.
     * 日付のみ、時間のみの文字列には対応していない。
     *
     * @param dateString RFC3339形式あるいはISO8601形式の日時文字列.
     * @param locale     日付のLocale
     * @param timezone   日付のTimezone
     * @return Dateオブジェクト / エラー発生時はnull.
     */
    public static Date toDate(final String dateString, final Locale locale, final TimeZone timezone) {
        Calendar calendar = toCalendar(dateString, locale, timezone);
        if (calendar != null) {
            return calendar.getTime();
        }
        return null;
    }

    /**
     * longからDateクラスのオブジェクトを生成する.
     * @param dateLong Dateのlong
     * @param locale 設定するDateのLocale
     * @param timezone 設定するDateのTimeZone
     * @return Dateオブジェクト / エラー発生時はnull.
     */
    public static Date toDate(final long dateLong, final Locale locale, final TimeZone timezone) {
        Calendar calendar = Calendar.getInstance(timezone, locale);
        calendar.setTimeInMillis(dateLong);
        return calendar.getTime();
    }

    /**
     * RFC3339形式あるいはISO8601形式の文字列からdateクラスのオブジェクトを生成する.
     * 日付のみ、時間のみの文字列には対応していない。
     *
     * @param dateString RFC3339形式あるいはISO8601形式の日時文字列.
     * @return Dateオブジェクト / エラー発生時はnull.
     */
    public static Date toDate(final String dateString) {
        return toDate(dateString, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * longからDateクラスのオブジェクトを生成する.
     * @param dateLong Dateのlong
     * @return Dateオブジェクト / エラー発生時はnull.
     */
    public static Date toDate(final long dateLong) {
        return toDate(dateLong, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * CalendarオブジェクトからRFC3339形式の文字列を生成する.
     * @param dateCalendar Calendarオブジェクト
     * @param locale 設定するDateのLocale
     * @param timezone 設定するDateのTimeZone
     * @return RFC3339形式の文字列 / エラー発生時はnull.
     */
    public static String toString(final Calendar dateCalendar, final Locale locale, final TimeZone timezone) {
        if (dateCalendar != null) {
            SimpleDateFormat format = new SimpleDateFormat(RFC_3339, locale);
            format.setTimeZone(timezone);
            return format.format(dateCalendar.getTime());
        }
        return null;
    }

    /**
     * DateオブジェクトからRFC3339形式の文字列を生成する.
     * @param date Dateオブジェクト
     * @param locale 設定するDateのLocale
     * @param timezone 設定するDateのTimeZone
     * @return RFC3339形式の文字列 / エラー発生時はnull.
     */
    public static String toString(final Date date, final Locale locale, final TimeZone timezone) {
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat(RFC_3339, locale);
            format.setTimeZone(timezone);
            return format.format(date);
        }
        return null;
    }

    /**
     * LongデータからRFC3339形式の文字列を生成する.
     * @param dateLong Dateオブジェクト
     * @param locale 設定するDateのLocale
     * @param timezone 設定するDateのTimeZone
     * @return RFC3339形式の文字列 / エラー発生時はnull.
     */
    public static String toString(final long dateLong, final Locale locale, final TimeZone timezone) {
        SimpleDateFormat format = new SimpleDateFormat(RFC_3339, locale);
        format.setTimeZone(timezone);
        return format.format(new Date(dateLong));
    }

    /**
     * CalendarオブジェクトからRFC3339形式の文字列を生成する.
     * @return RFC3339形式の文字列 / エラー発生時はnull.
     */
    public static String toString(final Calendar dateCalendar) {
        return toString(dateCalendar, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * DateオブジェクトからRFC3339形式の文字列を生成する.
     * @param date Dateオブジェクト
     * @return RFC3339形式の文字列 / エラー発生時はnull.
     */
    public static String toString(final Date date) {
        return toString(date, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * LongデータからRFC3339形式の文字列を生成する.
     * @param dateLong Dateオブジェクト
     * @return RFC3339形式の文字列 / エラー発生時はnull.
     */
    public static String toString(final long dateLong) {
        return toString(dateLong, Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * 現在のタイムスタンプをlongで返す.
     * @return long型のタイムスタンプ
     */
    public static long nowTimeStampLong() {
        return System.currentTimeMillis();
    }

    /**
     * 現在のタイムスタンプをDateオブジェクトで返す
     * @return Dateのタイムスタンプ
     */
    public static Date nowTimeStampDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 現在のタイムスタンプをRFC3339の文字列で返す.
     * @param locale Locale
     * @param timezone TimeZone
     * @return 現在のタイムスタンプの文字列
     */
    public static String nowTimeStampString(final Locale locale, final TimeZone timezone) {
        SimpleDateFormat format = new SimpleDateFormat(RFC_3339, locale);
        format.setTimeZone(timezone);
        return format.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 現在のタイムスタンプをRFC3339の文字列で返す.
     * @return 現在のタイムスタンプの文字列
     */
    public static String nowTimeStampString() {
        return nowTimeStampString(Locale.getDefault(), TimeZone.getDefault());
    }
}