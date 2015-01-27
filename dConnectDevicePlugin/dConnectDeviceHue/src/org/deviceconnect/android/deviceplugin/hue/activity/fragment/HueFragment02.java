/*
HueFargment02
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import java.util.List;

import org.deviceconnect.android.deviceplugin.hue.HueConstants;
import org.deviceconnect.android.deviceplugin.hue.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

/**
 * Hue設定画面(2)フラグメント.
 * 
 */
public class HueFragment02 extends Fragment implements OnClickListener {

    /** アクセスポイント. */
    private static PHAccessPoint sAccessPoint;

    /** HueSDKオブジェクト. */
    private static PHHueSDK sPhHueSDK;

    /** ステータスを表示するTextView. */
    private static TextView sTextViewStatus;

    /** Howtoを表示するTextView. */
    private static TextView sTextViewHowto;

    /** Button. */
    private static Button sButton;

    /** ステータス. */
    private static HueState sHueStatus = HueState.INIT;

    /** ImageView. */
    private static ImageView sImageView;

    /** 前回IPアドレスがスキャンできたかのフラグ. */
    private boolean mLastSearchWasIPScan = false;
    
    /** 次のハンドラーの時間. */
    private static long sNextTime;
    
    /** ハンドラーの再呼び出しの命令. */
    private static final int INVALIDATE = 1;
    
    /** ハンドラー用のCounter. */
    private static int sCount = 0;
    
    /** 周期. */
    private static final int CYCLE = 1000;
    
    /** Activity. */
    private Activity mActivity;
    
    /**
     * Hue接続状態.
     */
    private enum HueState {
        /** 未認証. */
        INIT,
        /** 未接続. */
        NOCONNECT,
        /** 認証失敗. */
        AUTHENTICATE_FAILD,
        /** 認証済み. */
        AUTHENTICATE_SUCCESS
    };
    /**
     * タイマーハンドラー.
     */
    private static final Handler HANDLER = new Handler() {
        @Override
        public void handleMessage(final Message message) {
            Message msg = message;
            if (msg.what == INVALIDATE) {
                if (sHueStatus == HueState.INIT) {
                    
                    // 画像をアニメーション.
                    if (sCount == 0) {
                        sImageView.setImageResource(R.drawable.img01);
                        sCount = 1;
                    } else {
                        sImageView.setImageResource(R.drawable.img02);
                        sCount = 0;
                    }
                    
                    msg = obtainMessage(INVALIDATE);
                    long current = SystemClock.uptimeMillis();

                    if (sNextTime < current) {
                        // 1000ms周期でタイマーイベントが発生
                        sNextTime = current + CYCLE;
                    }
                    sendMessageAtTime(msg, sNextTime);

                    // 1000ms周期でタイマーイベントが発生
                    sNextTime += CYCLE;

                } else if (sHueStatus == HueState.AUTHENTICATE_SUCCESS) {
                    
                    sTextViewStatus.setText(R.string.frag02_authsuccess);
                    sTextViewHowto.setText(R.string.frag02_authsuccess_howto);
                    sImageView.setImageResource(R.drawable.img05);
                    sButton.setText(R.string.frag02_authsuccess_btn);
                    
                    sTextViewStatus.invalidate();
                    sTextViewHowto.invalidate();
                    sImageView.invalidate();
                    sButton.setVisibility(View.VISIBLE);

                } else if (sHueStatus == HueState.AUTHENTICATE_FAILD) {

                    sTextViewStatus.setText(R.string.frag02_failed);
                    sTextViewHowto.setText("");
                    sImageView.setImageResource(R.drawable.img01);
                    sButton.setText(R.string.frag02_retry_btn);

                    sTextViewHowto.invalidate();
                    sImageView.invalidate();
                    sTextViewStatus.invalidate();
                    sButton.setVisibility(View.VISIBLE);

                } else if (sHueStatus == HueState.NOCONNECT) {

                    sTextViewStatus.setText(R.string.frag02_failed);
                    sTextViewHowto.setText("");
                    sImageView.setImageResource(R.drawable.img01);
                    sButton.setText(R.string.frag02_retry_btn);

                    sTextViewHowto.invalidate();
                    sImageView.invalidate();
                    sTextViewStatus.invalidate();
                    sButton.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    
    /**
     * hueブリッジのNotificationを受け取るためのリスナー.
     */
    private PHSDKListener mListener = new PHSDKListener() {

        @Override
        public void onBridgeConnected(final PHBridge b) {
            sHueStatus = HueState.AUTHENTICATE_SUCCESS;
            HANDLER.sendEmptyMessage(INVALIDATE);
            
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message = getString(R.string.frag02_connected);
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            // 接続.
            sPhHueSDK.setSelectedBridge(b);
            sPhHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            sPhHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration().getIpAddress(),
                    System.currentTimeMillis());
        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
            sHueStatus = HueState.INIT;

            // 認証を実施.
            sPhHueSDK.startPushlinkAuthentication(accessPoint);

            // アニメーションの開始.
            HANDLER.sendEmptyMessage(INVALIDATE);
        }

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoint) {
        }

        @Override
        public void onCacheUpdated(final List<Integer> list, final PHBridge bridge) {
        }

        @Override
        public void onConnectionLost(final PHAccessPoint accessPoint) {
            
            if (!sPhHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
                sPhHueSDK.getDisconnectedAccessPoint().add(accessPoint);
            }
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
            
            sPhHueSDK.getLastHeartbeat().put(bridge.getResourceCache()
                    .getBridgeConfiguration().getIpAddress(),  System.currentTimeMillis());
            for (int i = 0; i < sPhHueSDK.getDisconnectedAccessPoint().size(); i++) {

                if (sPhHueSDK.getDisconnectedAccessPoint().get(i)
                        .getIpAddress().equals(bridge.getResourceCache()
                                .getBridgeConfiguration().getIpAddress())) {
                    sPhHueSDK.getDisconnectedAccessPoint().remove(i);
                }
            }
            
        }

        @Override
        public void onError(final int code, final String message) {
            if (code == PHMessageType.BRIDGE_NOT_FOUND) {
                if (!mLastSearchWasIPScan) {
                    sPhHueSDK = PHHueSDK.getInstance();
                    PHBridgeSearchManager sm = (PHBridgeSearchManager) sPhHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                    sm.search(false, false, true);               
                    mLastSearchWasIPScan = true;
                } 
            }
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> errors) {
        }
    };

    
    /**
     * HueFragment02を返す.
     * @param accessPoint Access Point
     * @return HueFragment02
     */
    public static HueFragment02 newInstance(final PHAccessPoint accessPoint) {
        HueFragment02 fragment = new HueFragment02();

        sAccessPoint = accessPoint;

        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        View mRootView = inflater.inflate(R.layout.hue_fragment_02, container, false);

        mActivity = this.getActivity();
        
        if (mRootView != null) {

            // Macアドレスを画面に反映.
            TextView mMacTextView = (TextView) mRootView.findViewById(R.id.text_mac);
            mMacTextView.setText(sAccessPoint.getMacAddress());

            // IPアドレスを画面に反映.
            TextView mIpTextView = (TextView) mRootView.findViewById(R.id.text_ip);
            mIpTextView.setText(sAccessPoint.getIpAddress());

            // 現在の状態を表示.
            sTextViewStatus = (TextView) mRootView.findViewById(R.id.textStatus);

            // 作業方法を表示.
            sTextViewHowto = (TextView) mRootView.findViewById(R.id.textHowto);

            // ボタン.
            sButton = (Button) mRootView.findViewById(R.id.btnBridgeTouroku);
            sButton.setOnClickListener(this);
            sButton.setVisibility(View.GONE);

            // 画像を表示.
            sImageView = (ImageView) mRootView.findViewById(R.id.iv01);
        }

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // ステータスを初期状態(INIT)に設定.
        sHueStatus = HueState.INIT;
        sTextViewStatus.setText(R.string.frag02_init);
        sTextViewHowto.setText(R.string.frag02_init_howto);
        
        // Hueのインスタンスを取得.
        sPhHueSDK = PHHueSDK.create();
        
        // HueブリッジからのCallbackを受け取るためのリスナーを登録.
        sPhHueSDK.getNotificationManager().registerSDKListener(mListener);

        // User名を追加.
        sAccessPoint.setUsername(HueConstants.USERNAME);
        
        // アクセスポイントに接続.
        if (!sPhHueSDK.isAccessPointConnected(sAccessPoint)) {
            sPhHueSDK.connect(sAccessPoint);
        } else {
            sHueStatus = HueState.AUTHENTICATE_SUCCESS;
        }
        
        // アニメーションの開始.
        HANDLER.sendEmptyMessageDelayed(INVALIDATE, CYCLE);
    }
    


    @Override
    public void onClick(final View v) {

        if (sHueStatus == HueState.AUTHENTICATE_SUCCESS) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.setCustomAnimations(R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_left_enter, 
                    R.anim.fragment_slide_right_exit);
            
            transaction.replace(R.id.fragment_frame, new HueFragment03(sAccessPoint));

            transaction.commit();

        } else {
            sButton.setVisibility(View.INVISIBLE);

            // アクセスポイントに接続.
            if (!sPhHueSDK.isAccessPointConnected(sAccessPoint)) {
                sPhHueSDK.connect(sAccessPoint);
            } else {
                sHueStatus = HueState.AUTHENTICATE_SUCCESS;
            }
            
            // アニメーションの開始.
            HANDLER.sendEmptyMessageDelayed(INVALIDATE, CYCLE);
        }

    }

    // 画面終了
    @Override
    public void onDestroy() {

        if (mListener != null) {
            sPhHueSDK.getNotificationManager().unregisterSDKListener(mListener);
        }
        sPhHueSDK.disableAllHeartbeat();

        super.onDestroy();

    }
}
