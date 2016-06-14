package org.deviceconnect.android.manager.compat;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum PathConversionTable {

    INSTANCE;

    private static final Path[][] PATH_PAIRS = {
        {new Path("drive_controller"), new Path("driveController")},
        {new Path("file_descriptor"), new Path("fileDescriptor")},
        {new Path("media_player"), new Path("mediaPlayer")},
        {new Path("mediastream_recording/media_list"), new Path("mediaStreamRecording/mediaList")},
        {new Path("mediastream_recording/play_status"), new Path("mediaStreamRecording/playStatus")},
        {new Path("mediastream_recording"), new Path("mediaStreamRecording")},
        {new Path("omnidirectional_image"), new Path("omnidirectionalImage")},
        {new Path("remote_controller"), new Path("remoteController")}
    };

    private static final Map<Path, Path> FORWARD = new HashMap<Path, Path>();
    private static final Map<Path, Path> BACKWARD = new HashMap<Path, Path>();

    static {
        for (Path[] pair : PATH_PAIRS) {
            FORWARD.put(pair[0], pair[1]);
            BACKWARD.put(pair[1], pair[0]);
        }
    }

    public List<PathConversion> getConversions(final String supportedProfile) {
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

}
