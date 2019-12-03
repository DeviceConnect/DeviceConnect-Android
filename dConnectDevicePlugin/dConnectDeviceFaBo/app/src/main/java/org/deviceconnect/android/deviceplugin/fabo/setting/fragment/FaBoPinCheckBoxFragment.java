package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.fabo.core.R;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用するピンを複数選択するためのフラグメント.
 */
public class FaBoPinCheckBoxFragment extends FaBoBasePinFragment {

    /**
     * ピン情報を格納するアダプタ.
     * <p>
     * FaBoShield.Pin.getPinNumber()の値を保持します.
     * </p>
     */
    private MultiSelectPinAdapter mPinAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fabo_pin_list, container, false);

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
                pins.add(((FaBoShield.Pin) mPinAdapter.getItem(i)).getPinNumber());
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
        private List<FaBoShield.Pin> mPinList;

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

            FaBoShield.Pin pin = (FaBoShield.Pin) getItem(position);

            CheckBox checkBox = convertView.findViewById(R.id.item_fabo_check_box_pin);
            checkBox.setText(pin.getPinNames()[1]);
            checkBox.setChecked(mCheckedFlag.get(position));
            checkBox.setEnabled(!usedPin(pin.getPinNumber()));
            // CheckBox#setOnCheckedChangeListenerを使用するとListViewで
            // スクロールした時点でヘックが外れてしまう。
            // ここでは、その問題を回避するためにOnClickListenerを使用します。
            checkBox.setOnClickListener((view) -> {
                boolean isChecked = ((CheckBox) view).isChecked();
                mCheckedFlag.set(position, isChecked);
            });

            return convertView;
        }
    }
}
