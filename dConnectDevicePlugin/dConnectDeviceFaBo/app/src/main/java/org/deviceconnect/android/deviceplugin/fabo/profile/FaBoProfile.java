package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileDataUtil;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield.PIN_NO_A0;

public class FaBoProfile extends DConnectProfile {

    public FaBoProfile() {
        // GET /gotapi/fabo/service
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "service";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                List<Bundle> services = new ArrayList<>();

                for (DConnectService service : getFaBoDeviceService().getServiceProvider().getServiceList()) {
                    if (service instanceof VirtualService) {
                        ServiceData serviceData = ((VirtualService) service).getServiceData();

                        Bundle serviceBundle = new Bundle();
                        serviceBundle.putString("vid", serviceData.getServiceId());
                        serviceBundle.putString("name", serviceData.getName());

                        List<Bundle> profiles = new ArrayList<>();
                        for (ProfileData p : serviceData.getProfileDataList()) {
                            profiles.add(createProfileData(p));
                        }
                        serviceBundle.putParcelableArray("profiles", profiles.toArray(new Bundle[profiles.size()]));

                        services.add(serviceBundle);
                    }
                }

                response.putExtra("services", services.toArray(new Bundle[services.size()]));
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // POST /gotapi/fabo/service
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "service";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String name = request.getStringExtra("name");

                ServiceData serviceData = new ServiceData();
                serviceData.setName(name);

                VirtualService s = getFaBoDeviceService().addServiceData(serviceData);
                if (s != null) {
                    response.putExtra("vid", s.getServiceData().getServiceId());
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response, "Failed to create a virtual service.");
                }

                return true;
            }
        });

        // PUT /gotapi/fabo/service
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "service";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String name = request.getStringExtra("name");
                String vid = request.getStringExtra("vid");

                ServiceData serviceData = getFaBoDeviceService().getServiceData(vid);
                if (serviceData == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the virtual service. vid=" + vid);
                } else {
                    serviceData.setName(name);

                    VirtualService s = getFaBoDeviceService().updateServiceData(serviceData);
                    if (s != null) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setUnknownError(response, "Failed to create a virtual service.");
                    }
                }

                return true;
            }
        });

        // DELETE /gotapi/fabo/service
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "service";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String vid = request.getStringExtra("vid");

                ServiceData serviceData = getFaBoDeviceService().getServiceData(vid);
                if (serviceData == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the virtual service. vid=" + vid);
                } else {
                    getFaBoDeviceService().removeServiceData(serviceData);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // GET /gotapi/fabo/profile
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "profile";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String vid = request.getStringExtra("vid");

                ServiceData serviceData = getFaBoDeviceService().getServiceData(vid);
                if (serviceData == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the virtual service. vid=" + vid);
                } else {
                    List<Bundle> profiles = new ArrayList<>();
                    for (ProfileData p : serviceData.getProfileDataList()) {
                        profiles.add(createProfileData(p));
                    }
                    response.putExtra("profiles", profiles.toArray(new Bundle[profiles.size()]));
                    setResult(response, DConnectMessage.RESULT_OK);
                }

                return true;
            }
        });

        // POST /gotapi/fabo/profile
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "profile";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String vid = request.getStringExtra("vid");
                Integer type = parseInteger(request, "type");
                String pins = request.getStringExtra("pins");

                ProfileData.Type profileType = ProfileData.Type.getType(type);
                ServiceData serviceData = getFaBoDeviceService().getServiceData(vid);
                List<Integer> pinList = toPinList(pins);
                if (serviceData == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the virtual service.");
                } else if (pinList == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Format of pins is invalid.");
                } else if (profileType == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the profile type.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && pinList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "For GPIO, pins is required.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && !ProfileDataUtil.isMultiChoicePin(profileType) && pinList.size() > 1) {
                    MessageUtils.setInvalidRequestParameterError(response, "There is only one pin.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && usedPin(serviceData, profileType, pinList)) {
                    MessageUtils.setInvalidRequestParameterError(response, "pins already used in the " + serviceData.getName());
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && !isPinsSupported(pinList)) {
                    MessageUtils.setNotSupportAttributeError(response, "pins contains unsupported PIN.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && !checkPinType(profileType, pinList)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Pins that can not be used are included.");
                } else if (containsProfile(serviceData, profileType)) {
                    MessageUtils.setInvalidRequestParameterError(response, "This service already has the same profile.");
                } else {
                    ProfileData p = new ProfileData();
                    p.setServiceId(vid);
                    p.setType(profileType);
                    p.setPinList(pinList);

                    serviceData.addProfileData(p);

                    VirtualService s = getFaBoDeviceService().updateServiceData(serviceData);
                    if (s != null) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setUnknownError(response, "Failed to update the service data.");
                    }
                }

                return true;
            }
        });

        // PUT /gotapi/fabo/profile
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "profile";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String vid = request.getStringExtra("vid");
                Integer type = parseInteger(request, "type");
                String pins = request.getStringExtra("pins");

                ProfileData.Type profileType = ProfileData.Type.getType(type);
                ServiceData serviceData = getFaBoDeviceService().getServiceData(vid);
                List<Integer> pinList = toPinList(pins);
                if (serviceData == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the virtual service.");
                } else if (pinList == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Format of pins is invalid.");
                } else if (profileType == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the profile type.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && pinList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "For GPIO, pins is required.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && !ProfileDataUtil.isMultiChoicePin(profileType) && pinList.size() > 1) {
                    MessageUtils.setInvalidRequestParameterError(response, "There is only one pin.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && usedPin(serviceData, profileType, pinList)) {
                    MessageUtils.setInvalidRequestParameterError(response, "pins already used in the " + serviceData.getName());
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && !isPinsSupported(pinList)) {
                    MessageUtils.setNotSupportAttributeError(response, "pins contains unsupported PIN.");
                } else if (profileType.getCategory() == ProfileData.Category.GPIO && !checkPinType(profileType, pinList)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Pins that can not be used are included.");
                } else if (!isSameProfile(serviceData, profileType) && containsProfile(serviceData, profileType)) {
                    MessageUtils.setInvalidRequestParameterError(response, "This service already has the same profile.");
                } else {
                    ProfileData p = new ProfileData();
                    p.setServiceId(vid);
                    p.setType(profileType);
                    p.setPinList(pinList);

                    serviceData.addProfileData(p);

                    VirtualService s = getFaBoDeviceService().updateServiceData(serviceData);
                    if (s != null) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setUnknownError(response, "Failed to update the service data.");
                    }
                }

                return true;
            }
        });

        // DELETE /gotapi/fabo/profile
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "profile";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String vid = request.getStringExtra("vid");
                Integer type = parseInteger(request, "type");

                ProfileData.Type profileType = ProfileData.Type.getType(type);
                ServiceData serviceData = getFaBoDeviceService().getServiceData(vid);
                if (serviceData == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the virtual service.");
                } else if (profileType == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not found the profile type.");
                } else {
                    ProfileData profileData = serviceData.getProfileData(profileType);
                    if (profileData == null) {
                        MessageUtils.setInvalidRequestParameterError(response, "Virtual service has not the " + type);
                    } else {
                        serviceData.removeProfileData(profileData);

                        VirtualService s = getFaBoDeviceService().updateServiceData(serviceData);
                        if (s != null) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setUnknownError(response, "Failed to update the service data.");
                        }
                    }
                }

                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "fabo";
    }

    /**
     * 指定されたサービスデータが指定されたプロファイルデータを持っているか確認します.
     * <p>
     * Brickが異なっても同じプロファイルの場合には同じプロファイルとしてみなす。
     * </p>
     * @param serviceData サービスデータ
     * @param type プロファイルタイプ
     * @return プロファイルデータを持っている場合はtrue、それ以外はfalse
     */
    private boolean containsProfile(final ServiceData serviceData, final ProfileData.Type type) {
        for (ProfileData profileData : serviceData.getProfileDataList()) {
            if (profileData.getType().getProfileName().equals(type.getProfileName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 同じプロファイルを持っているか確認を行います.
     * <p>
     * Brickとプロファイル療法が同じ場合に同じプロファイルとみなす。
     * </p>
     * @param serviceData サービスデータ
     * @param type プロファイルタイプ
     * @return 同じプロファイルを持つ場合はtrue、それ以外はfalse
     */
    private boolean isSameProfile(final ServiceData serviceData, final ProfileData.Type type) {
        for (ProfileData profileData : serviceData.getProfileDataList()) {
            if (profileData.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * List<Integer>String[]に変換します.
     * <p>
     * 引数にnullが指定された場合は、空の配列を返却します。
     * </p>
     * @param list 変換前のリスト
     * @return 変換後int[]
     */
    private String[] toArray(final List<Integer> list) {
        if (list == null) {
            return new String[0];
        }
        String[] b = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            b[i] = FaBoShield.Pin.getPin(list.get(i)).getPinNames()[1];
        }
        return b;
    }

    /**
     * プロファイルデータ用のBundleを作成します.
     * @param p プロファイルデータ
     * @return プロファイルデータを格納したBundle
     */
    private Bundle createProfileData(final ProfileData p) {
        Bundle b = new Bundle();
        b.putInt("type", p.getType().getValue());
        b.putString("name", ProfileDataUtil.getProfileName(getContext(), p.getType()));
        b.putString("brick", p.getType().getBrick());
        b.putStringArray("pins", toArray(p.getPinList()));
        b.putString("category", p.getType().getCategory().getValue());
        return  b;
    }

    /**
     * カンマ区切りで送られてきた文字列をListに変換します.
     * @param pins ピンのデータ
     * @return ピンのデータ
     */
    private List<Integer> toPinList(final String pins) {
        if (pins == null || pins.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> pinList = new ArrayList<>();
        String[] split = pins.split(",");
        for (String s : split) {
            try {
                FaBoShield.Pin pin = FaBoShield.Pin.getPin(s);
                if (pin != null) {
                    if (!pinList.contains(pin.getPinNumber())) {
                        pinList.add(pin.getPinNumber());
                    }
                } else {
                    Integer i = Integer.parseInt(s);
                    if (isPin(i) && !pinList.contains(i)) {
                        pinList.add(i);
                    } else {
                        return null;
                    }
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return pinList;
    }

    /**
     * 指定された番号がピンに存在するか確認します.
     * @param pin ピン番号
     * @return ピンが存在する場合はtrue、それ以外はfalse
     */
    private boolean isPin(final int pin) {
        return FaBoShield.Pin.getPin(pin) != null;
    }

    /**
     * 指定されたピンのリストがサポートされているか確認します.
     * @param pins 確認するピンのリスト
     * @return 全てのピンがサポートされている場合はtrue、それ以外はfalse
     */
    private boolean isPinsSupported(final List<Integer> pins) {
        for (int pin : pins) {
            if (!isPinSupported(pin)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定されたピンがサポートされているか確認します.
     * @param pinNum 確認するピン番号
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    private boolean isPinSupported(final int pinNum) {
        FaBoShield.Pin pin = FaBoShield.Pin.getPin(pinNum);
        return pin != null && getFaBoDeviceService().getFaBoDeviceControl().isPinSupported(pin);
    }

    /**
     * ピンのタイプを確認します.
     * @param type ピンのタイプ
     * @param pins ピン
     * @return ピンのタイプが合って入ればtrue、それ以外はfalse
     */
    private boolean checkPinType(final ProfileData.Type type, final List<Integer> pins) {
        ProfileDataUtil.PinType pinType = ProfileDataUtil.getPinType(type);
        for (int pin : pins) {
            switch (pinType) {
                case ANALOG:
                    if (pin < PIN_NO_A0) {
                        return false;
                    }
                    break;
                case DIGITAL:
                    if (pin >= PIN_NO_A0) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * 指定されたピンが仮想サービスで使用されているか確認を行います。
     * @param serviceData 仮想サービスデータ
     * @param type プロファイルのタイプ
     * @param pins 確認を行うピンのリスト
     * @return 既に使用されている場合はtrue、それ以外はfalse
     */
    private boolean usedPin(final ServiceData serviceData, final ProfileData.Type type, final List<Integer> pins) {
        ProfileData profileData = serviceData.getProfileData(type);
        for (Integer p : pins) {
            if ((profileData == null || !profileData.usedPin(p)) && serviceData.usedPin(p)) {
                return true;
            }
        }
        return false;
    }

    private FaBoDeviceService getFaBoDeviceService() {
        return (FaBoDeviceService) getContext();
    }
}
