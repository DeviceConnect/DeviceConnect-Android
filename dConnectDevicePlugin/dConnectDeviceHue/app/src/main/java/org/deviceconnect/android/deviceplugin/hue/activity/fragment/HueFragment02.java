/*
HueFargment02
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import android.app.Activity;
import android.os.Bundle;
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

import org.deviceconnect.android.deviceplugin.hue.HueConstants;
import org.deviceconnect.android.deviceplugin.hue.R;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Hue設定画面(2)フラグメント.
 * 
 */
public class HueFragment02 extends Fragment implements OnClickListener {

    /** アクセスポイント. */
    private PHAccessPoint mAccessPoint;

    /** HueSDKオブジェクト. */
    private PHHueSDK mPhHueSDK;

    /** ステータスを表示するTextView. */
    private TextView mTextViewStatus;

    /** Howtoを表示するTextView. */
    private TextView mTextViewHowto;

    /** Button. */
    private Button mButton;

    /** ImageView. */
    private ImageView mImageView;

    /** 前回IPアドレスがスキャンできたかのフラグ. */
    private boolean mLastSearchWasIPScan = false;

    /** ハンドラー用のCounter. */
    private int mCount = 0;
    /** ステータス. */
    private HueState mHueStatus = HueState.INIT;

    /** アニメーション用スレッド. */
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    /** スレッドキャンセル用オブジェクト. */
    private ScheduledFuture<?> mFuture;

    /**
     * Hue接続状態.
     */
    private enum HueState {
        /** 未認証. */
        INIT,
        /** 未接続. */
        NOCONNECT,
        /** 認証失敗. */
        AUTHENTICATE_FAILED,
        /** 認証済み. */
        AUTHENTICATE_SUCCESS
    };

    /**
     * hueブリッジのNotificationを受け取るためのリスナー.
     */
    private PHSDKListener mListener = new PHSDKListener() {
        @Override
        public void onBridgeConnected(final PHBridge b) {
            mHueStatus = HueState.AUTHENTICATE_SUCCESS;
            successAuthorization();

            // 接続.
            mPhHueSDK.setSelectedBridge(b);
            mPhHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            mPhHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration().getIpAddress(),
                    System.currentTimeMillis());
        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
            mHueStatus = HueState.INIT;
            failAuthorization();

            // 認証を実施.
            mPhHueSDK.startPushlinkAuthentication(accessPoint);
        }

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoint) {
        }

        @Override
        public void onCacheUpdated(final List<Integer> list, final PHBridge bridge) {
        }

        @Override
        public void onConnectionLost(final PHAccessPoint accessPoint) {
            if (!mPhHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
                mPhHueSDK.getDisconnectedAccessPoint().add(accessPoint);
            }
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
            mPhHueSDK.getLastHeartbeat().put(bridge.getResourceCache().getBridgeConfiguration().getIpAddress(),
                    System.currentTimeMillis());
            for (int i = 0; i < mPhHueSDK.getDisconnectedAccessPoint().size(); i++) {
                if (mPhHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress()
                        .equals(bridge.getResourceCache().getBridgeConfiguration().getIpAddress())) {
                    mPhHueSDK.getDisconnectedAccessPoint().remove(i);
                }
            }
        }

        @Override
        public void onError(final int code, final String message) {
            if (code == PHMessageType.BRIDGE_NOT_FOUND) {
                if (!mLastSearchWasIPScan) {
                    mPhHueSDK = PHHueSDK.getInstance();
                    PHBridgeSearchManager sm = (PHBridgeSearchManager) mPhHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
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
     * 
     * @param accessPoint Access Point
     * @return HueFragment02
     */
    public static HueFragment02 newInstance(final PHAccessPoint accessPoint) {
        HueFragment02 fragment = new HueFragment02();
        fragment.setPHAccessPoint(accessPoint);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.hue_fragment_02, container, false);
        // Macアドレスを画面に反映.
        TextView macTextView = (TextView) rootView.findViewById(R.id.text_mac);
        macTextView.setText(mAccessPoint.getMacAddress());

        // IPアドレスを画面に反映.
        TextView ipTextView = (TextView) rootView.findViewById(R.id.text_ip);
        ipTextView.setText(mAccessPoint.getIpAddress());

        // 現在の状態を表示.
        mTextViewStatus = (TextView) rootView.findViewById(R.id.textStatus);

        // 作業方法を表示.
        mTextViewHowto = (TextView) rootView.findViewById(R.id.textHowto);

        // ボタン.
        mButton = (Button) rootView.findViewById(R.id.btnBridgeTouroku);
        mButton.setOnClickListener(this);
        mButton.setVisibility(View.GONE);

        // 画像を表示.
        mImageView = (ImageView) rootView.findViewById(R.id.iv01);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // ステータスを初期状態(INIT)に設定.
        mHueStatus = HueState.INIT;
        mTextViewStatus.setText(R.string.frag02_init);
        mTextViewHowto.setText(R.string.frag02_init_howto);

        // Hueのインスタンスを取得.
        mPhHueSDK = PHHueSDK.getInstance();
        // HueブリッジからのCallbackを受け取るためのリスナーを登録.
        mPhHueSDK.getNotificationManager().registerSDKListener(mListener);

        // User名を追加.
        mAccessPoint.setUsername(HueConstants.USERNAME);

        // アクセスポイントに接続.
        if (!mPhHueSDK.isAccessPointConnected(mAccessPoint)) {
            mPhHueSDK.connect(mAccessPoint);
            startAnimation();
        } else {
            mHueStatus = HueState.AUTHENTICATE_SUCCESS;
            successAuthorization();
        }
    }

    @Override
    public void onDestroy() {
        mPhHueSDK.getNotificationManager().unregisterSDKListener(mListener);
        mPhHueSDK.disableAllHeartbeat();

        stopAnimation();

        super.onDestroy();
    }

    @Override
    public void onClick(final View v) {
        if (mHueStatus == HueState.AUTHENTICATE_SUCCESS) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
            transaction.replace(R.id.fragment_frame, HueFragment03.newInstance(mAccessPoint));
            transaction.commit();
        } else {
            mButton.setVisibility(View.INVISIBLE);

            // アクセスポイントに接続.
            if (!mPhHueSDK.isAccessPointConnected(mAccessPoint)) {
                mPhHueSDK.connect(mAccessPoint);
            } else {
                mHueStatus = HueState.AUTHENTICATE_SUCCESS;
            }

            // アニメーションの開始.
            startAnimation();
        }
    }

    private void setPHAccessPoint(final PHAccessPoint accessPoint) {
        mAccessPoint = accessPoint;
    }

    private synchronized void startAnimation() {
        mFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                switch (mHueStatus) {
                    case INIT:
                        nextImage();
                        break;
                    default:
                    case NOCONNECT:
                    case AUTHENTICATE_FAILED:
                    case AUTHENTICATE_SUCCESS:
                        stopAnimation();
                        break;
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void stopAnimation() {
        if (mFuture != null) {
            mFuture.cancel(false);
            mFuture = null;
        }
    }

    private void nextImage() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCount == 0) {
                        mImageView.setImageResource(R.drawable.img01);
                    } else {
                        mImageView.setImageResource(R.drawable.img02);
                    }
                    mCount++;
                    mCount %= 2;
                }
            });
        }
    }

    private void successAuthorization() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextViewStatus.setText(R.string.frag02_authsuccess);
                    mTextViewHowto.setText(R.string.frag02_authsuccess_howto);
                    mImageView.setImageResource(R.drawable.img05);
                    mButton.setText(R.string.frag02_authsuccess_btn);
                    mButton.setVisibility(View.VISIBLE);

                    String message = getString(R.string.frag02_connected);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void failAuthorization() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextViewStatus.setText(R.string.frag02_failed);
                    mTextViewHowto.setText("");
                    mImageView.setImageResource(R.drawable.img01);
                    mButton.setText(R.string.frag02_retry_btn);
                    mButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
