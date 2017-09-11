/*
 UsbSerialDevice.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.device;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.ByteBuffer;

/**
 * USBシリアルデバイス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UsbSerialDevice {
    /** Service ID. */
    private String mServiceId = null;
    /** Name. */
    private String mName = null;
    /** Device Type. */
    private String mDeviceType = null;
    /** Status. */
    private String mStatus = null;
    /** Online. */
    private boolean mOnline = false;

    /** Port. */
    private UsbSerialPort mSerialPort = null;
    /** Mac Address. */
    private String mMacAddr = null;
    /** Serial IO Manager. */
    private SerialInputOutputManager mSerialIoManager = null;
    /** Receive buffer. */
    ByteBuffer mByteBuffer = ByteBuffer.allocate(1024);

    /** Constructor. */
    public UsbSerialDevice() {
    }

    /**
     * Get ServiceId.
     * @return ServiceId.
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * Get Name.
     * @return Name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Get Device Type.
     * @return DeviceType.
     */
    public String getDeviceType() {
        return mDeviceType;
    }

    /**
     * Get Status.
     * @return Status.
     */
    public String getStatus() {
        return mStatus;
    }

    /**
     * Get Online.
     * @return Online.
     */
    public boolean getOnline() {
        return mOnline;
    }

    /**
     * Get USB Serial Port.
     * @return USB Serial Port.
     */
    public UsbSerialPort getSerialPort() {
        return mSerialPort;
    }

    /**
     * Get Mac Address.
     * @return Mac Address.
     */
    public String getMacAddr() {
        return mMacAddr;
    }

    /**
     * Get USB SerialInputOutputManager.
     * @return USB SerialInputOutputManager.
     */
    public SerialInputOutputManager getSerialInputOutputManager() {
        return mSerialIoManager;
    }

    /**
     * Get Byte Buffer.
     * @return Byte Buffer.
     */
    public ByteBuffer getByteBuffer() {
        return mByteBuffer;
    }

    /**
     * Set ServiceId.
     * @param serviceId ServiceId.
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    /**
     * Set Name.
     * @param name Name.
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Set Device Type.
     * @param deviceType Device Type.
     */
    public void setDeviceType(final String deviceType) {
        mDeviceType = deviceType;
    }

    /**
     * Set Status.
     * @param status Status.
     */
    public void setStatus(final String status) {
        mStatus = status;
    }

    /**
     * Set Online.
     * @param online Online.
     */
    public void setOnline(final boolean online) {
        mOnline = online;
    }

    /**
     *  Set USB Serial Port.
     * @param serialPort Serial Port.
     */
    public void setSerialPort(final UsbSerialPort serialPort) {
        mSerialPort = serialPort;
    }

    /**
     *  Set Mac Address.
     * @param macAddr Mac Address.
     */
    public void setMacAddr(final String macAddr) {
        mMacAddr = macAddr;
    }

    /**
     * Set USB SerialInputOutputManager.
     * @param manager USB SerialInputOutputManager.
     */
    public void setSerialInputOutputManager(final SerialInputOutputManager manager) {
        mSerialIoManager = manager;
    }
}
