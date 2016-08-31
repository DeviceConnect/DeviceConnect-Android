package org.deviceconnect.android.deviceplugin.sphero.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Activityに受け渡すSpheroの情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class SpheroParcelable implements Parcelable{
    /**Spheroの状態. */
    public enum SpheroState {
        /** 接続. */
        Connected,
        /** 見つかっている. */
        Disconnected,
        /** 認識しているがBluetooth検索時に見つからない. */
        Remember,
        /** 削除時. */
        Delete,
        /** エラー時. */
        Error;
    }
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
    private SpheroState mIsConnected;

    public SpheroParcelable(final String spheroId, final String spheroName, final SpheroState isConnected) {
        setSpheroId(spheroId);
        setSpheroName(spheroName);
        setConnected(isConnected);
    }

    protected SpheroParcelable(Parcel in) {
        setSpheroId(in.readString());
        setSpheroName(in.readString());
        setConnected(SpheroState.valueOf(in.readString()));
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
        parcel.writeString(isConnected().name());
    }

    public String getSpheroId() {
        return mSpheroId;
    }

    public void setSpheroId(final String mSpheroId) {
        this.mSpheroId = mSpheroId;
    }

    public String getSpheroName() {
        return mSpheroName;
    }

    public void setSpheroName(String mSpheroName) {
        this.mSpheroName = mSpheroName;
    }

    public SpheroState isConnected() {
        return mIsConnected;
    }

    public void setConnected(final SpheroState mIsConnected) {
        this.mIsConnected = mIsConnected;
    }
}
