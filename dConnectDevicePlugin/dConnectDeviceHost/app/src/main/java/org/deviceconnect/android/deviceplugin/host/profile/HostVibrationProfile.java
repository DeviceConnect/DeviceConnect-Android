/*
 HostVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.concurrent.Executors;

/**
 * Vibration Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostVibrationProfile extends VibrationProfile {

    /**
     * 振動をキャンセルする事を示すフラグ.
     */
    private boolean mIsCancelled = false;

    private final DConnectApi mVibrationStartApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final long[] pattern = parsePattern(getPattern(request));

            if (pattern == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            } else {
                final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

                // Nexus7はVibratorなし
                if (vibrator == null || !vibrator.hasVibrator()) {
                    MessageUtils.setNotSupportAttributeError(response);
                    return true;
                }

                // Check pattern parameter.
                for (Long value : pattern) {
                    if (value < 0) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        return true;
                    }
                }

                // 振動パターンを開始させたら、すぐに処理を続けたいので、
                // 振動パターン再生部分は別スレッドで実行。
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean vibrateMode = true;
                    for (Long dur : pattern) {
                        if (mIsCancelled) {
                            break;
                        }

                        if (vibrateMode) {
                            vibrator.vibrate(dur);
                        }

                        // 振動モード: vibrate()は直にリターンされるので、振動時間分だけ待ち時間を入れる。
                        // 無振動モード: 無振動時間分だけ待ち時間を入れる。
                        try {
                            Thread.sleep(dur);
                        } catch (InterruptedException e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        vibrateMode = !vibrateMode;
                    }
                });

                // 振動パターン再生セッションを終えたので、キャンセルフラグを初期化。
                mIsCancelled = false;

                setResult(response, IntentDConnectMessage.RESULT_OK);

            }
            return true;
        }
    };

    private final DConnectApi mVibrationStopApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

            if (vibrator == null || !vibrator.hasVibrator()) {
                setResult(response, IntentDConnectMessage.RESULT_ERROR);
            } else {
                vibrator.cancel();
            }

            // cancel()は現在されているの振調パターンの1節しかキャンセルしないので、
            // それ以降の振動パターンの節の再生を防ぐ為に、キャンセルされたことを示す
            // フラグをたてる。
            mIsCancelled = true;

            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    public HostVibrationProfile() {
        addApi(mVibrationStartApi);
        addApi(mVibrationStopApi);
    }

}
