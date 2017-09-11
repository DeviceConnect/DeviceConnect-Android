/*
 SWKeyEventProfile.java
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
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * SonySW device plugin {@link KeyEventProfile} implementation.
 * 
 * @author NTT DOCOMO, INC.
 */
public class SWKeyEventProfile extends KeyEventProfile {

    /** Key Event profile event management flag. */
    private static int sFlagKeyEventEventManage = 0;
    /** Key Event profile event flag. (ondown) */
    private static final int FLAG_ON_DOWN = 0x0001;
    /** Key Event  profile event flag. (onup) */
    private static final int FLAG_ON_UP = 0x0002;
    /** Key Event  profile event flag. (onkeychange) */
    private static final int FLAG_ON_KEY_CHANGE = 0x0004;
    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_KEY_CHANGE = "onKeyChange";
    private final DConnectApi mGetOnKeyChangeApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyevent = SWApplication.getKeyEventCache(ATTRIBUTE_ON_KEY_CHANGE);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mGetOnDownApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyevent = SWApplication.getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_DOWN);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnUpApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyevent = SWApplication.getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_UP);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mPutOnKeyChangeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayKeyEventScreen();
                setKeyEventEventFlag(FLAG_ON_KEY_CHANGE);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };
    private final DConnectApi mPutOnDownApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayKeyEventScreen();
                setKeyEventEventFlag(FLAG_ON_DOWN);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnUpApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                displayKeyEventScreen();
                setKeyEventEventFlag(FLAG_ON_UP);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };
    private final DConnectApi mDeleteOnKeyChangeApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetKeyEventEventFlag(FLAG_ON_KEY_CHANGE))) {
                    clearKeyEventScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };
    private final DConnectApi mDeleteOnDownApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetKeyEventEventFlag(FLAG_ON_DOWN))) {
                    clearKeyEventScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnUpApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                if (!(resetKeyEventEventFlag(FLAG_ON_UP))) {
                    clearKeyEventScreen();
                }
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    public SWKeyEventProfile() {
        addApi(mGetOnKeyChangeApi);
        addApi(mGetOnDownApi);
        addApi(mGetOnUpApi);
        addApi(mPutOnKeyChangeApi);
        addApi(mPutOnDownApi);
        addApi(mPutOnUpApi);
        addApi(mDeleteOnKeyChangeApi);
        addApi(mDeleteOnDownApi);
        addApi(mDeleteOnUpApi);
    }

    /**
     * Display Key Event screen.
     */
    private void displayKeyEventScreen() {
        Intent intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
        if (((SWService) getService()).getWatchType() == SWService.WatchType.SW2) {
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.keyevent_control);
        } else {
            return; // This function not implemented. Because SW could not redraw xml layout data.
        }
        sendToHostApp(intent);
    }

    /**
     * Clear KeyEvent screen.
     */
    private void clearKeyEventScreen() {
        if (((SWService) getService()).getWatchType() == SWService.WatchType.SW2) {
            Intent intent = new Intent(Control.Intents.CONTROL_CLEAR_DISPLAY_INTENT);
            sendToHostApp(intent);
            intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.touch_clear_control_sw2);
            sendToHostApp(intent);
        }
    }

    /**
     * Set key event event flag.
     * 
     * @param flag Set flag.
     */
    private void setKeyEventEventFlag(final int flag) {
        sFlagKeyEventEventManage |= flag;
    }

    /**
     * Reset key event event flag.
     * 
     * @param flag Reset flag.
     * @return true : Other event register. false : No event registration.
     */
    private boolean resetKeyEventEventFlag(final int flag) {
        sFlagKeyEventEventManage &= ~(flag);
        return sFlagKeyEventEventManage != 0;
    }

    private void sendToHostApp(final Intent request) {
        ((SWService) getService()).sendRequest(request);
    }

    /**
     * Release KeyEvent.
     */
    public void releaseKeyEvent() {
        sFlagKeyEventEventManage = 0;
        clearKeyEventScreen();
    }
}
