/*
 HostCameraProfile.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Range;

import org.deviceconnect.android.deviceplugin.host.profile.utils.AutoExposure;
import org.deviceconnect.android.deviceplugin.host.profile.utils.AutoFocus;
import org.deviceconnect.android.deviceplugin.host.profile.utils.NoiseReduction;
import org.deviceconnect.android.deviceplugin.host.profile.utils.OpticalStabilization;
import org.deviceconnect.android.deviceplugin.host.profile.utils.Stabilization;
import org.deviceconnect.android.deviceplugin.host.profile.utils.WhiteBalance;
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
                Float focalLength = parseFloat(request, "focalLength");

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

                if (focalLength != null) {
                    if (focalLength == 0) {
                        settings.setFocalLength(null);
                    } else {
                        if (!settings.isSupportedFocalLength(focalLength)) {
                            MessageUtils.setInvalidRequestParameterError(response, "focalLength is invalid.");
                            return true;
                        }
                        settings.setFocalLength(focalLength);
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
}
