package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.param.FaBoShield;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPIO用のLightプロファイル.
 * <p>
 *  ID: #101<br>
 *  Name: LED Brick<br>
 * </p>
 */
public class GPIOLightProfile extends BaseFaBoProfile {

    /**
     * ライト操作を行うピンのリスト.
     */
    private List<FaBoShield.Pin> mPinList;

    /**
     * ライトフラッシング管理マップ.
     */
    private final Map<String, FlashingExecutor> mFlashingMap = new HashMap<>();

    /**
     * コンストラクタ.
     * @param pinList ライトに対応するPinのリスト
     */
    public GPIOLightProfile(final List<FaBoShield.Pin> pinList) {
        mPinList = pinList;

        // GET /gotpai/light
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Bundle[] lightList = new Bundle[mPinList.size()];
                for (int i = 0; i < mPinList.size(); i++) {
                    FaBoShield.Pin pin = mPinList.get(i);
                    lightList[i] = new Bundle();
                    lightList[i].putString("lightId", String.valueOf(pin.getPinNumber()));
                    lightList[i].putString("name", pin.getPinNames()[1]);
                    lightList[i].putBoolean("on", getFaBoDeviceControl().getDigital(pin) == FaBoShield.Level.HIGH);
                    lightList[i].putString("config", "");
                }
                response.putExtra("lights", lightList);
                setResult(response,  DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // POST /gotpai/light
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return lightOn(request, response);
            }
        });

        // PUT /gotpai/light
        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return lightOn(request, response);
            }
        });

        // DELETE /gotpai/light
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String lightId = request.getStringExtra("lightId");

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (mPinList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "Light does not exist.");
                } else {
                    FaBoShield.Pin pin = findLight(lightId);
                    if (pin != null) {
                        sendLightOff(pin);
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "Not found the light. lightId=" + lightId);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "light";
    }

    /**
     * LEDを点灯します.
     * @param request リクエスト
     * @param response レスポンス
     * @return 即座にレスポンスを返却するフラグ
     */
    private boolean lightOn(final Intent request, final Intent response) {
        String lightId = request.getStringExtra("lightId");
        Float brightness = parseFloat(request, "brightness");

        long[] flashing;
        try {
            flashing = getFlashing(request);
        } catch (Exception e) {
            MessageUtils.setInvalidRequestParameterError(response, "Format of flashing is invalid.");
            return true;
        }

        if (!getService().isOnline()) {
            MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
        } else if (mPinList.isEmpty()) {
            MessageUtils.setInvalidRequestParameterError(response, "Light does not exist.");
        } else {
            FaBoShield.Pin pin = findLight(lightId);
            if (pin != null) {
                if (flashing != null) {
                    flashing(pin, flashing);
                } else {
                    sendLightOn(pin);
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Not found the light. lightId=" + lightId);
            }
        }
        return true;
    }

    /**
     * 点滅間隔を取得する.
     * <p>
     * flashingが省略された場合にはnullを返却する。
     * </p>
     * @param request リクエスト
     * @return 点滅間隔
     * @throws IllegalArgumentException flashingのフォーマットが不正な場合に発生
     */
    private static long[] getFlashing(final Intent request) {
        String flashing = request.getStringExtra("flashing");
        if (flashing == null) {
            return null;
        }
        if (flashing.length() == 0) {
            throw new IllegalArgumentException("flashing is invalid.");
        }
        String[] split = flashing.split(",");
        long[] list = new long[split.length];
        for (int i = 0; i < split.length; i++) {
            try {
                list[i] = Integer.parseInt(split[i]);
                if (list[i] <= 0) {
                    throw new IllegalArgumentException("flashing is negative value.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("flashing is invalid.");
            }
        }
        return list;
    }

    /**
     * 指定されたlightIdに対応するPinを取得します.
     * <p>
     * 対応するPinが存在しない場合にはnullを返却します。
     * </p>
     * @param lightId ライトID
     * @return 対応するPinのインスタンス
     */
    private FaBoShield.Pin findLight(final String lightId) {
        if (lightId == null) {
            return mPinList.get(0);
        }
        try {
            int id = Integer.parseInt(lightId);
            for (FaBoShield.Pin pin : mPinList) {
                if (id == pin.getPinNumber()) {
                    return pin;
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 指定されたPinのLEDを点滅させます.
     * @param pin ピン情報
     * @param flashing フラッシュ
     */
    private void flashing(final FaBoShield.Pin pin, final long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get("" + pin.getPinNumber());
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put("" + pin.getPinNumber(), exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(final boolean isOn, final FlashingExecutor.CompleteListener listener) {
                if (isOn) {
                    sendLightOn(pin);
                } else {
                    sendLightOff(pin);
                }
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }

    /**
     * LEDを点灯します.
     * @param pin LEDが挿さっているピン
     */
    private void sendLightOn(final FaBoShield.Pin pin) {
        if (pin.getMode() != FaBoShield.Mode.GPIO_OUT) {
            getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_OUT);
        }
        getFaBoDeviceControl().writeDigital(pin, FaBoShield.Level.HIGH);
    }

    /**
     * LEDを消灯します.
     * @param pin LEDが挿さっているピン
     */
    private void sendLightOff(final FaBoShield.Pin pin) {
        if (pin.getMode() != FaBoShield.Mode.GPIO_OUT) {
            getFaBoDeviceControl().setPinMode(pin, FaBoShield.Mode.GPIO_OUT);
        }
        getFaBoDeviceControl().writeDigital(pin, FaBoShield.Level.LOW);
    }
}
