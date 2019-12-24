/*
 BleUuidUtils.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.util;

import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * BLE UUIDの扱うためのユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class BleUuidUtils {

    private static final String UUID_LONG_STYLE_PREFIX = "0000";
    private static final String UUID_LONG_STYLE_POSTFIX = "-0000-1000-8000-00805F9B34FB";

    /**
     * 格納できる最大文字列数.
     */
    public static final int DEVICE_INFO_MAX_LENGTH = 20;

    /**
     * Device Information サービス.
     *
     * @see <a href="https://www.bluetooth.com/ja-jp/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.device_information.xml">Device Information サービス</a>
     */
    public static final UUID SERVICE_DEVICE_INFORMATION = BleUuidUtils.fromShortValue(0x180A);

    /**
     * Manufacturer名を格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_MANUFACTURER_NAME = BleUuidUtils.fromShortValue(0x2A29);

    /**
     * モデル名を格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_MODEL_NUMBER = BleUuidUtils.fromShortValue(0x2A24);

    /**
     * シリアルナンバーを格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_SERIAL_NUMBER = BleUuidUtils.fromShortValue(0x2A25);

    /**
     * Battery サービス.
     *
     * @see <a href="https://www.bluetooth.com/ja-jp/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.battery_service.xml">Device Information サービス</a>
     */
    public static final UUID SERVICE_BATTERY = BleUuidUtils.fromShortValue(0x180F);

    /**
     * バッテリー残量を格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_BATTERY_LEVEL = BleUuidUtils.fromShortValue(0x2A19);

    /**
     * HID サービス.
     *
     * @see <a href="https://www.bluetooth.com/ja-jp/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.human_interface_device.xml">Device Information サービス</a>
     */
    public static final UUID SERVICE_BLE_HID = BleUuidUtils.fromShortValue(0x1812);

    /**
     * HIDの情報を格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_HID_INFORMATION = BleUuidUtils.fromShortValue(0x2A4A);

    /**
     * レポートマップを格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_REPORT_MAP = BleUuidUtils.fromShortValue(0x2A4B);

    /**
     * コントロールポイントを格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_HID_CONTROL_POINT = BleUuidUtils.fromShortValue(0x2A4C);

    /**
     * レポートを格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_REPORT = BleUuidUtils.fromShortValue(0x2A4D);

    /**
     * プロトコルモードを格納するキャラクタリスティック.
     */
    public static final UUID CHARACTERISTIC_PROTOCOL_MODE = BleUuidUtils.fromShortValue(0x2A4E);

    /**
     * Gatt Characteristic Descriptor.
     */
    public static final UUID DESCRIPTOR_REPORT_REFERENCE = BleUuidUtils.fromShortValue(0x2908);
    public static final UUID DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = BleUuidUtils.fromShortValue(0x2902);

    private BleUuidUtils() {
    }

    /**
     * Parses a UUID string with the format defined by toString().
     *
     * @param uuidString the UUID string to parse.
     * @return an UUID instance.
     * @throws NullPointerException if uuid is null.
     * @throws IllegalArgumentException if uuid is not formatted correctly.
     */
    @NonNull
    public static UUID fromString(@NonNull final String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            // may be a short style
            return UUID.fromString(UUID_LONG_STYLE_PREFIX + uuidString + UUID_LONG_STYLE_POSTFIX);
        }
    }

    /**
     * Obtains a UUID from Short style value.
     *
     * @param uuidShortValue the Short style UUID value.
     * @return an UUID instance.
     */
    @NonNull
    public static UUID fromShortValue(final int uuidShortValue) {
        return UUID.fromString(UUID_LONG_STYLE_PREFIX + String.format("%04X", uuidShortValue & 0xffff) + UUID_LONG_STYLE_POSTFIX);
    }

    /**
     * Obtains a ParcelUuid from Short style value.
     *
     * @param uuidShortValue the Short style UUID value.
     * @return an UUID instance.
     */
    @NonNull
    public static ParcelUuid parcelFromShortValue(final int uuidShortValue) {
        return ParcelUuid.fromString(UUID_LONG_STYLE_PREFIX + String.format("%04X", uuidShortValue & 0xffff) + UUID_LONG_STYLE_POSTFIX);
    }

    /**
     * UUID to short style value
     *
     * @param uuid the UUID
     * @return short style value, -1 if the specified UUID is not short style
     */
    public static int toShortValue(@NonNull final UUID uuid) {
        return (int)(uuid.getMostSignificantBits() >> 32 & 0xffff);
    }

    /**
     * check if full style or short (16bits) style UUID matches
     *
     * @param src the UUID to be compared
     * @param dst the UUID to be compared
     * @return true if the both of UUIDs matches
     */
    public static boolean matches(@NonNull final UUID src, @NonNull final UUID dst) {
        if (isShortUuid(src) || isShortUuid(dst)) {
            // at least one instance is short style: check only 16bits
            final long srcShortUUID = src.getMostSignificantBits() & 0x0000ffff00000000L;
            final long dstShortUUID = dst.getMostSignificantBits() & 0x0000ffff00000000L;
            return srcShortUUID == dstShortUUID;
        } else {
            return src.equals(dst);
        }
    }

    /**
     * Check if the specified UUID style is short style.
     *
     * @param src the UUID
     * @return true if the UUID is short style
     */
    private static boolean isShortUuid(@NonNull final UUID src) {
        return (src.getMostSignificantBits() & 0xffff0000ffffffffL) == 0L && src.getLeastSignificantBits() == 0L;
    }
}
