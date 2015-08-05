package org.deviceconnect.android.deviceplugin.theta.utils;


public class Vector3D {
    private static final int SIZE = 3;
    private final float[] mCoods = new float[SIZE];

    public Vector3D(final float x, final float y, final float z) {
        mCoods[0] = x;
        mCoods[1] = y;
        mCoods[2] = z;
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

    public float length() {
        float v = 0;
        for (int i = 0; i < SIZE; i++) {
            v += Math.pow(mCoods[i], 2);
        }
        return (float) Math.sqrt(v);
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
