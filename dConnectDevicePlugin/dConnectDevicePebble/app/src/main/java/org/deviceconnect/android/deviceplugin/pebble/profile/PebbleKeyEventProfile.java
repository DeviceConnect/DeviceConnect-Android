/*
 PebbleKeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import android.content.Intent;
import android.os.Bundle;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnReceivedEventListener;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnSendCommandListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.KeyEventProfileConstants;

import java.util.List;

/**
 * Pebble Key Event Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class PebbleKeyEventProfile extends KeyEventProfile {
    /** Error message for not setting sessionKey. */
    private static final String ERROR_MESSAGE = "sessionKey must be specified.";

    /** KeyEvent profile onDown cache. */
    Bundle mOnDownCache = null;

    /** KeyEvent profile onDown cache time. */
    long mOnDownCacheTime = 0;

    /** KeyEvent profile onUp cache. */
    Bundle mOnUpCache = null;

    /** KeyEvent profile onUp cache time. */
    long mOnUpCacheTime = 0;

    /** KeyEvent profile cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;

    /**
     * Get KeyEvent cache data.
     * 
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public Bundle getKeyEventCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            if (lCurrentTime - mOnDownCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDownCache;
            } else {
                return null;
            }
        } else if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            if (lCurrentTime - mOnUpCacheTime <= CACHE_RETENTION_TIME) {
                return mOnUpCache;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Set KeyEvent data to cache.
     * 
     * @param attr Attribute.
     * @param keyeventData Touch data.
     */
    public void setKeyEventCache(final String attr, final Bundle keyeventData) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            mOnDownCache = keyeventData;
            mOnDownCacheTime = lCurrentTime;
        } else if (attr.equals(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            mOnUpCache = keyeventData;
            mOnUpCacheTime = lCurrentTime;
        }
    }

    /**
     * Constructor.
     * 
     * @param service Pebble device service.
     */
    public PebbleKeyEventProfile(final PebbleDeviceService service) {
        service.getPebbleManager().addEventListener(PebbleManager.PROFILE_KEY_EVENT, new OnReceivedEventListener() {
            @Override
            public void onReceivedEvent(final PebbleDictionary dic) {
                // Set event data.
                Bundle keyevent = new Bundle();
                Long lKeyId = dic.getInteger(PebbleManager.KEY_PARAM_KEY_EVENT_ID);
                int nKeyId = Integer.valueOf(lKeyId.toString());
                Long lKeyType = dic.getInteger(PebbleManager.KEY_PARAM_KEY_EVENT_KEY_TYPE);
                int nKeyType = Integer.valueOf(lKeyType.toString());
                setConfig(keyevent, getConfig(nKeyType, nKeyId));
                setId(keyevent, nKeyId + getKeyTypeFlagValue(nKeyType));

                // Get event list from event listener.
                List<Event> evts = null;
                Long lAttribute = dic.getInteger(PebbleManager.KEY_ATTRIBUTE);
                if (lAttribute == PebbleManager.KEY_EVENT_ATTRIBUTE_ON_UP) {
                    evts = EventManager.INSTANCE.getEventList(service.getServiceId(), PROFILE_NAME, null,
                            ATTRIBUTE_ON_UP);
                } else if (lAttribute == PebbleManager.KEY_EVENT_ATTRIBUTE_ON_DOWN) {
                    evts = EventManager.INSTANCE.getEventList(service.getServiceId(), PROFILE_NAME, null,
                            ATTRIBUTE_ON_DOWN);
                } else {
                    return;
                }

                for (Event evt : evts) {
                    String attr = evt.getAttribute();
                    // Notify each to the event listener.
                    Intent intent = EventManager.createEventMessage(evt);
                    intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
                    ((PebbleDeviceService) getContext()).sendEvent(intent, evt.getAccessToken());
                    setKeyEventCache(attr, keyevent);
                }
            }
        });
    }

    @Override
    protected boolean onGetOnDown(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle keyevent = getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_DOWN);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnUp(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle keyevent = getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_UP);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {

            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            // To Pebble, Send registration request of key event.
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_KEY_EVENT);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.KEY_EVENT_ATTRIBUTE_ON_DOWN);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_PUT);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setUnknownError(response);
                    } else {
                        // Registration event listener.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else if (error == EventError.INVALID_PARAMETER) {
                            MessageUtils.setInvalidRequestParameterError(response);
                        } else {
                            MessageUtils.setUnknownError(response);
                        }
                    }
                    sendResponse(response);
                }
            });
            // Since returning the response asynchronously, it returns false.
            return false;
        }
    }

    @Override
    protected boolean onPutOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {

            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            // To Pebble, Send registration request of key event.
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_KEY_EVENT);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.KEY_EVENT_ATTRIBUTE_ON_UP);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_PUT);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setUnknownError(response);
                    } else {
                        // Registration event listener.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else if (error == EventError.INVALID_PARAMETER) {
                            MessageUtils.setInvalidRequestParameterError(response);
                        } else {
                            MessageUtils.setUnknownError(response);
                        }
                    }
                    sendResponse(response);
                }
            });
            // Since returning the response asynchronously, it returns false.
            return false;
        }
    }

    @Override
    protected boolean onDeleteOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();

            // To Pebble, Send cancellation request of key event.
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_KEY_EVENT);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.KEY_EVENT_ATTRIBUTE_ON_DOWN);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                }
            });
            // Remove event listener.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    }

    @Override
    protected boolean onDeleteOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();

            // To Pebble, Send cancellation request of key event.
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_KEY_EVENT);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.KEY_EVENT_ATTRIBUTE_ON_UP);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                }
            });
            // Remove event listener.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    }

    /**
     * Get configuration string.
     * 
     * @param nType Key Type.
     * @param nCode Key Code.
     * @return Configure string.
     */
    private String getConfig(final int nType, final int nCode) {
        switch (nType) {
        case PebbleManager.KEY_EVENT_KEY_TYPE_MEDIA:
            switch (nCode) {
            case PebbleManager.KEY_EVENT_KEY_ID_UP:
                return "MEDIA_NEXT";
            case PebbleManager.KEY_EVENT_KEY_ID_SELECT:
                return "MEDIA_PLAY";
            case PebbleManager.KEY_EVENT_KEY_ID_DOWN:
                return "MEDIA_PREVIOUS";
            case PebbleManager.KEY_EVENT_KEY_ID_BACK:
                return "MEDIA_BACK";
            default:
                return "";
            }
        case PebbleManager.KEY_EVENT_KEY_TYPE_DPAD_BUTTON:
            switch (nCode) {
            case PebbleManager.KEY_EVENT_KEY_ID_UP:
                return "DPAD_UP";
            case PebbleManager.KEY_EVENT_KEY_ID_SELECT:
                return "DPAD_CENTER";
            case PebbleManager.KEY_EVENT_KEY_ID_DOWN:
                return "DPAD_DOWN";
            case PebbleManager.KEY_EVENT_KEY_ID_BACK:
                return "DPAD_BACK";
            default:
                return "";
            }
        case PebbleManager.KEY_EVENT_KEY_TYPE_USER:
            switch (nCode) {
            case PebbleManager.KEY_EVENT_KEY_ID_UP:
                return "USER_CANCEL";
            case PebbleManager.KEY_EVENT_KEY_ID_SELECT:
                return "USER_SELECT";
            case PebbleManager.KEY_EVENT_KEY_ID_DOWN:
                return "USER_OK";
            case PebbleManager.KEY_EVENT_KEY_ID_BACK:
                return "USER_BACK";
            default:
                return "";
            }
        case PebbleManager.KEY_EVENT_KEY_TYPE_STD_KEY:
        default:
            switch (nCode) {
            case PebbleManager.KEY_EVENT_KEY_ID_UP:
                return "UP";
            case PebbleManager.KEY_EVENT_KEY_ID_SELECT:
                return "SELECT";
            case PebbleManager.KEY_EVENT_KEY_ID_DOWN:
                return "DOWN";
            case PebbleManager.KEY_EVENT_KEY_ID_BACK:
                return "BACK";
            default:
                return "";
            }
        }
    }

    /**
     * Get key type flag value.
     * 
     * @param nType Key Type.
     * @return Key Type Flag Value.
     */
    private int getKeyTypeFlagValue(final int nType) {
        switch (nType) {
        case PebbleManager.KEY_EVENT_KEY_TYPE_MEDIA:
            return KeyEventProfileConstants.KEYTYPE_MEDIA_CTRL;
        case PebbleManager.KEY_EVENT_KEY_TYPE_DPAD_BUTTON:
            return KeyEventProfileConstants.KEYTYPE_DPAD_BUTTON;
        case PebbleManager.KEY_EVENT_KEY_TYPE_USER:
            return KeyEventProfileConstants.KEYTYPE_USER;
        case PebbleManager.KEY_EVENT_KEY_TYPE_STD_KEY:
        default:
            return KeyEventProfileConstants.KEYTYPE_STD_KEY;
        }
    }
}
