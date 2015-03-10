/*
 HostFileDescriptorProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.io.IOException;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.file.FileData;
import org.deviceconnect.android.deviceplugin.host.file.FileDataManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileDescriptorProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.util.Log;

/**
 * FileDescriptorプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostFileDescriptorProfile extends FileDescriptorProfile {

    /** Error. */
    private static final int ERROR_VALUE_IS_NULL = 100;

    @Override
    protected boolean onGetOpen(final Intent request, final Intent response, final String serviceId, final String path,
            final Flag flag) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null || flag == Flag.UNKNOWN) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (path.trim().equals("") || path.trim().equals("/")) {
            MessageUtils.setUnknownError(response, "not found:" + path);
        } else {
            FileDataManager mgr = getFileDataManager();
            try {
                FileData file = mgr.openFileData(path, flag);
                if (BuildConfig.DEBUG) {
                    if (file != null) {
                        Log.e("Host", "file is opened.");
                    }
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } catch (IOException e) {
                MessageUtils.setIllegalDeviceStateError(response,
                        "file cannot open.");
            } catch (IllegalStateException e) {
                MessageUtils.setIllegalDeviceStateError(response,
                        "file is already opened.");
            }
        }
        return true;
    }

    @Override
    protected boolean onGetRead(final Intent request, final Intent response, final String serviceId, final String path,
            final Long length, final Long position) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null || length == null || length < 0 || (position != null && position < 0)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            long pos = 0;
            if (position != null) {
                pos = position.longValue();
            }

            if (length == null || length <= 0) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "length is invalid.");
                return true;
            }

            FileDataManager mgr = getFileDataManager();
            FileData file = mgr.getFileData(path);
            if (file == null) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "file is not opened.");
            } else {
                String fileData = mgr.readFile(file, (int) pos, length.intValue());
                if (fileData != null) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setSize(response, fileData.length());
                    setFileData(response, fileData);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response,
                            "file cannot read.");
                }
            }
        }
        return true;
    }

    @Override
    protected boolean onPutClose(final Intent request, final Intent response,
            final String serviceId, final String path) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            FileDataManager mgr = getFileDataManager();
            FileData file = mgr.getFileData(path);
            if (file != null) {
                mgr.closeFileData(path);
            } else {
                MessageUtils.setIllegalServerStateError(response,
                        "path is invalid.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutWrite(final Intent request, final Intent response, final String serviceId, final String path,
            final byte[] data, final Long position) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (path == null || data == null || (position != null && position < 0)) {
            MessageUtils.setInvalidRequestParameterError(response, "parameter is invalid.");
        } else {
            int pos = 0;
            if (position != null) {
                pos = position.intValue();
            }

            FileDataManager mgr = getFileDataManager();
            FileData file = mgr.getFileData(path);
            if (file == null) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "file is not opened.");
            } else if (file.getFlag() != Flag.RW) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "file is not opened.");
            } else {
                if (pos < 0 || data.length < pos) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "position is invalid ");
                } else {
                    if (mgr.writeFile(file, data, pos)) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setUnknownError(response,
                                "file cannot write.");
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnWatchFile(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL,
                        "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnWatchFile(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL,
                        "Can not unregister event.");
            }
        }
        return true;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return HostServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
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
     * サービスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response, "Service is not found.");
    }

    /**
     * ファイル管理クラスを取得する.
     * @return ファイル管理クラス
     */
    private FileDataManager getFileDataManager() {
        HostDeviceService service = (HostDeviceService) getContext();
        FileDataManager mgr = service.getFileDataManager();
        return mgr;
    }
}
