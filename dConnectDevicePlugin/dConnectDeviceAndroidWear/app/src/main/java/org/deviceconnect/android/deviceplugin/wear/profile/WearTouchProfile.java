/*
 WearTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


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
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.ATTRIBUTE_ON_TOUCH_CHANGE;
import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_CANCEL;
import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_DOUBLE_TAP;
import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_END;
import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_MOVE;
import static org.deviceconnect.android.deviceplugin.wear.profile.WearConst.STATE_START;

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
    /** Touch profile onTouchChange cache. */
    Bundle mOnTouchChangeCache = null;

    /** Touch profile onTouchChange cache time. */
    long mOnTouchChangeCacheTime = 0;
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
        if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH)) {
            if (lCurrentTime - mOnTouchCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_START)) {
            if (lCurrentTime - mOnTouchStartCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchStartCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_END)) {
            if (lCurrentTime - mOnTouchEndCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchEndCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_DOUBLE_TAP)) {
            if (lCurrentTime - mOnDoubleTapCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDoubleTapCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_MOVE)) {
            if (lCurrentTime - mOnTouchMoveCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchMoveCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_CANCEL)) {
            if (lCurrentTime - mOnTouchCancelCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCancelCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_CHANGE)) {
            if (lCurrentTime - mOnTouchChangeCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchChangeCache;
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
        if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH)) {
            mOnTouchCache = touchData;
            mOnTouchCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_START)) {
            mOnTouchStartCache = touchData;
            mOnTouchStartCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_END)) {
            mOnTouchEndCache = touchData;
            mOnTouchEndCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_DOUBLE_TAP)) {
            mOnDoubleTapCache = touchData;
            mOnDoubleTapCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_MOVE)) {
            mOnTouchMoveCache = touchData;
            mOnTouchMoveCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_CANCEL)) {
            mOnTouchCancelCache = touchData;
            mOnTouchCancelCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_CHANGE)) {
            mOnTouchChangeCache = touchData;
            mOnTouchChangeCacheTime = lCurrentTime;
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
        addApi(mGetOnTouch);
        addApi(mGetOnTouchStart);
        addApi(mGetOnTouchEnd);
        addApi(mGetOnDoubleTap);
        addApi(mGetOnTouchMove);
        addApi(mGetOnTouchCancel);
        addApi(mGetOnTouchChange);
        addApi(mPutOnTouch);
        addApi(mPutOnTouchStart);
        addApi(mPutOnTouchEnd);
        addApi(mPutOnDoubleTap);
        addApi(mPutOnTouchMove);
        addApi(mPutOnTouchCancel);
        addApi(mPutOnTouchChange);
        addApi(mDeleteOnTouch);
        addApi(mDeleteOnTouchStart);
        addApi(mDeleteOnTouchEnd);
        addApi(mDeleteOnDoubleTap);
        addApi(mDeleteOnTouchMove);
        addApi(mDeleteOnTouchCancel);
        addApi(mDeleteOnTouchChange);
    }
    private final DConnectApi mGetOnTouchChange = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(ATTRIBUTE_ON_TOUCH_CHANGE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mGetOnTouch = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchStart = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_START);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchEnd = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_END);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnDoubleTap = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchMove = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchCancel = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mPutOnTouchChange = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));

            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_REGISTER,
                    "", new OnMessageResultListener() {
                        @Override
                        public void onResult() {
                            // Event registration.
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
    private final DConnectApi mPutOnTouch = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));

            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        // Event registration.
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

    private final DConnectApi mPutOnTouchStart = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        // Event registration.
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

    private final DConnectApi mPutOnTouchEnd = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        // Event registration.
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

    private final DConnectApi mPutOnDoubleTap = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        // Event registration.
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

    private final DConnectApi mPutOnTouchMove = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        // Event registration.
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

    private final DConnectApi mPutOnTouchCancel = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER,
                "", new OnMessageResultListener() {
                    @Override
                    public void onResult() {
                        // Event registration.
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
    private final DConnectApi mDeleteOnTouchChange = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                    WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_UNREGISTER,
                    "", new OnMessageResultListener() {
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
    private final DConnectApi mDeleteOnTouch = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER,
                "", new OnMessageResultListener() {
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

    private final DConnectApi mDeleteOnTouchStart = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER,
                "", new OnMessageResultListener() {
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

    private final DConnectApi mDeleteOnTouchEnd = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER,
                "", new OnMessageResultListener() {
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

    private final DConnectApi mDeleteOnDoubleTap = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER,
                "", new OnMessageResultListener() {
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

    private final DConnectApi mDeleteOnTouchMove = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER,
                "", new OnMessageResultListener() {
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

    private final DConnectApi mDeleteOnTouchCancel = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId,
                WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER,
                "", new OnMessageResultListener() {
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
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@@@@SUCCESS");
        }

        String[] mDataArray = data.split(",", 0);
        String attr;
        String action = mDataArray[1];
        String state = null;
        if (action.equals(WearConst.PARAM_TOUCH_TOUCH)) {
            attr = ATTRIBUTE_ON_TOUCH;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHSTART)) {
            attr = ATTRIBUTE_ON_TOUCH_START;
            state = STATE_START;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHEND)) {
            attr = ATTRIBUTE_ON_TOUCH_END;
            state = STATE_END;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHMOVE)) {
            attr = ATTRIBUTE_ON_TOUCH_MOVE;
            state = STATE_MOVE;
        } else if (action.equals(WearConst.PARAM_TOUCH_TOUCHCANCEL)) {
            attr = ATTRIBUTE_ON_TOUCH_CANCEL;
            state = STATE_CANCEL;
        } else if (action.equals(WearConst.PARAM_TOUCH_DOUBLETAP)) {
            attr = ATTRIBUTE_ON_DOUBLE_TAP;
            state = STATE_DOUBLE_TAP;
        } else {
            attr = null;
            state = null;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "action: " + action + " attr: " + attr);
        }

        if (attr != null) {
            List<Event> events = EventManager.INSTANCE.getEventList(
                    nodeId, PROFILE_NAME, null, attr);
            List<Event> commonEvents = EventManager.INSTANCE.getEventList(
                    nodeId, PROFILE_NAME, null, ATTRIBUTE_ON_TOUCH_CHANGE);
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
            synchronized (events) {
                for (Event event : events) {
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
            synchronized (commonEvents) {
                for (Event event : commonEvents) {
                    String eventAttr = event.getAttribute();
                    Intent intent = EventManager.createEventMessage(event);
                    if (state != null) {
                        touches.putString("state", state);
                    }
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
