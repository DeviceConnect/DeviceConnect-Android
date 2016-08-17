/*
 SettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.sphero.BuildConfig;
import org.deviceconnect.android.deviceplugin.sphero.R;
import org.deviceconnect.android.deviceplugin.sphero.SpheroDeviceService;
import org.deviceconnect.android.deviceplugin.sphero.data.SpheroParcelable;
import org.deviceconnect.android.deviceplugin.sphero.setting.fragment.DeviceSelectionPageFragment;
import org.deviceconnect.android.deviceplugin.sphero.setting.fragment.PairingFragment;
import org.deviceconnect.android.deviceplugin.sphero.setting.fragment.WakeupFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 設定画面用アクティビティ.
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {
    /** Action NameSpace. */
    private static final String ACTION_NAMESPACE = SettingActivity.class.getPackage().getName() + ".action";

    /** 
     * 新規に検知したデバイスを追加するアクション.
     */
    public static final String ACTION_ADD_DEVICE = ACTION_NAMESPACE + ".ADD_DEVICE";

    /** 
     * 接続済みのデバイスを追加するアクション.
     */
    public static final String ACTION_ADD_CONNECTED_DEVICE = ACTION_NAMESPACE + ".ADD_CONNECTED_DEVICE";
    /**
     * 接続されていないデバイスを追加するアクション.
     */
    public static final String ACTION_ADD_FOUNDED_DEVICE = ACTION_NAMESPACE + ".ADD_FOUNDED_DEVICE";

    /** 
     * デバイスを削除するアクション.
     */
    public static final String ACTION_REMOVE_DEVICE = ACTION_NAMESPACE + ".REMOVE_DEVICE";
    
    /** 
     * すべてのデバイスを削除するアクション.
     */
    public static final String ACTION_REMOVE_DEVICE_ALL = ACTION_NAMESPACE + ".REMOVE_DEVICE_ALL";
    
    /** 
     * デバイスを削除するアクション.
     */
    public static final String ACTION_CONNECTED = ACTION_NAMESPACE + ".ACTION_CONNECTED";
    /**
     * デバイスを切断するアクション.
     */
    public static final String ACTION_DISCONNECTED = ACTION_NAMESPACE + ".ACTION_DISCONNECTED";
    /**
     * デバイスを削除するアクション.
     */
    public static final String ACTION_DELETED = ACTION_NAMESPACE + ".ACTION_DELETED";

    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_DEVICE = "device";
    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_IS_CONNECTED = "is_connected";

    /**
     * Extraキー : {@value} .
     */
    public static final String EXTRA_DEVICES = "devices";
    
    /** 
     * サービスから通知を受けるブロードキャストレシーバ.
     */
    private BroadcastReceiver mReceiver;
    
    /** 
     * デバイス操作のリスナー.
     * Fragmentをページングするため、weak参照で持つ。
     */
    private WeakReference<DeviceControlListener> mListener;
    
    /** 
     * ページのクラスリスト.
     */
    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
        WakeupFragment.class,
        PairingFragment.class,
        DeviceSelectionPageFragment.class,
    };
    /**
     * ページのタイトルリスト.
     */
    private static final int[] PAGE_TITLES = {
            R.string.page_1,
            R.string.page_2,
            R.string.page_3
    };
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ADD_DEVICE);
        filter.addAction(ACTION_REMOVE_DEVICE);
        filter.addAction(ACTION_REMOVE_DEVICE_ALL);
        filter.addAction(ACTION_ADD_CONNECTED_DEVICE);
        filter.addAction(ACTION_CONNECTED);
        filter.addAction(ACTION_DISCONNECTED);
        filter.addAction(ACTION_ADD_FOUNDED_DEVICE);
        filter.addAction(ACTION_DELETED);

        mReceiver = new ServiceReceiver();
        lbm.registerReceiver(mReceiver, filter);
    }

    @Override
    public int getPageCount() {
        return PAGES.length;
    }

    @Override
    public Fragment createPage(final int position) {
        Fragment page;
        try {
            page = (Fragment) PAGES[position].newInstance();
        } catch (InstantiationException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            page = null;
        } catch (IllegalAccessException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            page = null;
        }
        
        return page;
    }
    @Override
    public CharSequence getPageTitle(final int position) {
        return getString(PAGE_TITLES[position]);
    }
    /**
     * 指定されたアクションを投げる.
     * 
     * @param action アクション
     */
    private void sendAction(final String action) {
        Intent i = new Intent(this, SpheroDeviceService.class);
        i.setAction(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
    
    /**
     * 接続済みのデバイス一覧を取得するブロードキャストを投げる.
     */
    public void sendGetConnectedDevicesBroadcast() {
        sendAction(SpheroDeviceService.ACTION_GET_CONNECTED);
    }
    /**
     * 接続されていないデバイス一覧を取得するブロードキャストを投げる.
     */
    public void sendGetFoundedDevicesBroadcast() {
        sendAction(SpheroDeviceService.ACTION_GET_FOUND);
    }

    /**
     * 検知開始のブロードキャストを投げる.
     */
    public void sendStartDiscoveryBroadcast() {
        sendAction(SpheroDeviceService.ACTION_START_DISCOVERY);
    }
    
    /**
     * 検知停止のブロードキャストを投げる.
     */
    public void sendStopDiscoveryBroadcast() {
        sendAction(SpheroDeviceService.ACTION_STOP_DISCOVERY);
    }
    
    /**
     * 接続のブロードキャストを投げる.
     * 
     * @param serviceId サービスID
     */
    public void sendConnectBroadcast(final String serviceId) {
        Intent i = new Intent(this, SpheroDeviceService.class);
        i.setAction(SpheroDeviceService.ACTION_CONNECT);
        i.putExtra(SpheroDeviceService.EXTRA_ID, serviceId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }


    /**
     * Spheroを削除するブロードキャストを投げる.
     * @param serviceId サービスID
     */
    public void sendDeleteSpheroBroadcast(final String serviceId) {
        Intent i = new Intent(this, SpheroDeviceService.class);
        i.setAction(SpheroDeviceService.ACTION_DELETE_DEVICE);
        i.putExtra(SpheroDeviceService.EXTRA_ID, serviceId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    /**
     * 接続解除のブロードキャストを投げる.
     * 
     * @param serviceId サービスID
     */
    public void sendDisonnectBroadcast(final String serviceId) {
        Intent i = new Intent(this, SpheroDeviceService.class);
        i.setAction(SpheroDeviceService.ACTION_DISCONNECT);
        i.putExtra(SpheroDeviceService.EXTRA_ID, serviceId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
    
    /**
     * リスナーを設定する.
     * 
     * @param listener リスナー
     */
    public void setDeviceControlListener(final DeviceControlListener listener) {
        mListener = new WeakReference<SettingActivity.DeviceControlListener>(listener);
    }
    
    /**
     * サービスからのブロードキャストを受け取るレシーバー.
     */
    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            
            if (mListener == null || mListener.get() == null) {
                return;
            }
            
            String action = intent.getAction();
            if (action.equals(ACTION_ADD_DEVICE)) {
                SpheroParcelable sd = (SpheroParcelable) intent.getParcelableExtra(EXTRA_DEVICE);
                mListener.get().onDeviceFound(sd);
            } else if (action.equals(ACTION_ADD_FOUNDED_DEVICE)) {
                List<SpheroParcelable> devices = intent.getParcelableArrayListExtra(EXTRA_DEVICES);
                for (SpheroParcelable sd : devices) {
                    mListener.get().onDeviceFound(sd);
                }
            } else if (action.equals(ACTION_REMOVE_DEVICE)) {
                SpheroParcelable sd = (SpheroParcelable) intent.getParcelableExtra(EXTRA_DEVICE);
                mListener.get().onDeviceLost(sd);
            } else if (action.equals(ACTION_REMOVE_DEVICE_ALL)) {
                mListener.get().onDeviceLostAll();
            } else if (action.equals(ACTION_ADD_CONNECTED_DEVICE)) {
                List<Parcelable> devices = intent.getParcelableArrayListExtra(EXTRA_DEVICES);
                mListener.get().onConnectedDevices(devices);
            } else if (action.equals(ACTION_CONNECTED)) {
                SpheroParcelable sd = (SpheroParcelable) intent.getParcelableExtra(EXTRA_DEVICE);
                mListener.get().onDeviceConnected(sd);
            } else if (action.equals(ACTION_DISCONNECTED)) {
                SpheroParcelable sd = (SpheroParcelable) intent.getParcelableExtra(EXTRA_DEVICE);
                mListener.get().onDeviceDisconnected(sd);
            } else if (action.equals(ACTION_DELETED)) {
                SpheroParcelable sd = (SpheroParcelable) intent.getParcelableExtra(EXTRA_DEVICE);
                mListener.get().onDeviceDeleted(sd);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(mReceiver);
    }
    
    /**
     * デバイスの検知情報などの通知を受けるリスナー.
     */
    public interface DeviceControlListener {
        
        /**
         * 接続済みのデバイスを受信した場合に呼び出される.
         * 
         * @param devices 接続済みデバイスの一覧。無い場合は空のリストが渡される。
         */
        void onConnectedDevices(List<Parcelable> devices);
        
        /**
         * デバイスが見つかった場合に呼び出される.
         * 
         * @param device デバイス
         */
        void onDeviceFound(SpheroParcelable device);
        
        /**
         * デバイスが消失した場合に呼び出される.
         * 
         * @param device デバイス
         */
        void onDeviceLost(SpheroParcelable device);
        
        /**
         * すべてのデバイスの消失を通知します.
         */
        void onDeviceLostAll();
        
        /**
         * デバイスが接続された場合に呼び出される.
         * 
         * @param device デバイス
         */
        void onDeviceConnected(SpheroParcelable device);
        /**
         * デバイスが切断された場合に呼び出される.
         *
         * @param device デバイス
         */
        void onDeviceDisconnected(SpheroParcelable device);
        /**
         * デバイスが削除された場合に呼び出される.
         *
         * @param device デバイス
         */
        void onDeviceDeleted(SpheroParcelable device);
    }
}
