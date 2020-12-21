package org.deviceconnect.android.deviceplugin.host.sensor;

import java.util.List;

public interface HostTouchEventObserver {
    void observeTouchEvent(List<HostTouchEvent> event);

    class HostTouchEvent {
        private int mType;
        private int mX;
        private int mY;
        private int mId;

        public void setType(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }

        public void setX(int x) {
            mX = x;
        }

        public int getX() {
            return mX;
        }

        public void setY(int y) {
            mY = y;
        }

        public int getY() {
            return mY;
        }

        public void setId(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }
    }
}
