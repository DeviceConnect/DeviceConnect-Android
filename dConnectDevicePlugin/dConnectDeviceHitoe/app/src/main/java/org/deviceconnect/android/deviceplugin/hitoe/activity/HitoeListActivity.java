package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.hitoe.BuildConfig;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.ErrorDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.PinCodeDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.ProgressDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.util.BleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity is hitoe list screen.
 * @author NTT DOCOMO, INC.
 */

public abstract class HitoeListActivity extends FragmentActivity {
    /**
     * デフォルトのタイトル文字列.
     */
    public static final String DEFAULT_TITLE = "サービス一覧";

    /**
     * Adapter.
     */
    protected DeviceAdapter mDeviceAdapter;
    /**
     * Error Dialog.
     */
    private ErrorDialogFragment mErrorDialogFragment;
    /**
     * Progress Dialog.
     */
    private ProgressDialogFragment mProgressDialogFragment;
    /**
     * Handler.
     */
    protected final Handler mHandler = new Handler();
    /**
     * Bluetooth device list view.
     */
    protected ListView mListView;
    /**
     * footer view.
     */
    protected View mFooterView;

    /**
     * Enabled connected button.
     */
    protected boolean mEnableConnectedBtn;
    /**
     * Progress dialog flag.
     */
    protected boolean mCheckDialog;
    /**
     * Now connecting device.
     */
    protected HitoeDevice mConnectingDevice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hitoe_device_list);
        HitoeApplication app = (HitoeApplication) getApplication();
        app.initialize();
        setUI();

        mDeviceAdapter = new DeviceAdapter(this, createDeviceContainers());
        mListView = (ListView) findViewById(R.id.device_list_view);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setItemsCanFocus(true);

        LayoutInflater inflater = getLayoutInflater();
        mFooterView = inflater.inflate(R.layout.item_hitoe_searching, null);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            getActionBar().setTitle(DEFAULT_TITLE);
        }

    }

    /**
     * Initialize sub classs's ui.
     */
    protected abstract void setUI();

    @Override
    protected void onResume() {
        super.onResume();
        addFooterView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);

    }
    /**
     * Gets a instance of HitoeManager.
     *
     * @return HitoeManager
     */
    protected HitoeManager getManager() {
        HitoeApplication application =
                (HitoeApplication) getApplication();
        return application.getHitoeManager();
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Added the view at ListView.
     */
    protected void addFooterView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = getLayoutInflater();

                if (mFooterView != null) {
                    mListView.removeFooterView(mFooterView);
                }

                if (!BleUtils.isBLEPermission(HitoeListActivity.this)) {
                    Button btn = (Button) findViewById(R.id.btn_add_open);
                    btn.setVisibility(View.GONE);
                    mFooterView = inflater.inflate(R.layout.item_hitoe_error, null);
                    TextView textView = (TextView) mFooterView.findViewById(R.id.error_message);
                    textView.setText(getString(R.string.hitoe_setting_dialog_error_permission));
                    Button permission = (Button) mFooterView.findViewById(R.id.button_permission);
                    permission.setVisibility(View.VISIBLE);
                    permission.setText(R.string.bluetooth_settings_ble_permission_off);
                    permission.setOnClickListener((view) -> {
                        PermissionUtility.requestPermissions(HitoeListActivity.this, mHandler,
                                BleUtils.BLE_PERMISSIONS,
                                new PermissionUtility.PermissionRequestCallback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onFail(final String deniedPermission) {
                                    }
                                });
                    });

                    mListView.addFooterView(mFooterView);

                } else if (!BleUtils.isEnabled(HitoeListActivity.this)) {
                    Button btn = findViewById(R.id.btn_add_open);
                    btn.setVisibility(View.GONE);

                    mFooterView = inflater.inflate(R.layout.item_hitoe_error, null);
                    TextView textView = mFooterView.findViewById(R.id.error_message);
                    textView.setText(getString(R.string.hitoe_setting_dialog_disable_bluetooth));
                    Button bluetooth = mFooterView.findViewById(R.id.button_permission);
                    bluetooth.setVisibility(View.VISIBLE);
                    bluetooth.setText(R.string.bluetooth_settings_button);
                    bluetooth.setOnClickListener((view) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    });
                    mDeviceAdapter.clear();
                    mDeviceAdapter.notifyDataSetChanged();

                    mListView.addFooterView(mFooterView);
                } else if (BleUtils.isEnabled(HitoeListActivity.this)
                        && !mEnableConnectedBtn) {
                    mFooterView = inflater.inflate(R.layout.item_hitoe_searching, null);
                    mListView.addFooterView(mFooterView);
                } else if (BleUtils.isEnabled(HitoeListActivity.this)
                        && mEnableConnectedBtn && createDeviceContainers().size() == 0) {
                    Button btn = findViewById(R.id.btn_add_open);
                    btn.setVisibility(View.VISIBLE);

                    mFooterView = inflater.inflate(R.layout.item_hitoe_error, null);
                    TextView textView = (TextView) mFooterView.findViewById(R.id.error_message);
                    textView.setText(getString(R.string.alert_add_device));
                    Button bluetooth = mFooterView.findViewById(R.id.button_permission);
                    bluetooth.setVisibility(View.GONE);

                    mListView.addFooterView(mFooterView);
                } else {
                    Button btn = findViewById(R.id.btn_add_open);
                    btn.setVisibility(View.VISIBLE);
                    mDeviceAdapter.clear();
                    mDeviceAdapter.addAll(createDeviceContainers());
                    mDeviceAdapter.notifyDataSetChanged();

                }
            }
        });
    }
    /**
     * Create a list of device.
     *
     * @return list of device
     */
    protected List<HitoeDevice> createDeviceContainers() {
        getManager().readHitoeDeviceForDB();
        List<HitoeDevice> resDevice = new ArrayList<HitoeDevice>();
        for (HitoeDevice device:getManager().getRegisterDevices()) {
            if (device.getPinCode() != null && mEnableConnectedBtn) {
                resDevice.add(device);
            } else if (device.getPinCode() == null && !mEnableConnectedBtn) {
                resDevice.add(device);
            }
        }
        return resDevice;
    }

    /**
     * Connect to the BLE device that have heart rate service.
     *
     * @param device BLE device that have heart rate service.
     */
    protected void connectDevice(final HitoeDevice device) {
        if (BleUtils.isEnabled(this) && !mCheckDialog) {
            mConnectingDevice = device;
            showProgressDialog(device.getName());
            getManager().connectHitoeDevice(device);
        }
    }

    /**
     * Disconnect to the BLE device that have heart rate service.
     *
     * @param device BLE device that have heart rate service.
     */
    protected void disconnectDevice(final HitoeDevice device) {
        runOnUiThread(() -> {
            getManager().disconnectHitoeDevice(device);
            HitoeDevice container = findDeviceContainerByAddress(device.getId());
            if (container != null) {
                container.setRegisterFlag(false);
                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }



    /**
     * Display the dialog of connecting a ble device.
     *
     * @param name device name
     */
    protected void showProgressDialog(final String name) {
        dismissProgressDialog();

        Resources res = getResources();
        String title = res.getString(R.string.hitoe_setting_connecting_title);
        String message = res.getString(R.string.hitoe_setting_connecting_message, name);
        mProgressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        mProgressDialogFragment.show(getSupportFragmentManager(), "dialog");
        mCheckDialog = true;
    }

    /**
     * Dismiss the dialog of connecting a ble device.
     */
    protected void dismissProgressDialog() {
        mCheckDialog = false;
        mHandler.removeCallbacksAndMessages(null);

        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
            mProgressDialogFragment = null;
        }
    }

    /**
     * Display the error dialog of not connect device.
     *
     * @param name device name
     */
    protected void showErrorDialogNotConnect(final String name) {
        Resources res = getResources();
        String message;
        if (name == null) {
            message = res.getString(R.string.hitoe_setting_dialog_error_message,
                    getString(R.string.hitoe_setting_default_name));
        } else {
            message = res.getString(R.string.hitoe_setting_dialog_error_message, name);
        }
        showErrorDialog(message);
    }


    /**
     * Display the error dialog.
     *
     * @param message error message
     */
    protected void showErrorDialog(final String message) {
        dismissErrorDialog();
        try {
            Resources res = getResources();
            String title = res.getString(R.string.hitoe_setting_dialog_error_title);
            mErrorDialogFragment = ErrorDialogFragment.newInstance(title, message);
            mErrorDialogFragment.show(getSupportFragmentManager(), "error_dialog");
            mErrorDialogFragment.setOnDismissListener((dialog) -> {
                mErrorDialogFragment = null;
            });
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Dismiss the error dialog.
     */
    protected void dismissErrorDialog() {
        try {
            if (mErrorDialogFragment != null) {
                mErrorDialogFragment.dismiss();
                mErrorDialogFragment = null;
            }
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Look for a DeviceContainer with the given address.
     *
     * @param address address of device
     * @return The DeviceContainer that has the given address or null
     */
    protected HitoeDevice findDeviceContainerByAddress(final String address) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            HitoeDevice container = mDeviceAdapter.getItem(i);
            if (container.getId().equalsIgnoreCase(address)) {
                return container;
            }
        }
        return null;
    }


    /**
     * Returns true if this address contains the mDeviceAdapter.
     *
     * @param address address of device
     * @return true if address is an element of mDeviceAdapter, false otherwise
     */
    protected boolean containAddressForAdapter(final String address) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            HitoeDevice container = mDeviceAdapter.getItem(i);
            if (container.getId().equals(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this address contains the mDeviceAdapter.
     *
     * @param address address of device
     * @return true if address is an element of mDeviceAdapter, false otherwise
     */
    protected boolean containAddressForList(final String address) {
        List<HitoeDevice> devices = createDeviceContainers();
        int size = devices.size();
        for (int i = 0; i < size; i++) {
            HitoeDevice container = devices.get(i);
            if (container.getId().equals(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Device List's adapter.
     */
    protected class DeviceAdapter extends ArrayAdapter<HitoeDevice> {
        /**
         * Adapter inflater.
         */
        private LayoutInflater mInflater;

        /**
         * Constructor.
         * @param context context
         * @param objects hitoe's list
         */
        public DeviceAdapter(final Context context, final List<HitoeDevice> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_hitoe_device, null);
            }

            final HitoeDevice device = getItem(position);
            String name = device.getName();
            if (device.isRegisterFlag()) {
                if (getManager().containConnectedHitoeDevice(device.getId())) {
                    name += "\n" + getResources().getString(R.string.hitoe_setting_online);
                } else {
                    name += "\n" + getResources().getString(R.string.hitoe_setting_offline);
                }
            }

            TextView nameView = convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            TextView addressView = convertView.findViewById(R.id.device_address);
            addressView.setText(device.getId());
            final Button btn = convertView.findViewById(R.id.btn_connect_device);

            if (mEnableConnectedBtn) {
                btn.setVisibility(View.VISIBLE);

                if (device.isRegisterFlag()) {
                    btn.setBackgroundResource(R.drawable.button_red);
                    btn.setText(R.string.hitoe_setting_disconnect);
                } else {
                    btn.setBackgroundResource(R.drawable.button_blue);
                    btn.setText(R.string.hitoe_setting_connect);
                }
                btn.setOnClickListener((v) -> {
                    for (HitoeDevice d: getManager().getRegisterDevices()) {
                        if (!d.getName().equals(device.getName()) && d.isRegisterFlag()) {
                            getManager().disconnectHitoeDevice(d);
                        }
                    }

                    if (device.isRegisterFlag()) {
                        btn.setBackgroundResource(R.drawable.button_blue);
                        btn.setText(R.string.hitoe_setting_connect);
                        disconnectDevice(device);
                    } else {
                        if (device.getPinCode() == null) {
                            final Resources res = getResources();
                            PinCodeDialogFragment pinDialog = PinCodeDialogFragment.newInstance();
                            pinDialog.show(getSupportFragmentManager(), "pin_dialog");
                            pinDialog.setOnPinCodeListener((pin) -> {
                                if (pin.isEmpty()) {
                                    showErrorDialog(
                                            res.getString(R.string.hitoe_setting_dialog_error_message02));
                                    return;
                                }
                                device.setPinCode(pin);
                                connectDevice(device);
                            });
                        } else {
                            connectDevice(device);
                        }
                        mHandler.postDelayed(() -> {
                            if (mCheckDialog) {
                                device.setPinCode(null);
                                runOnUiThread(() -> {
                                    dismissProgressDialog();
                                    Resources res = getResources();
                                    showErrorDialog(
                                            res.getString(R.string.hitoe_setting_dialog_error_message04));
                                });
                            }
                        },  HitoeConstants.DISCOVERY_CYCLE_TIME);

                    }
                });
            } else {
                btn.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
