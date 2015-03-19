/*
 TestFileDescriptorProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import java.io.ByteArrayOutputStream;

import org.deviceconnect.android.deviceplugin.test.R;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileDescriptorProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

/**
 * JUnit用テストデバイスプラグイン、FileDescriptorプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestFileDescriptorProfile extends FileDescriptorProfile {

    /**
     * 容量.
     */
    public static final int BYTE = 64000;

    /**
     * Base64エンコードされたファイルデータの文字列表現.
     */
    public static final String FILE_DATA = Base64.encodeToString(new byte[] {0}, Base64.DEFAULT);

    /**
     * 書き込み先のファイルを示すメディアID.
     */
    public static final String PATH = "test.txt";

    /**
     * 書き込むデータを保存したファイルのURI.
     */
    public static final String URI = "test_uri";

    /**
     * ファイルの現在の更新時間.
     */
    public static final String CURR = "2014-06-01T00:00:00+0900";

    /**
     * ファイルの前回の更新時間.
     */
    public static final String PREV = "2014-06-01T00:00:00+0900";

    /**
     * ビットマップの圧縮率.
     */
    private static final int COMPRESSION_QUALITY = 100;
    
    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response, "Service ID is empty.");
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response, "Service is not found.");
    }

    @Override
    protected boolean onGetOpen(final Intent request, final Intent response, final String serviceId, 
            final String path, final Flag flag) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null || flag == Flag.UNKNOWN) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onGetRead(final Intent request, final Intent response, final String serviceId, 
            final String path, final Long length, final Long position) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null || length == null || length < 0 || (position != null && position < 0)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            Bitmap b = BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.test);
            b.compress(CompressFormat.PNG, COMPRESSION_QUALITY, ba);
            if (b.isRecycled()) {
                b.recycle();
                b = null;
            }

            setResult(response, DConnectMessage.RESULT_OK);
            setSize(response, BYTE);
            setFileData(response, FILE_DATA);
        }
       
        return true;
    }

    @Override
    protected boolean onPutClose(final Intent request, final Intent response, final String serviceId, 
            final String path) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onPutWrite(final Intent request, final Intent response, final String serviceId,
            final String path, final byte[] data, final Long position) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null || data == null || (position != null && position < 0)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "path=" + path + " , data=" + data + ", position=" + position);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onGetOnWatchFile(final Intent request, final Intent response,
            final String serviceId) {
        setResult(response, DConnectMessage.RESULT_OK);
        setFile(response);
        return true;
    }

    @Override
    protected boolean onPutOnWatchFile(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent intent = MessageUtils.createEventIntent();
            setSessionKey(intent, sessionKey);
            setServiceID(intent, serviceId);
            setProfile(intent, getProfileName());
            setAttribute(intent, ATTRIBUTE_ON_WATCH_FILE);
            setFile(intent);
            Util.sendBroadcast(getContext(), intent);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnWatchFile(final Intent request, final Intent response, final String serviceId, 
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    /**
     * メッセージにテスト用データを設定する.
     * @param message メッセージ
     */
    private static void setFile(final Intent message) {
        Bundle obj = new Bundle();
        setPath(obj, PATH);
        setCurr(obj, CURR);
        setPrev(obj, PREV);
        setFile(message, obj);
    }

}
