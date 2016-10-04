package org.deviceconnect.android.deviceplugin.alljoyn;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * A helper class to perform a one-shot AllJoyn session task.
 *
 * @author NTT DOCOMO, INC.
 */
public class OneShotSessionHandler {

    private static final int JOIN_RETRY_MAX = 5;

    private OneShotSessionHandler() {

    }

    public static void run(@NonNull final Context context, @NonNull final String busName,
                           final short port, @NonNull final SessionJoinCallback callback) {
        final AllJoynDeviceApplication app =
                (AllJoynDeviceApplication) context.getApplicationContext();

        AllJoynDeviceApplication.ResultReceiver resultReceiver = app.new ResultReceiver() {
            private int failedCount = 0;

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != AllJoynDeviceApplication.RESULT_OK) {
                    if (failedCount > JOIN_RETRY_MAX) {
                        callback.onSessionFailed(busName, port);
                    } else {
                        ++failedCount;
                        app.joinSession(busName, port, this);
                    }
                    return;
                }
                int sessionId = resultData.getInt(AllJoynDeviceApplication.PARAM_SESSION_ID);

                callback.onSessionJoined(busName, port, sessionId);

                app.leaveSession(sessionId, app.new ResultReceiver());
            }
        };

        app.joinSession(busName, port, resultReceiver);
    }

    public interface SessionJoinCallback {
        void onSessionJoined(@NonNull String busName, short port, int sessionId);

        void onSessionFailed(@NonNull String busName, short port);
    }
}
