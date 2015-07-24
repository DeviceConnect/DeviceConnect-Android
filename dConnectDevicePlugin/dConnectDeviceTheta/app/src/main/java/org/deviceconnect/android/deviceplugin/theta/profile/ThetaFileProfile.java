/*
 ThetaFileProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.ThetaApi;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiClient;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiTask;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.ThetaFileInfo;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Theta File Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaFileProfile extends FileProfile {

    private final ThetaApiClient mClient;

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaApiClient}
     * @param fileMgr an instance of {@link FileManager}
     */
    public ThetaFileProfile(final ThetaApiClient client, final FileManager fileMgr) {
        super(fileMgr);
        mClient = client;
    }

    @Override
    protected boolean onGetReceive(final Intent request, final Intent response, final String serviceId,
                                   final String path) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response, "path must be specified.");
            getService().sendResponse(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    String[] components = path.split("/");
                    String filename = null;
                    ThetaFileInfo targetFile = null;
                    if (components.length == 2) {
                        List<ThetaFileInfo> list = api.getFileInfoListFromDefaultStorage();
                        filename = components[1];
                        for (Iterator<ThetaFileInfo> it = list.iterator(); it.hasNext(); ) {
                            ThetaFileInfo fileInfo = it.next();
                            if (filename.equals(fileInfo.mName)) {
                                targetFile = fileInfo;
                                break;
                            }
                        }
                    }
                    if (targetFile != null) {
                        byte[] data = api.getFile(targetFile);
                        setResult(response, DConnectMessage.RESULT_OK);
                        setMIMEType(response, targetFile.mMimeType);
                        setURI(response, filename, data);
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "File not found: " + path);
                    }
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onGetList(final Intent request, final Intent response, final String serviceId,
                                final String path, final String mimeType, final String order,
                                final Integer offset, final Integer limit) {
        final String sortDirection;
        final String sortTargetParam;

        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        if (path != null && !path.equals("/")) {
            MessageUtils.setInvalidRequestParameterError(response, "the specified directory is not found.");
            return true;
        }
        if (order != null) {
            String[] conditions = order.split(",");
            if (conditions.length != 2) {
                MessageUtils.setInvalidRequestParameterError(response, "order is invalid.");
                return true;
            }
            sortTargetParam = conditions[0];
            sortDirection = conditions[1];
            if (!sortTargetParam.equals(PARAM_PATH) && !sortTargetParam.equals(PARAM_FILE_NAME)
                && !sortTargetParam.equals(PARAM_MIME_TYPE) && !sortTargetParam.equals(PARAM_UPDATE_DATE)
                && !sortTargetParam.equals(PARAM_FILE_SIZE) && !sortTargetParam.equals(PARAM_FILE_TYPE)) {
                MessageUtils.setInvalidRequestParameterError(response, "target parameter name is invalid.");
                return true;
            }
            if (!sortDirection.equals(Order.ASC.getValue()) && !sortDirection.equals(Order.DSEC.getValue())) {
                MessageUtils.setInvalidRequestParameterError(response, "direction of order is invalid.");
                return true;
            }
        } else {
            sortDirection = null;
            sortTargetParam = null;
        }
        if (offset != null) {
            if (offset < 0) {
                MessageUtils.setInvalidRequestParameterError(response, "offset must be 0 or positive integer.");
                return true;
            }
        } else {
            if (request.getStringExtra(PARAM_OFFSET) != null) {
                MessageUtils.setInvalidRequestParameterError(response, "offset must be integer.");
                getContext().sendBroadcast(response);
                return true;
            }
        }
        if (limit != null) {
            if (limit < 0) {
                MessageUtils.setInvalidRequestParameterError(response, "limit must be 0 or positive integer.");
                return true;
            }
        } else {
            if (request.getStringExtra(PARAM_LIMIT) != null) {
                MessageUtils.setInvalidRequestParameterError(response, "limit must be integer.");
                getContext().sendBroadcast(response);
                return true;
            }
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    List<ThetaFileInfo> list = api.getFileInfoListFromDefaultStorage();
                    list = sortFileInfoList(sortTargetParam, sortDirection, list);

                    int start = offset != null ? offset : 0;
                    if (start >= list.size()) {
                        MessageUtils.setInvalidRequestParameterError(response, "offset is too large.");
                        getContext().sendBroadcast(response);
                        return;
                    }
                    int end = limit != null ? start + limit : start;
                    if (end > list.size()) {
                        end = list.size();
                    }
                    list = (start < end) ? list.subList(start, end) : new LinkedList<ThetaFileInfo>();

                    List<Bundle> file = new ArrayList<Bundle>();
                    for (Iterator<ThetaFileInfo> it = list.iterator(); it.hasNext(); ) {
                        ThetaFileInfo fileInfo = it.next();
                        Bundle b = new Bundle();
                        setPath(b, "/" + fileInfo.mName);
                        setMIMEType(b, fileInfo.mMimeType);
                        setFileName(b, fileInfo.mName);
                        setUpdateDate(b, fileInfo.mDate);
                        setFileSize(b, fileInfo.mSize);
                        file.add(b);
                    }
                    setFiles(response, file);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    private List<ThetaFileInfo> sortFileInfoList(final String targetParam, final String direction,
                                                 final List<ThetaFileInfo> fileInfoList) {
        boolean isAsc = true;
        if (direction != null) {
            isAsc = direction.equals(Order.ASC.getValue());
        }
        Comparator<ThetaFileInfo> comparator = null;

        if (PARAM_PATH.equals(targetParam)) {
            comparator = new Comparator<ThetaFileInfo>() {
                public int compare(final ThetaFileInfo f1, final ThetaFileInfo f2) {
                    return f1.mName.compareTo(f2.mName);
                }
            };
        } else if (PARAM_MIME_TYPE.equals(targetParam)) {
            comparator = new Comparator<ThetaFileInfo>() {
                public int compare(final ThetaFileInfo f1, final ThetaFileInfo f2) {
                    return f1.mMimeType.compareTo(f2.mMimeType);
                }
            };
        } else if (PARAM_FILE_NAME.equals(targetParam)) {
            comparator = new Comparator<ThetaFileInfo>() {
                public int compare(final ThetaFileInfo f1, final ThetaFileInfo f2) {
                    return f1.mName.compareTo(f2.mName);
                }
            };
        } else if (PARAM_UPDATE_DATE.equals(targetParam)) {
            comparator = new Comparator<ThetaFileInfo>() {
                public int compare(final ThetaFileInfo f1, final ThetaFileInfo f2) {
                    return f1.mDate.compareTo(f2.mDate);
                }
            };
        } else if (PARAM_FILE_SIZE.equals(targetParam)) {
            comparator = new Comparator<ThetaFileInfo>() {
                public int compare(final ThetaFileInfo f1, final ThetaFileInfo f2) {
                    return (f1.mSize - f2.mSize);
                }
            };
        }
        // NOTE: It is not necessary to implement sorting by file type because the feature
        // to create any directories in the storage of THETA is not provided.
        if (comparator != null) {
            Collections.sort(fileInfoList, comparator);
        }
        if (!isAsc) {
            Collections.reverse(fileInfoList);
        }
        return fileInfoList;
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }
}
