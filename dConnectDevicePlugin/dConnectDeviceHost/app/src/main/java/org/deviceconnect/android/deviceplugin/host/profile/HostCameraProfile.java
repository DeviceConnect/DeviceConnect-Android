/*
 HostCameraProfile.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Range;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera Profile. (Experimental)
 *
 * @author NTT DOCOMO, INC.
 */
public class HostCameraProfile extends DConnectProfile {

    private HostMediaRecorderManager mRecorderManager;

    @Override
    public String getProfileName() {
        return "camera";
    }

    public HostCameraProfile(final HostMediaRecorderManager recorderManager) {
        mRecorderManager = recorderManager;


        // GET /gotapi/camera/options
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "options";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = request.getStringExtra("target");

                Camera2Recorder recorder = getCameraRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "camera is not found.");
                    return true;
                }

                HostMediaRecorder.Settings settings = recorder.getSettings();

                Bundle photo = new Bundle();

                AutoFocus af = AutoFocus.valueOf(settings.getPreviewAutoFocusMode());
                if (af != null) {
                    photo.putString("autoFocus", af.getName());
                }

                WhiteBalance wb = WhiteBalance.valueOf(settings.getPreviewWhiteBalance());
                if (wb != null) {
                    photo.putString("whiteBalance", wb.getName());
                }

                Integer wbTemperature = settings.getPreviewWhiteBalanceTemperature();
                if (wbTemperature != null) {
                    photo.putInt("whiteBalanceTemperature", wbTemperature);
                }

                AutoExposure ae = AutoExposure.valueOf(settings.getAutoExposureMode());
                if (ae != null) {
                    photo.putString("autoExposure", ae.getName());
                }

                Long sensorExposureTime = settings.getSensorExposureTime();
                if (sensorExposureTime != null) {
                    photo.putLong("sensorExposureTime", sensorExposureTime);
                }

                Integer sensorSensitivity = settings.getSensorSensitivity();
                if (sensorSensitivity != null) {
                    photo.putInt("sensorSensitivity", sensorSensitivity);
                }

                Long sensorFrameDuration = settings.getSensorFrameDuration();
                if (sensorFrameDuration != null) {
                    photo.putLong("sensorFrameDuration", sensorFrameDuration);
                }

                Stabilization st = Stabilization.valueOf(settings.getStabilizationMode());
                if (st != null) {
                    photo.putString("stabilization", st.getName());
                }

                OpticalStabilization ost = OpticalStabilization.valueOf(settings.getOpticalStabilizationMode());
                if (ost != null) {
                    photo.putString("opticalStabilization", ost.getName());
                }

                NoiseReduction nr = NoiseReduction.valueOf(settings.getNoiseReduction());
                if (nr != null) {
                    photo.putString("noiseReduction", nr.getName());
                }

                Float fl = settings.getFocalLength();
                if (fl != null) {
                    photo.putString("focalLength", String.valueOf(fl));
                }

                Bundle support = new Bundle();

                List<Integer> supportedAF = settings.getSupportedAutoFocusModeList();
                if (supportedAF != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Integer a : supportedAF) {
                        AutoFocus v = AutoFocus.valueOf(a);
                        if (v != null) {
                            list.add(v.getName());
                        }
                    }
                    support.putStringArrayList("autoFocus", list);
                }

                List<Integer> supportedWB = settings.getSupportedWhiteBalanceModeList();
                if (supportedWB != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Integer a : supportedWB) {
                        WhiteBalance v = WhiteBalance.valueOf(a);
                        if (v != null) {
                            list.add(v.getName());
                        }
                    }
                    support.putStringArrayList("whiteBalance", list);
                }

                Range<Integer> supportedWBT = settings.getSupportedWhiteBalanceTemperature();
                if (supportedWBT != null) {
                    Bundle wbt = new Bundle();
                    wbt.putInt("min", supportedWBT.getLower());
                    wbt.putInt("max", supportedWBT.getUpper());
                    support.putBundle("whiteBalanceTemperature", wbt);
                }

                List<Integer> supportedAE = settings.getSupportedAutoExposureModeList();
                if (supportedAE != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Integer a : supportedAE) {
                        AutoExposure v = AutoExposure.valueOf(a);
                        if (v != null) {
                            list.add(v.getName());
                        }
                    }
                    support.putStringArrayList("autoExposure", list);
                }

                Range<Long> supportedSET = settings.getSupportedSensorExposureTime();
                if (supportedSET != null) {
                    Bundle set = new Bundle();
                    set.putLong("min", supportedSET.getLower());
                    set.putLong("max", supportedSET.getUpper());
                    support.putBundle("sensorExposureTime", set);
                }

                Range<Integer> supportedSS = settings.getSupportedSensorSensitivity();
                if (supportedSS != null) {
                    Bundle ss = new Bundle();
                    ss.putInt("min", supportedSS.getLower());
                    ss.putInt("max", supportedSS.getUpper());
                    support.putBundle("sensorSensitivity", ss);
                }

                Long supportedSFD = settings.getMaxSensorFrameDuration();
                if (supportedSFD != null) {
                    Bundle sfd = new Bundle();
                    sfd.putLong("min", 0L);
                    sfd.putLong("max", supportedSFD);
                    support.putBundle("sensorFrameDuration", sfd);
                }

                List<Integer> supportedStab = settings.getSupportedStabilizationList();
                if (supportedStab != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Integer a : supportedStab) {
                        Stabilization v = Stabilization.valueOf(a);
                        if (v != null) {
                            list.add(v.getName());
                        }
                    }
                    support.putStringArrayList("stabilization", list);
                }

                List<Integer> supportedOStab = settings.getSupportedOpticalStabilizationList();
                if (supportedOStab != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Integer a : supportedOStab) {
                        OpticalStabilization v = OpticalStabilization.valueOf(a);
                        if (v != null) {
                            list.add(v.getName());
                        }
                    }
                    support.putStringArrayList("opticalStabilization", list);
                }

                List<Integer> supportedNR = settings.getSupportedNoiseReductionList();
                if (supportedNR != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Integer a : supportedNR) {
                        NoiseReduction v = NoiseReduction.valueOf(a);
                        if (v != null) {
                            list.add(v.getName());
                        }
                    }
                    support.putStringArrayList("noiseReduction", list);
                }

                List<Float> supportedFL = settings.getSupportedFocalLengthList();
                if (supportedFL != null) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add("none");
                    for (Float a : supportedFL) {
                        list.add(String.valueOf(a));
                    }
                    support.putStringArrayList("focalLength", list);
                }

                photo.putBundle("support", support);

                response.putExtra("photo", photo);
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // PUT /gotapi/camera/options
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "options";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = request.getStringExtra("target");
                String autoFocus = request.getStringExtra("autoFocus");
                String whiteBalance = request.getStringExtra("whiteBalance");
                Integer whiteBalanceTemperature = parseInteger(request, "whiteBalanceTemperature");
                String autoExposure = request.getStringExtra("autoExposure");
                Long sensorExposureTime = parseLong(request, "sensorExposureTime");
                Integer sensorSensitivity = parseInteger(request, "sensorSensitivity");
                Long sensorFrameDuration = parseLong(request, "sensorFrameDuration");
                String stabilization = request.getStringExtra("stabilization");
                String opticalStabilization = request.getStringExtra("opticalStabilization");
                String noiseReduction = request.getStringExtra("noiseReduction");
                String focalLength = request.getStringExtra("focalLength");

                Camera2Recorder recorder = getCameraRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "camera is not found.");
                    return true;
                }

                HostMediaRecorder.Settings settings = recorder.getSettings();

                AutoFocus af = AutoFocus.nameOf(autoFocus);
                if (af != null) {
                    if (af == AutoFocus.NONE) {
                        settings.setPreviewAutoFocusMode(null);
                    } else {
                        if (!settings.isSupportedAutoFocusMode(af.getValue())) {
                            MessageUtils.setInvalidRequestParameterError(response, "autoFocus is invalid.");
                            return true;
                        }
                        settings.setPreviewAutoFocusMode(af.getValue());
                    }
                }

                WhiteBalance wb = WhiteBalance.nameOf(whiteBalance);
                if (wb != null) {
                    if (wb == WhiteBalance.NONE) {
                        settings.setPreviewWhiteBalance(null);
                    } else {
                        if (!settings.isSupportedWhiteBalanceMode(wb.getValue())) {
                            MessageUtils.setInvalidRequestParameterError(response, "whiteBalance is invalid.");
                            return true;
                        }
                        settings.setPreviewWhiteBalance(wb.getValue());
                    }
                }

                if (whiteBalanceTemperature != null) {
                    if (!settings.isSupportedWhiteBalanceTemperature(whiteBalanceTemperature)) {
                        MessageUtils.setInvalidRequestParameterError(response, "whiteBalanceTemperature is invalid.");
                        return true;
                    }
                    settings.setPreviewWhiteBalanceTemperature(whiteBalanceTemperature);
                }

                AutoExposure ae = AutoExposure.nameOf(autoExposure);
                if (ae != null) {
                    if (ae == AutoExposure.NONE) {
                        settings.setAutoExposureMode(null);
                    } else {
                        if (!settings.isSupportedAutoExposureMode(ae.getValue())) {
                            MessageUtils.setInvalidRequestParameterError(response, "autoExposure is invalid.");
                            return true;
                        }
                        settings.setAutoExposureMode(ae.getValue());
                    }
                }

                if (sensorExposureTime != null) {
                    if (!settings.isSupportedSensorExposureTime(sensorExposureTime)) {
                        MessageUtils.setInvalidRequestParameterError(response, "sensorExposureTime is invalid.");
                        return true;
                    }
                    settings.setSensorExposureTime(sensorExposureTime);
                }

                if (sensorSensitivity != null) {
                    if (!settings.isSupportedSensorSensorSensitivity(sensorSensitivity)) {
                        MessageUtils.setInvalidRequestParameterError(response, "sensorSensitivity is invalid.");
                        return true;
                    }
                    settings.setSensorSensitivity(sensorSensitivity);
                }

                if (sensorFrameDuration != null) {
                    if (!settings.isSupportedSensorFrameDuration(sensorFrameDuration)) {
                        MessageUtils.setInvalidRequestParameterError(response, "sensorFrameDuration is invalid.");
                        return true;
                    }
                    settings.setSensorFrameDuration(sensorFrameDuration);
                }

                Stabilization stab = Stabilization.nameOf(stabilization);
                if (stab != null) {
                    if (stab == Stabilization.NONE) {
                        settings.setStabilizationMode(null);
                    } else {
                        if (!settings.isSupportedStabilization(stab.getValue())) {
                            MessageUtils.setInvalidRequestParameterError(response, "stabilization is invalid.");
                            return true;
                        }
                        settings.setStabilizationMode(stab.getValue());
                    }
                }

                OpticalStabilization opticalStab = OpticalStabilization.nameOf(opticalStabilization);
                if (opticalStab != null) {
                    if (opticalStab == OpticalStabilization.NONE) {
                        settings.setOpticalStabilizationMode(null);
                    } else {
                        if (!settings.isSupportedOpticalStabilization(opticalStab.getValue())) {
                            MessageUtils.setInvalidRequestParameterError(response, "opticalStabilization is invalid.");
                            return true;
                        }
                        settings.setOpticalStabilizationMode(opticalStab.getValue());
                    }
                }

                NoiseReduction nr = NoiseReduction.nameOf(noiseReduction);
                if (nr != null) {
                    if (nr == NoiseReduction.NONE) {
                        settings.setNoiseReduction(null);
                    } else {
                        if (!settings.isSupportedNoiseReduction(nr.getValue())) {
                            MessageUtils.setInvalidRequestParameterError(response, "noiseReduction is invalid.");
                            return true;
                        }
                        settings.setNoiseReduction(nr.getValue());
                    }
                }

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });
    }

    private Camera2Recorder getCameraRecorder(final String target) {
        if (mRecorderManager == null) {
            return null;
        }
        HostMediaRecorder recorder = mRecorderManager.getRecorder(target);
        if (recorder instanceof Camera2Recorder) {
            return (Camera2Recorder) recorder;
        }
        return null;
    }

    public enum NoiseReduction {
        NONE("none", null),
        FAST("fast", CameraMetadata.NOISE_REDUCTION_MODE_FAST),
        HIGH_QUALITY("high_quality", CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY),
        MINIMAL("minimal", CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL),
        OFF("off", CameraMetadata.NOISE_REDUCTION_MODE_OFF),
        ZERO_SHUTTER_LAG("zero_shutter_lag", CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG);

        private final String mName;
        private final Integer mValue;

        NoiseReduction(String name, Integer value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public Integer getValue() {
            return mValue;
        }

        public static NoiseReduction nameOf(String name) {
            for (NoiseReduction s : values()) {
                if (s.mName.equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return null;
        }

        public static NoiseReduction valueOf(Integer value) {
            for (NoiseReduction s : values()) {
                if (s.mValue == value) {
                    return s;
                }
            }
            return null;
        }
    }

    public enum OpticalStabilization {
        NONE("none", null),
        OFF("off", CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF),
        On("on", CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);

        private final String mName;
        private final Integer mValue;

        OpticalStabilization(String name, Integer value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public Integer getValue() {
            return mValue;
        }

        public static OpticalStabilization nameOf(String name) {
            for (OpticalStabilization s : values()) {
                if (s.mName.equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return null;
        }

        public static OpticalStabilization valueOf(Integer value) {
            for (OpticalStabilization s : values()) {
                if (s.mValue == value) {
                    return s;
                }
            }
            return null;
        }
    }

    public enum Stabilization {
        NONE("none", null),
        OFF("off", CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF),
        On("on", CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);

        private final String mName;
        private final Integer mValue;

        Stabilization(String name, Integer value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public Integer getValue() {
            return mValue;
        }

        public static Stabilization nameOf(String name) {
            for (Stabilization s : values()) {
                if (s.mName.equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return null;
        }

        public static Stabilization valueOf(Integer value) {
            for (Stabilization s : values()) {
                if (s.mValue == value) {
                    return s;
                }
            }
            return null;
        }
    }

    public enum AutoExposure {
        NONE("none", null),
        OFF("off", CameraMetadata.CONTROL_AE_MODE_OFF),
        ON("on", CameraMetadata.CONTROL_AE_MODE_ON),
        AUTO_FLASH("on_auto_flash", CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH),
        ALWAYS_FLASH("on_always_flash", CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH),
        AUTO_FLASH_REDEYE("on_auto_flash_redeye", CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE),
        EXTERNAL_FLASH("on_external_flash", CameraMetadata.CONTROL_AE_MODE_ON_EXTERNAL_FLASH);

        private final String mName;
        private final Integer mValue;

        AutoExposure(String name, Integer value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public Integer getValue() {
            return mValue;
        }

        public static AutoExposure nameOf(String name) {
            for (AutoExposure m : values()) {
                if (m.mName.equalsIgnoreCase(name)) {
                    return m;
                }
            }
            return null;
        }

        public static AutoExposure valueOf(Integer value) {
            for (AutoExposure m : values()) {
                if (m.mValue == value) {
                    return m;
                }
            }
            return null;
        }
    }

    public enum WhiteBalance {
        NONE("none", null),
        AUTO("auto", CameraMetadata.CONTROL_AWB_MODE_AUTO),
        INCANDESCENT("incandescent", CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT),
        FLUORESCENT("fluorescent", CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT),
        WARM_FLUORESCENT("warm-fluorescent", CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT),
        DAYLIGHT("daylight", CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT),
        CLOUDY_DAYLIGHT("cloudy-daylight", CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT),
        TWILIGHT("twilight", CameraMetadata.CONTROL_AWB_MODE_TWILIGHT),
        SHADE("shade", CameraMetadata.CONTROL_AWB_MODE_SHADE);

        /** カメラの位置を表現する名前. */
        private final String mName;

        /**
         * カメラの番号.
         */
        private final Integer mValue;

        WhiteBalance(String name, Integer value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public Integer getValue() {
            return mValue;
        }

        public static WhiteBalance nameOf(String name) {
            for (WhiteBalance wb : values()) {
                if (wb.mName.equals(name)) {
                    return wb;
                }
            }
            return null;
        }

        public static WhiteBalance valueOf(Integer value) {
            for (WhiteBalance wb : values()) {
                if (wb.mValue == value) {
                    return wb;
                }
            }
            return null;
        }
    }

    public enum AutoFocus {
        NONE("none", null),
        OFF("off", CameraMetadata.CONTROL_AF_MODE_OFF),
        AUTO("auto", CameraMetadata.CONTROL_AF_MODE_AUTO),
        MACRO("macro", CameraMetadata.CONTROL_AF_MODE_MACRO),
        CONTINUOUS_VIDEO("continuous_video", CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO),
        CONTINUOUS_PICTURE("continuous_picture", CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE),
        EDOF("edof", CameraMetadata.CONTROL_AF_MODE_EDOF);

        private final String mName;
        private final Integer mValue;

        AutoFocus(String name, Integer value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public Integer getValue() {
            return mValue;
        }

        public static AutoFocus nameOf(String name) {
            for (AutoFocus af : values()) {
                if (af.mName.equals(name)) {
                    return af;
                }
            }
            return null;
        }

        public static AutoFocus valueOf(Integer value) {
            for (AutoFocus af : values()) {
                if (af.mValue == value) {
                    return af;
                }
            }
            return null;
        }
    }
}
