/*
HueFargment02
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;

import org.deviceconnect.android.deviceplugin.hue.db.HueManager;
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

    /** ステータスを表示するTextView. */
    private TextView mTextViewStatus;

    /** Howtoを表示するTextView. */
    private TextView mTextViewHowTo;

    /** Button. */
    private Button mButton;

    /** ImageView. */
    private ImageView mImageView;

    /** ハンドラー用のCounter. */
    private int mCount = 0;

    /** ステータス. */
    private HueManager.HueState mHueStatus = HueManager.HueState.INIT;

    /** アニメーション用スレッド. */
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /** スレッドキャンセル用オブジェクト. */
    private ScheduledFuture<?> mFuture;



    /**
     * hueブリッジのNotificationを受け取るためのリスナー.
     */
    private PHSDKListener mListener = new PHSDKListener() {

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
            mHueStatus = HueManager.HueState.INIT;
            HueManager.INSTANCE.startPushlinkAuthentication(accessPoint);
            authenticateHueBridge();
        }

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoint) {
        }

        @Override
        public void onCacheUpdated(final List<Integer> list, final PHBridge bridge) {
        }

        @Override
        public void onBridgeConnected(final PHBridge phBridge, final String s) {
            mHueStatus = HueManager.HueState.AUTHENTICATE_SUCCESS;
            successAuthorization();
        }

        @Override
        public void onConnectionLost(final PHAccessPoint accessPoint) {
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
        }

        @Override
        public void onError(final int code, final String message) {
            if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                failAuthorization();
            } else if (code == PHHueError.NO_CONNECTION || code == PHHueError.BRIDGE_NOT_RESPONDING) {
                showNotConnection();
            }
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> errors) {
        }
    };

    /**
     * HueFragment02を返す.
     *
     * @param accessPoint 選択されたアクセスポイント
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
        TextView macTextView = rootView.findViewById(R.id.text_mac);
        macTextView.setText(mAccessPoint.getMacAddress());

        // IPアドレスを画面に反映.
        TextView ipTextView = rootView.findViewById(R.id.text_ip);
        ipTextView.setText(mAccessPoint.getIpAddress());

        // 現在の状態を表示.
        mTextViewStatus = rootView.findViewById(R.id.textStatus);

        // 作業方法を表示.
        mTextViewHowTo = rootView.findViewById(R.id.textHowto);

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
        mHueStatus = HueManager.HueState.INIT;

        HueManager.INSTANCE.addSDKListener(mListener);
        // Hueブリッジへの認証開始
        startAuthenticate();
    }

    @Override
    public void onPause() {
        HueManager.INSTANCE.stopPushlinkAuthentication();
        HueManager.INSTANCE.removeSDKListener(mListener);
        stopAnimation();

        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        if (mHueStatus == HueManager.HueState.AUTHENTICATE_SUCCESS) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
            transaction.replace(R.id.fragment_frame, HueFragment03.newInstance(mAccessPoint));
            transaction.commit();
        } else {
            mButton.setVisibility(View.INVISIBLE);
            startAuthenticate();
        }
    }

    private void setPHAccessPoint(final PHAccessPoint accessPoint) {
        mAccessPoint = accessPoint;
    }

    /**
     * Hueブリッジのボタン押下アニメーションを開始します.
     */
    private synchronized void startAnimation() {
        if (mFuture != null) {
            mFuture.cancel(false);
        }

        mFuture = mExecutor.scheduleAtFixedRate(() -> {
            switch (mHueStatus) {
                case INIT:
                    nextImage();
                    break;
                default:
                case NO_CONNECT:
                case AUTHENTICATE_FAILED:
                case AUTHENTICATE_SUCCESS:
                    stopAnimation();
                    break;
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Hueブリッジのボタン押下アニメーションを停止します.
     */
    private synchronized void stopAnimation() {
        if (mFuture != null) {
            mFuture.cancel(false);
            mFuture = null;
        }
    }

    /**
     * 指定されたRunnableをUIスレッド上で実行します.
     * @param run 実行するRunnable
     */
    private void runOnUiThread(final Runnable run) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(run);
        }
    }

    /**
     * 次の画像を表示します.
     */
    private void nextImage() {
        runOnUiThread(new Runnable() {
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

    /**
     * 認証成功を表示します.
     */
    private void successAuthorization() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewStatus.setText(R.string.frag02_authsuccess);
                mTextViewHowTo.setText(R.string.frag02_authsuccess_howto);
                mImageView.setImageResource(R.drawable.img05);
                mButton.setText(R.string.frag02_authsuccess_btn);
                mButton.setVisibility(View.VISIBLE);

                String message = getString(R.string.frag02_connected);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 認証失敗を表示します.
     */
    private void failAuthorization() {
        runOnUiThread(() -> {
            stopAnimation();

            mTextViewStatus.setText(R.string.frag02_failed);
            mTextViewHowTo.setText("");
            mImageView.setImageResource(R.drawable.img01);
            mButton.setText(R.string.frag02_retry_btn);
            mButton.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Hueブリッジの認証を行います.
     * <p>
     * ユーザにHueブリッジのボタンを押下してもらう必要があります。
     * </p>
     */
    private void startAuthenticate() {
        HueManager.INSTANCE.startAuthenticate(mAccessPoint, new HueManager.HueConnectionListener() {
            @Override
            public void onConnected() {
                mHueStatus = HueManager.HueState.AUTHENTICATE_SUCCESS;
                successAuthorization();
            }

            @Override
            public void onNotConnected() {
                authenticateHueBridge();
            }
        });
    }

    /**
     * Hueブリッジへの誘導を行う文言を表示します.
     */
    private void authenticateHueBridge() {
        runOnUiThread(() -> {
            mTextViewStatus.setText(R.string.frag02_init);
            mTextViewHowTo.setText(R.string.frag02_init_howto);
            mButton.setText(R.string.frag02_retry_btn);
            mButton.setVisibility(View.INVISIBLE);
            startAnimation();
        });
    }

    /**
     * Hueブリッジからレスポンスがなかった場合のエラーダイアログを表示します.
     */
    private void showNotConnection() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.frag02_failed)
                    .setMessage(R.string.hue_dialog_no_connect)
                    .setPositiveButton(R.string.hue_dialog_ok, (dialog, which) -> {
                        moveFirstFragment();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    /**
     * 最初のフラグメントに移動します.
     */
    private void moveFirstFragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
        transaction.replace(R.id.fragment_frame, new HueFragment01());
        transaction.commit();
    }
}
