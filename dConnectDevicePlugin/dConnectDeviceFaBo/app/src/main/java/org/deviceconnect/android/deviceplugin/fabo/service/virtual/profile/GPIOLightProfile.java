package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.param.ArduinoUno;
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
    private List<ArduinoUno.Pin> mPinList;

    /**
     * ライトフラッシング管理マップ.
     */
    private final Map<String, FlashingExecutor> mFlashingMap = new HashMap<>();

    /**
     * コンストラクタ.
     * @param pinList ライトに対応するPinのリスト
     */
    public GPIOLightProfile(final List<ArduinoUno.Pin> pinList) {
        mPinList = pinList;

        // GET /gotpai/light
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Bundle[] lightList = new Bundle[mPinList.size()];
                for (int i = 0; i < mPinList.size(); i++) {
                    ArduinoUno.Pin pin = mPinList.get(i);
                    lightList[i] = new Bundle();
                    lightList[i].putString("lightId", String.valueOf(pin.getPinNumber()));
                    lightList[i].putString("name", pin.getPinNames()[1]);
                    lightList[i].putBoolean("on", getFaBoDeviceService().getDigitalValue(pin) == 1);
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

                if (mPinList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "Light does not exist.");
                } else {
                    ArduinoUno.Pin pin = findLight(lightId);
                    if (pin != null) {
                        getFaBoDeviceService().digitalWrite(pin, ArduinoUno.Level.LOW);
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
        long[] flashing = getFlashing(request);

        if (mPinList.isEmpty()) {
            MessageUtils.setInvalidRequestParameterError(response, "Light does not exist.");
        } else {
            ArduinoUno.Pin pin = findLight(lightId);
            if (pin != null) {
                if (flashing != null) {
                    flashing(pin, flashing);
                } else {
                    getFaBoDeviceService().digitalWrite(pin, ArduinoUno.Level.HIGH);
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
    private ArduinoUno.Pin findLight(final String lightId) {
        if (lightId == null) {
            return mPinList.get(0);
        }
        try {
            int id = Integer.parseInt(lightId);
            for (ArduinoUno.Pin pin : mPinList) {
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
    private void flashing(final ArduinoUno.Pin pin, final long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get("" + pin.getPinNumber());
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put("" + pin.getPinNumber(), exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(final boolean isOn, final FlashingExecutor.CompleteListener listener) {
                if (isOn) {
                    getFaBoDeviceService().digitalWrite(pin, ArduinoUno.Level.HIGH);
                } else {
                    getFaBoDeviceService().digitalWrite(pin, ArduinoUno.Level.LOW);
                }
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }
}
