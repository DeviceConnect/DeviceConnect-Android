/*
 Based on Controller Service 14.12 Interface Definition
 */

package org.allseen.LSF.ControllerService;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;
import org.alljoyn.bus.annotation.BusSignal;

/*
 * The BusInterface annotation is used to tell the code this interface is an AllJoyn
 * interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.allseen.LSF.ControllerService")
public interface ControllerService {

    /*
     * The BusMethod annotation signifies this function should be used as part of the AllJoyn
     * interface. The runtime is smart enough to figure out what the input and output of the
     * method is based on the input/output arguments of the method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusMethod(name = "LightingResetControllerService", replySignature = "u")
    int lightingResetControllerService() throws BusException;

    @BusMethod(name = "GetControllerServiceVersion", replySignature = "u")
    int getControllerServiceVersion() throws BusException;

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

    /*
     * The BusSignal annotation signifies this signal should be used as part of the
     * AllJoyn interface.
     *
     * All signals that use the BusSignal annotation can throw a BusException and should
     * indicate this fact.
     */
    @BusSignal(name = "ControllerServiceLightingReset", sessionless = true)
    void controllerServiceLightingReset() throws BusException;
}
