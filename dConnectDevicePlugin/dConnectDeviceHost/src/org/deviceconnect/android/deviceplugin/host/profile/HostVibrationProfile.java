/*
 HostVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

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

    @Override
    protected boolean onPutVibrate(final Intent request, final Intent response, final String serviceId,
            final long[] pattern) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (pattern == null) {
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
            Executors.newSingleThreadExecutor().execute(new Thread() {
                public void run() {
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
                };
            });

            // 振動パターン再生セッションを終えたので、キャンセルフラグを初期化。
            mIsCancelled = false;

            setResult(response, IntentDConnectMessage.RESULT_OK);

        }
        return true;
    }

    @Override
    protected boolean onDeleteVibrate(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {

            // Vibration Stop API
            if (ATTRIBUTE_VIBRATE.equals(getAttribute(request))) {
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
            } else {
                setResult(response, IntentDConnectMessage.RESULT_ERROR);
            }
        }
        return true;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);

        return match.find();
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }
}
