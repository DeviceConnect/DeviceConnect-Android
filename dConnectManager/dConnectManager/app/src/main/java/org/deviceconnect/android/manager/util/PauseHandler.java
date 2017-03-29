/*
 PauseHandler.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import android.os.Handler;
import android.os.Message;

import java.util.Vector;

public abstract class PauseHandler extends Handler {

    /**
     * Message Queue Buffer
     */
    private final Vector<Message> mMessageQueueBuffer = new Vector<>();

    /**
     * Flag indicating the pause state
     */
    private boolean mPaused;

    /**
     * Resume the handler
     */
    public final void resume() {
        mPaused = false;

        while (mMessageQueueBuffer.size() > 0) {
            final Message msg = mMessageQueueBuffer.elementAt(0);
            mMessageQueueBuffer.removeElementAt(0);
            sendMessage(msg);
        }
    }

    /**
     * Pause the handler
     */
    public final void pause() {
        mPaused = true;
    }

    /**
     * Notification that the message is about to be stored as the activity is
     * mPaused. If not handled the message will be saved and replayed when the
     * activity resumes.
     *
     * @param message
     *            the message which optional can be handled
     * @return true if the message is to be stored
     */
    protected abstract boolean storeMessage(final Message message);

    /**
     * Notification message to be processed. This will either be directly from
     * handleMessage or played back from a saved message when the activity was
     * mPaused.
     *
     * @param message
     *            the message to be handled
     */
    protected abstract void processMessage(final Message message);

    @Override
    public final void handleMessage(final Message msg) {
        if (mPaused) {
            if (storeMessage(msg)) {
                Message msgCopy = new Message();
                msgCopy.copyFrom(msg);
                mMessageQueueBuffer.add(msgCopy);
            }
        } else {
            processMessage(msg);
        }
    }
}
