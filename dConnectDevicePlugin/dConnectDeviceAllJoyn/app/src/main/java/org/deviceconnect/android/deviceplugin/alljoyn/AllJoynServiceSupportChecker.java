package org.deviceconnect.android.deviceplugin.alljoyn;

import android.support.annotation.NonNull;

import org.alljoyn.services.common.BusObjectDescription;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A utility class to check support AllJoyn services.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynServiceSupportChecker {

    private AllJoynServiceSupportChecker() {

    }

    public static List<String> getSupportedDCProfiles(@NonNull DConnectProfileProvider provider,
                                                      @NonNull AllJoynServiceEntity service) {
        BusObjectDescription[] busObjects = service.proxyObjects;
        List<String> interfaces = new LinkedList<>();
        for (BusObjectDescription busObject : busObjects) {
            Collections.addAll(interfaces, busObject.interfaces);
        }

        List<String> supportedProfiles = new LinkedList<>();

        for (DConnectProfile profile : provider.getProfileList()) {
            // Prerequisite profiles.
            if (profile instanceof AuthorizationProfile
                    || profile instanceof ServiceDiscoveryProfile
                    || profile instanceof SystemProfile
                    || profile instanceof ServiceInformationProfile) {
                supportedProfiles.add(profile.getProfileName());
            }
            // Optional profiles
            else if (profile instanceof LightProfile) {
                if (interfaces.containsAll(AllJoynConstants.LAMP_CONTROLLER_INTERFACE_SET.interfaces) ||
                        interfaces.containsAll(AllJoynConstants.SINGLE_LAMP_INTERFACE_SET.interfaces)) {
                    supportedProfiles.add(profile.getProfileName());
                }
            }
        }

        return supportedProfiles;
    }

//    public static List<AllJoynServiceInterfaceSet> getSupportedAJInterfaceSets() {
//
//    }

    public static boolean isSupported(@NonNull BusObjectDescription[] busObjects) {
        List<String> interfaces = new LinkedList<>();
        for (BusObjectDescription busObject : busObjects) {
            Collections.addAll(interfaces, busObject.interfaces);
        }

        // Each supported AllJoyn interface set represents a collection of required AllJoyn
        // interfaces to realize a certain DeviceConnect profile (e.g. AllJoyn Lamp service
        // interfaces are required for the DeviceConnect Light profile).
        // If AllJoyn bus object in question contains any of supported interface sets, then
        // assumedly this bus object is able to become a DeviceConect service.
        for (AllJoynServiceInterfaceSet supportedInterfaceSet :
                AllJoynConstants.SUPPORTED_INTERFACE_SETS) {
            if (interfaces.containsAll(supportedInterfaceSet.interfaces)) {
                return true;
            }
        }
        return false;
    }


}
