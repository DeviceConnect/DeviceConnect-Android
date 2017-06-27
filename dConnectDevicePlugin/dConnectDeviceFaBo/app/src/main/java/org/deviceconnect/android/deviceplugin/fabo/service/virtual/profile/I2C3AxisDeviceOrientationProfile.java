package org.deviceconnect.android.deviceplugin.fabo.service.virtual.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.device.IADXL345;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * I2C用加速度センサープロファイル.
 * <p>
 * 以下のFaBoのBrickに対応します。<br>
 * ID: #201<br>
 * Name: 3Axis I2C Brick<br>
 * </p>
 */
public class I2C3AxisDeviceOrientationProfile extends BaseFaBoProfile {

    /**
     * イベントを送信するためのインターバル.
     */
    private long mInterval = 100;

    /**
     * 前回送信したイベントの時間.
     */
    private long mSendTime;

    /**
     * ADXL345からのデータを受け取るためのリスナー.
     */
    private IADXL345.OnADXL345Listener mOnADXL345Listener;

    public I2C3AxisDeviceOrientationProfile() {
        // GET /gotapi/deviceOrientation/onDeviceOrientation
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onDeviceOrientation";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else {
                    IADXL345 adxl345 = getFaBoDeviceControl().getADXL345();
                    if (adxl345 != null) {
                        adxl345.read(new IADXL345.OnADXL345Listener() {
                            @Override
                            public void onStarted() {
                            }

                            @Override
                            public void onData(final double x, final double y, final double z) {
                                response.putExtra("orientation", createOrientation(x, y, z));
                                setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            }

                            @Override
                            public void onError(final String message) {
                                MessageUtils.setIllegalDeviceStateError(response, message);
                                sendResponse(response);
                            }
                        });
                    }
                }
                return false;
            }
        });

        // PUT /gotapi/deviceOrientation/onDeviceOrientation
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDeviceOrientation";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                final IADXL345 adxl345 = getFaBoDeviceControl().getADXL345();
                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "FaBo device is not connected.");
                } else if (adxl345 == null) {
                    MessageUtils.setNotSupportAttributeError(response, "Not support.");
                } else {
                    Long interval = parseLong(request, "interval");
                    if (interval != null) {
                        mInterval = interval;
                    } else {
                        mInterval = 100;
                    }

                    boolean empty = isEmptyEvent();

                    EventError error = EventManager.INSTANCE.addEvent(request);
                    switch (error) {
                        case NONE:
                            if (empty) {
                                mOnADXL345Listener = new IADXL345.OnADXL345Listener() {
                                    @Override
                                    public void onStarted() {
                                        setResult(response, DConnectMessage.RESULT_OK);
                                        sendResponse(response);
                                    }

                                    @Override
                                    public void onError(final String message) {
                                        MessageUtils.setIllegalDeviceStateError(response, message);
                                        sendResponse(response);
                                        EventManager.INSTANCE.removeEvent(request);
                                        adxl345.stopRead(mOnADXL345Listener);
                                    }

                                    @Override
                                    public void onData(final double x, final double y, final double z) {
                                        notifySensorEvent(x, y, z);
                                    }
                                };
                                adxl345.startRead(mOnADXL345Listener);
                                return false;
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
                return true;
            }
        });

        // DELETE /gotapi/deviceOrientation/onDeviceOrientation
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDeviceOrientation";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                IADXL345 adxl345 = getFaBoDeviceControl().getADXL345();
                if (adxl345 == null) {
                    MessageUtils.setNotSupportProfileError(response, "Not support");
                } else {
                    EventError error = EventManager.INSTANCE.removeEvent(request);
                    switch (error) {
                        case NONE:
                            if (isEmptyEvent()) {
                                adxl345.stopRead(mOnADXL345Listener);
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        case NOT_FOUND:
                            MessageUtils.setIllegalDeviceStateError(response, "Not register event.");
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "deviceOrientation";
    }

    /**
     * イベント登録が空か確認を行う.
     * @return イベントが空の場合はtrue、それ以外はfalse
     */
    private boolean isEmptyEvent() {
        String serviceId = getService().getId();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                "deviceOrientation", null, "onDeviceOrientation");
        return events.isEmpty();
    }

    /**
     * Orientationオブジェクトを作成します.
     * @param x x軸への加速度
     * @param y y軸への加速度
     * @param z z¥軸への加速度
     * @return Orientationオブジェクト
     */
    private Bundle createOrientation(final double x, final double y, final double z) {
        Bundle accelerationIncludingGravity = new Bundle();
        accelerationIncludingGravity.putDouble("x", x);
        accelerationIncludingGravity.putDouble("y", y);
        accelerationIncludingGravity.putDouble("z", z);

        Bundle orientation = new Bundle();
        orientation.putParcelable("accelerationIncludingGravity",
                accelerationIncludingGravity);
        return orientation;
    }

    /**
     * 加速度センサーの値を通知します.
     * @param x x軸への加速度
     * @param y y軸への加速度
     * @param z z¥軸への加速度
     */
    private void notifySensorEvent(final double x, final double y, final double z) {
        long interval = (System.currentTimeMillis() - mSendTime);
        if (interval >= mInterval) {
            String serviceId = getService().getId();
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    "deviceOrientation", null, "onDeviceOrientation");

            for (Event event : events) {
                Bundle orientation = createOrientation(x, y, z);
                orientation.putLong("interval", interval);

                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("orientation", orientation);
                sendEvent(intent, event.getAccessToken());
            }

            mSendTime = System.currentTimeMillis();
        }
    }
}
