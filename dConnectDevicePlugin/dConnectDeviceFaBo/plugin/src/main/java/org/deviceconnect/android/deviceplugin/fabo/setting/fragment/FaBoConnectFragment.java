/*
FaBoConnectFragment
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.core.BuildConfig;
import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.setting.FaBoArduinoActivity;

import java.util.HashMap;

import io.fabo.serialkit.FaBoUsbConst;

/**
 * 設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoConnectFragment extends FaBoArduinoFragment {

    /**
     * デバッグ用TAG.
     */
    private static final String TAG = "FABO_PLUGIN_SETTING";

    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Arduinoとの接続が完了した時にActivityを終了するフラグを格納するキー.
     */
    public static final String EXTRA_FINISH_FLAG = "flag";

    /**
     * 作業内容を表示するためのTextView.
     */
    private TextView mTextViewComment;

    /**
     * ログを表示するためのTextView.
     */
    private TextView mTextViewLog;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fabo_connect, container, false);
        mTextViewComment = (TextView) root.findViewById(R.id.textViewComment);
        mTextViewLog = (TextView) root.findViewById(R.id.textViewLog);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // USBの結果受信用のBroadcast Receiverを設定.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(mUSBResultReceiver, filter);

        FaBoArduinoActivity a = (FaBoArduinoActivity) getActivity();
        FaBoDeviceControl ctrl = a.getFaBoDeviceControl();
        if (ctrl != null) {
            checkUsbDevice();
        }
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mUSBResultReceiver);
        super.onStop();
    }

    @Override
    public void onBindService() {
        checkUsbDevice();
    }

    @Override
    public void onUnbindService() {
    }

    /**
     * 終了フラグを確認して、Activityを終了します.
     */
    private void checkAndFinish() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (isFinishFlag()) {
            activity.finish();
        }
    }

    /**
     * 接続した時にActivityを終了するか確認します.
     * @return trueの場合はActivityを終了、それ以外は終了しません。
     */
    private boolean isFinishFlag() {
        Bundle args = getArguments();
        return args == null || args.getBoolean(EXTRA_FINISH_FLAG, true);
    }

    /**
     * USB機器の確認を行います.
     */
    private void checkUsbDevice() {
        if (DEBUG) {
            Log.i(TAG, "checkUsbDevice()");
        }

        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        FaBoArduinoActivity a = (FaBoArduinoActivity) activity;
        final FaBoDeviceControl ctrl = a.getFaBoDeviceControl();
        if (ctrl == null) {
            return;
        }

        clearLogMessage();

        UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        for (final UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == FaBoUsbConst.ARDUINO_UNO_VID) {
                mTextViewComment.setText(R.string.arduinoorg_find);
                addLogMessage(R.string.fragment_connect_org_recognition);

                int status = ctrl.getStatus();
                if (status == FaBoConst.STATUS_FABO_RUNNING) {
                    mTextViewComment.setText(R.string.success_connect);
                    checkAndFinish();
                } else if (status == FaBoConst.STATUS_FABO_NOCONNECT) {
                    sendOpenUsbDevice(activity, device);
                }
            } else if (device.getVendorId() == FaBoUsbConst.ARDUINO_CC_UNO_VID) {
                mTextViewComment.setText(R.string.arduinocc_find);
                addLogMessage(R.string.fragment_connect_cc_recognition);

                int status = ctrl.getStatus();
                if (status == FaBoConst.STATUS_FABO_RUNNING) {
                    mTextViewComment.setText(R.string.success_connect);
                    checkAndFinish();
                } else if (status == FaBoConst.STATUS_FABO_NOCONNECT) {
                    sendOpenUsbDevice(activity, device);
                }
            }
        }
    }

    /**
     * Usb機器のオープンリクエストを送信します.
     * @param context コンテキスト
     * @param device USB機器
     */
    private void sendOpenUsbDevice(Context context, UsbDevice device) {
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        intent.putExtra(UsbManager.EXTRA_DEVICE, device);
        context.sendBroadcast(intent);
    }

    /**
     * FaboDeviceServiceからのUSB接続結果を処理します.
     *
     * @param intent USB接続処理結果
     */
    private void checkOpenUsbResult(final Intent intent) {
        if (DEBUG) {
            Log.i(TAG, "checkOpenUsbResult");
        }

        int resultId = intent.getIntExtra("resultId", 0);
        if (resultId == FaBoConst.FAILED_CONNECT_ARDUINO) {
            mTextViewComment.setText(R.string.failed_connect_arduino);
            checkAndFinish();
        } else if (resultId == FaBoConst.SUCCESS_CONNECT_ARDUINO) {
            mTextViewComment.setText(R.string.success_connect_arduino);
        } else if (resultId == FaBoConst.SUCCESS_CONNECT_FIRMATA) {
            mTextViewComment.setText(R.string.success_connect);
            addLogMessage(R.string.fragment_connect_check_firmata);
            checkAndFinish();
        }
    }

    /**
     * USBが外された時の処理を行います.
     */
    private void detachedUsbDevice() {
        if (DEBUG) {
            Log.i(TAG, "detachedUsbDevice()");
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        clearLogMessage();
        mTextViewComment.setText(R.string.disconnect_usb);
        checkAndFinish();
    }

    /**
     * Broadcast receiver for usb event.
     */
    private BroadcastReceiver mUSBResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (DEBUG) {
                Log.i(TAG, "Received action=" + action);
            }

            if (FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT.equals(action)) {
                checkOpenUsbResult(intent);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                detachedUsbDevice();
            }
        }
    };

    /**
     * ログ表示用TextViewのメッセージをクリアします.
     */
    private void clearLogMessage() {
        mTextViewLog.setText("");
    }

    /**
     * ログ表示用TextViewにメッセージを追加します.
     *
     * @param resId リソースID
     */
    private void addLogMessage(final int resId) {
        addLogMessage(getString(resId));
    }

    /**
     * ログ表示用TextViewにメッセージを追加します.
     *
     * @param msg 追加するメッセージ
     */
    private synchronized void addLogMessage(final String msg) {
        String lastMsg = mTextViewLog.getText().toString();
        if (DEBUG) {
            Log.i(TAG, "lastMsg" + lastMsg);
        }

        String newMsg = lastMsg;
        if (!lastMsg.isEmpty()) {
            newMsg += "<br>";
        }
        newMsg += "<font color=\"#00FF00\"> ✓ </font>" + msg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTextViewLog.setText(Html.fromHtml(newMsg, 0));
        } else {
            mTextViewLog.setText(Html.fromHtml(newMsg));
        }
    }
}
