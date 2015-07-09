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
import java.util.Iterator;
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
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    String[] components = path.split("/");
                    if (components.length != 2) {
                        MessageUtils.setInvalidRequestParameterError(response, "path is invalid.");
                        getService().sendResponse(response);
                        return;
                    }

                    String filename = components[1];
                    List<ThetaFileInfo> list = api.getFileInfoListFromDefaultStorage();
                    ThetaFileInfo target = null;
                    for (Iterator<ThetaFileInfo> it = list.iterator(); it.hasNext(); ) {
                        ThetaFileInfo fileInfo = it.next();
                        if (filename.equals(fileInfo.mName)) {
                            target = fileInfo;
                            break;
                        }
                    }

                    if (target != null) {
                        byte[] data = api.getFile(target);

                        Log.d("AAA", "target: filename=" + target.mName + " size=" + data.length);

                        setResult(response, DConnectMessage.RESULT_OK);
                        setMIMEType(response, target.mMimeType);
                        setURI(response, filename, data);
                    } else {
                        MessageUtils.setUnknownError(response, "File not found: " + path);
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
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    List<ThetaFileInfo> list = api.getFileInfoListFromDefaultStorage();
                    List<Bundle> file = new ArrayList<Bundle>();
                    for (Iterator<ThetaFileInfo> it = list.iterator(); it.hasNext(); ) {
                        ThetaFileInfo fileInfo = it.next();
                        Bundle b = new Bundle();
                        setPath(b, "/" + fileInfo.mName);
                        setMIMEType(b, fileInfo.mMimeType);
                        setFileName(b, fileInfo.mName);
                        // TODO: Set Update Date
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

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }
}
