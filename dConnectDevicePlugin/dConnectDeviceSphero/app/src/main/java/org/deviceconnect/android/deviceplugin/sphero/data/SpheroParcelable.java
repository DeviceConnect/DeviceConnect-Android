package org.deviceconnect.android.deviceplugin.sphero.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Activityに受け渡すSpheroの情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class SpheroParcelable implements Parcelable{
    /**
     * Sphero id.
     */
    private String mSpheroId;

    /**
     * Sphero name.
     */
    private String mSpheroName;

    /**
     * Sphero connected.
     */
    private boolean mIsConnected;

    public SpheroParcelable(final String spheroId, final String spheroName, final boolean isConnected) {
        setSpheroId(spheroId);
        setSpheroName(spheroName);
        setConnected(isConnected);
    }

    protected SpheroParcelable(Parcel in) {
        setSpheroId(in.readString());
        setSpheroName(in.readString());
        setConnected(in.readByte() != 0);
    }

    public static final Creator<SpheroParcelable> CREATOR = new Creator<SpheroParcelable>() {
        @Override
        public SpheroParcelable createFromParcel(Parcel in) {
            return new SpheroParcelable(in);
        }

        @Override
        public SpheroParcelable[] newArray(int size) {
            return new SpheroParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getSpheroId());
        parcel.writeString(getSpheroName());
        parcel.writeByte((byte) (isConnected() ? 1 : 0));
    }

    public String getSpheroId() {
        return mSpheroId;
    }

    public void setSpheroId(String mSpheroId) {
        this.mSpheroId = mSpheroId;
    }

    public String getSpheroName() {
        return mSpheroName;
    }

    public void setSpheroName(String mSpheroName) {
        this.mSpheroName = mSpheroName;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setConnected(boolean mIsConnected) {
        this.mIsConnected = mIsConnected;
    }
}
