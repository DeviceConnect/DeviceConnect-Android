package org.deviceconnect.android.manager.compat;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class PathConversionTable {

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

    public static final PathConversion BATTERY_CHARGING_TIME
        = new PathConversion(
                new Path("battery", "chargingtime"), new Path("battery", "chargingTime"));

    public static final PathConversion BATTERY_DISCHARGING_TIME
        = new PathConversion(new Path("battery", "dischargingtime"),
                new Path("battery", "dischargingTime"));

    private static final Map<Path, Path> FORWARD = new LinkedHashMap<Path, Path>();
    private static final Map<Path, Path> BACKWARD = new LinkedHashMap<Path, Path>();

    static {
        for (Path[] pair : PATH_PAIRS) {
            FORWARD.put(pair[0], pair[1]);
            BACKWARD.put(pair[1], pair[0]);
        }
    }

    public static List<PathConversion> getConversions(final String supportedProfile) {
        List<PathConversion> conversions = new ArrayList<PathConversion>();
        for(Map.Entry<Path, Path> entry : FORWARD.entrySet()) {
            if (entry.getValue().mProfileName.equals(supportedProfile)) {
                conversions.add(new PathConversion(entry.getKey(), entry.getValue()));
            }
        }
        if (conversions.size() > 0) {
            return conversions;
        }
        for(Map.Entry<Path, Path> entry : BACKWARD.entrySet()) {
            if (entry.getValue().mProfileName.equals(supportedProfile)) {
                conversions.add(new PathConversion(entry.getKey(), entry.getValue()));
            }
        }
        return conversions;
    }

    public static Path forwardPath(final String pathExpression) {
        return FORWARD.get(Path.parsePath(pathExpression));
    }

    public static String forwardProfileName(final String profileName) {
        Path forward = FORWARD.get(new Path(profileName, null, null));
        return forward.mProfileName;
    }

}
