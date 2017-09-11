/*
 SWTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import android.content.Intent;
import android.os.Bundle;

import com.sonyericsson.extras.liveware.aef.control.Control;

import org.deviceconnect.android.deviceplugin.sw.R;
import org.deviceconnect.android.deviceplugin.sw.SWApplication;
import org.deviceconnect.android.deviceplugin.sw.service.SWService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * SonySW device plug-in {@link TouchProfile} implementation.
 * 
 * @author NTT DOCOMO, INC.
 */
public class SWTouchProfile extends TouchProfile {

    /** Touch profile event management flag. */
    private static int sFlagTouchEventManage = 0;
    /** Touch profile event flag. (ontouch) */
    private static final int FLAG_ON_TOUCH = 0x0001;
    /** Touch profile event flag. (ontouchstart) */
    private static final int FLAG_ON_TOUCH_START = 0x0002;
    /** Touch profile event flag. (ontouchend) */
    private static final int FLAG_ON_TOUCH_END = 0x0004;
    /** Touch profile event flag. (ondoubletap) */
    private static final int FLAG_ON_DOUBLE_TAP = 0x0008;
    /** Touch profile event flag. (ontouchchange). */
    private static final int FLAG_ON_TOUCH_CHANGE = 0x0040;
    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_TOUCH_CHANGE = "onTouchChange";
    private final DConnectApi mGetOnTouchChangeApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = SWApplication.getTouchCache(ATTRIBUTE_ON_TOUCH_CHANGE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mGetOnTouchApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchStartApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_START);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchEndApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_END);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnDoubleTapApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mPutOnTouchChangeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayTouchScreen();
                setTouchEventFlag(FLAG_ON_TOUCH_CHANGE);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };
    private final DConnectApi mPutOnTouchApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayTouchScreen();
                setTouchEventFlag(FLAG_ON_TOUCH);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnTouchStartApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayTouchScreen();
                setTouchEventFlag(FLAG_ON_TOUCH_START);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnTouchEndApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayTouchScreen();
                setTouchEventFlag(FLAG_ON_TOUCH_END);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnDoubleTapApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayTouchScreen();
                setTouchEventFlag(FLAG_ON_DOUBLE_TAP);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchChangeApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetTouchEventFlag(FLAG_ON_TOUCH_CHANGE))) {
                    clearTouchScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };
    private final DConnectApi mDeleteOnTouchApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetTouchEventFlag(FLAG_ON_TOUCH))) {
                    clearTouchScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchStartApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetTouchEventFlag(FLAG_ON_TOUCH_START))) {
                    clearTouchScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchEndApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetTouchEventFlag(FLAG_ON_TOUCH_END))) {
                    clearTouchScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDoubleTapApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetTouchEventFlag(FLAG_ON_DOUBLE_TAP))) {
                    clearTouchScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    public SWTouchProfile() {
        addApi(mGetOnTouchApi);
        addApi(mGetOnTouchStartApi);
        addApi(mGetOnTouchEndApi);
        addApi(mGetOnDoubleTapApi);
        addApi(mGetOnTouchChangeApi);
        // SW not support "GET TouchMove".
        // SW not support "GET TouchCancel".
        addApi(mPutOnTouchApi);
        addApi(mPutOnTouchStartApi);
        addApi(mPutOnTouchEndApi);
        addApi(mPutOnDoubleTapApi);
        addApi(mPutOnTouchChangeApi);
        // SW not support "PUT TouchMove".
        // SW not support "PUT TouchCancel".
        addApi(mDeleteOnTouchApi);
        addApi(mDeleteOnTouchStartApi);
        addApi(mDeleteOnTouchEndApi);
        addApi(mDeleteOnDoubleTapApi);
        addApi(mDeleteOnTouchChangeApi);
        // SW not support "DELETE TouchMove".
        // SW not support "DELETE TouchCancel".
    }

    /**
     * Display Touch screen.
     */
    private void displayTouchScreen() {
        Intent intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
        if (((SWService) getService()).getWatchType() == SWService.WatchType.SW2) {
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.touch_control_sw2);
        } else {
            return; // This function not implemented. Because SW could not redraw xml layout data.
        }
        sendToHostApp(intent);
    }

    /**
     * Clear Touch screen.
     */
    private void clearTouchScreen() {
        if (((SWService) getService()).getWatchType() == SWService.WatchType.SW2) {
            Intent intent = new Intent(Control.Intents.CONTROL_CLEAR_DISPLAY_INTENT);
            sendToHostApp(intent);
            intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.touch_clear_control_sw2);
            sendToHostApp(intent);
        } else  {
            return; // This function not implemented. Because SW could not redraw xml layout data.
        }
    }

    /**
     * Set touch event flag.
     * 
     * @param flag Set flag.
     */
    private void setTouchEventFlag(final int flag) {
        sFlagTouchEventManage |= flag;
    }

    /**
     * Reset touch event flag.
     * 
     * @param flag Reset flag.
     * @return true : Other event register. false : No event registration.
     */
    private boolean resetTouchEventFlag(final int flag) {
        sFlagTouchEventManage &= ~(flag);
        return sFlagTouchEventManage != 0;
    }

    private void sendToHostApp(final Intent request) {
        ((SWService) getService()).sendRequest(request);
    }

    /**
     * Release Touch Event.
     */
    public void releaseTouchEvent() {
        sFlagTouchEventManage = 0;
        clearTouchScreen();
    }
}
