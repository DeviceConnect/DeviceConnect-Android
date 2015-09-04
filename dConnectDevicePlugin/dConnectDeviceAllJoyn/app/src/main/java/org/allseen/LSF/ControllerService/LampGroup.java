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
@BusInterface(name = "org.allseen.LSF.ControllerService.LampGroup")
public interface LampGroup {

    class BaseReturnValue {
        @Position(0)
        public int responseCode = 0;
        @Position(1)
        public String lampGroupID;
    }

    class GetAllLampGroupIDs_return_value_uas {
        @Position(0)
        public int responseCode = 0;
        @Position(1)
        public String[] lampGroupIDs;
    }

    class GetLampGroupName_return_value_usss extends BaseReturnValue {
        @Position(2)
        public String language;
        @Position(3)
        public String lampGroupName;
    }

    class SetLampGroupName_return_value_uss {
        @Position(0)
        public int responseCode = 0;
        @Position(1)
        public String lampID;
        @Position(2)
        public String language;
    }

    class CreateLampGroup_return_value_us extends BaseReturnValue {
    }

    class UpdateLampGroup_return_value_us extends BaseReturnValue {
    }

    class DeleteLampGroup_return_value_us extends BaseReturnValue {
    }

    class GetLampGroup_return_value_usasas extends BaseReturnValue {
        @Position(2)
        public String[] lampID;
        @Position(3)
        public String[] lampGroupIDs;
    }

    class TransitionLampGroupState_return_value_us extends BaseReturnValue {
    }

    class PulseLampGroupWithState_return_value_us extends BaseReturnValue {
    }

    class PulseLampGroupWithPreset_return_value_us extends BaseReturnValue {
    }

    class TransitionLampGroupStateToPreset_return_value_us extends BaseReturnValue {
    }

    class TransitionLampGroupStateField_return_value_uss extends BaseReturnValue {
        @Position(2)
        public String lampGroupStateFieldName;
    }

    class ResetLampGroupState_return_value_us extends BaseReturnValue {
    }

    class ResetLampGroupStateField_return_value_uss extends BaseReturnValue {
        @Position(2)
        public String lampGroupStateFieldName;
    }

    /*
     * The BusMethod annotation signifies this function should be used as part of the AllJoyn
     * interface. The runtime is smart enough to figure out what the input and output of the
     * method is based on the input/output arguments of the method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusMethod(name = "GetAllLampGroupIDs", replySignature = "uas")
    GetAllLampGroupIDs_return_value_uas getAllLampGroupIDs() throws BusException;

    @BusMethod(name = "GetLampGroupName", signature = "ss", replySignature = "usss")
    GetLampGroupName_return_value_usss getLampGroupName(String lampGroupID, String language) throws BusException;

    @BusMethod(name = "SetLampGroupName", signature = "sss", replySignature = "uss")
    SetLampGroupName_return_value_uss setLampGroupName(String lampGroupID, String lampName, String language) throws BusException;

    @BusMethod(name = "CreateLampGroup", signature = "asasss", replySignature = "us")
    CreateLampGroup_return_value_us createLampGroup(String[] lampIDs, String[] lampGroupIDs, String lampGroupName, String language) throws BusException;

    @BusMethod(name = "UpdateLampGroup", signature = "sasas", replySignature = "us")
    UpdateLampGroup_return_value_us updateLampGroup(String lampGroupID, String[] lampIDs, String[] lampGroupIDs) throws BusException;

    @BusMethod(name = "DeleteLampGroup", signature = "s", replySignature = "us")
    DeleteLampGroup_return_value_us deleteLampGroup(String lampGroupID) throws BusException;

    @BusMethod(name = "GetLampGroup", signature = "s", replySignature = "usasas")
    GetLampGroup_return_value_usasas getLampGroup(String lampGroupID) throws BusException;

    @BusMethod(name = "TransitionLampGroupState", signature = "sa{sv}u", replySignature = "us")
    TransitionLampGroupState_return_value_us transitionLampGroupState(String lampGroupID, Map<String, Variant> lampState, int transitionPeriod) throws BusException;

    @BusMethod(name = "PulseLampGroupWithState", signature = "sa{sv}a{sv}uuu", replySignature = "us")
    PulseLampGroupWithState_return_value_us pulseLampGroupWithState(String lampGroupID, Map<String, Variant> fromLampState, Map<String, Variant> toLampState, int period, int duration, int numPulses) throws BusException;

    @BusMethod(name = "PulseLampGroupWithPreset", signature = "suuuuu", replySignature = "us")
    PulseLampGroupWithPreset_return_value_us pulseLampGroupWithPreset(String lampGroupID, int fromPresetID, int toPresetID, int period, int duration, int numPulses) throws BusException;

    @BusMethod(name = "TransitionLampGroupStateToPreset", signature = "suu", replySignature = "us")
    TransitionLampGroupStateToPreset_return_value_us transitionLampGroupStateToPreset(String lampGroupID, int presetID, int transitionPeriod) throws BusException;

    @BusMethod(name = "TransitionLampGroupStateField", signature = "sssu", replySignature = "uss")
    TransitionLampGroupStateField_return_value_uss transitionLampGroupStateField(String lampGroupID, String lampGroupStateFieldName, String lampGroupStateFieldValue, int transitionPeriod) throws BusException;

    @BusMethod(name = "ResetLampGroupState", signature = "s", replySignature = "us")
    ResetLampGroupState_return_value_us resetLampGroupState(String lampGroupID) throws BusException;

    @BusMethod(name = "ResetLampGroupStateField", signature = "ss", replySignature = "uss")
    ResetLampGroupStateField_return_value_uss resetLampGroupStateField(String lampGroupID, String lampGroupStateFieldName) throws BusException;

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
    @BusSignal(name = "LampGroupsNameChanged", replySignature = "as", sessionless = true)
    void lampGroupsNameChanged(String lampGroupsIDs) throws BusException;

    @BusSignal(name = "LampGroupsCreated", replySignature = "as", sessionless = true)
    void lampGroupsCreated(String lampGroupsIDs) throws BusException;

    @BusSignal(name = "LampGroupsUpdated", replySignature = "as", sessionless = true)
    void lampGroupsUpdated(String lampGroupsIDs) throws BusException;

    @BusSignal(name = "LampGroupsDeleted", replySignature = "as", sessionless = true)
    void lampGroupsDeleted(String[] lampGroupsIDs) throws BusException;
}
