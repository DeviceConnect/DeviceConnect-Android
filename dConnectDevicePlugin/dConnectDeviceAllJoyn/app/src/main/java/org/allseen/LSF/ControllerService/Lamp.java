/*
 Based on Controller Service 14.12 Interface Definition
 */

package org.allseen.LSF.ControllerService;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;
import org.alljoyn.bus.annotation.BusSignal;
import org.alljoyn.bus.annotation.Position;

import java.util.Map;

/*
 * The BusInterface annotation is used to tell the code this interface is an AllJoyn
 * interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.allseen.LSF.ControllerService.Lamp")
public interface Lamp {

    public class BaseReturnValue {
        @Position(0)
        public int responseCode = 0;
        @Position(1)
        public String lampID;
    }

    public class GetAllLampIDs_return_value_uas {
        @Position(0)
        public int responseCode = 0;
        @Position(1)
        public String[] lampIDs;
    }

    public class GetLampSupportedLanguages_return_value_usas extends BaseReturnValue {
        @Position(2)
        public String[] supportedLanguages;
    }

    public class GetLampManufacturer_return_value_usss extends BaseReturnValue {
        @Position(2)
        public String language;
        @Position(3)
        public String manufacturer;
    }

    public class GetLampName_return_value_usss extends BaseReturnValue {
        @Position(2)
        public String language;
        @Position(3)
        public String lampName;
    }

    public class SetLampName_return_value_usss extends BaseReturnValue {
        @Position(2)
        public String language;
    }

    public class GetLampDetails_return_value_usa_sv extends BaseReturnValue {
        @Position(2)
        public Map<String, Variant> lampDetails;
    }

    public class GetLampParameters_return_value_usa_sv extends BaseReturnValue {
        @Position(2)
        public Map<String, Variant> lampParameters;
    }

    public class GetLampParametersField_return_value_usss extends BaseReturnValue {
        @Position(2)
        public String lampParameterFieldName;
        @Position(3)
        public String lampParameterFieldValue;
    }

    public class GetLampState_return_value_usa_sv extends BaseReturnValue {
        @Position(2)
        public Map<String, Variant> lampState;
    }

    public class GetLampStateField_return_value_usss extends BaseReturnValue {
        @Position(2)
        public String lampStateFieldName;
        @Position(3)
        public String lampStateFieldValue;
    }

    public class TransitionLampState_return_value_us extends BaseReturnValue {
    }

    public class ResetLampState_return_value_us extends BaseReturnValue {
    }

    public class ResetLampStateField_return_value_uss extends BaseReturnValue {
        @Position(2)
        public String lampStateFieldName;
    }

    public class GetLampFaults_return_value_usau extends BaseReturnValue {
        @Position(2)
        public int[] lampFaults;
    }

    public class ClearLampFaults_return_value_usu extends BaseReturnValue {
        @Position(2)
        public int lampFault;
    }


    public class GetLampServiceVersion_return_value_usu extends BaseReturnValue {
        @Position(2)
        public int lampServiceVersion;
    }

    /*
     * The BusMethod annotation signifies this function should be used as part of the AllJoyn
     * interface. The runtime is smart enough to figure out what the input and output of the
     * method is based on the input/output arguments of the method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusMethod(name = "GetAllLampIDs", replySignature = "uas")
    GetAllLampIDs_return_value_uas getAllLampIDs() throws BusException;

    @BusMethod(name = "GetLampSupportedLanguages", signature = "s", replySignature = "usas")
    GetLampSupportedLanguages_return_value_usas getLampSupportedLanguages(String lampID) throws BusException;

    @BusMethod(name = "GetLampManufacturer", signature = "ss", replySignature = "usss")
    GetLampManufacturer_return_value_usss getLampManufacturer(String lampID, String language) throws BusException;

    @BusMethod(name = "GetLampName", signature = "ss", replySignature = "usss")
    GetLampName_return_value_usss getLampName(String lampID, String language) throws BusException;

    @BusMethod(name = "SetLampName", signature = "sss", replySignature = "uss")
    SetLampName_return_value_usss setLampName(String lampID, String lampName, String language) throws BusException;

    @BusMethod(name = "GetLampDetails", signature = "s", replySignature = "usa{sv}")
    GetLampDetails_return_value_usa_sv getLampDetails(String lampID) throws BusException;

    @BusMethod(name = "GetLampParameters", signature = "s", replySignature = "usa{sv}")
    GetLampParameters_return_value_usa_sv getLampParameters(String lampID) throws BusException;

    @BusMethod(name = "GetLampParametersField", signature = "ss", replySignature = "usss")
    GetLampParametersField_return_value_usss getLampParametersField(String lampID, String lampParameterFieldName) throws BusException;

    @BusMethod(name = "GetLampState", signature = "s", replySignature = "usa{sv}")
    GetLampState_return_value_usa_sv getLampState(String lampID) throws BusException;

    @BusMethod(name = "GetLampStateField", signature = "ss", replySignature = "usss")
    GetLampStateField_return_value_usss getLampStateField(String lampID, String lampStateFieldName) throws BusException;

    @BusMethod(name = "TransitionLampState", signature = "sa{sv}u", replySignature = "us")
    TransitionLampState_return_value_us transitionLampState(String lampID, Map<String, Variant> lampState, int transitionPeriod) throws BusException;

    @BusMethod(name = "ResetLampState", signature = "s", replySignature = "us")
    ResetLampState_return_value_us resetLampState(String lampID) throws BusException;

    @BusMethod(name = "ResetLampStateField", signature = "ss", replySignature = "uss")
    ResetLampStateField_return_value_uss resetLampStateField(String lampID, String lampStateFieldName) throws BusException;

    @BusMethod(name = "GetLampFaults", signature = "s", replySignature = "usau")
    GetLampFaults_return_value_usau getLampFaults(String lampID) throws BusException;

    @BusMethod(name = "ClearLampFaults", signature = "ss", replySignature = "usu")
    ClearLampFaults_return_value_usu clearLampFaults(String lampID, String lampFault) throws BusException;

    @BusMethod(name = "GetLampServiceVersion", signature = "s", replySignature = "usu")
    GetLampServiceVersion_return_value_usu getLampServiceVersion(String lampID) throws BusException;

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
    @BusSignal(name = "LampNameChanged", replySignature = "ss", sessionless = true)
    void emitLampNameChanged(String lampID, String lampName) throws BusException;

    @BusSignal(name = "LampStateChanged", replySignature = "ss", sessionless = true)
    void emitLampStateChanged(String lampID, String lampName) throws BusException;

    @BusSignal(name = "LampsFound", replySignature = "s", sessionless = true)
    void emitLampsFound(String lampID) throws BusException;

    @BusSignal(name = "LampsLost", replySignature = "as", sessionless = true)
    void emitLampsLost(String[] lampIDs) throws BusException;
}
