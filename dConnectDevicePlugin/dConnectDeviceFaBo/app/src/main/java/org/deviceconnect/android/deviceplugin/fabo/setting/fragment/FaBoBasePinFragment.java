package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.app.Fragment;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileDataUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class FaBoBasePinFragment extends Fragment {

    /**
     * 使用できるピンのリストを取得します.
     *
     * @return ピンのリスト
     */
    List<ArduinoUno.Pin> getCanUsePinList() {
        List<ArduinoUno.Pin> names = new ArrayList<>();
        for (ArduinoUno.Pin pin : ArduinoUno.Pin.values()) {
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
    private boolean checkCanUsePin(final ArduinoUno.Pin pin) {
        ProfileDataUtil.PinType type = getPinType();
        if (type == ProfileDataUtil.PinType.ANALOG && pin.getPinNumber() > ArduinoUno.PIN_NO_D13) {
            return true;
        } else if (type == ProfileDataUtil.PinType.DIGITAL && pin.getPinNumber() <= ArduinoUno.PIN_NO_D13) {
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
     * 選択されているピンのリストを取得します.
     *
     * @return 選択されているピンのリスト
     */
    public abstract List<Integer> getSelectedPins();
}
