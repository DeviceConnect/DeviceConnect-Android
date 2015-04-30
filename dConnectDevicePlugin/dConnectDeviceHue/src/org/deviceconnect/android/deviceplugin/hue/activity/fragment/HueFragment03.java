/*
HueFargment03
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deviceconnect.android.deviceplugin.hue.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;

/**
 * Hue設定画面(3)フラグメント.
 */
public class HueFragment03 extends Fragment implements OnClickListener {

    /** ライトの登録. */
    private static Button sButtonRegister;

    /** HueSDK. */
    private static PHHueSDK sPhHueSDK;

    /** ProgressView. */
    private View mProgressView;

    /** 接続したアクセスポイント. */
    private final PHAccessPoint mAccessPoint;

    /** Activity. */
    private Activity mActivity;

    /** Search flag. */
    private Boolean mIsSearch = false;

    /**
     * ライト検索リスナー.
     */
    private class PHLightListenerImpl implements PHLightListener {

        /** LightHeader. */
        private final List<PHBridgeResource> mLightHeaders = new LinkedList<PHBridgeResource>();

        @Override
        public void onError(final int code, final String message) {
            mIsSearch = false;
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onReceivingLights(final List<PHBridgeResource> lightHeaders) {
            for (PHBridgeResource header : lightHeaders) {
                boolean duplicated = false;
                for (PHBridgeResource cache : mLightHeaders) {
                    if (cache.getIdentifier().equals(header.getIdentifier())) {
                        duplicated = true;
                        break;
                    }
                }
                if (!duplicated) {
                    mLightHeaders.add(header);
                }
            }
        }

        @Override
        public void onSearchComplete() {
            if (sPhHueSDK != null) {
                PHBridge b = sPhHueSDK.getSelectedBridge();
                if (b != null) {
                    if (sPhHueSDK.isHeartbeatEnabled(b)) {
                        sPhHueSDK.disableHeartbeat(b);
                    }
                    int count = 0;
                    Boolean result = false;
                    do {
                        result = sPhHueSDK.disconnect(b);
                    } while (count++ < 3 && result == false);

                    if (!sPhHueSDK.isAccessPointConnected(mAccessPoint)) {
                        sPhHueSDK.connect(mAccessPoint);
                    }
                    sPhHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
                    sPhHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration().getIpAddress(),
                            System.currentTimeMillis());
                }
            }
            mIsSearch = false;

            if (mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivity != null) {
                            mProgressView.setVisibility(View.GONE);
                            mProgressView.invalidate();
                            String message = getString(R.string.frag03_light_result1);
                            message += mLightHeaders.size() + getString(R.string.frag03_light_result2);
                            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        @Override
        public void onStateUpdate(final Map<String, String> list, final  List<PHHueError> error) {
        }

        @Override
        public void onReceivingLightDetails(final PHLight light) {
        }
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    /**
     * コンストラクタ.
     * @param accessPoint Access Point
     */
    public HueFragment03(final PHAccessPoint accessPoint) {
        this.mAccessPoint = accessPoint;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.hue_fragment_03, container, false);
        if (mRootView != null) {
            sButtonRegister = (Button) mRootView.findViewById(R.id.btnSearchLight);
            sButtonRegister.setOnClickListener(this);
            mProgressView = mRootView.findViewById(R.id.progress_light_search);
        }
        return mRootView;
    }

    /**
     * ライト検索.
     */
    private void searchLight() {

        sPhHueSDK = PHHueSDK.create();
        PHBridge bridge = sPhHueSDK.getSelectedBridge();
        
        if (bridge == null) {
            mProgressView.setVisibility(View.GONE);
            mIsSearch = false;
            return;
        } else {
            mProgressView.setVisibility(View.VISIBLE);
            bridge.findNewLights(new PHLightListenerImpl());
        }
    }


    @Override
    public void onClick(final View view) {
        if (view.equals(sButtonRegister)) {
            if (!mIsSearch) {
                mIsSearch = true;
                searchLight();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsSearch = false;
    }
}
