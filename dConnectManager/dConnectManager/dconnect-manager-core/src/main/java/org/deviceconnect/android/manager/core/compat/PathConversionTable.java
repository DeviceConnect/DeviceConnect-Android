/*
 PathConversionTable.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.compat;


import java.util.ArrayList;
import java.util.List;

/**
 * パス変換テーブル.
 *
 * @author NTT DOCOMO, INC.
 */
public class PathConversionTable {

    private static final Path[][] PATH_PAIRS = {
        {new Path("drive_controller"), new Path("driveController")},
        {new Path("file_descriptor"), new Path("fileDescriptor")},
        {new Path("media_player", "media_list"), new Path("mediaPlayer", "mediaList")},
        {new Path("media_player", "play_status"), new Path("mediaPlayer", "playStatus")},
        {new Path("media_player"), new Path("mediaPlayer")},
        {new Path("mediastream_recording"), new Path("mediaStreamRecording")},
        {new Path("omnidirectional_image"), new Path("omnidirectionalImage")},
        {new Path("remote_controller"), new Path("remoteController")}
    };

    static final PathConversion BATTERY_CHARGING_TIME
        = new PathConversion(
                new Path("battery", "chargingtime"), new Path("battery", "chargingTime"));

    static final PathConversion BATTERY_DISCHARGING_TIME
        = new PathConversion(new Path("battery", "dischargingtime"),
                new Path("battery", "dischargingTime"));

    public static final List<PathConversion> OLD_TO_NEW = new ArrayList<PathConversion>();
    public static final List<PathConversion> NEW_TO_OLD = new ArrayList<PathConversion>();

    static {
        for (Path[] pair : PATH_PAIRS) {
            OLD_TO_NEW.add(new PathConversion(pair[0], pair[1]));
            NEW_TO_OLD.add(new PathConversion(pair[1], pair[0]));
        }
        NEW_TO_OLD.add(BATTERY_CHARGING_TIME);
        NEW_TO_OLD.add(BATTERY_DISCHARGING_TIME);
    }

    static Path forwardPath(final String pathExpression) {
        return forwardPath(Path.parsePath(pathExpression));
    }

    static Path forwardPath(final Path path) {
        for (PathConversion conversion : OLD_TO_NEW) {
            if (conversion.mFrom.matches(path)) {
                return conversion.mTo;
            }
        }
        return path;
    }

    public static String forwardProfileName(final String profileName) {
        for (Path[] pair : PATH_PAIRS) {
            Path oldPath = pair[0];
            Path newPath = pair[1];
            if (oldPath.mProfileName.equalsIgnoreCase(profileName)) {
                return newPath.mProfileName;
            }
        }
        return profileName;
    }

}
