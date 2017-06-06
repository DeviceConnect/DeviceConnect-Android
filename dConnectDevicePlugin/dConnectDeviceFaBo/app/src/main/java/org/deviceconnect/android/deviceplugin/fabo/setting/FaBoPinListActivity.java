package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用するピンを選択するための画面を表示するActivity.
 */
public class FaBoPinListActivity extends Activity {

    /**
     * ピン情報を格納するアダプタ.
     * <p>
     * ArduinoUno.Pin.getPinNumber()の値を保持します.
     * </p>
     */
    private PinAdapter mPinAdapter;

    /**
     * プロファイルのタイプ.
     * <p>
     * ProfileData.Typeの値.
     * </p>
     */
    private ProfileData mProfileData;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_pin_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mProfileData = getIntent().getParcelableExtra("profile");

        mPinAdapter = new PinAdapter();

        ListView listView = (ListView) findViewById(R.id.activity_fabo_pin_list_view);
        listView.setAdapter(mPinAdapter);

        Button saveBtn = (Button) findViewById(R.id.activity_fabo_pin_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePinList();
            }
        });

        // バックキーを押下された時を考慮してキャンセルを設定しておく
        setResult(RESULT_CANCELED, new Intent());
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ピンが一つも選択されていない場合にエラーダイアログを表示します.
     */
    private void showNoSelectPin() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_error_title)
                .setMessage(R.string.activity_fabo_virtual_service_pin_no_select_message)
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok, null)
                .show();
    }

    /**
     * プロファイルで使用するピンのリストを返却します.
     */
    private void savePinList() {
        ArrayList<Integer> pins = new ArrayList<>();
        for (int i = 0; i < mPinAdapter.getCount(); i++) {
            if (mPinAdapter.mCheckFlag.get(i)) {
                pins.add(((ArduinoUno.Pin) mPinAdapter.getItem(i)).getPinNumber());
            }
        }

        if (pins.isEmpty()) {
            showNoSelectPin();
        } else {
            mProfileData.setPinList(pins);

            Intent intent = new Intent();
            intent.putExtra("profile", mProfileData);
            setResult(RESULT_OK, intent);
            finish();
        }
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


    /**
     * ピンを格納するアダプタ.
     */
    private class PinAdapter extends BaseAdapter {

        private List<Boolean> mCheckFlag = new ArrayList<>();

        PinAdapter() {
            for (int i = 0; i < getCount(); i++) {
                mCheckFlag.add(containsPin(i));
            }
        }

        @Override
        public int getCount() {
            return ArduinoUno.Pin.values().length;
        }

        @Override
        public Object getItem(final int position) {
            return ArduinoUno.Pin.values()[position];
        }

        @Override
        public long getItemId(final int id) {
            return id;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_fabo_check_box_pin, null);
            }

            ArduinoUno.Pin pin = (ArduinoUno.Pin) getItem(position);

            CheckBox nameTV = (CheckBox) convertView.findViewById(R.id.item_fabo_check_box_pin);
            nameTV.setText(pin.getPinNames()[1]);
            nameTV.setChecked(mCheckFlag.get(position));
            // CheckBox#setOnCheckedChangeListenerを使用するとListViewで
            // スクロールした時点でヘックが外れてしまう。
            // ここでは、その問題を回避するためにOnClickListenerを使用します。
            nameTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = ((CheckBox) view).isChecked();
                    mCheckFlag.set(position, isChecked);
                }
            });

            return convertView;
        }
    }
}
