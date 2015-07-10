/* Generated from Lamp service introspection XML */

package org.allseen.LSF;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;
import org.alljoyn.bus.annotation.BusSignal;

import java.util.HashMap;

/*
 * The BusInterface annotation is used to tell the code this interface is an AllJoyn
 * interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.allseen.LSF.LampState")
public interface LampState {

    /*
     * The BusMethod annotation signifies this function should be used as part of the AllJoyn
     * interface. The runtime is smart enough to figure out what the input and output of the
     * method is based on the input/output arguments of the method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusMethod(name = "TransitionLampState", signature = "ta{sv}u", replySignature = "u")
    int transitionLampState(long Timestamp, HashMap<String, Variant> NewState, int TransitionPeriod) throws BusException;

    @BusMethod(name = "ApplyPulseEffect", signature = "a{sv}a{sv}uuut", replySignature = "u")
    int applyPulseEffect(HashMap<String, Variant> FromState, HashMap<String, Variant> ToState, int period, int duration, int numPulses, long timestamp) throws BusException;

    /*
     * The BusProperty annotation signifies this property should be used as part of the
     * AllJoyn interface. The runtime is smart enough to figure out what the input and output of
     * the property is based on the input/output arguments of the property.
     *
     * All properties that use the BusProperty annotation can throw a BusException and should
     * indicate this fact.
     */
    @BusProperty(name = "Version", signature = "u")
    int getVersion() throws BusException;

    @BusProperty(name = "OnOff", signature = "b")
    void setOnOff(boolean in_value) throws BusException;

    @BusProperty(name = "OnOff", signature = "b")
    boolean getOnOff() throws BusException;

    @BusProperty(name = "Hue", signature = "u")
    void setHue(int in_value) throws BusException;

    @BusProperty(name = "Hue", signature = "u")
    int getHue() throws BusException;

    @BusProperty(name = "Saturation", signature = "u")
    void setSaturation(int in_value) throws BusException;

    @BusProperty(name = "Saturation", signature = "u")
    int getSaturation() throws BusException;

    @BusProperty(name = "ColorTemp", signature = "u")
    void setColorTemp(int in_value) throws BusException;

    @BusProperty(name = "ColorTemp", signature = "u")
    int getColorTemp() throws BusException;

    @BusProperty(name = "Brightness", signature = "u")
    void setBrightness(int in_value) throws BusException;

    @BusProperty(name = "Brightness", signature = "u")
    int getBrightness() throws BusException;

    /*
     * The BusSignal annotation signifies this signal should be used as part of the
     * AllJoyn interface.
     *
     * All signals that use the BusSignal annotation can throw a BusException and should
     * indicate this fact.
     */
    @BusSignal(name = "LampStateChanged", replySignature = "s")
    void lampStateChanged(String LampID) throws BusException;
}
