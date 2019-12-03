package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.deviceconnect.android.deviceplugin.fabo.core.R;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用するピンを1つだけ選択するためのフラグメント.
 */
public class FaBoPinRadioGroupFragment extends FaBoBasePinFragment {

    /**
     * 選択されたPin.
     */
    private FaBoShield.Pin mSelectedPin;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fabo_pin_group, container, false);

        List<FaBoShield.Pin> pins = getCanUsePinList();

        RadioGroup group =  view.findViewById(R.id.activity_fabo_pin_layout);
        for (FaBoShield.Pin pin : pins) {
            group.addView(createRadioButton(inflater, pin),
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        for (FaBoShield.Pin pin : pins) {
            if (containsPin(pin.getPinNumber())) {
                RadioButton radioButton = group.findViewWithTag(pin);
                radioButton.setChecked(true);
                mSelectedPin = pin;
            }
        }

        return view;
    }

    /**
     * ピンを選択するためのRadioButtonを作成します.
     * @param inflater インフレータ
     * @param pin ピン
     * @return RadioButtonのインスタンス
     */
    private RadioButton createRadioButton(final LayoutInflater inflater,final FaBoShield.Pin pin) {
        RadioButton radio = (RadioButton) inflater.inflate(R.layout.item_fabo_radio_button_pin, null, false);
        radio.setText(pin.getPinNames()[1]);
        radio.setTag(pin);
        radio.setEnabled(!usedPin(pin.getPinNumber()));
        radio.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                mSelectedPin = pin;
            }
        });
        return radio;
    }

    @Override
    public List<Integer> getSelectedPins() {
        List<Integer> pins = new ArrayList<>();
        if (mSelectedPin != null) {
            pins.add(mSelectedPin.getPinNumber());
        }
        return pins;
    }
}

