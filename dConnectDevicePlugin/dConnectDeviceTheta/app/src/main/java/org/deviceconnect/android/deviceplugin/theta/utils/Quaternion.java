package org.deviceconnect.android.deviceplugin.theta.utils;


import android.util.Log;

public class Quaternion {
    private final float mReal;
    private final Vector3D mImaginary;

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

    public static Vector3D rotate(final Vector3D point, final Vector3D axis, final float radian) {
        Quaternion P = new Quaternion(0, point);

        float c = (float) Math.cos(radian / 2.0f);
        float s = (float) Math.sin(radian / 2.0f);
        Quaternion Q = new Quaternion(c, axis.multiply(s));
        Quaternion R = Q.conjugate();

        Quaternion PR = R.multiply(P);
        Quaternion QPR = PR.multiply(Q);
        return QPR.imaginary();
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
}
