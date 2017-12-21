/*
 SmartMeterPowerMeterProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.profiles;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.smartmeter.BuildConfig;
import org.deviceconnect.android.deviceplugin.smartmeter.SmartMeterMessageService;
import org.deviceconnect.android.deviceplugin.smartmeter.device.UsbSerialDevice;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * SmartMeter PowerMeter Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class SmartMeterPowerMeterProfile extends DConnectProfile {
    public SmartMeterPowerMeterProfile() {
        // PUT /powerMeter
        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setNotSupportActionError(response, "Not support power on API.");
                return true;
            }
        });

        // DELETE /powerMeter
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                MessageUtils.setNotSupportActionError(response, "Not support power off API.");
                return true;
            }
        });

        // GET /powerMeter
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // USBシリアルデバイスの取得
                UsbSerialDevice device = ((SmartMeterMessageService) getContext()).getUsbSerialDevice(serviceId);
                if (device == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not match serviceId.");
                    return true;
                }
                if (!(((SmartMeterMessageService) getContext()).getOnlineStatus(device.getServiceId()))) {
                    MessageUtils.setIllegalDeviceStateError(response, "Not connect smartmeter.");
                    return true;
                }

                // 動作状態取得.
                ((SmartMeterMessageService) getContext()).getOperationStatus(response);
                return false;
            }
        });

        // GET /powerMeter/instantaneousCurrent
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "instantaneousCurrent";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                String unit = (String) request.getExtras().get("unit");

                // USBシリアルデバイスの取得
                UsbSerialDevice device = ((SmartMeterMessageService) getContext()).getUsbSerialDevice(serviceId);
                if (device == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not match serviceId.");
                    return true;
                }
                if (!(((SmartMeterMessageService) getContext()).getOnlineStatus(device.getServiceId()))) {
                    MessageUtils.setIllegalDeviceStateError(response, "Not connect smartmeter.");
                    return true;
                }

                // 瞬時電流量単位パラメータ取得
                if (unit == null) {
                    unit = "A";
                }

                switch (unit) {
                    case "A":
                    case "mA":
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "unit parse error");
                        sendResultError(response);
                        return true;
                }

                // 瞬時電流量取得.
                ((SmartMeterMessageService) getContext()).getInstantaneousCurrent(unit, response);
                return false;
            }
        });

        // GET /powerMeter/instantaneousPower
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "instantaneousPower";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                String unit = (String) request.getExtras().get("unit");

                // USBシリアルデバイスの取得
                UsbSerialDevice device = ((SmartMeterMessageService) getContext()).getUsbSerialDevice(serviceId);
                if (device == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not match serviceId.");
                    return true;
                }
                if (!(((SmartMeterMessageService) getContext()).getOnlineStatus(device.getServiceId()))) {
                    MessageUtils.setIllegalDeviceStateError(response, "Not connect smartmeter.");
                    return true;
                }

                // 瞬時電力量単位パラメータ取得
                if (unit == null) {
                    unit = "W";
                }

                switch (unit) {
                    case "kW":
                    case "W":
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "unit parse error");
                        sendResultError(response);
                        return true;
                }

                // 瞬時電力量取得.
                ((SmartMeterMessageService) getContext()).getInstantaneousPower(unit, response);
                return false;
            }
        });

        // GET /powerMeter/integratedPower
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "integratedPower";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                String date = (String) request.getExtras().get("date");
                String unit = (String) request.getExtras().get("unit");
                Integer count = parseInteger(request, "count");
                String powerFlow = (String) request.getExtras().get("powerFlow");

                // USBシリアルデバイスの取得
                UsbSerialDevice device = ((SmartMeterMessageService) getContext()).getUsbSerialDevice(serviceId);
                if (device == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not match serviceId.");
                    return true;
                }
                if (!(((SmartMeterMessageService) getContext()).getOnlineStatus(device.getServiceId()))) {
                    MessageUtils.setIllegalDeviceStateError(response, "Not connect smartmeter.");
                    return true;
                }

                // APIの日付パラメータ解析
                Calendar calendar;
                if (date != null) {
                    calendar = RFC3339DateUtils.toCalendar(date);
                    if (calendar == null) {
                        MessageUtils.setInvalidRequestParameterError(response, "date parse error");
                        sendResultError(response);
                        return true;
                    }
                } else {
                    // APIが呼ばれた時点の日時を取得
                    calendar = Calendar.getInstance();
                }
                Calendar nowCalender = Calendar.getInstance();

                // 積算電力量単位パラメータ取得
                if (unit == null) {
                    unit = "Wh";
                }

                switch (unit) {
                    case "kWh":
                    case "Wh":
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "unit parse error");
                        sendResultError(response);
                        return true;
                }

                // 積算電力量コマ数パラメータ取得
                if (count == null) {
                    count = 24;
                }
                switch (count) {
                    case 24:
                        break;
                    case 48:
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "count parse error");
                        sendResultError(response);
                        return true;
                }

                // 積算電力量方向指定パラメータ取得
                if (powerFlow == null) {
                    powerFlow = "normal";
                }
                switch (powerFlow) {
                    case "normal":
                        break;
                    case "reverse":
                        // 逆方向計測値をサポートしているか確認
                        if (!((SmartMeterMessageService) getContext()).getENLUtilInstance().isDefineProperty(0xE4, "get")) {
                            MessageUtils.setIllegalDeviceStateError(response, "device does not support reverse direction");
                            sendResultError(response);
                            return true;
                        }
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response, "powerFlow parse error");
                        sendResultError(response);
                        return true;
                }

                // 取得日付パラメータが、指定日数範囲内に納まっているか確認
                long now = nowCalender.getTime().getTime();
                long select = calendar.getTime().getTime();
                final long DAY_MILLISECOND = (1000 * 60 * 60 * 24);
                long diff = now - select;
                long diffDay = diff / DAY_MILLISECOND;

                if (diffDay < 0) {
                    MessageUtils.setInvalidRequestParameterError(response, "date parse error");
                    sendResultError(response);
                    return true;
                } else if (diffDay > 99) {
                        MessageUtils.setInvalidRequestParameterError(response, "date range over");
                        sendResultError(response);
                        return true;
                }
                // 積算電力量取得.
                ((SmartMeterMessageService) getContext()).getDailyData((int)diffDay, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), count, powerFlow, unit, response);
                return false;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "powerMeter";
    }

    /**
     * エラーレスポンス送信.
     * @param response response.
     */
    private void sendResultError(Intent response) {
        ((SmartMeterMessageService) getContext()).sendResponse(response);
    }
}
