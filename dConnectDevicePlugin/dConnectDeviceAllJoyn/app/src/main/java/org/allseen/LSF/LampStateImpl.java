/* Generated from Lamp service introspection XML */

package org.allseen.LSF;

import java.util.HashMap;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.annotation.BusSignalHandler;

import android.os.Handler;

/* The AllJoyn service implementation of org.allseen.LSF.LampState. */
public class LampStateImpl implements LampState, BusObject {
    private Handler mHandler;   /* Means by which to send message to the user interface. */
    private int mMessageId;     /* Identifier to be used for this interface instance in UI messages. */

    /*
     * Constructor for the LampStateImpl class.
     * h is used to send messages sent back to the user interface.
     * messageId, is the identifier to use for sending message from this interface instance.
     */
    public LampStateImpl(Handler h, int messageId)
    {
        mHandler = h;
        mMessageId = messageId;
        sendUiMessage("Created an instance of LampStateImp.");
    }

    /*
     * Intentionally empty implementation of the method since the true
     * implementation is on the server side.
     */
    public int transitionLampState(long Timestamp, HashMap<String, Variant> NewState, int TransitionPeriod) {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the method since the true
     * implementation is on the server side.
     */
    public int applyPulseEffect(HashMap<String, Variant> FromState, HashMap<String, Variant> ToState, int period, int duration, int numPulses, long timestamp) {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public int getVersion() {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public void setOnOff(boolean in_value) {
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public boolean getOnOff() {
        boolean returnValue = false;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public void setHue(int in_value) {
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public int getHue() {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public void setSaturation(int in_value) {
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public int getSaturation() {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public void setColorTemp(int in_value) {
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public int getColorTemp() {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public void setBrightness(int in_value) {
    }

    /*
     * Intentionally empty implementation of the property since the true
     * implementation is on the server side.
     */
    public int getBrightness() {
        int returnValue = 0;

        return returnValue;
    }

    /*
     * This receives the signal from the emitter at the service.
     */
    @BusSignalHandler(iface = "org.allseen.LSF.LampState", signal = "LampStateChanged")
    public void lampStateChanged(String LampID) {
        sendUiMessage("Signal org.allseen.LSF.LampState::LampStateChanged() received.");

        String ajcgMessageString;

        ajcgMessageString = String.format("LampID = '%s'", LampID);

        sendUiMessage(ajcgMessageString);
    }

    /* Helper function to send a message to the UI thread. */
    private void sendUiMessage(String message) {
        mHandler.sendMessage(mHandler.obtainMessage(mMessageId, message));
    }
}
