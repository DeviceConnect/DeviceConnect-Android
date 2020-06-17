package org.deviceconnect.android.deviceplugin.midi;

import org.deviceconnect.android.service.DConnectService;

public abstract class DConnectMidiService extends DConnectService {

    protected DConnectMidiService(String id) {
        super(id);
    }

    public void destroy() {
    }

}
