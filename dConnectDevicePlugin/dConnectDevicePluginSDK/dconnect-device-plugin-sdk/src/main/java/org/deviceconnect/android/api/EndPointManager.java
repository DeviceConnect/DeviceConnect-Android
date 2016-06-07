package org.deviceconnect.android.api;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum EndPointManager {

    INSTANCE;

    private final List<EndPoint> mEndPoints = new ArrayList<EndPoint>();

    public void addEndPoint(final EndPoint endPoint) {
        synchronized (mEndPoints) {
            removeEndPoint(endPoint.getId());
            mEndPoints.add(endPoint);
        }
    }

    public void removeEndPoint(final String id) {
        synchronized (mEndPoints) {
            for (Iterator<EndPoint> it = mEndPoints.iterator(); it.hasNext();) {
                EndPoint endPoint = it.next();
                if (endPoint.getId().equals(id)) {
                    it.remove();
                    return;
                }
            }
        }
    }

    public EndPoint getEndPoint(final String id) {
        synchronized (mEndPoints) {
            for (Iterator<EndPoint> it = mEndPoints.iterator(); it.hasNext();) {
                EndPoint endPoint = it.next();
                if (endPoint.getId().equals(id)) {
                    return endPoint;
                }
            }
        }
        return null;
    }

}
