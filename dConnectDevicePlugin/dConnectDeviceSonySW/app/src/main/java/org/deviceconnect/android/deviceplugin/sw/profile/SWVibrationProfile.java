/*
 SWVibrationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import android.content.Intent;

import com.sonyericsson.extras.liveware.aef.control.Control;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.deviceplugin.sw.service.SWService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.VibrationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

/**
 * SonySWデバイスプラグインの{@link VibrationProfile}実装.
 * @author NTT DOCOMO, INC.
 */
public class SWVibrationProfile extends VibrationProfile {

    private final DConnectApi mPutVibrateApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            long[] pattern = parsePattern(getPattern(request));
            if (pattern == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }
            runThread(getService(), pattern);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mDeleteVibrateApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_VIBRATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Intent intent = new Intent(Control.Intents.CONTROL_STOP_VIBRATE_INTENT);
            sendToHostApp(intent);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    public SWVibrationProfile(final SWService.WatchType type) {
        addApi(mPutVibrateApi);
        switch (type) {
            case SW2:
                break;
            case MN2:
                addApi(mDeleteVibrateApi);
                break;
            default:
                break;
        }
    }

    /**
     * リクエストパラメータ省略時のバイブレーション鳴動時間.
     * 
     * @return デフォルト鳴動時間
     */
    @Override
    protected long getMaxVibrationTime() {
        return SWConstants.MAX_VIBRATION_TIME;
    }

    /**
     * SmartExtensionAPIの仕様とDeviceConnectのpatternの仕様が違うため.
     * 非同期で処理をDeviceConnect側の仕様に合わせる 例) pattern=100,10,50 で
     * 100msec振動、10msec止まる、50msec振動
     *
     * @param service サービス
     * @param origPattern 振動パターン
     */
    private void runThread(final DConnectService service, final long[] origPattern) {
        Thread thread = new Thread() {
            public void run() {
                long[] pattern;
                if (origPattern.length % 2 != 0) {
                    pattern = new long[origPattern.length + 1];
                    for (int i = 0; i < origPattern.length; i++) {
                        pattern[i] = origPattern[i];
                    }
                    pattern[pattern.length - 1] = 0;
                } else {
                    pattern = origPattern;
                }

                long prevInterval = 0;
                for (int cnt = 0; cnt < pattern.length; cnt += 2) {
                    try {
                        if (prevInterval > 0) {
                            Thread.sleep(prevInterval);
                        }
                    } catch (InterruptedException e) {
                        // スレッドに対して割り込まれたら終了
                        return;
                    }

                    long on = (pattern[cnt] > 0) ? pattern[cnt] : 0;
                    long off = (pattern[cnt + 1] > 0) ? pattern[cnt + 1] : 0;
                    prevInterval = on + off;
                    intentPut((int) on, (int) off, 1, service);
                }
            }
        };
        thread.start();
    }

    /**
     * intent送信.
     * 
     * @param onDuration 振動時間
     * @param offDuration 振動しない時間
     * @param repeats 繰り返し回数
     * @param service サービス
     */
    protected void intentPut(final int onDuration, final int offDuration, final int repeats,
                             final DConnectService service) {
        if (onDuration <= 0) {
            return;
        }
        int onDURATION;
        if (onDuration > SWConstants.MAX_VIBRATION_TIME) {
            onDURATION = SWConstants.MAX_VIBRATION_TIME;
        } else {
            onDURATION = onDuration;
        }

        Intent intent = new Intent(Control.Intents.CONTROL_VIBRATE_INTENT);
        intent.putExtra(Control.Intents.EXTRA_ON_DURATION, onDURATION);
        intent.putExtra(Control.Intents.EXTRA_OFF_DURATION, offDuration);
        intent.putExtra(Control.Intents.EXTRA_REPEATS, repeats);
        sendToHostApp(intent);
    }

    private void sendToHostApp(final Intent request) {
        ((SWService) getService()).sendRequest(request);
    }
}
