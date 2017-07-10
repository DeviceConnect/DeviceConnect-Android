package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * GPIO用のVibrationプロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #105<br>
 * Name: Vibration Brick<br>
 * </p>
 */
public class GPIOVibrationProfile extends BaseFaBoProfile {

    /**
     * 振動パターンで使われる区切り文字.
     *
     */
    private static final String VIBRATION_PATTERN_DELIM = ",";

    /**
     * バイブレーションに接続されているピンのリスト.
     */
    private List<FaBoShield.Pin> mPinList;

    /**
     * ライトフラッシング管理マップ.
     */
    private FlashingExecutor mFlashingExecutor;

    /**
     * コンストラクタ.
     * @param pinList 操作を行うピンのリスト
     */
    public GPIOVibrationProfile(final List<FaBoShield.Pin> pinList) {
        mPinList = pinList;

        // PUT /gotapi/vibration/vibrate
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "vibrate";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final long[] pattern = parsePattern(request);

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (mPinList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "Vibration does not exist.");
                } else {
                    if (pattern != null) {
                        flashing(pattern);
                    } else {
                        for (FaBoShield.Pin pin : mPinList) {
                            sendVibrateOn(pin);
                        }
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });


        // DELETE /gotapi/vibration/vibrate
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "vibrate";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (mPinList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "Vibration does not exist.");
                } else {
                    for (FaBoShield.Pin pin : mPinList) {
                        sendVibrateOff(pin);
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "vibration";
    }

    /**
     * 鳴動パターンを文字列から解析し、数値の配列に変換する.<br>
     * 数値の前後の半角のスペースは無視される。その他の半角、全角のスペースは不正なフォーマットとして扱われる。
     *
     * @param request リクエスト
     * @return 鳴動パターンの配列。解析できないフォーマットの場合nullを返す。
     */
    private long[] parsePattern(final Intent request) {
        final String pattern = request.getStringExtra("pattern");

        if (pattern == null || pattern.length() == 0) {
            return new long[] { 500 };
        }

        long[] result = null;

        if (pattern.contains(VIBRATION_PATTERN_DELIM)) {
            String[] times = pattern.split(VIBRATION_PATTERN_DELIM);
            ArrayList<Long> values = new ArrayList<Long>();
            for (String time : times) {
                try {
                    String valueStr = time.trim();
                    if (valueStr.length() == 0) {
                        if (values.size() != times.length - 1) {
                            // 数値の間にスペースがある場合はフォーマットエラー
                            // ex. 100, , 100
                            values.clear();
                        }
                        break;
                    }
                    long value = Long.parseLong(time.trim());
                    if (value < 0) {
                        // 数値が負の値の場合はフォーマットエラー
                        values.clear();
                        break;
                    }
                    values.add(value);
                } catch (NumberFormatException e) {
                    values.clear();
                    mLogger.warning("Exception in the VibrationProfile#parsePattern() method. " + e.toString());
                    break;
                }
            }

            if (values.size() != 0) {
                result = new long[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    result[i] = values.get(i);
                }
            }
        } else {
            try {
                long time = Long.parseLong(pattern);
                if (time >= 0) {
                    result = new long[] {time};
                }
            } catch (NumberFormatException e) {
                mLogger.warning("Exception in the VibrationProfile#parsePattern() method. " + e.toString());
            }
        }

        return result;
    }

    /**
     * 指定されたPinのLEDを点滅させます.
     * @param flashing フラッシュ
     */
    private void flashing(final long[] flashing) {
        if (mFlashingExecutor == null) {
            mFlashingExecutor = new FlashingExecutor();
        }
        mFlashingExecutor.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(final boolean isOn, final FlashingExecutor.CompleteListener listener) {
                if (isOn) {
                    for (FaBoShield.Pin pin : mPinList) {
                        sendVibrateOn(pin);
                    }
                } else {
                    for (FaBoShield.Pin pin : mPinList) {
                        sendVibrateOff(pin);
                    }
                }
                listener.onComplete();
            }
        });
        mFlashingExecutor.start(flashing);
    }

    /**
     * Vibratorを振動させます.
     * @param pin Vibratorが挿さっているピン
     */
    private void sendVibrateOn(final FaBoShield.Pin pin) {
        if (pin.getMode() != FaBoShield.Mode.GPIO_OUT) {
            getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_OUT);
        }
        getFaBoDeviceControl().writeDigital(pin, FaBoShield.Level.HIGH);
    }

    /**
     * Vibratorの振動を止めます.
     * @param pin Vibratorが挿さっているピン
     */
    private void sendVibrateOff(final FaBoShield.Pin pin) {
        if (pin.getMode() != FaBoShield.Mode.GPIO_OUT) {
            getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_OUT);
        }
        getFaBoDeviceControl().writeDigital(pin, FaBoShield.Level.LOW);
    }
}
