/*
 DateUtilsTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.utils.RFC3339DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * DateUtilsのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class RFC3339DateUtilsTest {
    /**
     * テスト用の特殊文字.
     */
    private static final String SPECIAL_CHARACTER_STRING = "!\"#$%&\\'()-^¥@[;:],./__?><}*+{`|~=', '!\"#$%&\\'()-^¥@[;:],./__?><}*+{`|~=";
    /**
     * テスト用のRFC3339の文字列.
     */
    private static final String RFC3339_TEST_DATE = "2017-12-02T12:23:12+09:00";
    /**
     * テスト用のISO8601の文字列.
     */
    private static final String ISO8601_TEST_DATE = "2017-12-02T12:23:12+0900";
    /**
     * テスト用の日付文字列.
     */
    private static final String TEST_DATE = "2017-12-02";
    /**
     * テスト用の時刻文字列.
     */
    private static final String TEST_TIME = "12:23:12";
    /**
     * テスト用のlongデータ.
     */
    private static final long TEST_LONG_DATE = 1512184992000L;

    /**
     * DateUtilsでカレンダーオブジェクトを生成する時に、空の文字列が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toCalendar_EmptyString() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar("");
        assertThat(null, is(calendar));
    }

    /**
     * DateUtilsでカレンダーオブジェクトを生成する時に、特殊文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toCalendar_SpecialCharacterString() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar(SPECIAL_CHARACTER_STRING);
        assertThat(null, is(calendar));
    }

    /**
     * DateUtilsでカレンダーオブジェクトを生成する時に、RFC3339の文字が指定されたとき、Calendarオブジェクトが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・Calendarオブジェクトが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toCalendar_RFC3339DateString() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar(RFC3339_TEST_DATE);
        assertThat(calendar, notNullValue());
        assertThat(2017, is(calendar.get(Calendar.YEAR)));
        assertThat(12, is(calendar.get(Calendar.MONTH) + 1));
        assertThat(2, is(calendar.get(Calendar.DATE)));
    }

    /**
     * DateUtilsでカレンダーオブジェクトを生成する時に、ISO86011の文字が指定されたとき、Calendarオブジェクトが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・Calendarオブジェクトが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toCalendar_ISO8601DateString() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar(ISO8601_TEST_DATE);
        assertThat(calendar, notNullValue());
        assertThat(2017, is(calendar.get(Calendar.YEAR)));
        assertThat(12, is(calendar.get(Calendar.MONTH) + 1));
        assertThat(2, is(calendar.get(Calendar.DATE)));
    }

    /**
     * DateUtilsでカレンダーオブジェクトを生成する時に、日付のみの文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toCalendar_RFC3339DateOnly() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar(TEST_DATE);
        assertThat(null, is(calendar));
    }

    /**
     * DateUtilsでカレンダーオブジェクトを生成する時に、時間のみの文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toCalendar_RFC3339DateTimeOnly() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar(TEST_TIME);
        assertThat(null, is(calendar));
    }

    /**
     * DateUtilsでDateオブジェクトを生成する時に、空文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toDate_EmptyString() throws Exception {
        Date date = RFC3339DateUtils.toDate("");
        assertThat(null, is(date));
    }

    /**
     * DateUtilsでDateオブジェクトを生成する時に、特殊文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toDate_SpecialCharacterString() throws Exception {
        Date date = RFC3339DateUtils.toDate(SPECIAL_CHARACTER_STRING);
        assertThat(null, is(date));
    }

    /**
     * DateUtilsでDateオブジェクトを生成する時に、RFC3339の日付文字が指定されたとき、Dateオブジェクトが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・Dateオブジェクトが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toDate_RFC3339DateString() throws Exception {
        Date date = RFC3339DateUtils.toDate(RFC3339_TEST_DATE);
        assertThat(date, notNullValue());
        assertThat(TEST_LONG_DATE, is(date.getTime()));
    }

    /**
     * DateUtilsでDateオブジェクトを生成する時に、ISO8601の日付文字が指定されたとき、Dateオブジェクトが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・Dateオブジェクトが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toDate_ISO8601DateString() throws Exception {
        Date date = RFC3339DateUtils.toDate(ISO8601_TEST_DATE);
        assertThat(date, notNullValue());
        assertThat(TEST_LONG_DATE, is(date.getTime()));
    }
    /**
     * DateUtilsでDateオブジェクトを生成する時に、日付のみの文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toDate_RFC3339DateOnly() throws Exception {
        Date date = RFC3339DateUtils.toDate(TEST_DATE);
        assertThat(null, is(date));
    }

    /**
     * DateUtilsでDateオブジェクトを生成する時に、時間の文字が指定されたとき、nullが返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・nullが返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toDate_RFC3339DateTimeOnly() throws Exception {
        Date date = RFC3339DateUtils.toDate(TEST_TIME);
        assertThat(null, is(date));
    }

    /**
     * DateUtilsでRFC3339の日付文字列を生成する時に、Calendarオブジェクトが指定されたとき、RFC3339の文字列が返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・RFC3339の文字列が返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toString_RFC3339Calendar() throws Exception {
        Calendar calendar = RFC3339DateUtils.toCalendar(RFC3339_TEST_DATE);
        String dateString = RFC3339DateUtils.toString(calendar);
        assertThat(RFC3339_TEST_DATE, is(dateString));
    }

    /**
     * DateUtilsでRFC3339の日付文字列を生成する時に、Dateオブジェクトが指定されたとき、RFC3339の文字列が返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・RFC3339の文字列が返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toString_RFC3339Date() throws Exception {
        Date date = RFC3339DateUtils.toDate(RFC3339_TEST_DATE);
        String dateString = RFC3339DateUtils.toString(date);
        assertThat(RFC3339_TEST_DATE, is(dateString));
    }


    /**
     * DateUtilsでRFC3339の日付文字列を生成する時に、Calendarオブジェクトが指定されたとき、RFC3339の文字列が返されることを確認する.
     * <pre>
     * 【期待する動作】
     * ・RFC3339の文字列が返されること.
     * </pre>
     * @throws Exception テスト失敗
     */
    @Test
    public void DateUtils_toString_RFC3339Long() throws Exception {
        String dateString = RFC3339DateUtils.toString(TEST_LONG_DATE);
        assertThat(RFC3339_TEST_DATE, is(dateString));
    }

}
