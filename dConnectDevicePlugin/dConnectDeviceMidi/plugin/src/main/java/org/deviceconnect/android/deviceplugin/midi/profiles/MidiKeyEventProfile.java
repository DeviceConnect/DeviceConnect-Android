/*
 MidiKeyEventProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteOnMessage;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

import java.util.List;
import java.util.logging.Logger;

/**
 * KeyEvent プロファイルの実装.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiKeyEventProfile extends BaseMidiOutputProfile {

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("midi-plugin");

    /**
     * キーイベントのキータイプ定義.
     */
    private static final int KEY_TYPE_USER = 0x00008000;

    public MidiKeyEventProfile() {

        // GET /gotapi/keyEvent/onDown
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onEventCacheRequest(KeyDownEvent.class, response);
            }
        });

        // PUT /gotapi/keyEvent/onDown
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onAddEventRequest(request, response);
            }
        });

        // DELETE /gotapi/keyEvent/onDown
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onRemoveEventRequest(request, response);
            }
        });

        // GET /gotapi/keyEvent/onKeyChange
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onEventCacheRequest(KeyChangeEvent.class, response);
            }
        });

        // PUT /gotapi/keyEvent/onKeyChange
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onAddEventRequest(request, response);
            }
        });

        // DELETE /gotapi/keyEvent/onKeyChange
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onRemoveEventRequest(request, response);
            }
        });

        // GET /gotapi/keyEvent/onUp
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onUp";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onEventCacheRequest(KeyUpEvent.class, response);
            }
        });

        // PUT /gotapi/keyEvent/onUp
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onUp";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onAddEventRequest(request, response);
            }
        });

        // DELETE /gotapi/keyEvent/onUp
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onUp";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onRemoveEventRequest(request, response);
            }
        });

    }

    @Override
    public String getProfileName() {
        return "keyEvent";
    }

    @Override
    public void convertMessageToEvent(final int port, final @NonNull MidiMessage message, final long timestamp, final @NonNull List<MessageEvent> results) {
        if (message instanceof NoteMessage) {
            int channel = port * ((NoteMessage) message).getChannelNumber();
            int noteNumber = ((NoteMessage) message).getNoteNumber();
            boolean isOn = message instanceof NoteOnMessage;
            int keyId = createKeyId(channel, noteNumber);

            mLogger.info("KeyEvent Event: noteNumber=" + noteNumber + ", keyId=" + keyId);

            results.add(new KeyChangeEvent(timestamp, keyId, isOn));
            if (isOn) {
                results.add(new KeyDownEvent(timestamp, keyId));
            } else {
                results.add(new KeyUpEvent(timestamp, keyId));
            }
        }
    }

    private static int createKeyId(final int channel, final int noteNumber) {
        return KEY_TYPE_USER + (channel * 0x7F) + noteNumber;
    }

    private abstract class KeyEvent extends MessageEvent {
        final int mId;

        KeyEvent(final long timestamp, final int id) {
            super(timestamp);
            mId = id;
        }

        int getId() {
            return mId;
        }

        @Override
        void putExtras(final Intent intent) {
            Bundle keyEvent = new Bundle();
            keyEvent.putInt("id", getId());
            keyEvent.putString("config", "");
            intent.putExtra("keyevent", keyEvent);
        }

        @NonNull
        @Override
        String getProfile() {
            return getProfileName();
        }

        @Override
        String getInterface() {
            return null;
        }
    }

    private class KeyChangeEvent extends KeyEvent {
        static final String STATE_UP = "up";
        static final String STATE_DOWN = "down";

        final String mState;

        KeyChangeEvent(final long timestamp, final int id, final String state) {
            super(timestamp, id);
            mState = state;
        }

        KeyChangeEvent(final long timestamp, final int id, final boolean isDown) {
            this(timestamp, id, isDown ? STATE_DOWN : STATE_UP);
        }

        String getState() {
            return mState;
        }

        @Override
        void putExtras(final Intent intent) {
            Bundle keyEvent = new Bundle();
            keyEvent.putInt("id", getId());
            keyEvent.putString("state", getState());
            keyEvent.putString("config", "");
            intent.putExtra("keyevent", keyEvent);
        }

        @Override
        String getAttribute() {
            return "onKeyChange";
        }
    }

    private class KeyDownEvent extends KeyEvent {

        KeyDownEvent(long timestamp, int id) {
            super(timestamp, id);
        }

        @Override
        String getAttribute() {
            return "onDown";
        }
    }

    private class KeyUpEvent extends KeyEvent {

        KeyUpEvent(long timestamp, int id) {
            super(timestamp, id);
        }

        @Override
        String getAttribute() {
            return "onUp";
        }
    }
}