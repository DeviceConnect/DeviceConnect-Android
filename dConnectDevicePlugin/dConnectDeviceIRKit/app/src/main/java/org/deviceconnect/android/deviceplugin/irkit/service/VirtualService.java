package org.deviceconnect.android.deviceplugin.irkit.service;


import android.content.Intent;


import org.deviceconnect.android.deviceplugin.irkit.IRKitManager;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitLightProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitPowerProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitTVProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class VirtualService extends DConnectService {

    private final IRKitDBHelper mDBHelper;

    private final DConnectServiceProvider mServiceProvider;

    public VirtualService(final VirtualDeviceData device, final IRKitDBHelper dbHelper,
                          final DConnectServiceProvider provider) {
        super(device.getServiceId());
        mDBHelper = dbHelper;
        mServiceProvider = provider;

        setName(device.getDeviceName());
        setNetworkType(NetworkType.WIFI);

        List<VirtualProfileData> profiles =
            mDBHelper.getVirtualProfiles(device.getServiceId(), null);
        for (VirtualProfileData profile : profiles) {
            if (LightProfile.PROFILE_NAME.equalsIgnoreCase(profile.getProfile())) {
                addProfile(new IRKitLightProfile());
            } else if (IRKitTVProfile.PROFILE_NAME.equalsIgnoreCase(profile.getProfile())) {
                addProfile(new IRKitTVProfile());
                addProfile(new IRKitPowerProfile());
            }
        }
    }

    @Override
    public boolean isOnline() {
        if (!isIRExist()) {
            return false;
        }
        IRKitService ikKit = getIRKitService();
        if (ikKit == null) {
            return false;
        }
        return ikKit.isOnline();
    }

    private IRKitService getIRKitService() {
        for (DConnectService irKit : mServiceProvider.getServiceList()) {
            if (irKit instanceof IRKitService
                && this.getId().startsWith(irKit.getId())) {
                return (IRKitService) irKit;
            }
        }
        return null;
    }
    /**
     * ライト用の赤外線を送信する.
     * @param serviceId サービスID
     * @param method HTTP Method
     * @param uri URI
     * @param response レスポンス
     * @return true:同期　false:非同期
     */
    public boolean sendTVRequest(final String serviceId, final String method, final String uri,
                                        final Intent response) {
        boolean send = true;
        IRKitDBHelper helper = new IRKitDBHelper(getContext());
        List<VirtualProfileData> requests = helper.getVirtualProfiles(serviceId, "TV");
        if (requests.size() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "Invalid ServiceId");
            return send;
        }
        VirtualProfileData vData = null;
        for (VirtualProfileData req : requests) {
            if (req.getUri().equalsIgnoreCase(uri)
                    && req.getMethod().equals(method)
                    && req.getIr() != null) {
                vData = req;
                break;
            }
        }
        if (vData != null) {
            send = sendIR(vData.getIr(), response);
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "IR is not registered for that request");
        }
        return send;
    }
    /**
     * 一つでも赤外線が登録されているかをチェックする.
     * @return true:登録されている, false:登録されていない
     */
    private boolean isIRExist() {
        List<VirtualProfileData> requests = mDBHelper.getVirtualProfiles(getId(), null);
        for (VirtualProfileData request : requests) {
            if (request.getIr() != null && request.getIr().contains("{\"format\":\"raw\",")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 赤外線を送信する.
     * @param message 赤外線
     * @param response レスポンス
     * @return true:同期 false:非同期
     */
    public boolean sendIR(final String message, final Intent response) {
        IRKitService irKit = getIRKitService();
        if (irKit == null) {
            MessageUtils.setIllegalServerStateError(response, "IRKit is disconnected.");
            return true;
        }
        IRKitManager.INSTANCE.sendMessage(irKit.getIp(), message, new IRKitManager.PostMessageCallback() {
            @Override
            public void onPostMessage(boolean result) {
                if (result) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT,  DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                }
                ((DConnectMessageService) getContext()).sendResponse(response);
            }
        });
        return false;
    }


}
