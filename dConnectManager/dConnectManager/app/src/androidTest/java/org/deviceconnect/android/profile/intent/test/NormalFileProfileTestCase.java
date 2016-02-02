/*
 NormalFileProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestFileProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.FileProfileConstants.FileType;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Fileプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalFileProfileTestCase extends IntentDConnectTestCase {

    /**
     * バッファサイズ.
     */
    private static final int BUF_SIZE = 1024;

    /**
     * ファイル一覧取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=file
     *     attribute=list
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・fileにBundle[]型の値が返ってくること。
     * ・file[0].mediaIdにString型の値が返ってくること。
     * ・file[0].mimeTypeにString型の値が返ってくること。
     * ・file[0].fileNameにString型の値が返ってくること。
     * ・file[0].fileSizeにint型の値が返ってくること。
     * </pre>
     */
    @Test
    public void testGetList001() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileProfileConstants.ATTRIBUTE_LIST);
        Intent response = sendRequest(request);

        assertTrue(response.hasExtra(IntentDConnectMessage.EXTRA_RESULT));
        assertEquals(IntentDConnectMessage.RESULT_OK, 
                response.getIntExtra(IntentDConnectMessage.EXTRA_RESULT, -1));
        Parcelable[] files = (Parcelable[]) response.getParcelableArrayExtra(FileProfileConstants.PARAM_FILES);
        Bundle file = (Bundle) files[0];
        assertEquals(TestFileProfileConstants.PATH, file.getString(FileProfileConstants.PARAM_PATH));
        assertEquals(TestFileProfileConstants.MIME_TYPE, file.getString(FileProfileConstants.PARAM_MIME_TYPE));
        assertEquals(TestFileProfileConstants.FILE_NAME, file.getString(FileProfileConstants.PARAM_FILE_NAME));
        assertEquals(TestFileProfileConstants.BYTE, file.getInt(FileProfileConstants.PARAM_FILE_SIZE));
    }

    /**
     * ファイル一覧取得テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=file
     *     attribute=list
     *     mimeType=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・fileにBundle[]型の値が返ってくること。
     * ・file[0].mediaIdにString型の値が返ってくること。
     * ・file[0].mimeTypeにString型の値が返ってくること。
     * ・file[0].fileNameにString型の値が返ってくること。
     * ・file[0].fileSizeにint型の値が返ってくること。
     * </pre>
     */
    @Test
    public void testGetList002() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileProfileConstants.ATTRIBUTE_LIST);
        request.putExtra(FileProfileConstants.PARAM_MIME_TYPE, TestFileProfileConstants.MIME_TYPE);
        Intent response = sendRequest(request);

        assertTrue(response.hasExtra(IntentDConnectMessage.EXTRA_RESULT));
        assertEquals(IntentDConnectMessage.RESULT_OK, 
                response.getIntExtra(IntentDConnectMessage.EXTRA_RESULT, -1));
        Parcelable[] files = (Parcelable[]) response.getParcelableArrayExtra(FileProfileConstants.PARAM_FILES);
        Bundle file = (Bundle) files[0];
        assertEquals(TestFileProfileConstants.PATH, file.getString(FileProfileConstants.PARAM_PATH));
        assertEquals(TestFileProfileConstants.MIME_TYPE, file.getString(FileProfileConstants.PARAM_MIME_TYPE));
        assertEquals(TestFileProfileConstants.FILE_NAME, file.getString(FileProfileConstants.PARAM_FILE_NAME));
        assertEquals(TestFileProfileConstants.BYTE, file.getInt(FileProfileConstants.PARAM_FILE_SIZE));
    }

    /**
     * ファイル受信テストを行う.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra:
     *     profile=file
     *     attribute=list
     *     mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・mimeTypeにString型の値が返ってくること。
     * ・uriにString型の値が返ってくること。
     * </pre>
     */
    @Test
    public void testGetReceive() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileProfileConstants.ATTRIBUTE_RECEIVE);
        request.putExtra(FileProfileConstants.PARAM_PATH, TestFileProfileConstants.PATH);
        Intent response = sendRequest(request);
        assertResultOK(response);
        assertNotNull(TestFileProfileConstants.MIME_TYPE, 
                response.getStringExtra(FileProfileConstants.PARAM_MIME_TYPE));
        assertNotNull(TestFileProfileConstants.URI,
                response.getStringExtra(FileProfileConstants.PARAM_URI));
    }

    /**
     * ファイルの送信を行う.
     * <pre>
     * Action: POST
     * Extra:
     *     profile=file
     *     attribute=send
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testSend() {
        final String name = "test.png";
        Intent request = new Intent(IntentDConnectMessage.ACTION_POST);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileProfileConstants.ATTRIBUTE_SEND);
        request.putExtra(FileProfileConstants.PARAM_PATH, TestFileProfileConstants.PATH);
        request.putExtra(FileProfileConstants.PARAM_FILE_TYPE, FileType.FILE.getValue());

        String uri = getContentProviderFileUri(name);
        request.putExtra(FileProfileConstants.PARAM_URI, uri);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * ファイルの送信を行う.
     * <pre>
     * Action: DELETE
     * Extra:
     *     profile=file
     *     attribute=remove
     *     mediaId=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testRemove() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_DELETE);
        request.putExtra(DConnectMessage.EXTRA_SERVICE_ID, getServiceId());
        request.putExtra(DConnectMessage.EXTRA_PROFILE, FileProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, FileProfileConstants.ATTRIBUTE_REMOVE);
        request.putExtra(FileProfileConstants.PARAM_PATH, TestFileProfileConstants.PATH);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

    /**
     * assetsフォルダ内のファイルをアプリ領域に保存する.
     * 
     * @param name assetファイル名
     * @return URIを示す文字列
     */
    private String getContentProviderFileUri(final String name) {
        return "content://org.deviceconnect.android.test.file/" + name;
    }
}
