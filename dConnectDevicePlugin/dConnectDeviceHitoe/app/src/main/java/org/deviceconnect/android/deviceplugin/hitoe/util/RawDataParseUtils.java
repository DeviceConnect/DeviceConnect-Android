/*
 RawDataParseUtils
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.util;

import org.deviceconnect.android.deviceplugin.hitoe.data.AccelerationData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.PoseEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.data.StressEstimationData;
import org.deviceconnect.android.deviceplugin.hitoe.data.TargetDeviceData;
import org.deviceconnect.android.deviceplugin.hitoe.data.WalkStateData;
import org.deviceconnect.profile.PoseEstimationProfileConstants;
import org.deviceconnect.profile.WalkStateProfileConstants;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class containing utility methods parse raw data.
 * @author NTT DOCOMO, INC.
 */
public final class RawDataParseUtils {

    /**
     * Private Constructor.
     */
    private RawDataParseUtils() {
    }


    /**
     * Parse HeartRate data.
     * @param raw raw data
     * @return HeartRate object
     */
    public static HeartData parseHeartRate(final String raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.Rate, "heart rate",
                                                    147842, "beat per min",  264864);
    }

    /**
     * Parse RRI data.
     * @param raw raw data
     * @return RRI object
     */
    public static HeartData parseRRI(final String raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.RRI, "RR interval",
                147240, "ms",  264338);
    }

    /**
     * Parse EnergyExpended data.
     * @param raw raw data
     * @return EnergyExpended object
     */
    public static HeartData parseEnergyExpended(final String raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.EnergyExpended, "energy expended",
                119, "Calories",  6784);
    }


    /**
     * Parse Hitoe Device info data.
     * @param hitoe hitoe device
     * @param batteryLevel battery level
     * @return Hitoe device info object
     */
    public static TargetDeviceData parseDeviceData(final HitoeDevice hitoe, final float batteryLevel) {
        TargetDeviceData device = new TargetDeviceData();
        device.setProductName(hitoe.getName());
        if (batteryLevel > -1.0) {
            device.setBatteryLevel((batteryLevel + 1) / 4.0f);
        }
        return device;
    }


    /**
     * Parse Acceleration Data.
     * @param data Accleration data
     * @param raw raw data
     * @return Acceleration object
     */
    public static AccelerationData parseAccelerationData(final AccelerationData data, final String raw) {
        if (raw == null) {
            return data;
        }
        String[] lineList = raw.split(HitoeConstants.BR);
        String[] list = lineList[0].split(HitoeConstants.COMMA, -1);
        String[] accList = list[1].split(HitoeConstants.COLON, -1);
        double[] accelList = new double[3];
        for (int i = 0; i < accList.length; i++) {
            accelList[i] = Double.valueOf(accList[i]);
        }
        data.setAccelX(accelList[0]);
        data.setAccelY(accelList[1]);
        data.setAccelZ(accelList[2]);
        return data;
    }



    /**
     * Parse ECG Data.
     * @param raw raw data
     * @return ECG data object
     */
    public static HeartData parseECG(final String raw) {
        String[] lineList = raw.split(HitoeConstants.BR);
        HeartData heart = new HeartData();
        for (int i = 0; i < lineList.length; i++) {
            String val = lineList[i];
            if (val == null) {
                continue;
            }
            String[] list = val.split(HitoeConstants.COMMA, -1);
            long timestamp = Long.parseLong(list[0]);
            String[] ecgList = list[1].split(HitoeConstants.COLON, -1);
            String date = RFC3339DateUtils.toString(timestamp);
            heart.setValue(Float.parseFloat(ecgList[0]));
            heart.setTimeStamp(timestamp);
            heart.setTimeStampString(date);
        }
        heart.setHeartRateType(HeartData.HeartRateType.ECG);
        heart.setMderFloat(MDERFloatConvreterUtils.convertMDERFloatToFloat(heart.getValue()));
        heart.setType("ecg beat");
        heart.setTypeCode(663568);
        heart.setUnit("mVolt * miliSecond");
        heart.setUnitCode(3328);

        return heart;

    }

    /**
     * Parse Stress Estimation data.
     * @param raw raw data
     * @return Stress Estimation object
     */
    public static StressEstimationData parseStressEstimation(final String raw) {
        StressEstimationData stress = new StressEstimationData();
        if (raw == null) {
            return stress;
        }
        String[] lineList = raw.split(HitoeConstants.BR);
        String[] stressList = lineList[0].split(HitoeConstants.COMMA, -1);
        if (stressList[0].isEmpty() || stressList[1].isEmpty()) {
            return stress;
        }
        long timestamp = Long.parseLong(stressList[0]);
        double lfhf = Double.parseDouble(stressList[1]);
        stress.setLFHFValue(lfhf);
        stress.setTimeStamp(timestamp);
        stress.setTimeStampString(RFC3339DateUtils.toString(timestamp));
        return stress;
    }


    /**
     * Parse Pose Estimation data.
     * @param raw raw data
     * @return Pose Estimation object
     */
    public static PoseEstimationData parsePoseEstimation(final String raw) {
        PoseEstimationData pose = new PoseEstimationData();
        if (raw == null) {
            return pose;
        }
        String[] lineList = raw.split(HitoeConstants.BR);
        String[] poseList = lineList[0].split(HitoeConstants.COMMA, -1);
        long timestamp  = 0;
        try {
            timestamp = Long.parseLong(poseList[0]);
        } catch (NumberFormatException e) {
            return pose;
        }
        pose.setTimeStamp(timestamp);
        pose.setTimeStampString(RFC3339DateUtils.toString(timestamp));

        String type = poseList[1];

        int backForward = Integer.parseInt(poseList[2]);
        int leftRight = Integer.parseInt(poseList[3]);

        if (type.equals("LyingLeft")) {
            pose.setPoseState(PoseEstimationProfileConstants.PoseState.FaceLeft);
        } else if (type.equals("LyingRight")) {
            pose.setPoseState(PoseEstimationProfileConstants.PoseState.FaceRight);
        } else if (type.equals("LyingFaceUp")) {
            pose.setPoseState(PoseEstimationProfileConstants.PoseState.FaceUp);
        } else if (type.equals("LyingFaceDown")) {
            pose.setPoseState(PoseEstimationProfileConstants.PoseState.FaceDown);
        } else {
            if (backForward > HitoeConstants.BACK_FORWARD_THRESHOLD) {
                pose.setPoseState(PoseEstimationProfileConstants.PoseState.Forward);
            } else if (backForward < -1 * HitoeConstants.BACK_FORWARD_THRESHOLD) {
                pose.setPoseState(PoseEstimationProfileConstants.PoseState.Backward);
            } else if (leftRight > HitoeConstants.LEFT_RIGHT_THRESHOLD) {
                pose.setPoseState(PoseEstimationProfileConstants.PoseState.Leftside);
            } else if (leftRight < -1 * HitoeConstants.LEFT_RIGHT_THRESHOLD) {
                pose.setPoseState(PoseEstimationProfileConstants.PoseState.Rightside);
            } else {
                pose.setPoseState(PoseEstimationProfileConstants.PoseState.Standing);
            }
        }

        return pose;
    }

    /**
     * Parse Walk State data.
     * @param data exist walk state data
     * @param raw raw data
     * @return walk state object
     */
    public static WalkStateData parseWalkState(final WalkStateData data, final String raw) {
        String[] lineList = raw.split(HitoeConstants.BR);
        String[] walkList = lineList[0].split(HitoeConstants.COMMA, -1);
        long timestamp  = 0;
        try {
            timestamp = Long.parseLong(walkList[0]);
        } catch (NumberFormatException e) {
            return data;
        }
        data.setTimeStamp(timestamp);
        data.setTimeStampString(RFC3339DateUtils.toString(timestamp));
        data.setStep(Integer.parseInt(walkList[1]));
        if (walkList[4].equals("Walking")) {
            data.setState(WalkStateProfileConstants.WalkState.Walking);
        } else if (walkList[4].equals("Running")) {
            data.setState(WalkStateProfileConstants.WalkState.Running);
        } else {
            data.setState(WalkStateProfileConstants.WalkState.Stop);
        }
        data.setSpeed(Double.parseDouble(walkList[6]));
        data.setDistance(Double.parseDouble(walkList[7]));
        return data;
    }

    /**
     * Parse Walk State data for balance.
     * @param data walk state data
     * @param raw raw data
     * @return walk state object
     */
    public static WalkStateData parseWalkStateForBalance(final WalkStateData data, final String raw) {
        String[] lineList = raw.split(HitoeConstants.BR);
        String[] walkList = lineList[0].split(HitoeConstants.COMMA, -1);
        if (walkList.length <= 1) {
            return data;
        }
        data.setBalance(Double.parseDouble(walkList[1]));
        return data;
    }

    /**
     * Parse HeartRate data.
     * @param raw raw data
     * @param heartRateType HeartRate type
     * @param type type
     * @param typeCode type code
     * @param unit unit
     * @param unitCode unit Code
     * @return HeartRate data
     */
    private static HeartData parseHeartRate(final String raw, final HeartData.HeartRateType heartRateType,
                                            final String type, final int typeCode,
                                            final String unit, final int unitCode) {
        HeartData heart = new HeartData();
        String[] lineList = raw.split(HitoeConstants.BR);
        String rateString = lineList[lineList.length - 1];
        heart.setHeartRateType(heartRateType);
        if (rateString == null) {
            return null;
        }
        String[] hrValue = splitComma(rateString);
        float rate = Float.parseFloat(hrValue[1]);
        heart.setValue(rate);
        heart.setMderFloat(MDERFloatConvreterUtils.convertMDERFloatToFloat(rate));
        heart.setType(type);
        heart.setTypeCode(typeCode);
        heart.setUnit(unit);
        heart.setUnitCode(unitCode);
        heart.setTimeStamp(Long.parseLong(hrValue[0]));
        heart.setTimeStampString(RFC3339DateUtils.toString(Long.parseLong(hrValue[0])));
        return heart;

    }



    /**
     * Split Comma.
     * @param val split value
     * @return string array
     */
    private static String[] splitComma(final String val) {
        return val.split(",", -1);
    }

 }
