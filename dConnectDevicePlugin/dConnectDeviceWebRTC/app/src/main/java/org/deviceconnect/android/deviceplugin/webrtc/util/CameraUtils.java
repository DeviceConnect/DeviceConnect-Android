/*
 CameraUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.hardware.Camera;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to operate the camera.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public final class CameraUtils {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "CAMERA";

    private CameraUtils() {
    }

    /**
     * Gets a list of CameraFormat by id.
     * <p>
     *     id follows:
     *     <ul>
     *         <li>{@link android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK}</li>
     *         <li>{@link android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT}</li>
     *     </ul>
     * </p>
     * @param id id
     * @retur　list of CameraFormat
     */
    public static List<CameraFormat> getSupportedFormats(final int id) {
        final ArrayList<CameraFormat> formatList = new ArrayList<>();

        Camera camera;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {
            return formatList;
        }

        try {
            Camera.Parameters parameters = camera.getParameters();
            List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
            int[] range = { 0, 0 };
            if (listFpsRange != null) {
                range = listFpsRange.get(listFpsRange.size() - 1);
            }
            List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : supportedSizes) {
                formatList.add(new CameraFormat(size.width, size.height, id,
                        range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]/1000,
                        range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]/1000));
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e);
            }
        } finally {
            camera.release();
        }
        return formatList;
    }

    /**
     * Checks whether this device support a front camera.
     * @return true if this device support a front camera, false otherwise
     */
    public static boolean hasFrontFacingDevice() {
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            try {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return true;
                }
            } catch (Exception e) {
                // do nothing.
            }
        }
        return false;
    }

    /**
     * Gets the default CameraFormat by id.
     * @return default CameraFormat
     */
    private static CameraFormat getDefaultFormat(final int id, final int width, final int height) {
        CameraFormat selectCameraFormat = null;
        int dist = Integer.MAX_VALUE;

        List<CameraFormat> formats = getSupportedFormats(id);
        if (formats != null) {
            for (CameraFormat format : formats) {
                int dw = format.getWidth() - width;
                int dh = format.getHeight() - height;
                int d = dw * dw + dh * dh;
                if (selectCameraFormat == null || d < dist) {
                    selectCameraFormat = format;
                    dist = d;
                }
            }
        }
        return selectCameraFormat;
    }

    /**
     * Gets the default CameraFormat.
     * @return default CameraFormat
     */
    public static CameraFormat getDefaultFormat() {
        return getDefaultFormat(320, 240);
    }

    /**
     * Gets the default CameraFormat.
     * @param width width of camera
     * @param height height of camera
     * @return default CameraFormat
     */
    public static CameraFormat getDefaultFormat(int width, int height) {
        if (hasFrontFacingDevice()) {
            return getDefaultFormat(Camera.CameraInfo.CAMERA_FACING_FRONT, width, height);
        } else {
            return getDefaultFormat(Camera.CameraInfo.CAMERA_FACING_BACK, width, height);
        }
    }

    /**
     * Converts the CameraFormat to String.
     * @param format CameraFormat
     * @return String
     */
    public static String formatToText(final CameraFormat format) {
        StringBuilder builder = new StringBuilder();
        builder.append("size:[" + format.getWidth() + "x" + format.getHeight() + "]");
        builder.append(",");
        builder.append("fps:[" + format.getMinFrameRate() + "-" + format.getMaxFrameRate() + "]");
        builder.append(",");
        if (format.getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            builder.append("facing: back");
        } else if (format.getFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            builder.append("facing: front");
        }
        return builder.toString();
    }

    /**
     * Converts the String to CameraFormat.
     * @param text text
     * @return CameraFormat
     */
    public static CameraFormat textToFormat(final String text) {
        if (text == null || text.length() == 0) {
            return null;
        }

        String[] txt = text.split(",");
        if (txt.length != 3) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "text format is invalid.");
            }
            return null;
        }

        try {
            String sizeStr = txt[0].substring("size:[".length(), txt[0].length() - 1);
            String fpsStr = txt[1].substring("fps:[".length(), txt[1].length() - 1);
            String facingStr = txt[2].substring("facing: ".length());

            String[] size = sizeStr.split("x");
            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);

            String[] fps = fpsStr.split("-");
            int minFps = Integer.parseInt(fps[0]);
            int maxFps = Integer.parseInt(fps[1]);

            int facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            if (facingStr.equals("front")) {
                facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else if (facingStr.equals("back")) {
                facing = Camera.CameraInfo.CAMERA_FACING_BACK;
            }

            return new CameraFormat(width, height, facing, minFps, maxFps);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
            return null;
        }
    }

    /**
     * CameraFormat.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class CameraFormat {
        /**
         * Width of camera.
         */
        private int mWidth;

        /**
         * Height of camera.
         */
        private int mHeight;

        /**
         * The number of the maximum fps.
         */
        private int mMaxFrameRate;

        /**
         * The number of the minimum fps.
         */
        private int mMinFrameRate;

        /**
         * Facing of camera.
         */
        private int mFacing;

        /**
         * Constructor.
         * @param width width
         * @param height height
         * @param facing facing
         * @param minFrameRate minimum fps
         * @param maxFrameRate maximum fps
         */
        public CameraFormat(final int width, final int height, final int facing,
                            final int minFrameRate, final int maxFrameRate) {
            mWidth = width;
            mHeight = height;
            mFacing = facing;
            mMinFrameRate = minFrameRate;
            mMaxFrameRate = maxFrameRate;
        }

        /**
         * Gets the width of camera.
         * @return width
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * Gets the height of camera.
         * @return height
         */
        public int getHeight() {
            return mHeight;
        }

        /**
         * Gets the number of the maximum fps.
         * @return fps
         */
        public int getMaxFrameRate() {
            return mMaxFrameRate;
        }

        /**
         * Gets the number of the minimum fps.
         * @return fps
         */
        public int getMinFrameRate() {
            return mMinFrameRate;
        }

        /**
         * Gets the facing of camera.
         * @return facing
         */
        public int getFacing() {
            return mFacing;
        }

        /**
         * Sets the width of camera.
         * @param width width
         */
        public void setWidth(final int width) {
            mWidth = width;
        }

        /**
         * Sets the height of camera.
         * @param height height
         */
        public void setHeight(final int height) {
            mHeight = height;
        }

        /**
         * Sets the number of the maximum fps.
         * @param maxFrameRate fps
         */
        public void setMaxFrameRate(int maxFrameRate) {
            mMaxFrameRate = maxFrameRate;
        }

        /**
         * Sets the number of the minimum fps.
         * @param minFrameRate fps
         */
        public void setMinFrameRate(int minFrameRate) {
            mMinFrameRate = minFrameRate;
        }

        /**
         * Sets the facing of camera.
         *
         * facing follows:
         * <ul>
         *     <li>{@link android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK}</li>
         *     <li>{@link android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT}</li>
         * </ul>
         *
         * @param facing facing
         */
        public void setFacing(int facing) {
            mFacing = facing;
        }

        @Override
        public String toString() {
            return formatToText(this);
        }
    }
}
