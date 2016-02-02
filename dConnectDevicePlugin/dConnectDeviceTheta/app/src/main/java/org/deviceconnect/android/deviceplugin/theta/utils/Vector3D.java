/*
 Vector3D.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.utils;


public class Vector3D {
    private static final int SIZE = 3;
    private final float[] mCoods = new float[SIZE];

    public Vector3D(final float x, final float y, final float z) {
        mCoods[0] = x;
        mCoods[1] = y;
        mCoods[2] = z;
    }

    public Vector3D(final float[] coods) {
        for (int i = 0; i < mCoods.length; i++) {
            mCoods[i] = coods[i];
        }
    }

    public Vector3D(final Vector3D v) {
        this(v.mCoods[0], v.mCoods[1], v.mCoods[2]);
    }

    public float x() {
        return mCoods[0];
    }

    public float y() {
        return mCoods[1];
    }

    public float z() {
        return mCoods[2];
    }

    public float norm() {
        float v = 0;
        for (int i = 0; i < SIZE; i++) {
            v += Math.pow(mCoods[i], 2);
        }
        return (float) Math.sqrt(v);
    }

    public Vector3D normalize() {
        float norm = norm();
        if (norm == 0) {
            return this;
        }
        float[] coods = new float[SIZE];
        for (int i = 0; i < mCoods.length; i++) {
            coods[i] = mCoods[i] / norm;
        }
        return new Vector3D(coods);
    }

    public Vector3D add(final Vector3D that) {
        return new Vector3D(
            x() + that.x(),
            y() + that.y(),
            z() + that.z());
    }

    public Vector3D multiply(final float a) {
        return new Vector3D(a * x(), a * y(), a * z());
    }

    public static Vector3D add(Vector3D... args) {
        Vector3D v = new Vector3D(0, 0, 0);
        for (int i = 0; i < args.length; i++) {
            v = v.add(args[i]);
        }
        return v;
    }

    public static Vector3D multiply(final float a, final Vector3D v) {
        return v.multiply(a);
    }

    public static float innerProduct(final Vector3D a, final Vector3D b) {
        float v = 0;
        for (int i = 0; i < SIZE; i++) {
            v += a.mCoods[i] * b.mCoods[i];
        }
        return v;
    }

    public static Vector3D outerProduct(final Vector3D a, final Vector3D b) {
        float x = a.y() * b.z() - a.z() * b.y();
        float y = a.z() * b.x() - a.x() * b.z();
        float z = a.x() * b.y() - a.y() * b.x();
        return new Vector3D(x, y, z);
    }
}
