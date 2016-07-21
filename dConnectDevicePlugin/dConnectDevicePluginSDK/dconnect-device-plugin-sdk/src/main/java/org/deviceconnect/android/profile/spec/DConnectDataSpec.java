package org.deviceconnect.android.profile.spec;


public abstract class DConnectDataSpec implements DConnectSpecConstants {

    final DataType mDataType;

    protected DConnectDataSpec(final DataType type) {
        mDataType = type;
    }

    public DataType getDataType() {
        return mDataType;
    }
}
