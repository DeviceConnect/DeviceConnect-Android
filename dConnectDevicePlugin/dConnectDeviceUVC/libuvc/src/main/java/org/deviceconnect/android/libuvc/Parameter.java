/*
 Parameter.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import java.util.HashMap;
import java.util.Set;

/**
 * UVC カメラのプレビューを再生するときに設定するパラメータ.
 *
 * @author NTT DOCOMO, INC.
 */
public class Parameter {
    public static final float QUALITY_HIGH = 0.175f;
    public static final float QUALITY_MIDDLE = 0.125f;
    public static final float QUALITY_LOW = 0.100f;

    private float mBPP = QUALITY_MIDDLE;
    private boolean mUseH264 = false;

    // 以下の変数は、NDK より値が設定されます。

    /**
     * Video Stream Format Descriptor の bFormatIndex を保持します.
     */
    private int mFormatIndex;

    /**
     * Video Stream Frame Descriptor の bDescriptorSubType を保持します.
     */
    private int mFrameType;

    /**
     * Video Stream Frame Descriptor の bFrameIndex を保持します.
     */
    private int mFrameIndex;

    /**
     * Video Stream Frame Descriptor の mWidth を保持します.
     */
    private int mWidth;

    /**
     * Video Stream Frame Descriptor の mHeight を保持します.
     */
    private int mHeight;

    /**
     * FPS を保持します.
     */
    private int mFps;

    /**
     * Video Stream Frame Descriptor の bFrameIntervalType から FPS を計算して保持します.
     */
    private int[] mFpsList;

    /**
     * 必要に応じて NDK からのパラメータを格納するマップ.
     *
     * 現在は値を Long で定義しているが、他の型が必要になったら再度検討すること。
     */
    private final HashMap<String, Long> mExtras = new HashMap<>();

    Parameter() {
    }

    Parameter(Parameter p) {
        mFormatIndex = p.mFormatIndex;
        mFrameType = p.mFrameType;
        mFrameIndex = p.mFrameIndex;
        mWidth = p.mWidth;
        mHeight = p.mHeight;
        mFps = p.mFps;
        mFpsList = p.mFpsList;
        mBPP = p.mBPP;
        mUseH264 = p.mUseH264;
        for (String key : p.mExtras.keySet()) {
            mExtras.put(key, p.mExtras.get(key));
        }
    }

    /**
     * NDK 側で使用するフォーマットのインデックスを取得します.
     *
     * @return フォーマットのインデックス
     */
    int getFormatIndex() {
        return mFormatIndex;
    }

    /**
     * NDK 側で使用するフレームのインデックスを取得します.
     *
     * @return フレームのインデックス
     */
    int getFrameIndex() {
        return mFrameIndex;
    }

    /**
     * フレームのフォーマットタイプを取得します.
     *
     * @return フレームのフォーマットタイプ
     */
    public FrameType getFrameType() {
        return FrameType.valueOf(mFrameType);
    }

    /**
     * フレームの横幅を取得します.
     *
     * @return 横幅
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * フレームの縦幅を取得します.
     *
     * @return 縦幅
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * FPSを取得します.
     *
     * @return FPS
     */
    public int getFps() {
        return mFps;
    }

    /**
     * FPSを設定します.
     *
     * @param fps FPS
     * @throws IllegalArgumentException サポートしていないFPSが指定された場合に発生
     */
    public void setFPS(int fps) {
        if (mFpsList != null && !hasSupportFps(fps)) {
            throw new IllegalArgumentException("not support fps.");
        }
        mFps = fps;
    }

    /**
     * フレームに指定できるFPSのリストを取得します.
     *
     * @return fps FPSの一覧
     */
    public int[] getSupportedFps() {
        return mFpsList;
    }

    /**
     * キーを指定してフレームが持っているオプションを取得します.
     * <p>
     * 指定されたキーに対応するオプションがない場合には null を返却します。
     * </p>
     * @param key キー
     * @return オプションの値
     */
    public Long getExtra(final String key) {
        return mExtras.get(key);
    }

    /**
     * キーの一覧を取得します.
     *
     * @return キーの一覧
     */
    public Set<String> getExtraKeys() {
        return mExtras.keySet();
    }

    /**
     * オプションに値を追加します.
     * <p>
     * この関数はNDKから呼び出されるので変更する場合には注意してください。
     * </p>
     * @param key キー
     * @param value 値
     */
    void putExtra(final String key, final long value) {
        mExtras.put(key, value);
    }

    /**
     * Bits Per Pixel の係数を設定します.
     * <p>
     * 以下の定数を定義しています。
     * <ul>
     * <li>{@code QUALITY_HIGH}</li>
     * <li>{@code QUALITY_MIDDLE}</li>
     * <li>{@code QUALITY_LOW}</li>
     * </ul>
     * </p>
     * <p>
     * 0.01〜0.25の間で設定することができます。<br>
     * 係数が大きいほど、品質が上がりますが、容量が増えます。
     * </p>
     * @param bpp Bits Per Pixel
     */
    public void setBPP(float bpp) {
        if (bpp < 0.01f || bpp > 0.25f) {
            throw new IllegalArgumentException();
        }
        mBPP = bpp;
    }

    /**
     * BitRateを取得します.
     *
     * @return bitrate
     */
    public int getBitRate() {
        return (int) (mBPP * getFps() * getWidth() * getHeight());
    }

    /**
     * H264 Extension Unit を使用するか設定します.
     *
     * H264 Extension Unit を持っていない場合は、true を設定することができません。
     *
     * @param use 使用する場合は true、それ以外は false
     */
    public void setUseH264(boolean use) {
        if (!mExtras.containsKey("h264")) {
            return;
        }
        mUseH264 = use;
    }

    /**
     * H264 Extension Unit を使用するか確認します.
     *
     * @return 使用する場合は true、それ以外は false
     */
    public boolean isUseH264() {
        return mUseH264;
    }

    /**
     * H264 の Extension Unit が存在するか確認します.
     *
     * @return Extension Unit を持っている場合は true、それ以外は false
     */
    public boolean hasExtH264() {
        return mExtras.containsKey("h264");
    }

    private boolean hasSupportFps(int fps) {
        for (int f : mFpsList) {
            if (f == fps) {
                return true;
            }
        }
        return false;
    }

    private String toFpsList() {
        StringBuilder builder = new StringBuilder();
        if (mFpsList != null) {
            for (int fps : mFpsList) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(fps);
            }
        }
        return "[ " + builder.toString() + " ]";
    }

    @Override
    public String toString() {
        return "Param: [ \n" +
                "  formatIndex: " + mFormatIndex + ",\n" +
                "  frameType: " + mFrameType + ",\n" +
                "  frameIndex: " + mFrameIndex + ",\n" +
                "  width: " + mWidth + ",\n" +
                "  height: " + mHeight + ",\n" +
                "  fps: " + mFps + ",\n" +
                "  useH264: " + mUseH264 + ",\n" +
                "  supportedFps: " + toFpsList() + ",\n" +
                "  extra: " + mExtras + "\n" +
                "]\n";
    }
}
