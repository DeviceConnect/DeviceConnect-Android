package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用するピンを複数選択するためのフラグメント.
 */
public class FaBoPinCheckBoxFragment extends FaBoBasePinFragment {

    /**
     * ピン情報を格納するアダプタ.
     * <p>
     * ArduinoUno.Pin.getPinNumber()の値を保持します.
     * </p>
     */
    private MultiSelectPinAdapter mPinAdapter;

    /**
     * プロファイルのタイプ.
     * <p>
     * ProfileData.Typeの値.
     * </p>
     */
    private ProfileData mProfileData;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fabo_pin_list, container, false);

        mProfileData = getActivity().getIntent().getParcelableExtra("profile");

        mPinAdapter = new MultiSelectPinAdapter();

        ListView listView = (ListView) view.findViewById(R.id.activity_fabo_pin_list_view);
        listView.setAdapter(mPinAdapter);

        return view;
    }

    @Override
    public List<Integer> getSelectedPins() {
        ArrayList<Integer> pins = new ArrayList<>();
        for (int i = 0; i < mPinAdapter.getCount(); i++) {
            if (mPinAdapter.mCheckedFlag.get(i)) {
                pins.add(((ArduinoUno.Pin) mPinAdapter.getItem(i)).getPinNumber());
            }
        }
        return pins;
    }

    /**
     * 複数のピン選択するためのデータを格納するアダプタ.
     */
    private class MultiSelectPinAdapter extends BaseAdapter {

        /**
         * 選択状態を保持するリスト.
         */
        private List<Boolean> mCheckedFlag = new ArrayList<>();

        /**
         * ピンのリスト.
         */
        private List<ArduinoUno.Pin> mPinList;

        /**
         * コンストラクタ.
         */
        MultiSelectPinAdapter() {
            mPinList = getCanUsePinList();
            for (int i = 0; i < getCount(); i++) {
                mCheckedFlag.add(containsPin(i));
            }
        }

        @Override
        public int getCount() {
            return mPinList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mPinList.get(position);
        }

        @Override
        public long getItemId(final int id) {
            return id;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_fabo_check_box_pin, null);
            }

            ArduinoUno.Pin pin = (ArduinoUno.Pin) getItem(position);

            CheckBox nameTV = (CheckBox) convertView.findViewById(R.id.item_fabo_check_box_pin);
            nameTV.setText(pin.getPinNames()[1]);
            nameTV.setChecked(mCheckedFlag.get(position));
            // CheckBox#setOnCheckedChangeListenerを使用するとListViewで
            // スクロールした時点でヘックが外れてしまう。
            // ここでは、その問題を回避するためにOnClickListenerを使用します。
            nameTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = ((CheckBox) view).isChecked();
                    mCheckedFlag.set(position, isChecked);
                }
            });

            return convertView;
        }

        /**
         * 指定されたピンがmProfileDataに含まれているか確認します.
         * @param pin ピン
         * @return 含まれている場合はtrue、それ以外はfalse
         */
        private boolean containsPin(final int pin) {
            if (mProfileData == null) {
                return false;
            }
            for (int pinNum : mProfileData.getPinList()) {
                if (pin == pinNum) {
                    return true;
                }
            }
            return false;
        }
    }
}
