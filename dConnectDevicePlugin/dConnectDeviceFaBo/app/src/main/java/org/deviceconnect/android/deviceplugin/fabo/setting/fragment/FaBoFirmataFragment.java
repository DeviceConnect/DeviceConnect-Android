/*
FaBoFirmwareFragment
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
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.setting.FaBoSettingActivity;

import io.fabo.android.stk500.Stk500v1;

/**
 * 設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoFirmataFragment extends Fragment {

    /** Context. */
    private static Context mContext;

    /** LOG. */
    private static final String TAG = "DEBUG_DCONNECT";

    /** Connnect button. */
    private Button mButtonConnect;

    /** Send button. */
    private Button mButtonSend;

    /** TextView. */
    private TextView mTextViewCommment;

    /** STK500. */
    private Stk500v1 mStk500v1;

    private FaBoSettingActivity parent;


    /**
     * newInstance.
     *
     * @return fragment Fragment instance.
     */
    public static FaBoFirmataFragment newInstance() {
        FaBoFirmataFragment fragment = new FaBoFirmataFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "Firmata onCreateView:");


        View root = inflater.inflate(R.layout.firmata, container, false);

        // Get context.
        mContext = getActivity().getBaseContext();

        mTextViewCommment = (TextView) root.findViewById(R.id.textViewComment);
        mButtonConnect = (Button) root.findViewById(R.id.buttonConnect);
        mButtonSend = (Button) root.findViewById(R.id.buttonSend);

        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンがクリックされた時にUSBを開く.
                if (mStk500v1.openUsb()) {
                    mButtonSend.setVisibility(Button.VISIBLE);
                    mTextViewCommment.setText("USBに接続しました。");
                } else {
                    mTextViewCommment.setText("USBの接続に失敗しました。");
                }
            }
        });
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStk500v1.setData(R.raw.standardfirmata_hex);
                mStk500v1.sendFirmware();
                parent.moveConnectFirmata();
            }
        });


        Log.i(TAG, "addReceiver, mUsbReceiver");
        // USBの装着、脱着をReceiverで取得.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Stk500v1.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(mUsbReceiver, filter);


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTextViewCommment.setText("USB初期化.");

        // SerialPortの生成
        mStk500v1 = new Stk500v1(getActivity().getBaseContext());
        mStk500v1.enableDebug();

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "Firmata onPause:");

        // SerialPortを閉じる
        mStk500v1.closeUsb();
        mStk500v1 = null;
        try {
            getActivity().unregisterReceiver(mUsbReceiver);
        }catch (Exception e){
            Log.i(TAG, "Error:" + e);
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (mStk500v1.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mTextViewCommment.setText("USBに接続しました。");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // USBを閉じる
                mStk500v1.closeUsb();
                mTextViewCommment.setText("USBをクローズしました。");
            } else {
                mTextViewCommment.setText("不明なIntent");
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        parent = (FaBoSettingActivity) activity;
        super.onAttach(activity);
    }
}
