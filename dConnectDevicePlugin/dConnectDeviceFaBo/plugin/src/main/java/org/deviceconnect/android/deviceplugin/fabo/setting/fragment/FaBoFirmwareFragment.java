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
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.param.FaBoConst;
import org.deviceconnect.android.deviceplugin.fabo.setting.FaBoArduinoActivity;

import java.util.HashMap;

import io.fabo.android.stk500.StkWriter;
import io.fabo.android.stk500.StkWriterListenerInterface;
import io.fabo.serialkit.FaBoUsbConst;

/**
 * 設定画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoFirmwareFragment extends FaBoArduinoFragment implements StkWriterListenerInterface {

    /**
     * Context.
     */
    private Context mContext;

    /**
     * Connect button.
     */
    private Button mButtonConnect;

    /**
     * Send button.
     */
    private Button mButtonSend;

    /**
     * Back button.
     */
    private Button mButtonBack;

    /**
     * TextView.
     */
    private TextView mTextViewComment;

    /**
     * STK500.
     */
    private StkWriter mStkWriter;

    /**
     * Parent activity.
     */
    private FaBoArduinoActivity mParent;

    /**
     * Activity.
     */
    private Activity mActivity;

    /**
     * newInstance.
     *
     * @return fragment Fragment instance.
     */
    public static FaBoFirmwareFragment newInstance() {
        FaBoFirmwareFragment fragment = new FaBoFirmwareFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Root view.
        View root = inflater.inflate(R.layout.fragment_fabo_firmata, container, false);

        // Get context.
        mContext = getActivity().getBaseContext();
        mActivity = getActivity();

        // Get ui component.
        mTextViewComment = (TextView) root.findViewById(R.id.textViewComment);
        mButtonConnect = (Button) root.findViewById(R.id.buttonConnect);
        mButtonSend = (Button) root.findViewById(R.id.buttonSend);
        mButtonBack = (Button) root.findViewById(R.id.buttonBack);

        // USBへの接続ボタン
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // FaBoとの接続用のUSBをCloseする.
                Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CLOSE_USB);
                mContext.sendBroadcast(intent);

                // ボタンがクリックされた時にUSBを開く.
                if (mStkWriter.openUsb()) {
                    mButtonSend.setVisibility(Button.VISIBLE);
                    mButtonSend.setEnabled(true);
                }
            }
        });

        // 戻るボタン.
        mButtonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // FaBoとの接続用のUSBをCloseする.
                Intent intent = new Intent(FaBoConst.DEVICE_TO_ARDUINO_CHECK_USB);
                mContext.sendBroadcast(intent);

                // 前のページに戻る.
//                mParent.moveConnectFirmata();
            }
        });

        // Firmwareの送信ボタン
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonSend.setEnabled(false);

                // Firmwareを転送.
                mStkWriter.setData(R.raw.standardfirmata_hex);
                mStkWriter.sendFirmware();
            }
        });

        // USBの装着、脱着をReceiverで取得.
        IntentFilter filter = new IntentFilter();
        filter.addAction(StkWriter.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(mUsbReceiver, filter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // SerialPortの生成
        //mStkWriter = new StkWriter(getActivity().getBaseContext());
        //mStkWriter.setListener(this);

        // USBデバイスのチェック.
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == FaBoUsbConst.ARDUINO_UNO_VID) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.arduinoorg_find);
                        mButtonConnect.setEnabled(false);
                        mButtonSend.setVisibility(Button.INVISIBLE);
                        mButtonBack.setVisibility(Button.INVISIBLE);
                    }
                });
            } else if (device.getVendorId() == FaBoUsbConst.ARDUINO_CC_UNO_VID) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.arduinocc_find);
                        mButtonConnect.setEnabled(true);
                        mButtonSend.setVisibility(Button.INVISIBLE);
                        mButtonBack.setVisibility(Button.INVISIBLE);
                    }
                });
                break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // SerialPortを閉じる
        //mStkWriter.closeUsb();
        //mStkWriter = null;
        try {
            getActivity().unregisterReceiver(mUsbReceiver);
        } catch (Exception ignored) {
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (StkWriter.ACTION_USB_PERMISSION.equals(action)) {

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // USBを閉じる
                /*
                mStkWriter.closeUsb();
                mTextViewComment.setText(R.string.disconnect_usb);
                mButtonConnect.setEnabled(false);
                mButtonSend.setVisibility(Button.INVISIBLE);
                mButtonBack.setVisibility(Button.INVISIBLE);
                */
            }
        }
    };

    @Override
    public void onChangeStatus(int status) {

        switch (status) {
            case StkWriter.STATUS_USB_INIT:
                break;
            case StkWriter.STATUS_USB_OPEN:
                break;
            case StkWriter.STATUS_USB_CONNECT:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mButtonSend.setVisibility(Button.VISIBLE);
                        mButtonSend.setEnabled(true);
                        mButtonConnect.setEnabled(false);
                        mTextViewComment.setText(R.string.firmware_usb_find);
                    }
                });
                break;
            case StkWriter.STATUS_USB_CLOSE:
                break;
            case StkWriter.STATUS_UART_START:
                break;
            case StkWriter.STATUS_FIRMWARE_SEND_INIT:
                break;
            case StkWriter.STATUS_FIRMWARE_SEND_START:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.firmware_start_send);
                    }
                });
                break;
            case StkWriter.STATUS_FIRMWARE_SEND_FINISH:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStkWriter.closeUsb();
                        mTextViewComment.setText(R.string.firmware_success_send);
                        mButtonBack.setVisibility(Button.VISIBLE);
                    }
                });
                break;
        }
    }

    @Override
    public void onError(int status) {
        switch (status) {
            case StkWriter.ERROR_FAILED_CONNECTION:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.can_not_connet_usb);
                        mButtonConnect.setEnabled(true);
                        mButtonSend.setVisibility(Button.INVISIBLE);
                    }
                });
                break;
            case StkWriter.ERROR_FAILED_OPEN:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.can_not_open_usb);
                        mButtonConnect.setEnabled(true);
                        mButtonSend.setVisibility(Button.INVISIBLE);
                    }
                });
                break;
            case StkWriter.ERROR_FAILED_SEND_FIRMRARE:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewComment.setText(R.string.failed_send_firmata);
                        mButtonConnect.setEnabled(true);
                        mButtonSend.setVisibility(Button.INVISIBLE);
                    }
                });
                break;
            case StkWriter.ERROR_NO_FOUND_FIRMARE:
                break;
            case StkWriter.ERROR_NOT_INIT_USB:
                break;
            case StkWriter.ERROR_NOT_WRITE_UART:
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        mParent = (FaBoArduinoActivity) activity;
        super.onAttach(activity);
    }
}
