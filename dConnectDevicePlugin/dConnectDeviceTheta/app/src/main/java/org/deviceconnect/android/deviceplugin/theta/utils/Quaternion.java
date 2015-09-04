/*
 Quaternion.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.utils;


public class Quaternion {
    private final float mReal;
    private final Vector3D mImaginary;

    public Quaternion(final Quaternion q) {
        this(q.real(), q.imaginary());
    }

    public Quaternion(final float real, final Vector3D imaginary) {
        mReal = real;
        mImaginary = imaginary;
    }

    public float real() {
        return mReal;
    }

    public Vector3D imaginary() {
        return mImaginary;
    }

    public Quaternion conjugate() {
        return new Quaternion(real(), imaginary().multiply(-1));
    }

    public Quaternion multiply(final Quaternion a) {
        final Quaternion b = this;

        // NOTE:
        //     if Q = (q; V) and R = (r; W),
        //         Q x R = (q * r - V * W; q * W + r * V + V x W)

        float real = a.real() * b.real() - Vector3D.innerProduct(a.imaginary(), b.imaginary());
        Vector3D imaginary = Vector3D.add(
            a.imaginary().multiply(b.real()),
            b.imaginary().multiply(a.real()),
            Vector3D.outerProduct(a.imaginary(), b.imaginary()));
        return new Quaternion(real, imaginary);
    }

    public static Quaternion multiply(final Quaternion... args) {
        Quaternion v = args[args.length - 1];
        for (int i = args.length - 2; i >= 0; i--) {
            v = v.multiply(args[i]);
        }
        return v;
    }

    public Vector3D rotate(final Vector3D target) {
        Quaternion P = new Quaternion(0, target);
        Quaternion Q = this;
        Quaternion R = this.conjugate();
        return R.multiply(P).multiply(Q).imaginary();
    }

    public static Vector3D rotate(final Vector3D point, final Vector3D nonNormalizedAxis, final float radian) {
        Quaternion Q = quaternionFromAxisAndAngle(nonNormalizedAxis.normalize(), radian);
        return Q.rotate(point);
    }

    public static Vector3D rotateXYZ(final Vector3D point, final float rotateX, final float rotateY,
                                  final float rotateZ) {
        Vector3D axisX = new Vector3D(1, 0, 0);
        Vector3D axisY = new Vector3D(0, 1, 0);
        Vector3D axisZ = new Vector3D(0, 0, 1);

        Vector3D result = point;
        result = Quaternion.rotate(result, axisX, rotateX);
        result = Quaternion.rotate(result, axisY, rotateY);
        result = Quaternion.rotate(result, axisZ, rotateZ);
        return result;
    }

    public static Quaternion quaternionFromAxisAndAngle(final Vector3D normalizedAxis, final float radian) {
        float c = (float) Math.cos(radian / 2.0f);
        float s = (float) Math.sin(radian / 2.0f);
        return new Quaternion(c, normalizedAxis.multiply(s));
    }
}
