/*
 UVCCameraNative.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import java.util.ArrayList;

import static org.deviceconnect.android.libuvc.UVCCameraNative.RequestType.SET_CUR;

/**
 * NDKとのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
class UVCCameraNative {

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * コントロールを定義します.
     * <p>
     * MEMO uvc.h で定義されている値と一致する必要あります。
     * </p>
     */
    enum ControlType {
        /**
         * カメラターミナルコントロールを定義します.
         */
        TYPE_CT(0),

        /**
         * プロセシングユニットコントロールを定義します.
         */
        TYPE_PU(1),

        /**
         * エンコードユニットコントロールを定義します.
         */
        TYPE_EU(2);

        private int mValue;

        ControlType(final int value) {
            mValue = value;
        }
    }

    /**
     * Video Class-Specific Request Codes.
     * <p>
     * MEMO uvc.h で定義されている値と一致する必要あります。
     * </p>
     */
    enum RequestType {
        SET_CUR(0x01),
        SET_CUR_ALL(0x11),
        GET_CUR(0x81),
        GET_MIN(0x82),
        GET_MAX(0x83),
        GET_RES(0x84),
        GET_LEN(0x85),
        GET_INFO(0x86),
        GET_DEF(0x87);

        private int mValue;

        RequestType(final int value) {
            mValue = value;
        }
    }

    /**
     * コントロールのインターフェース.
     */
    interface Control {
        /**
         * コントロールのタイプを取得します.
         *
         * @return コントロールのタイプ
         */
        ControlType getType();

        /**
         * コントロールの機能を取得します.
         *
         * @return コントロールの機能
         */
        int getValue();
    }

    /**
     * カメラターミナルコントロール.
     */
    enum CameraTerminalControl implements Control {
        CT_CONTROL_UNDEFINED(0x00),
        CT_SCANNING_MODE_CONTROL(0x01),
        CT_AE_MODE_CONTROL(0x02),
        CT_AE_PRIORITY_CONTROL(0x03),
        CT_EXPOSURE_TIME_ABSOLUTE_CONTROL(0x04),
        CT_EXPOSURE_TIME_RELATIVE_CONTROL(0x05),
        CT_FOCUS_ABSOLUTE_CONTROL(0x06),
        CT_FOCUS_RELATIVE_CONTROL(0x07),
        CT_FOCUS_AUTO_CONTROL(0x08),
        CT_IRIS_ABSOLUTE_CONTROL(0x09),
        CT_IRIS_RELATIVE_CONTROL(0x0A),
        CT_ZOOM_ABSOLUTE_CONTROL(0x0B),
        CT_ZOOM_RELATIVE_CONTROL(0x0C),
        CT_PANTILT_ABSOLUTE_CONTROL(0x0D),
        CT_PANTILT_RELATIVE_CONTROL(0x0E),
        CT_ROLL_ABSOLUTE_CONTROL(0x0F),
        CT_ROLL_RELATIVE_CONTROL(0x10),
        CT_PRIVACY_CONTROL(0x11),
        CT_FOCUS_SIMPLE_CONTROL(0x12),
        CT_WINDOW_CONTROL(0x13),
        CT_REGION_OF_INTEREST_CONTROL(0x14);

        private int mValue;

        CameraTerminalControl(final int value) {
            mValue = value;
        }

        @Override
        public ControlType getType() {
            return ControlType.TYPE_CT;
        }

        @Override
        public int getValue() {
            return mValue;
        }

        static CameraTerminalControl valueOf(final int value) {
            for (CameraTerminalControl ct : values()) {
                if (ct.mValue == value) {
                    return ct;
                }
            }
            return CT_CONTROL_UNDEFINED;
        }
    }

    /**
     * プロセシングユニットコントロール.
     */
    enum ProcessingUnitControl implements Control {
        PU_CONTROL_UNDEFINED(0x00),
        PU_BACKLIGHT_COMPENSATION_CONTROL(0x01),
        PU_BRIGHTNESS_CONTROL(0x02),
        PU_CONTRAST_CONTROL(0x03),
        PU_GAIN_CONTROL(0x04),
        PU_POWER_LINE_FREQUENCY_CONTROL(0x05),
        PU_HUE_CONTROL(0x06),
        PU_SATURATION_CONTROL(0x07),
        PU_SHARPNESS_CONTROL(0x08),
        PU_GAMMA_CONTROL(0x09),
        PU_WHITE_BALANCE_TEMPERATURE_CONTROL(0x0A),
        PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL(0x0B),
        PU_WHITE_BALANCE_COMPONENT_CONTROL(0x0C),
        PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL(0x0D),
        PU_DIGITAL_MULTIPLIER_CONTROL(0x0E),
        PU_DIGITAL_MULTIPLIER_LIMIT_CONTROL(0x0F),
        PU_HUE_AUTO_CONTROL(0x10),
        PU_ANALOG_VIDEO_STANDARD_CONTROL(0x11),
        PU_ANALOG_LOCK_STATUS_CONTROL(0x12),
        PU_CONTRAST_AUTO_CONTROL(0x13);

        private int mValue;

        ProcessingUnitControl(final int value) {
            mValue = value;
        }

        @Override
        public ControlType getType() {
            return ControlType.TYPE_PU;
        }

        @Override
        public int getValue() {
            return mValue;
        }

        static ProcessingUnitControl valueOf(final int value) {
            for (ProcessingUnitControl pu : values()) {
                if (pu.mValue == value) {
                    return pu;
                }
            }
            return PU_CONTROL_UNDEFINED;
        }
    }

    static int startVideo(long nativePtr, Parameter p) {
        return startVideo(nativePtr, p.getFormatIndex(), p.getFrameIndex(), p.getFps(), p.isUseH264());
    }

    static int setControl(long nativePtr, Control control, byte[] value) {
        return applyControl(nativePtr, control.getType().mValue, control.getValue(), SET_CUR.mValue, value);
    }

    static int getControl(long nativePtr, Control control, RequestType request, byte[] value) {
        return applyControl(nativePtr, control.getType().mValue, control.getValue(), request.mValue, value);
    }

    static native long open(int fd);
    static native int getParameter(long nativePtr, ArrayList parameters);
    static native int getOption(long nativePtr, Option option);
    static native int startVideo(long nativePtr, int formatIndex, int frameIndex, int fps, boolean useH264);
    static native int stopVideo(long nativePtr);
    static native int captureStillImage(long nativePtr, Frame frame);
    static native int close(long nativePtr);
    static native int handleEvent(long nativePtr, UVCCamera uvcCamera);
    static native int isRunning(long nativePtr);
    static native int detachInterface(long nativePtr, int interfaceNum);
    static native int setConfig(long nativePtr, int configId);
    static native int applyControl(long nativePtr, int type, int control, int request, byte[] value);
    static native int getStillCaptureMethod(long nativePtr);
}
