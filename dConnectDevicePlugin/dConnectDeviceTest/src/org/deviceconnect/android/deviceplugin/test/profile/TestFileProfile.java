/*
 TestFileProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、Fileプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestFileProfile extends FileProfile {

    /**
     * 容量.
     */
    public static final int BYTE = 64000;

    /**
     * ファイル名.
     */
    public static final String FILE_NAME = "test.png";

    /**
     * ファイルのメディアID.
     */
    public static final String PATH = "/test.png";

    /**
     * ファイルのMIMEタイプ.
     */
    public static final String MIME_TYPE = "image/png";

    /**
     * コンストラクタ.
     * 
     * @param fileMgr ファイルマネージャ
     */
    public TestFileProfile(final FileManager fileMgr) {
        super(fileMgr);
    }
    
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
    protected boolean onGetReceive(final Intent request, final Intent response, final String serviceId, 
            final String path) {
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            String uri = getFileManager().getContentUri() + "/" + getFileNameFromPath(path);
            setURI(response, uri);
            setMIMEType(response, MIME_TYPE);
        }
        return true;
    }

    @Override
    protected boolean onGetList(final Intent request, final Intent response, final String serviceId, final String path,
            final String mimeType, final String order, final Integer offset, final Integer limit) {
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            List<Bundle> files = new ArrayList<Bundle>();
            Bundle file = new Bundle();
            setFileName(file, FILE_NAME);
            setPath(file, PATH);
            setMIMEType(file, MIME_TYPE);
            setUpdateDate(file, new SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZ", Locale.getDefault()).format(new Date()));
            setFileSize(file, BYTE);
            setFileType(response, FileType.FILE);
            files.add(file);
            setFiles(response, files);
            setCount(response, files.size());
        }
        return true;
    }

    @Override
    protected boolean onPostSend(final Intent request, final Intent response, final String serviceId, 
            final String path, final String mimeType, final byte[] data) {
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String u = null;
            try {
                // MEMO: テスト簡素化のため、テストプラグイン内ではディレクトリツリーを持たせない.
                String filename = getFileNameFromPath(path);
                u = getFileManager().saveFile(filename, data);
            } catch (IOException e) {
                u = null;
            }
            if (u == null) {
                MessageUtils.setUnknownError(response, "Failed to save file.");
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
            }
        }
        return true;
    }

    @Override
    protected boolean onPostMkdir(final Intent request, final Intent response,
                                        final String serviceId, final String path) {
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteRmdir(final Intent request, final Intent response,
                                    final String serviceId, final String path, final boolean force) {
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    /**
     * FileパスからFile名を取得する.
     * @param path Fileパス
     * @return File名
     */
    private String getFileNameFromPath(final String path) {
        String[] components = path.split("/");
        if (components.length == 0) {
            return path;
        }
        return components[components.length - 1];
    }

    @Override
    protected boolean onDeleteRemove(final Intent request, final Intent response, final String serviceId, 
            final String path) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            getFileManager().removeFile(getFileNameFromPath(path));
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }
}
