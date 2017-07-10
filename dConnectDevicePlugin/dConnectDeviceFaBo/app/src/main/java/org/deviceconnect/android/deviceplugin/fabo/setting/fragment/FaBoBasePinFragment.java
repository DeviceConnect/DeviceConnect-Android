package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.app.Fragment;

import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileDataUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ピンを選択するための画面を作成するための基底フラグメント.
 */
public abstract class FaBoBasePinFragment extends Fragment {

    /**
     * 使用できるピンのリストを取得します.
     *
     * @return ピンのリスト
     */
    List<FaBoShield.Pin> getCanUsePinList() {
        List<FaBoShield.Pin> names = new ArrayList<>();
        for (FaBoShield.Pin pin : FaBoShield.Pin.values()) {
            if (checkCanUsePin(pin)) {
                names.add(pin);
            }
        }
        return names;
    }

    /**
     * 指定されたピンが使用できるか確認します.
     * @param pin 確認するピン
     * @return 使用できる場合はtrue、それ以外はfalse
     */
    private boolean checkCanUsePin(final FaBoShield.Pin pin) {
        ProfileDataUtil.PinType type = getPinType();
        if (type == ProfileDataUtil.PinType.ANALOG && pin.getPinNumber() > FaBoShield.PIN_NO_D13) {
            return true;
        } else if (type == ProfileDataUtil.PinType.DIGITAL && pin.getPinNumber() <= FaBoShield.PIN_NO_D13) {
            return true;
        }
        return type == ProfileDataUtil.PinType.ALL;
    }

    /**
     * 使用できるピンのタイプを取得します.
     *
     * @return ピンのタイプ
     */
    private ProfileDataUtil.PinType getPinType() {
        ProfileData p = getActivity().getIntent().getParcelableExtra("profile");
        return ProfileDataUtil.getPinType(p);
    }


    /**
     * 指定されたピンがmProfileDataに含まれているか確認します.
     * @param pin ピン
     * @return 含まれている場合はtrue、それ以外はfalse
     */
    protected boolean containsPin(final int pin) {
        ProfileData profileData = getActivity().getIntent().getParcelableExtra("profile");
        if (profileData == null) {
            return false;
        }
        for (int pinNum : profileData.getPinList()) {
            if (pin == pinNum) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定されたピンが使用されているかを確認します.
     * @param pin 使用されているか確認をするピン
     * @return 使用されている場合はtrue、それ以外はfalse
     */
    protected boolean usedPin(final int pin) {
        if (containsPin(pin)) {
            return false;
        }
        ArrayList<Integer> pins = getActivity().getIntent().getIntegerArrayListExtra("pins");
        for (int p : pins) {
            if (p == pin) {
                return true;
            }
        }
        return false;
    }

    /**
     * 選択されているピンのリストを取得します.
     *
     * @return 選択されているピンのリスト
     */
    public abstract List<Integer> getSelectedPins();
}
