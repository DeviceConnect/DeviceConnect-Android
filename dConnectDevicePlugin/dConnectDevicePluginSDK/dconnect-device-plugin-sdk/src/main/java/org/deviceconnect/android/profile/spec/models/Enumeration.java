package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.List;

public class Enumeration<T> implements DConnectSpec {

    private List<T> mEnum;

    public List<T> getEnum() {
        return mEnum;
    }

    public void setEnum(List<T> anEnum) {
        mEnum = anEnum;
    }

    public void add(T a) {
        mEnum.add(a);
    }

    @Override
    public Bundle toBundle() {
        return null;
    }
}
