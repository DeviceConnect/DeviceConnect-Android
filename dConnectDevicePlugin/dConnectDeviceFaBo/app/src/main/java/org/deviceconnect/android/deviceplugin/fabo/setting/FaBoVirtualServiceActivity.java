package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.core.R;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileDataUtil;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;
import org.deviceconnect.android.message.DConnectMessageService;

import java.util.ArrayList;
import java.util.List;

/**
 * 仮想サービスの設定を行うActivity.
 */
public class FaBoVirtualServiceActivity extends Activity {

    /**
     * プロファイル追加リクエストコード.
     */
    private static final int REQUEST_CODE_ADD_PROFILE = 101;

    /**
     * プロファイル更新リクエストコード.
     */
    private static final int REQUEST_CODE_UPDATE_PROFILE = 102;

    /**
     * 新規作成フラグ.
     * <p>
     * 新規作成の仮想サービスの場合はtrue、それ以外はfalse。
     * </p>
     */
    private boolean mNewCreateFlag = false;

    /**
     * 仮想サービスデータ.
     */
    private ServiceData mServiceData;

    /**
     * FaBoDeviceServiceのインスタンス.
     */
    private FaBoDeviceService mFaBoDeviceService;

    /**
     * FaBoDeviceServiceにバインドフラグ.
     * trueの時は接続中
     */
    private boolean mIsBound;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_virtual_service);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.activity_fabo_virtual_service_title);

        Intent intent = getIntent();
        if (intent != null) {
            mServiceData = intent.getParcelableExtra("service");
        }

        if (mServiceData == null) {
            mNewCreateFlag = true;
            mServiceData = new ServiceData();
            mServiceData.setName("New Service");
        }

        TextView serviceIdTV =  findViewById(R.id.activity_fabo_service_id);
        serviceIdTV.setText(mServiceData.getServiceId());

        EditText serviceNameET = findViewById(R.id.activity_fabo_service_name);
        serviceNameET.setText(mServiceData.getName());
        serviceNameET.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        Button saveBtn = findViewById(R.id.activity_fabo_service_save_btn);
        saveBtn.setOnClickListener((view) -> {
            saveVirtualService();
        });

        Button addBtn =  findViewById(R.id.activity_fabo_profile_add_btn);
        addBtn.setOnClickListener((view) -> {
            openProfileActivity();
        });

        LinearLayout profileLayout = findViewById(R.id.activity_fabo_profile_list);
        for (ProfileData profileData : mServiceData.getProfileDataList()) {
            View view = createProfileView(profileData);
            profileLayout.addView(view);
        }
    }

    @Override
    protected void onPause() {
        unbindMessageService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindMessageService();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_PROFILE:
                if (resultCode == RESULT_OK) {
                    ProfileData profileData = data.getParcelableExtra("profile");
                    mServiceData.addProfileData(profileData);

                    View view = createProfileView(profileData);
                    LinearLayout profileLayout = findViewById(R.id.activity_fabo_profile_list);
                    profileLayout.addView(view);
                }
                break;

            case REQUEST_CODE_UPDATE_PROFILE:
                if (resultCode == RESULT_OK) {
                    ProfileData profileData = data.getParcelableExtra("profile");
                    List<ProfileData> list = mServiceData.getProfileDataList();
                    for (ProfileData pd : list) {
                        if (pd.getType() == profileData.getType()) {
                            pd.setPinList(profileData.getPinList());
                        }
                    }
                    resetProfileLayout();
                }
                break;
        }
    }

    /**
     * サービスとバインドします.
     */
    private void bindMessageService() {
        if (!mIsBound) {
            Intent intent = new Intent(getApplicationContext(), Util.getDConnectMessageServiceClass(this));
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * サービスをアンバインドします.
     */
    private void unbindMessageService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * プロファイルのリストをを再設定します.
     */
    private void resetProfileLayout() {
        LinearLayout profileLayout = findViewById(R.id.activity_fabo_profile_list);
        profileLayout.removeAllViews();

        for (ProfileData profileData : mServiceData.getProfileDataList()) {
            View view = createProfileView(profileData);
            profileLayout.addView(view);
        }
    }

    /**
     * プロファイル選択画面を開きます.
     */
    private void openProfileActivity() {
        ArrayList<ProfileData> list = new ArrayList<>();
        for (ProfileData p : mServiceData.getProfileDataList()) {
            list.add(p);
        }
        Intent intent = new Intent();
        intent.setClass(this, FaBoProfileListActivity.class);
        intent.putParcelableArrayListExtra("profile", list);
        intent.putIntegerArrayListExtra("pins", new ArrayList<>(getUsePins()));
        startActivityForResult(intent, REQUEST_CODE_ADD_PROFILE);
    }

    /**
     * プロファイルを表示するためのViewを作成します.
     *
     * @param profileData プロファイルデータ
     * @return プロファイルを表示するためのView
     */
    private View createProfileView(final ProfileData profileData) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_fabo_profile_a, null);
        if (profileData.getType().getValue() < 100) {
            view.setOnClickListener((v) -> {
                openPinActivity(profileData);
            });
        }
        TextView nameTV = view.findViewById(R.id.item_fabo_profile_name);
        nameTV.setText(ProfileDataUtil.getProfileName(this, profileData.getType()));

        TextView pinsTV =  view. findViewById(R.id.item_fabo_profile_pins);
        pinsTV.setText(createPinInfo(profileData));

        view.findViewById(R.id.item_fabo_remove_profile_btn).setOnClickListener((v) -> {
            showConfirmRemoveProfile(profileData);
        });

        return view;
    }

    /**
     * 使用しているピンの一覧を文字列にして取得します.
     * @param profileData プロファイル
     * @return ピンの一覧の文字列
     */
    private String createPinInfo(ProfileData profileData) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : profileData.getPinList()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            FaBoShield.Pin pin = FaBoShield.Pin.getPin(i);
            if (pin != null) {
                sb.append(pin.getPinNames()[1]);
            }
        }
        return getString(R.string.activity_fabo_virtual_service_profile_pins, sb.toString());
    }

    /**
     * ピンの設定画面を開きます.
     *
     * @param profileData 設定画面
     */
    private void openPinActivity(final ProfileData profileData) {
        Intent intent = new Intent();
        intent.setClass(this, FaBoPinListActivity.class);
        intent.putExtra("profile", profileData);
        intent.putIntegerArrayListExtra("pins", new ArrayList<>(getUsePins()));
        startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE);
    }

    /**
     * 仮想サービスで使用されているピンの一覧を取得します.
     * @return ピンの一覧
     */
    private List<Integer> getUsePins() {
        return mServiceData.getUsePins();
    }

    /**
     * FaBoDeviceServiceと接続できていない場合のエラーダイアログを表示します.
     */
    private void showNotConnectServiceError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_error_title)
                .setMessage(R.string.activity_fabo_virtual_service_not_bind)
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok, null)
                .show();
    }

    /**
     * サービス名が登録されていない場合のエラーダイアログを表示します.
     */
    private void showNoNameError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_error_title)
                .setMessage(R.string.activity_fabo_virtual_service_no_name)
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok, null)
                .show();
    }

    /**
     * 文字数が長すぎる場合のエラーダイアログを表示します.
     */
    private void showTooLong() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_error_title)
                .setMessage(R.string.activity_fabo_virtual_service_name_too_long)
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok, null)
                .show();
    }

    /**
     * プロファイルが登録されていない場合のエラーダイアログを表示します.
     */
    private void showNoProfileError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_error_title)
                .setMessage(R.string.activity_fabo_virtual_service_no_select_message)
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok, null)
                .show();
    }

    /**
     * ServiceDataの保存完了ダイアログを表示します.
     */
    private void showSaveServiceData() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_dialog_title)
                .setMessage(R.string.activity_fabo_virtual_service_save_message)
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok,
                        (dialogInterface, i) -> {
                            finish();
                        })
                .show();
    }

    /**
     * プロファイル削除確認ダイアログを表示します.
     * @param profileData 削除確認を行うプロファイルデータ
     */
    private void showConfirmRemoveProfile(final ProfileData profileData) {
        String name = ProfileDataUtil.getProfileName(this, profileData.getType());
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_remove_profile_title)
                .setMessage(getString(R.string.activity_fabo_virtual_service_remove_profile_message, name))
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok,
                        (dialogInterface, i) -> {
                            mServiceData.removeProfileData(profileData);
                            resetProfileLayout();
                        })
                .setNegativeButton(R.string.activity_fabo_virtual_service_error_no, null)
                .show();
    }

    /**
     * ServiceDataの保存に失敗した場合のエラーダイアログを表示します.
     */
    private void showSaveServiceDataError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_fabo_virtual_service_remove_profile_title)
                .setMessage(getString(R.string.activity_fabo_virtual_service_remove_error_message))
                .setPositiveButton(R.string.activity_fabo_virtual_service_error_ok, null)
                .show();
    }

    /**
     * 仮想サービスを保存します.
     */
    private void saveVirtualService() {
        if (mFaBoDeviceService == null) {
            showNotConnectServiceError();
            return;
        }

        EditText nameET = (EditText) findViewById(R.id.activity_fabo_service_name);
        String serviceName = nameET.getText().toString();
        if (serviceName.isEmpty()) {
            showNoNameError();
        } else if (serviceName.length() > 32) {
            showTooLong();
        } else if (mServiceData.getProfileDataList().isEmpty()) {
            showNoProfileError();
        } else {
            mServiceData.setName(serviceName);

            VirtualService service;
            if (mNewCreateFlag) {
                service = mFaBoDeviceService.addServiceData(mServiceData);
            } else {
                service = mFaBoDeviceService.updateServiceData(mServiceData);
            }

            if (service != null) {
                showSaveServiceData();
            } else {
                showSaveServiceDataError();
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mFaBoDeviceService = (FaBoDeviceService) ((DConnectMessageService.LocalBinder) service).getMessageService();
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mFaBoDeviceService = null;
            mIsBound = false;
        }
    };
}
