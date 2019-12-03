package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.fabo.core.R;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileDataUtil;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoBasePinFragment;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoPinCheckBoxFragment;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoPinRadioGroupFragment;

import java.util.List;

/**
 * 使用するピンを選択するための画面を表示するActivity.
 */
public class FaBoPinListActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_pin_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.activity_fabo_virtual_service_pin_title);

        ProfileData p = getIntent().getParcelableExtra("profile");
        if (ProfileDataUtil.isMultiChoicePin(p)) {
            showPinFragment(new FaBoPinCheckBoxFragment());
        } else {
            showPinFragment(new FaBoPinRadioGroupFragment());
        }

        Button saveBtn = findViewById(R.id.activity_fabo_pin_save_btn);
        saveBtn.setOnClickListener((view) -> {
            savePinList();
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
     * ピンを選択するフラグメントを表示します.
     * @param fragment 表示するフラグメント
     */
    private void showPinFragment(final FaBoBasePinFragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
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
        FaBoBasePinFragment fragment = (FaBoBasePinFragment) getFragmentManager().findFragmentById(R.id.container);

        List<Integer> pins = fragment.getSelectedPins();
        if (pins.isEmpty()) {
            showNoSelectPin();
        } else {
            ProfileData p = getIntent().getParcelableExtra("profile");
            p.setPinList(pins);

            Intent intent = new Intent();
            intent.putExtra("profile", p);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
