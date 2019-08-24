/*
 WearKeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.wear.BuildConfig;
import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageEventListener;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_DOWN;
import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_UP;

/**
 * Key Event Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearKeyEventProfile extends KeyEventProfile {

    /** Tag. */
    private static final String TAG = "WEAR";

    /** KeyEvent profile onDown cache. */
    Bundle mOnDownCache = null;

    /** KeyEvent profile onDown cache time. */
    long mOnDownCacheTime = 0;

    /** KeyEvent profile onDown cache. */
    Bundle mOnUpCache = null;

    /** KeyEvent profile onUp cache time. */
    long mOnUpCacheTime = 0;
    /** KeyEvent profile onKeyChange cache. */
    Bundle mOnKeyChangeCache = null;
    /** KeyEvent profile onKeyChange cache time. */
    long mOnKeyChangeCacheTime = 0;
    /** KeyEvent profile cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;
    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_KEY_CHANGE = "onKeyChange";
    /**
     * Get KeyEvent cache data.
     * 
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public Bundle getKeyEventCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            if (lCurrentTime - mOnDownCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDownCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            if (lCurrentTime - mOnUpCacheTime <= CACHE_RETENTION_TIME) {
                return mOnUpCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_KEY_CHANGE)) {
            if (lCurrentTime - mOnKeyChangeCacheTime <= CACHE_RETENTION_TIME) {
                return mOnKeyChangeCache;
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
        if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            mOnDownCache = keyeventData;
            mOnDownCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            mOnUpCache = keyeventData;
            mOnUpCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_KEY_CHANGE)) {
            mOnKeyChangeCache = keyeventData;
            mOnKeyChangeCacheTime = lCurrentTime;
        }
    }

    /**
     * Receive event listener for Android Wear.
     */
    private OnMessageEventListener mListener = new OnMessageEventListener() {
        @Override
        public void onEvent(final String nodeId, final String message) {
            sendMessageToEvent(WearUtils.createServiceId(nodeId), message);
        }
    };

    /**
     * Constructor.
     * 
     * @param mgr Android Wear management class.
     */
    public WearKeyEventProfile(final WearManager mgr) {
        mgr.addMessageEventListener(WearConst.WEAR_TO_DEVICE_KEYEVENT_DATA, mListener);
        addApi(mGetOnKeyChange);
        addApi(mGetOnDown);
        addApi(mGetOnUp);
        addApi(mPutOnKeyChange);
        addApi(mPutOnDown);
        addApi(mPutOnUp);
        addApi(mDeleteOnKeyChange);
        addApi(mDeleteOnDown);
        addApi(mDeleteOnUp);
    }

    private final DConnectApi mGetOnDown = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyevent = getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_DOWN);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnUp = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyEvent = getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_UP);
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyEvent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mGetOnKeyChange = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyevent = getKeyEventCache(ATTRIBUTE_ON_KEY_CHANGE);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mPutOnDown = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER, "",
                new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                        sendResponse(response);
                    }

                    @Override
                    public void onError() {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            return false;
        }
    };

    private final DConnectApi mPutOnUp = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER, "",
                new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                        sendResponse(response);
                    }

                    @Override
                    public void onError() {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            return false;
        }
    };
    private final DConnectApi mPutOnKeyChange = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_REGISTER, "",
                    new OnMessageResultListener() {
                        @Override
                        public void onResult() {
                            EventError error = EventManager.INSTANCE.addEvent(request);
                            if (error == EventError.NONE) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                setResult(response, DConnectMessage.RESULT_ERROR);
                            }
                            sendResponse(response);
                        }

                        @Override
                        public void onError() {
                            MessageUtils.setIllegalDeviceStateError(response);
                            sendResponse(response);
                        }
                    });
            return false;
        }
    };
    private final DConnectApi mDeleteOnDown = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER, "",
                new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                    }

                    @Override
                    public void onError() {
                    }
                });

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnUp = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER, "",
                new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                    }

                    @Override
                    public void onError() {
                    }
                });

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };
    private final DConnectApi mDeleteOnKeyChange = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_UNREGISTER, "",
                    new OnMessageResultListener() {
                        @Override
                        public void onResult() {
                        }

                        @Override
                        public void onError() {
                        }
                    });

            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };
    /**
     * Send a message to the registration event.
     * 
     * @param nodeId node id
     * @param data Received Strings.
     */
    private void sendMessageToEvent(final String nodeId, final String data) {
        String[] mDataArray = data.split(",", 0);
        String attr = null;
        String state = null;
        if (mDataArray[0].equals(WearConst.PARAM_KEYEVENT_DOWN)) {
            attr = ATTRIBUTE_ON_DOWN;
            state = STATE_DOWN;
        } else if (mDataArray[0].equals(WearConst.PARAM_KEYEVENT_UP)) {
            attr = ATTRIBUTE_ON_UP;
            state = STATE_UP;
        } else {
            return;
        }

        List<Event> events = EventManager.INSTANCE.getEventList(nodeId, PROFILE_NAME, null, attr);
        List<Event> keyEvents = EventManager.INSTANCE.getEventList(nodeId, PROFILE_NAME, null, ATTRIBUTE_ON_KEY_CHANGE);

        Bundle keyevent = new Bundle();

        keyevent.putInt(KeyEventProfile.PARAM_ID, Integer.parseInt(mDataArray[1]));
        keyevent.putString(KeyEventProfile.PARAM_CONFIG, mDataArray[2]);

        synchronized (events) {
            for (Event event : events) {

                String eventAttr = event.getAttribute();
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
                ((WearDeviceService) getContext()).sendEvent(intent, event.getAccessToken());
                setKeyEventCache(eventAttr, keyevent);
            }
        }
        synchronized (keyEvents) {
            for (Event event : keyEvents) {
                String eventAttr = event.getAttribute();
                keyevent.putString("state", state);
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
                ((WearDeviceService) getContext()).sendEvent(intent, event.getAccessToken());
                setKeyEventCache(eventAttr, keyevent);
            }
        }

    }

    /**
     * Get Android Wear management class.
     * 
     * @return WearManager management class.
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
