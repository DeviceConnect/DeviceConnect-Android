package org.deviceconnect.android.deviceplugin.theta;

import java.util.ArrayList;
import java.util.List;

public class ThetaStorageInfo {

    private final String name;
    private final List<ThetaFileInfo> infoList = new ArrayList<ThetaFileInfo>();

    public ThetaStorageInfo(int storageId) {
        name = String.valueOf(storageId);
    }

    void addFileInfo(final ThetaFileInfo info) {
        infoList.add(info);
    }

    public List<ThetaFileInfo> getFileInfoList() {
        return infoList;
    }

    public String getName() {
        return name;
    }
}
