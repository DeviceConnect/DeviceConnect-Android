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
@BusInterface(name = "org.allseen.LSF.LampParameters")
public interface LampParameters {

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
    @BusProperty (name = "Energy_Usage_Milliwatts", signature = "u")
    int getEnergy_Usage_Milliwatts() throws BusException;
    @BusProperty (name = "Brightness_Lumens", signature = "u")
    int getBrightness_Lumens() throws BusException;
}
