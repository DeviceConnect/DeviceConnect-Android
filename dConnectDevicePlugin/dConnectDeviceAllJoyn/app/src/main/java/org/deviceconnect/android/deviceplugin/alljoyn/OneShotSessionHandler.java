package org.deviceconnect.android.deviceplugin.alljoyn;

import android.content.Context;
import android.os.Bundle;

public class OneShotSessionHandler {
    private OneShotSessionHandler() {

    }

    public static void run(final Context context, final String busName, final short port,
                           final SessionJoinCallback callback) {
        final AllJoynDeviceApplication app =
                (AllJoynDeviceApplication) context.getApplicationContext();

        app.joinSession(busName, port, app.new ResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != AllJoynDeviceApplication.RESULT_OK) {
                    callback.onSessionFailed(busName, port);
                    return;
                }
                int sessionId = resultData.getInt(AllJoynDeviceApplication.PARAM_SESSION_ID);

                callback.onSessionJoined(busName, port, sessionId);

                app.leaveSession(sessionId, app.new ResultReceiver());
            }
        });
    }

    public interface SessionJoinCallback {
        void onSessionJoined(String busName, short port, int sessionId);

        void onSessionFailed(String busName, short port);
    }
}
