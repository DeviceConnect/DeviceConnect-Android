/*
 NormalFileDescriptorProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestFileDescriptorProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;


/**
 * FileDescriptorプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalFileDescriptorProfileTestCase extends IntentDConnectTestCase {

    /**
     * バッファサイズ.
     */
    private static final int BUF_SIZE = 1024;

    /**
     * ファイルの長さ.
     */
    private static final long FILE_LENGTH = 256;

    /**
     * ファイルをオープンするテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     attribute=open
     *     mediaId=xxxx
     *     flag=xxxx
     *     mode=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * </pre>
     */
    @Test
    public void testOpen() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_OPEN);
        request.putExtra(FileDescriptorProfileConstants.PARAM_PATH, TestFileDescriptorProfileConstants.PATH);
        request.putExtra(FileDescriptorProfileConstants.PARAM_FLAG, "r");
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ファイルをクローズするテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     attribute=close
     *     mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * </pre>
     */
    @Test
    public void testClose() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_CLOSE);
        request.putExtra(FileDescriptorProfileConstants.PARAM_PATH, TestFileDescriptorProfileConstants.PATH);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * 指定したサイズ分のデータをファイルから読み込むテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     attribute=read
     *     mediaId=xxxx
     *     length=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * ・sizeにint型の値が返ってくること。
     * ・fileDataにString型のデータが返ってくること。
     * </pre>
     */
    @Test
    public void testRead001() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_READ);
        request.putExtra(FileDescriptorProfileConstants.PARAM_PATH, TestFileDescriptorProfileConstants.PATH);
        request.putExtra(FileDescriptorProfileConstants.PARAM_LENGTH, FILE_LENGTH);
        Intent response = sendRequest(request);
        assertResultOK(response);
        assertTrue(response.hasExtra(FileDescriptorProfileConstants.PARAM_SIZE));
        assertEquals(TestFileDescriptorProfileConstants.BYTE,
                response.getIntExtra(FileDescriptorProfileConstants.PARAM_SIZE, -1));
        assertTrue(response.hasExtra(FileDescriptorProfileConstants.PARAM_FILE_DATA));
    }

    /**
     * 指定した位置から、指定したサイズ分のデータをファイルから読み込むテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     attribute=read
     *     mediaId=xxxx
     *     length=xxxx
     *     position=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * ・sizeにint型のデータが返ってくること。
     * ・fileDataにString型のデータが返ってくること。
     * </pre>
     */
    @Test
    public void testRead002() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_READ);
        request.putExtra(FileDescriptorProfileConstants.PARAM_PATH, TestFileDescriptorProfileConstants.PATH);
        request.putExtra(FileDescriptorProfileConstants.PARAM_LENGTH, FILE_LENGTH);
        request.putExtra(FileDescriptorProfileConstants.PARAM_POSITION, 0L);
        Intent response = sendRequest(request);

        assertResultOK(response);
        assertTrue(response.hasExtra(FileDescriptorProfileConstants.PARAM_SIZE));
        assertEquals(TestFileDescriptorProfileConstants.BYTE,
                response.getIntExtra(FileDescriptorProfileConstants.PARAM_SIZE, -1));
        assertTrue(response.hasExtra(FileDescriptorProfileConstants.PARAM_FILE_DATA));
    }

    /**
     * ファイルにデータを書き込むテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     attribute=write
     *     mediaId=xxxx
     *     uri=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * </pre>
     * @throws IOException テストファイルの保存に失敗した場合
     */
    @Test
    public void testWrite001() throws IOException {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        request.putExtra(FileDescriptorProfileConstants.PARAM_PATH, TestFileDescriptorProfileConstants.PATH);
        String uri = getContentProviderFileUri("test.png");
        request.putExtra(FileDescriptorProfileConstants.PARAM_URI, uri);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ファイル内の指定した位置にデータを書き込むテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     attribute=write
     *     mediaId=xxxx
     *     uri=xxxx
     *     position=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * </pre>
     * @throws IOException テストファイルの保存に失敗した場合
     */
    @Test
    public void testWrite002() throws IOException {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_WRITE);
        request.putExtra(FileDescriptorProfileConstants.PARAM_PATH, TestFileDescriptorProfileConstants.PATH);
        String uri = getContentProviderFileUri("test.png");
        request.putExtra(FileDescriptorProfileConstants.PARAM_URI, uri);
        request.putExtra(FileDescriptorProfileConstants.PARAM_POSITION, 0);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ファイルの更新通知のコールバック登録テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     callback=onwatchfile
     *     sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * ・コールバック登録後にイベントを受信すること。
     * </pre>
     */
    @Test
    public void testWatchFile01() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        Intent response = sendRequest(request);
        assertResultOK(response);
        Intent event = waitForEvent();
        assertTrue(event.hasExtra(FileDescriptorProfileConstants.PARAM_FILE));
        Bundle file = event.getBundleExtra(FileDescriptorProfileConstants.PARAM_FILE);
        assertEquals(TestFileDescriptorProfileConstants.CURR, 
                file.getString(FileDescriptorProfileConstants.PARAM_CURR));
        assertEquals(TestFileDescriptorProfileConstants.PREV,
                file.getString(FileDescriptorProfileConstants.PARAM_PREV));
    }

    /**
     * ファイルの更新通知のコールバック解除テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: DELETE
     * Extra:
     *     requestCode=xxxx
     *     serviceId=xxxx
     *     profile=file_descriptor
     *     callback=onwatchfile
     *     sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・requestCodeにリクエストコードが返ってくること。
     * </pre>
     */
    @Test
    public void testWatchFile02() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileDescriptorProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileDescriptorProfileConstants.ATTRIBUTE_ON_WATCH_FILE);
        request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, TEST_SESSION_KEY);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * assetsフォルダ内のファイルへのURIを返却します.
     * 
     * @param name assetファイル名
     * @return URIを示す文字列
     */
    private String getContentProviderFileUri(final String name) {
        return "content://org.deviceconnect.android.test.file/" + name;
    }
}
