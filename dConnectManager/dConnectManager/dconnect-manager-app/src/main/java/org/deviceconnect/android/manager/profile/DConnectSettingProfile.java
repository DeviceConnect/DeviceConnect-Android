/*
 DConnectSettingProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.manager.protection.CopyGuardSetting;
import org.deviceconnect.android.manager.protection.SimpleCopyGuard;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

import static org.deviceconnect.android.manager.core.BuildConfig.DEBUG;

/**
 * Setting プロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectSettingProfile extends DConnectProfile {

    private static final String INTERFACE_COPY_GUARD = "copyGuard";

    private static final String ATTR_ON_CHANGE = "onChange";

    private final SimpleCopyGuard mCopyGuard;

    private final HandlerThread mHandlerThread;

    private final CopyGuardSetting.EventListener mEventListener = ((setting, isEnabled) -> {
        List<Event> events = EventManager.INSTANCE.getEventList(null,
                getProfileName(),
                INTERFACE_COPY_GUARD,
                ATTR_ON_CHANGE);
        for (Event event : events) {
            Intent intent = createEventMessage(event);
            intent.putExtra("enabled", isEnabled);
            sendEvent(intent, event.getAccessToken());
        }
    });

    private static Intent createEventMessage(final Event event) {
        Intent message = MessageUtils.createEventIntent();
        message.putExtra(DConnectMessage.EXTRA_SERVICE_ID, event.getServiceId());
        message.putExtra(DConnectMessage.EXTRA_PROFILE, event.getProfile());
        message.putExtra(DConnectMessage.EXTRA_INTERFACE, event.getInterface());
        message.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, event.getAttribute());
        message.putExtra(DConnectMessage.EXTRA_ACCESS_TOKEN, event.getAccessToken());
        String receiverName = event.getReceiverName();
        if (receiverName != null) {
            ComponentName cn = ComponentName.unflattenFromString(receiverName);
            message.setComponent(cn);
        }
        return message;
    }

    public void destroy() {
        mHandlerThread.quitSafely();
    }

    public DConnectSettingProfile(final Context context, final int appIconId) {
        mHandlerThread = new HandlerThread("SettingProfileThread");
        mHandlerThread.start();
        mCopyGuard = new SimpleCopyGuard(context, appIconId);
        mCopyGuard.setEventListener(mEventListener, new Handler(mHandlerThread.getLooper()));

        // GET /gotapi/setting/copyGuard
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "copyGuard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                root.putBoolean("enabled", mCopyGuard.isEnabled());
                response.putExtras(root);
                return true;
            }
        });

        // PUT /gotapi/setting/copyGuard
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "copyGuard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                try {
                    mCopyGuard.enable();
                    setResult(response, DConnectMessage.RESULT_OK);
                    return true;
                } catch (Throwable e) {
                    MessageUtils.setUnknownError(response, "Failed to enable copy guard: " + e.getMessage());
                    return true;
                }
            }
        });

        // DELETE /gotapi/setting/copyGuard
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "copyGuard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                mCopyGuard.disable();
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // PUT /gotapi/setting/copyGuard/onChange
        addApi(new PutApi() {
            @Override
            public String getInterface() {
                return "copyGuard";
            }

            @Override
            public String getAttribute() {
                return "onChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (DEBUG) {
                    Log.d("ABC", "PUT /gotapi/setting/copyGuard/onChange: receiver = " + request.getParcelableExtra("receiver"));
                }

                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /gotapi/setting/copyGuard/onChange
        addApi(new DeleteApi() {
            @Override
            public String getInterface() {
                return "copyGuard";
            }

            @Override
            public String getAttribute() {
                return "onChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event is not registered.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "setting";
    }
}
