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
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.setting.FaBoSettingActivity;

import java.util.HashMap;

/**
 * 設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoConnectFragment extends Fragment {

    /** TAG. */
    private static final String TAG = "FABO_PLUGIN_SETTING";

    private static final String DEBUG = "DEBUG";


    /** Context. */
    private Context mContext;

    /** Activity. */
    private Activity mActivity;

    /** TextView. */
    private TextView mTextViewComment;

    /** TextView. */
    private TextView mTextViewLog;

    /** Button. */
    //private Button mOutputButton;

    /** Usb Device. */
    private static UsbDevice mDevice;

    /** Log message. */
    private static String logMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(DEBUG, "onCreateView");

        // Root view.
        View root = inflater.inflate(R.layout.connect, container, false);

        // Get context.
        mContext = getActivity().getBaseContext();
        mActivity = getActivity();

        // Textview for comment.
        mTextViewComment = (TextView) root.findViewById(R.id.textViewComment);

        // TextView for log.
        mTextViewLog = (TextView) root.findViewById(R.id.textViewLog);

        /*
        // ボタンが押されたらUSBに接続.
        mOutputButton = (Button) root.findViewById(R.id.outputButton);
        mOutputButton.setEnabled(false);
        mOutputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Serviceにメッセージを送信.
                Intent mIntent = new Intent(mContext, FaBoDeviceService.class);
                mContext.startService(mIntent);

                // USB OpenのコマンドをServiceにBroadcast.
                Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
                intent.putExtra("usbDevice", mDevice);
                mContext.sendBroadcast(intent);
            }
        });
        */

        return root;
    }

    @Override
    public void onResume() {

        Log.i(DEBUG, "onResume()");

        super.onResume();

        // USBの結果受信用のBroadcast Receiverを設定.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        filter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUSBResultEvent, filter);

        // Arduino Unoとの接続状態をチェック.
        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        mContext.sendBroadcast(intent);
        clearLogMessage();

        // Old device need wait
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkUsbDevice();
    }

    @Override
    public void onStop() {
        Log.i(DEBUG, "onStop()");

        mContext.unregisterReceiver(mUSBResultEvent);
        super.onStop();
    }

    private void checkUsbDevice() {
        Log.i(DEBUG, "checkUsbDevice()");

        // USBデバイスのチェック.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        for (final UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == 10755) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.arduinoorg_find);
                        mDevice = device;
                        //mOutputButton.setEnabled(false);
                        addLogMessage("Arduino Uno(ORG)を認識");

                        // Serviceにメッセージを送信.
                        Intent mIntent = new Intent(mContext, FaBoDeviceService.class);
                        mContext.startService(mIntent);

                        // USB OpenのコマンドをServiceにBroadcast.
                        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
                        intent.putExtra("usbDevice", mDevice);
                        mContext.sendBroadcast(intent);
                    }
                });
            } else if (device.getVendorId() == 9025) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDevice = device;
                        //mOutputButton.setEnabled(true);

                        // Serviceにメッセージを送信.
                        Intent mIntent = new Intent(mContext, FaBoDeviceService.class);
                        mContext.startService(mIntent);

                        // USB OpenのコマンドをServiceにBroadcast.
                        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
                        intent.putExtra("usbDevice", mDevice);
                        mContext.sendBroadcast(intent);
                    }
                });
                break;
            }
        }
    }

    /**
     * FaboDeviceServiceからのUSB接続結果を処理します.
     * @param intent USB接続処理結果
     */
    private void checkOpenUsbResult(final Intent intent) {
        Log.i(DEBUG, "checkOpenUsbResult");
        int resultId = intent.getIntExtra("resultId", 0);
        if (resultId == FaBoConst.CAN_NOT_FIND_USB) {
            mTextViewComment.setText(R.string.not_found_arduino);
        } else if (resultId == FaBoConst.FAILED_OPEN_USB) {
            mTextViewComment.setText(R.string.failed_open_usb);
        } else if (resultId == FaBoConst.FAILED_CONNECT_ARDUINO) {
            mTextViewComment.setText(R.string.failed_connect_arduino);
        } else if (resultId == FaBoConst.SUCCESS_CONNECT_ARDUINO) {
            mTextViewComment.setText(R.string.success_connect_arduino);
        } else if (resultId == FaBoConst.SUCCESS_CONNECT_FIRMATA) {
            mTextViewComment.setText(R.string.success_connect);
            //mOutputButton.setEnabled(false);
            addLogMessage("Firmataの動作を確認");
        } else if (resultId == FaBoConst.FAILED_CONNECT_FIRMATA) {
            /*Activity activity = getActivity();
            if (activity != null) {
                ((FaBoSettingActivity) activity).moveWriteFirmata();
            }
            */
        }
    }

    /**
     * FaboDeviceServiceからのUSB接続状態を処理します.
     * @param intent USB接続状態
     */
    private void checkUsbResult(final Intent intent) {
        int statusId = intent.getIntExtra("statusId", 0);
        if (statusId == FaBoConst.STATUS_FABO_NOCONNECT) {
            checkUsbDevice();
        } else if (statusId == FaBoConst.STATUS_FABO_INIT) {
        } else if (statusId == FaBoConst.STATUS_FABO_RUNNING) {
            mTextViewComment.setText(R.string.success_connect);
            //mOutputButton.setEnabled(false);
        }
    }

    /**
     * USBが外された時の処理を行います.
     */
    private void detachedUsbDevice() {
        Log.i(DEBUG, "detachedUsbDevice()");
        mTextViewComment.setText(R.string.disconnect_usb);
        //mOutputButton.setEnabled(false);
        Intent closeIntent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
        mContext.sendBroadcast(closeIntent);
        clearLogMessage();
    }

    /**
     * Broadcast receiver for usb event.
     */
    private BroadcastReceiver mUSBResultEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT.equals(action)) {
                checkOpenUsbResult(intent);
                Log.i(DEBUG, "DEVICE_TO_ARDUINO_OPEN_USB_RESULT");
            } else if (FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT.equals(action)) {
                checkUsbResult(intent);
                Log.i(DEBUG, "DEVICE_TO_ARDUINO_CHECK_USB_RESULT");
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                detachedUsbDevice();
                Log.i(DEBUG, "ACTION_USB_DEVICE_DETACHED");

            }
        }
    };

    private void clearLogMessage() {
        mTextViewLog.setText("");
    }

    private void addLogMessage(String msg) {
        String lastMsg = (String) mTextViewLog.getText().toString();
        Log.i(TAG, "lastMsg" + lastMsg);
        String newMsg = lastMsg;
        if(lastMsg != "") newMsg += "<br>";
        newMsg += "<font color=\"#00FF00\"> ✓ </font>" + msg;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTextViewLog.setText(Html.fromHtml(newMsg, 0));
        } else {
            mTextViewLog.setText(Html.fromHtml(newMsg));
        }
    }
}
