/* Generated from Lamp service introspection XML */

package org.allseen.LSF;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusProperty;

/*
 * The BusInterface annotation is used to tell the code this interface is an AllJoyn
 * interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.allseen.LSF.LampDetails")
public interface LampDetails {

    /*
     * The BusProperty annotation signifies this property should be used as part of the
     * AllJoyn interface. The runtime is smart enough to figure out what the input and output of
     * the property is based on the input/output arguments of the property.
     *
     * All properties that use the BusProperty annotation can throw a BusException and should
     * indicate this fact.
     */
    @BusProperty (name = "Version", signature = "u")
    int getVersion() throws BusException;
    @BusProperty (name = "Make", signature = "u")
    int getMake() throws BusException;
    @BusProperty (name = "Model", signature = "u")
    int getModel() throws BusException;
    @BusProperty (name = "Type", signature = "u")
    int getType() throws BusException;
    @BusProperty (name = "LampType", signature = "u")
    int getLampType() throws BusException;
    @BusProperty (name = "LampBaseType", signature = "u")
    int getLampBaseType() throws BusException;
    @BusProperty (name = "LampBeamAngle", signature = "u")
    int getLampBeamAngle() throws BusException;
    @BusProperty (name = "Dimmable", signature = "b")
    boolean getDimmable() throws BusException;
    @BusProperty (name = "Color", signature = "b")
    boolean getColor() throws BusException;
    @BusProperty (name = "VariableColorTemp", signature = "b")
    boolean getVariableColorTemp() throws BusException;
    @BusProperty (name = "HasEffects", signature = "b")
    boolean getHasEffects() throws BusException;
    @BusProperty (name = "MinVoltage", signature = "u")
    int getMinVoltage() throws BusException;
    @BusProperty (name = "MaxVoltage", signature = "u")
    int getMaxVoltage() throws BusException;
    @BusProperty (name = "Wattage", signature = "u")
    int getWattage() throws BusException;
    @BusProperty (name = "IncandescentEquivalent", signature = "u")
    int getIncandescentEquivalent() throws BusException;
    @BusProperty (name = "MaxLumens", signature = "u")
    int getMaxLumens() throws BusException;
    @BusProperty (name = "MinTemperature", signature = "u")
    int getMinTemperature() throws BusException;
    @BusProperty (name = "MaxTemperature", signature = "u")
    int getMaxTemperature() throws BusException;
    @BusProperty (name = "ColorRenderingIndex", signature = "u")
    int getColorRenderingIndex() throws BusException;
    @BusProperty (name = "LampID", signature = "s")
    String getLampID() throws BusException;
}
