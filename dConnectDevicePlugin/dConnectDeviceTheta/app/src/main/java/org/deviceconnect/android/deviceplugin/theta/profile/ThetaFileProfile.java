/*
 ThetaFileProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Theta File Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaFileProfile extends FileProfile {

    private final ThetaDeviceClient mClient;

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaDeviceClient}
     * @param fileMgr an instance of {@link FileManager}
     */
    public ThetaFileProfile(final ThetaDeviceClient client, final FileManager fileMgr) {
        super(fileMgr);
        mClient = client;
    }

    @Override
    protected boolean onGetReceive(final Intent request, final Intent response, final String serviceId,
                                   final String path) {
        if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response, "path must be specified.");
            return true;
        }
        if (!path.startsWith("/")) {
            MessageUtils.setInvalidRequestParameterError(response, "path must start with '/'.");
            return true;
        }

        String[] components = path.split("/");
        if (components.length == 2) {
            final String fileName = components[1];
            mClient.fetchObject(serviceId, fileName, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onObjectFetched(final byte[] data, final String mimeType) {
                    try {
                        setResult(response, DConnectMessage.RESULT_OK);
                        setMIMEType(response, mimeType);
                        setURI(response, fileName, data);
                    } catch (IOException e) {
                        MessageUtils.setUnknownError(response, e.getMessage());
                    }

                    getService().sendResponse(response);
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    MessageUtils.setUnknownError(response, cause.getMessage());
                    getService().sendResponse(response);
                }

            });
            return false;
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "File not found: " + path);
            return true;
        }
    }

    @Override
    protected boolean onGetList(final Intent request, final Intent response, final String serviceId,
                                final String path, final String mimeType, final String order,
                                final Integer offset, final Integer limit) {
        final String sortDirection;
        final String sortTargetParam;

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
                return true;
            }
        }

        mClient.fetchAllObjectList(serviceId, new ThetaDeviceClient.DefaultListener() {

            @Override
            public void onObjectList(final List<ThetaObject> objList) {
                int start = offset != null ? offset : 0;
                if (start >= objList.size()) {
                    MessageUtils.setInvalidRequestParameterError(response, "offset is too large.");
                    getService().sendResponse(response);
                    return;
                }

                List<ThetaObject> list = sortObjectList(sortTargetParam, sortDirection, objList);
                int end = limit != null ? start + limit : list.size();
                if (end > list.size()) {
                    end = list.size();
                }
                list = (start < end) ? list.subList(start, end) : new LinkedList<ThetaObject>();

                List<Bundle> file = new ArrayList<Bundle>();
                for (ThetaObject obj : list) {
                    Bundle b = new Bundle();
                    setPath(b, "/" + obj.getFileName());
                    setMIMEType(b, obj.getMimeType());
                    setFileName(b, obj.getFileName());
                    setUpdateDate(b, obj.getCreationTime());
                    //setFileSize(b, obj.mSize); // TODO
                    file.add(b);
                }
                setFiles(response, file);
                setResult(response, DConnectMessage.RESULT_OK);
                getService().sendResponse(response);
            }

            @Override
            public void onFailed(final ThetaDeviceException cause) {
                switch (cause.getReason()) {
                    case ThetaDeviceException.NOT_FOUND_THETA:
                        MessageUtils.setNotFoundServiceError(response);
                        break;
                    default:
                        MessageUtils.setUnknownError(response, cause.getMessage());
                        break;
                }
                getService().sendResponse(response);
            }

        });
        return false;
    }

    private List<ThetaObject> sortObjectList(final String targetParam, final String direction,
                                               final List<ThetaObject> list) {
        boolean isAsc = true;
        if (direction != null) {
            isAsc = direction.equals(Order.ASC.getValue());
        }
        Comparator<ThetaObject> comparator = null;

        if (PARAM_PATH.equals(targetParam)) {
            comparator = new Comparator<ThetaObject>() {
                public int compare(final ThetaObject f1, final ThetaObject f2) {
                    return f1.getFileName().compareTo(f2.getFileName());
                }
            };
        } else if (PARAM_MIME_TYPE.equals(targetParam)) {
            comparator = new Comparator<ThetaObject>() {
                public int compare(final ThetaObject f1, final ThetaObject f2) {
                    return f1.getMimeType().compareTo(f2.getMimeType());
                }
            };
        } else if (PARAM_FILE_NAME.equals(targetParam)) {
            comparator = new Comparator<ThetaObject>() {
                public int compare(final ThetaObject f1, final ThetaObject f2) {
                    return f1.getFileName().compareTo(f2.getFileName());
                }
            };
        } else if (PARAM_UPDATE_DATE.equals(targetParam)) {
            comparator = new Comparator<ThetaObject>() {
                public int compare(final ThetaObject f1, final ThetaObject f2) {
                    long t1 = f1.getCreationTimeWithUnixTime();
                    long t2 = f2.getCreationTimeWithUnixTime();
                    return (t1 < t2) ? -1 : (t1 == t2) ? 0 : -1;
                }
            };
        } else if (PARAM_FILE_SIZE.equals(targetParam)) {
            comparator = new Comparator<ThetaObject>() {
                public int compare(final ThetaObject f1, final ThetaObject f2) {
                    return 0; //(f1.mSize - f2.mSize); // TODO
                }
            };
        }
        // NOTE: It is not necessary to implement sorting by file type because the feature
        // to create any directories in the storage of THETA is not provided.
        if (comparator != null) {
            Collections.sort(list, comparator);
        }
        if (!isAsc) {
            Collections.reverse(list);
        }
        return list;
    }

    @Override
    protected boolean onDeleteRemove(final Intent request, final Intent response,
                                     final String serviceId, final String path) {
        if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response, "path must be specified.");
            return true;
        }
        if (!path.startsWith("/")) {
            MessageUtils.setInvalidRequestParameterError(response, "path must start with '/'.");
            return true;
        }

        String[] components = path.split("/");
        if (components.length == 2) {
            String fileName = components[1];
            mClient.removeObject(serviceId, fileName, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onObjectRemoved() {
                    setResult(response, DConnectMessage.RESULT_OK);
                    getService().sendResponse(response);
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        case ThetaDeviceException.NOT_FOUND_OBJECT:
                            MessageUtils.setInvalidRequestParameterError(response, "File not found: " + path);
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                    getService().sendResponse(response);
                }

            });
            return false;
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "File not found: " + path);
            return true;
        }
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }
}
