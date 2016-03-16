/*
FaBoSettingActivity
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import org.restlet.resource.Resource;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoConnectFragment extends Fragment {

    /** Context. */
    private static Context mContext;

    /** Activity. */
    private static Activity mActivity;

    /** TAG. */
    private static final String TAG = "FABO_PLUGIN2";

    /** TextView. */
    private TextView mTextViewCommment;

    /** Button. */
    private Button mOutputButton;

    /** Status. */
    private static int mStatus;


    private FaBoSettingActivity parent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "Connect onCreateView:");

        View root = inflater.inflate(R.layout.connect, container, false);

        // Get context.
        mContext = getActivity().getBaseContext();
        mActivity = getActivity();

        // Textview.
        mTextViewCommment = (TextView) root.findViewById(R.id.textViewComment);

        // ボタンが押されたらUSBに接続.
        mOutputButton = (Button) root.findViewById(R.id.outputButton);
        mOutputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(mContext, FaBoDeviceService.class);
                mContext.startService(mIntent);

                Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB);
                mContext.sendBroadcast(intent);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCommment.setText("USBに接続");
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        Log.i(TAG, "status:" + mStatus);

        // USBの結果受信用のBroadcast Receiverを設定.
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT);
        mIntentFilter.addAction(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT);
        mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUSBResultEvent, mIntentFilter);

        Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
        mContext.sendBroadcast(intent);

        // USBデバイスのチェック.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            if (device.getVendorId() == 10755) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCommment.setText("Arduino.org製のArduinoは非対応です。");
                        mOutputButton.setEnabled(true);
                    }
                });
            } else if (device.getVendorId() == 9025) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCommment.setText("Arduino.cc製のArduinoを認識しました。");
                        mOutputButton.setEnabled(true);
                    }
                });
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        //mContext.unregisterReceiver(mUSBResultEvent);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        mContext.unregisterReceiver(mUSBResultEvent);
        mStatus = FaBoConst.STATUS_ACTIVITY_STOP;
    }

    /**
     * Broadcast receiver for usb event.
     */
    private BroadcastReceiver mUSBResultEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action:" + action);
            if(action.equals(FaBoConst.DEVICE_TO_ARDUINO_OPEN_USB_RESULT)) {
                int resultId = intent.getIntExtra("resultId", 0);
                Log.i(TAG, "resultId;" + resultId);
                if(resultId == FaBoConst.CAN_NOT_FIND_USB){
                    mTextViewCommment.setText(R.string.not_found_arduino);
                } else if (resultId == FaBoConst.FAILED_OPEN_USB){
                    mTextViewCommment.setText(R.string.failed_open_usb);
                } else if (resultId == FaBoConst.FAILED_CONNECT_ARDUINO){
                    mTextViewCommment.setText(R.string.failed_connect_arduino);
                } else if (resultId == FaBoConst.SUCCESS_CONNECT_ARDUINO){
                    String lastText = (String) mTextViewCommment.getText();
                    mTextViewCommment.setText(lastText + "\n" +  getString(R.string.success_connect_arduino));
                } else if (resultId == FaBoConst.SUCCESS_CONNECT_FIRMATA){
                    String lastText = (String) mTextViewCommment.getText();
                    mTextViewCommment.setText(lastText + "\n" + getString(R.string.success_connect_firmata));
                    mStatus = FaBoConst.STATUS_RUNNING;
                } else if (resultId == FaBoConst.FAILED_CONNECT_FIRMATA){
                    parent.moveWriteFirmata();
                    mStatus = FaBoConst.STATUS_STOP;
                }
            } else  if(action.equals(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB_RESULT)) {
                int statusId = intent.getIntExtra("statusId", 0);
                Log.i(TAG, "statusId;" + statusId);
                if(statusId == FaBoConst.STATUS_RUNNING) {
                    mTextViewCommment.setText(R.string.success_connect_firmata);
                    mStatus = FaBoConst.STATUS_RUNNING;
                }
            } else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                mTextViewCommment.setText(R.string.disconnect_usb);
                mOutputButton.setEnabled(false);
                mStatus = FaBoConst.STATUS_STOP;
                Intent closeIntent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
                mContext.sendBroadcast(closeIntent);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        parent = (FaBoSettingActivity) activity;
        super.onAttach(activity);
    }

}
