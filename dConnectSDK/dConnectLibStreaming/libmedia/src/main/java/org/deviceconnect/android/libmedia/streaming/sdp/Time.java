package org.deviceconnect.android.libmedia.streaming.sdp;

public class Time {
    private Integer mStart;
    private Integer mStop;

    public Time() {
        mStart = 0;
        mStop = 0;
    }

    public Time(Integer start, Integer stop) {
        mStart = start;
        mStop = stop;
    }

    public Time(String line) {
        String[] params = line.split(" ");
        mStart = Integer.parseInt(params[0]);
        mStop = Integer.parseInt(params[1]);
    }

    public Integer getStart() {
        return mStart;
    }

    public void setStart(Integer start) {
        mStart = start;
    }

    public Integer getStop() {
        return mStop;
    }

    public void setStop(Integer stop) {
        mStop = stop;
    }

    @Override
    public String toString() {
        return "t=" + mStart.toString() + " " + mStop.toString();
    }
}
