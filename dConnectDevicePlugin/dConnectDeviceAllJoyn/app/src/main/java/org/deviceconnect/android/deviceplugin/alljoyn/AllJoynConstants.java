package org.deviceconnect.android.deviceplugin.alljoyn;

/**
 * Constants used in AllJoyn plugin.
 *
 * @author NTT DOCOMO, INC.
 */
public interface AllJoynConstants {

    /**
     * @see <a href="https://allseenalliance.org/developers/learn/base-services/configuration/interface">
     * https://allseenalliance.org/developers/learn/base-services/configuration/interface</a>
     */
    AllJoynServiceInterfaceSet CONFIGURATION_INTERFACE_SET =
            new AllJoynServiceInterfaceSet("Configuration"
                    , "org.alljoyn.Config"
            );

    // AllJoyn Lighting service framework, Lamp service
    AllJoynServiceInterfaceSet SINGLE_LAMP_INTERFACE_SET =
            new AllJoynServiceInterfaceSet("LightingLamp"
                    , "org.allseen.LSF.LampDetails"
                    , "org.allseen.LSF.LampParameters"
                    , "org.allseen.LSF.LampService"
                    , "org.allseen.LSF.LampState"
            );

    // AllJoyn Lighting service framework, Controller Service
    AllJoynServiceInterfaceSet LAMP_CONTROLLER_INTERFACE_SET =
            new AllJoynServiceInterfaceSet("LightingController"
                    , "org.allseen.LSF.ControllerService"
                    , "org.allseen.LSF.ControllerService.Lamp"
//                    , "org.allseen.LSF.ControllerService.LampGroup"
//                    , "org.allseen.LSF.ControllerService.Preset"
//                    , "org.allseen.LSF.ControllerService.Scene"
//                    , "org.allseen.LSF.ControllerService.MasterScene"
//                    , "org.allseen.LeaderElectionAndStateSync"
            );

    AllJoynServiceInterfaceSet[] SUPPORTED_INTERFACE_SETS = new AllJoynServiceInterfaceSet[]{
            CONFIGURATION_INTERFACE_SET
            , SINGLE_LAMP_INTERFACE_SET
            , LAMP_CONTROLLER_INTERFACE_SET
    };

}
