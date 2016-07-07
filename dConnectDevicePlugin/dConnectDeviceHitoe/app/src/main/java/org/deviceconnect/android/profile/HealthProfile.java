/*
 HealthProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.HealthProfileConstants;

/**
 * Health プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての健康機器操作機能を提供するAPI.<br/>
 * スマートデバイスに対しての健康機器操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Health Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>

 * @author NTT DOCOMO, INC.
 */
public class HealthProfile extends DConnectProfile implements HealthProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }


    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに心拍数を設定する.
     * 
     * @param response レスポンス
     * @param heartRate 心拍数
     */
    public static void setHeartRate(final Intent response, final int heartRate) {
        response.putExtra(PARAM_HEART_RATE, heartRate);
    }
    /**
     * レスポンスにHealthを設定する.
     *
     * @param response レスポンス
     * @param heart 心拍数,RRIオブジェクト
     */
    public static void setHeart(final Intent response, final Bundle heart) {
        response.putExtra(PARAM_HEART, heart);
    }
    /**
     * レスポンスに心拍数を設定する.
     *
     * @param response レスポンス
     * @param rate 心拍数オブジェクト
     */
    public static void setRate(final Bundle response, final Bundle rate) {
        response.putBundle(PARAM_RATE, rate);
    }
    /**
     * レスポンスにRRIを設定する.
     *
     * @param response レスポンス
     * @param rr RRIオブジェクト
     */
    public static void setRRI(final Bundle response, final Bundle rr) {
        response.putBundle(PARAM_RR, rr);
    }
    /**
     * レスポンスにEnergyExtendedを設定する.
     *
     * @param response レスポンス
     * @param energy RRIオブジェクト
     */
    public static void setEnergyExtended(final Bundle response, final Bundle energy) {
        response.putBundle(PARAM_ENERGY, energy);
    }
    /**
     * レスポンスにHealthデバイス情報を設定する.
     *
     * @param response レスポンス
     * @param device Healthデバイスオブジェクト
     */
    public static void setDevice(final Bundle response, final Bundle device) {
        response.putBundle(PARAM_DEVICE, device);
    }

    /**
     * レスポンスに測定値を設定する.
     *
     * @param response レスポンス
     * @param value 測定値
     */
    public static void setValue(final Bundle response, final float value) {
        response.putFloat(PARAM_VALUE, value);
    }

    /**
     * レスポンスにMDER Float値を設定する.
     *
     * @param response レスポンス
     * @param mder MDER Float値
     */
    public static void setMDERFloat(final Bundle response, final String mder) {
        response.putString(PARAM_MDER_FLOAT, mder);
    }
    /**
     * レスポンスにtype値を設定する.
     *
     * @param response レスポンス
     * @param type type
     */
    public static void setType(final Bundle response, final String type) {
        response.putString(PARAM_TYPE, type);
    }
    /**
     * レスポンスにtypeCode値を設定する.
     *
     * @param response レスポンス
     * @param typeCode typeCode
     */
    public static void setTypeCode(final Bundle response, final int typeCode) {
        response.putInt(PARAM_TYPE_CODE, typeCode);
    }
    /**
     * レスポンスにunit値を設定する.
     *
     * @param response レスポンス
     * @param unit unit
     */
    public static void setUnit(final Bundle response, final String unit) {
        response.putString(PARAM_UNIT, unit);
    }
    /**
     * レスポンスにUnitCode値を設定する.
     *
     * @param response レスポンス
     * @param unitCode UnitCode
     */
    public static void setUnitCode(final Bundle response, final int unitCode) {
        response.putInt(PARAM_UNIT_CODE, unitCode);
    }
    /**
     * レスポンスにTimeStamp値を設定する.
     *
     * @param response レスポンス
     * @param timeStamp TimeStamp
     */
    public static void setTimestamp(final Bundle response, final long timeStamp) {
        response.putLong(PARAM_TIMESTAMP, timeStamp);
    }
    /**
     * レスポンスにTimeStampString値を設定する.
     *
     * @param response レスポンス
     * @param timeStampString TimeStampString
     */
    public static void setTimestampString(final Bundle response, final String timeStampString) {
        response.putString(PARAM_TIMESTAMP_STRING, timeStampString);
    }

    /**
     * レスポンスにProductName値を設定する.
     *
     * @param response レスポンス
     * @param productName productName
     */
    public static void setProductName(final Bundle response, final String productName) {
        response.putString(PARAM_PRODUCT_NAME, productName);
    }
    /**
     * レスポンスにManufacturerName値を設定する.
     *
     * @param response レスポンス
     * @param manufacturerName ManufacturerName
     */
    public static void setManufacturerName(final Bundle response, final String manufacturerName) {
        response.putString(PARAM_MANUFACTURER_NAME, manufacturerName);
    }
    /**
     * レスポンスにModelNumber値を設定する.
     *
     * @param response レスポンス
     * @param modelNumber ModelNumber
     */
    public static void setModelNumber(final Bundle response, final String modelNumber) {
        response.putString(PARAM_MODEL_NUMBER, modelNumber);
    }
    /**
     * レスポンスにFirmwareRevision値を設定する.
     *
     * @param response レスポンス
     * @param firmwareRevision FirmwareRevision
     */
    public static void setFirmwareRevision(final Bundle response, final String firmwareRevision) {
        response.putString(PARAM_FIRMWARE_REVISION, firmwareRevision);
    }
    /**
     * レスポンスにSerialNumber値を設定する.
     *
     * @param response レスポンス
     * @param serialNumber SerialNumber
     */
    public static void setSerialNumber(final Bundle response, final String serialNumber) {
        response.putString(PARAM_SERIAL_NUMBER, serialNumber);
    }
    /**
     * レスポンスにSoftwareRevision値を設定する.
     *
     * @param response レスポンス
     * @param softwareRevision SoftwareRevision
     */
    public static void setSoftwareRevision(final Bundle response, final String softwareRevision) {
        response.putString(PARAM_SOFTWARE_REVISION, softwareRevision);
    }
    /**
     * レスポンスにHardwareRevision値を設定する.
     *
     * @param response レスポンス
     * @param hardwareRevision HardwareRevision
     */
    public static void setHardwareRevision(final Bundle response, final String hardwareRevision) {
        response.putString(PARAM_HARDWARE_REVISION, hardwareRevision);
    }
    /**
     * レスポンスにPartNumber値を設定する.
     *
     * @param response レスポンス
     * @param partNumber PartNumber
     */
    public static void setPartNumber(final Bundle response, final String partNumber) {
        response.putString(PARAM_PART_NUMBER, partNumber);
    }
    /**
     * レスポンスにProtocolRevision値を設定する.
     *
     * @param response レスポンス
     * @param protocolRevision ProtocolRevision
     */
    public static void setProtocolRevision(final Bundle response, final String protocolRevision) {
        response.putString(PARAM_PROTOCOL_REVISION, protocolRevision);
    }
    /**
     * レスポンスにSystemId値を設定する.
     *
     * @param response レスポンス
     * @param systemId SystemId
     */
    public static void setSystemId(final Bundle response, final String systemId) {
        response.putString(PARAM_SYSTEM_ID, systemId);
    }
    /**
     * レスポンスにBatteryLevel値を設定する.
     *
     * @param response レスポンス
     * @param batteryLevel BatteryLevel
     */
    public static void setBatteryLevel(final Bundle response, final float batteryLevel) {
        response.putFloat(PARAM_BATTERY_LEVEL, batteryLevel);
    }
    // ------------------------------------
    // ゲッターメソッド群
    // ------------------------------------
}
