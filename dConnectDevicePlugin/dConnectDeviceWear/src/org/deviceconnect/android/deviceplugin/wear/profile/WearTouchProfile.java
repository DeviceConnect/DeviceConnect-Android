/*
 WearTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.deviceplugin.wear.BuildConfig;
import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageEventListener;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi.SendMessageResult;

/**
 * Touch Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearTouchProfile extends TouchProfile {

    /** Tag. */
    private static final String TAG = "WEAR";

    /** Touch profile onTouch cache. */
    Bundle mOnTouchCache = null;
    
    /** Touch profile onTouch cache time. */
    long mOnTouchCacheTime = 0;
    
    /** Touch profile onTouchStart cache. */
    Bundle mOnTouchStartCache = null;
    
    /** Touch profile onTouchStart cache time. */
    long mOnTouchStartCacheTime = 0;
    
    /** Touch profile onTouchEnd cache. */
    Bundle mOnTouchEndCache = null;
    
    /** Touch profile onTouchEnd cache time. */
    long mOnTouchEndCacheTime = 0;
    
    /** Touch profile onDoubleTap cache. */
    Bundle mOnDoubleTapCache = null;
    
    /** Touch profile onDoubleTap cache time. */
    long mOnDoubleTapCacheTime = 0;
    
    /** Touch profile onTouchMove cache. */
    Bundle mOnTouchMoveCache = null;
    
    /** Touch profile onTouchMove cache time. */
    long mOnTouchMoveCacheTime = 0;
    
    /** Touch profile onTouchCancel cache. */
    Bundle mOnTouchCancelCache = null;
    
    /** Touch profile onTouchCancel cache time. */
    long mOnTouchCancelCacheTime = 0;
    
    /** Touch profile cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;
    
    /**
     * Get Touch cache data.
     * 
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public Bundle getTouchCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(ATTRIBUTE_ON_TOUCH)) {
            if (lCurrentTime - mOnTouchCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCache;
            } else {
                return null;
            }
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_START)) {
            if (lCurrentTime - mOnTouchStartCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchStartCache;
            } else {
                return null;
            }
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_END)) {
            if (lCurrentTime - mOnTouchEndCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchEndCache;
            } else {
                return null;
            }
        } else if (attr.equals(ATTRIBUTE_ON_DOUBLE_TAP)) {
            if (lCurrentTime - mOnDoubleTapCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDoubleTapCache;
            } else {
                return null;
            }
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_MOVE)) {
            if (lCurrentTime - mOnTouchMoveCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchMoveCache;
            } else {
                return null;
            }
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_CANCEL)) {
            if (lCurrentTime - mOnTouchCancelCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCancelCache;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Set Touch data to cache.
     * 
     * @param attr Attribute.
     * @param touchData Touch data.
     */
    public void setTouchCache(final String attr, final Bundle touchData) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(ATTRIBUTE_ON_TOUCH)) {
            mOnTouchCache = touchData;
            mOnTouchCacheTime = lCurrentTime;
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_START)) {
            mOnTouchStartCache = touchData;
            mOnTouchStartCacheTime = lCurrentTime;
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_END)) {
            mOnTouchEndCache = touchData;
            mOnTouchEndCacheTime = lCurrentTime;
        } else if (attr.equals(ATTRIBUTE_ON_DOUBLE_TAP)) {
            mOnDoubleTapCache = touchData;
            mOnDoubleTapCacheTime = lCurrentTime;
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_MOVE)) {
            mOnTouchMoveCache = touchData;
            mOnTouchMoveCacheTime = lCurrentTime;
        } else if (attr.equals(ATTRIBUTE_ON_TOUCH_CANCEL)) {
            mOnTouchCancelCache = touchData;
            mOnTouchCancelCacheTime = lCurrentTime;
        }
    }

    /**
     * Event receive listener from Android Wear.
     */
    private OnMessageEventListener mListener = new OnMessageEventListener() {
        @Override
        public void onEvent(final String nodeId, final String message) {
            sendMessageToEvent(WearUtils.createServiceId(nodeId), message);
        }
    };

    /**
     * Constructor.
     * @param mgr Android Wear管理クラス
     */
    public WearTouchProfile(final WearManager mgr) {
        mgr.addMessageEventListener(WearConst.WEAR_TO_DEVICE_TOUCH_DATA, mListener);
    }

    @Override
    protected boolean onGetOnTouch(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchStart(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_START);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchEnd(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_END);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnDoubleTap(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchMove(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchCancel(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        // Event registration.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onPutOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        // Event registration.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onPutOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        // Event registration.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onPutOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        // Event registration.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onPutOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        // Event registration.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onPutOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        // Event registration.
                        EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    getContext().sendBroadcast(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    getContext().sendBroadcast(response);
                }
            });
            return false;
        }
    }

    @Override
    protected boolean onDeleteOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
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
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, 
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
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
        }
        return true;
    }

    /**
     * Send a message to the registration event.
     * 
     * @param nodeId node id
     * @param data Received Strings.
     */
    private void sendMessageToEvent(final String nodeId, final String data) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@@@@SUCCESS");
        }

        String[] mDataArray = data.split(",", 0);
        String attr = null;
        String action = mDataArray[1];

        if (action.equals(WearConst.PARAM_TOUCH_TOUCH)) {
            attr = ATTRIBUTE_ON_TOUCH;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHSTART)) {
            attr = ATTRIBUTE_ON_TOUCH_START;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHEND)) {
            attr = ATTRIBUTE_ON_TOUCH_END;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHMOVE)) {
            attr = ATTRIBUTE_ON_TOUCH_MOVE;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHCANCEL)) {
            attr = ATTRIBUTE_ON_TOUCH_CANCEL;
        } else if (action.equals(WearConst.PARAM_TOUCH_DOUBLETAP)) {
            attr = ATTRIBUTE_ON_DOUBLE_TAP;
        } else {
            attr = null;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "action: " + action + " attr: " + attr);
        }

        if (attr != null) {
            List<Event> events = EventManager.INSTANCE.getEventList(
                    nodeId, PROFILE_NAME, null, attr);
            synchronized (events) {
                for (Event event : events) {
                    Bundle touchdata = new Bundle();
                    List<Bundle> touchlist = new ArrayList<Bundle>();
                    Bundle touches = new Bundle();
                    int count = Integer.parseInt(mDataArray[0]);
                    int index = 2;
                    for (int n = 0; n < count; n++) {
                        touchdata.putInt(TouchProfile.PARAM_ID, Integer.parseInt(mDataArray[index++]));
                        touchdata.putFloat(TouchProfile.PARAM_X, Float.parseFloat(mDataArray[index++]));
                        touchdata.putFloat(TouchProfile.PARAM_Y, Float.parseFloat(mDataArray[index++]));
                        touchlist.add((Bundle) touchdata.clone());
                    }
                    touches.putParcelableArray(TouchProfile.PARAM_TOUCHES,
                            touchlist.toArray(new Bundle[touchlist.size()]));
                    String eventAttr = event.getAttribute();
                    Intent intent = EventManager.createEventMessage(event);
                    intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
                    ((WearDeviceService) getContext()).sendEvent(intent, event.getAccessToken());
                    setTouchCache(eventAttr, touches);
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "event: " + event);
                        Log.i(TAG, "touches: " + touches);
                        Log.i(TAG, "intent: " + intent);
                    }
                }
            }
        }
    }

    /**
     * Get Android Wear management class.
     * @return WearManager management class.
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
