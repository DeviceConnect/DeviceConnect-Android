/*
 UVSphere.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.opengl.model;


import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * UV sphere model class.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVSphere {

    private final int COORDS_PER_VERTEX = 3;
    private final int TEXTURE_COORDS_PER_VERTEX = 2;

    private int mStrips;
    private int mStripePointsNum;
    private ArrayList<FloatBuffer> mVertices;
    private ArrayList<FloatBuffer> mTextureCoords;

    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int textureStride = TEXTURE_COORDS_PER_VERTEX * 4;
    private final float mRadius;

    /**
     * Constructor
     * Sphere is displayed according to the number of partitions.
     * The longitude is created from the number of partitions which is half the number of
     * latitude lines 1 and the number of polygons which is double the number of
     * partitions set in the radius specified as the origin coordinates.
     *
     * @param radius Radius
     * @param divide Number of partitions (must be an even number)
     */
    public UVSphere(float radius, int divide) {
        mVertices = new ArrayList<>();
        mTextureCoords = new ArrayList<>();
        mRadius = radius;

        if (radius <= 0 || divide <= 0 || 0 != (divide % 2)) {
            throw new IllegalArgumentException();
        }

        mStrips = divide / 2;
        mStripePointsNum = (divide + 1) * 2;
        makeSphereVertices(radius, divide);
    }

    public float getRadius() {
        return mRadius;
    }

    /**
     * Sphere drawing method
     *
     * @param mPositionHandle Handler value tied to gl_Position in vertex shader
     * @param mUVHandle Handler value tied to the UV coordinates provided to the fragment shader via the varyig variable
     */
    public void draw(int mPositionHandle, int mUVHandle) {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mUVHandle);

        for (int i = 0; i < mStrips; i++) {
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertices.get(i));
            GLES20.glVertexAttribPointer(mUVHandle, TEXTURE_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, textureStride, mTextureCoords.get(i));

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mStripePointsNum);
        }

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mUVHandle);
    }


    private void makeSphereVertices(float radius, int divide) {

        float altitude = 0.0f;
        float altitudeDelta = 0.0f;
        float azimuth = 0.0f;
        float ex = 0.0f;
        float ey = 0.0f;
        float ez = 0.0f;

        for (int i = 0; i < divide / 2; ++i) {
            altitude = (float) (Math.PI / 2.0 - i * (Math.PI * 2) / divide);
            altitudeDelta = (float) (Math.PI / 2.0 - (i + 1) * (Math.PI * 2) / divide);


            float[] vertices = new float[divide * 6 + 6];
            float[] texCoords = new float[divide * 4 + 4];

            for (int j = 0; j <= divide; ++j) {
                azimuth = (float) (Math.PI - (j * (Math.PI * 2) / divide));

                // first point
                ex = (float) (Math.cos(altitudeDelta) * Math.cos(azimuth));
                ey = (float) Math.sin(altitudeDelta);
                ez = (float) (Math.cos(altitudeDelta) * Math.sin(azimuth));

                vertices[6 * j + 0] = radius * ex;
                vertices[6 * j + 1] = radius * ey;
                vertices[6 * j + 2] = radius * ez;

                texCoords[4 * j + 0] = 1.0f - (j / (float) divide);
                texCoords[4 * j + 1] = 2 * (i + 1) / (float) divide;

                // second point
                ex = (float) (Math.cos(altitude) * Math.cos(azimuth));
                ey = (float) Math.sin(altitude);
                ez = (float) (Math.cos(altitude) * Math.sin(azimuth));

                vertices[6 * j + 3] = radius * ex;
                vertices[6 * j + 4] = radius * ey;
                vertices[6 * j + 5] = radius * ez;

                texCoords[4 * j + 2] = 1.0f - (j / (float) divide);
                texCoords[4 * j + 3] = 2 * i / (float) divide;
            }

            mVertices.add(makeFloatBufferFromArray(vertices));
            mTextureCoords.add(makeFloatBufferFromArray(texCoords));
        }

        return;
    }


    private FloatBuffer makeFloatBufferFromArray(float[] array) {

        FloatBuffer fb = ByteBuffer.allocateDirect(array.length * Float.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(array);
        fb.position(0);

        return fb;
    }


}
