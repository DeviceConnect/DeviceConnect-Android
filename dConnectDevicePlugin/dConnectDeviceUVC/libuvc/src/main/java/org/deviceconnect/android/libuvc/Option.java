/*
 Option.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl;
import org.deviceconnect.android.libuvc.UVCCameraNative.Control;
import org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl;
import org.deviceconnect.android.libuvc.UVCCameraNative.RequestType;

import java.util.HashMap;
import java.util.Map;

import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_AE_MODE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_AE_PRIORITY_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_EXPOSURE_TIME_ABSOLUTE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_FOCUS_ABSOLUTE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_FOCUS_AUTO_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_IRIS_ABSOLUTE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_ROLL_ABSOLUTE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_SCANNING_MODE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.CameraTerminalControl.CT_ZOOM_ABSOLUTE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_BRIGHTNESS_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_CONTRAST_AUTO_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_CONTRAST_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_GAIN_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_GAMMA_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_HUE_AUTO_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_HUE_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_SATURATION_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_SHARPNESS_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_WHITE_BALANCE_COMPONENT_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL;
import static org.deviceconnect.android.libuvc.UVCCameraNative.ProcessingUnitControl.PU_WHITE_BALANCE_TEMPERATURE_CONTROL;

/**
 * UVCCameraの設定を行うクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class Option {
    /**
     * スキャニングモード.
     */
    public enum ScanningMode {
        /**
         * インターレース.
         */
        Interlaced,

        /**
         * プログレッシブ.
         */
        Progressive
    }

    /**
     * 露出モード.
     */
    public enum ExposureMode {
        /**
         * マニュアル.
         */
        Manual(1),

        /**
         * オート.
         */
        Auto(1 << 1),

        /**
         * シャッター.
         */
        Shutter(1 << 2),

        /**
         * レンズの口径.
         */
        Aperture (1 << 3);

        private int mValue;

        ExposureMode(final int value) {
            mValue = value;
        }

        int getValue() {
            return mValue;
        }

        private static ExposureMode valueOf(final int value) {
            for (ExposureMode e : values()) {
                if (e.mValue == value) {
                    return e;
                }
            }
            return Manual;
        }
    }

    /**
     * カメラターミナルコントロールの情報を格納するマップ.
     */
    private final Map<CameraTerminalControl, Data> mCameraTerminalControls = new HashMap<>();

    /**
     * プロセシングユニットコントロールの情報を格納するマップ.
     */
    private final Map<ProcessingUnitControl, Data> mProcessingUnitControls = new HashMap<>();

    /**
     * このオプションの設定元のカメラ.
     */
    private final UVCCamera mUVCCamera;

    /**
     * コンストラクタ.
     * @param camera カメラ
     */
    Option(final UVCCamera camera) {
        mUVCCamera = camera;
    }

    //// CT ////

    /**
     * スキャニングモードがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedScanningMode() {
        return isSupported(CT_SCANNING_MODE_CONTROL);
    }

    /**
     * スキャニングモードを設定します.
     *
     * @param mode スキャニングモード
     */
    public void setScanningMode(final ScanningMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode is null.");
        }
        setByteControl(CT_SCANNING_MODE_CONTROL, mode == ScanningMode.Progressive ? 1 : 0);
    }

    /**
     * 設定されているスキャニングモードを取得します.
     *
     * @return スキャニングモード
     */
    public ScanningMode getScanningMode() {
        int sm = getCurSettingData(CT_SCANNING_MODE_CONTROL);
        if (sm == 0) {
            return ScanningMode.Interlaced;
        } else {
            return ScanningMode.Progressive;
        }
    }

    /**
     * 露出モードがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedExposureMode() {
        return isSupported(CT_AE_MODE_CONTROL);
    }

    /**
     * 露出モードの設定を行います.
     *
     * @param mode 露出モード
     */
    public void setExposureMode(final ExposureMode mode) {
        setByteControl(CT_AE_MODE_CONTROL, mode.getValue());
    }

    /**
     * 露出モードを取得します.
     *
     * @return 露出モード
     */
    public ExposureMode getExposureMode() {
        return ExposureMode.valueOf(getCurSettingData(CT_AE_MODE_CONTROL));
    }

    /**
     * 露出プライオリティがサポートされているか確認します.
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedExposurePriority() {
        return isSupported(CT_AE_PRIORITY_CONTROL);
    }

    /**
     * 露出プライオリティの有効・無効を設定します.
     *
     * @param exposurePriority 有効にする場合にはtrue、それ以外はfalse
     */
    public void setExposurePriority(final boolean exposurePriority) {
        setByteControl(CT_AE_PRIORITY_CONTROL, exposurePriority ? 1 : 0);
    }

    /**
     * 露出プライオリティを取得します.
     *
     * @return 有効の場合はtrue、それ以外はfalse
     */
    public boolean isExposurePriority() {
        return isSettingData(CT_AE_PRIORITY_CONTROL);
    }

    /**
     * 露出時間がサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedExposureTime() {
        return isSupported(CT_EXPOSURE_TIME_ABSOLUTE_CONTROL);
    }

    /**
     * 露出時間を設定します.
     *
     * @param exposureTime 露出時間
     */
    public void setExposureTime(final int exposureTime) {
        if (exposureTime < getMinExposureTime() || getMaxExposureTime() < exposureTime) {
            throw new IllegalArgumentException("exposureTime is invalid.");
        }
        setIntControl(CT_EXPOSURE_TIME_ABSOLUTE_CONTROL, exposureTime);
    }

    /**
     * 露出時間を取得します.
     *
     * @return 露出時間
     */
    public int getExposureTime() {
        return getCurSettingData(CT_EXPOSURE_TIME_ABSOLUTE_CONTROL);
    }

    /**
     * 露出時間の最大値を取得します.
     *
     * @return 露出時間の最大値
     */
    public int getMaxExposureTime() {
        return getMaxSettingData(CT_EXPOSURE_TIME_ABSOLUTE_CONTROL);
    }

    /**
     * 露出時間の最小値を取得します.
     *
     * @return 露出時間の最小値
     */
    public int getMinExposureTime() {
        return getMinSettingData(CT_EXPOSURE_TIME_ABSOLUTE_CONTROL);
    }

    /**
     * Zoom がサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedZoom() {
        return isSupported(CT_ZOOM_ABSOLUTE_CONTROL);
    }

    /**
     * Zoom を設定します.
     *
     * @param zoom zoom
     */
    public void setZoom(final int zoom) {
        if (zoom < getMinZoom() || getMaxZoom() < zoom) {
            throw new IllegalArgumentException("zoom is invalid.");
        }
        setShortControl(CT_ZOOM_ABSOLUTE_CONTROL, zoom);
    }

    /**
     * 設定されている Zoom の値を取得します.
     *
     * @return Zoomの値
     */
    public int getZoom() {
        return getCurSettingData(CT_ZOOM_ABSOLUTE_CONTROL);
    }

    /**
     * Zoom の最大値を取得します.
     *
     * @return Zoomの最大値
     */
    public int getMaxZoom() {
        return getMaxSettingData(CT_ZOOM_ABSOLUTE_CONTROL);
    }

    /**
     * Zoom の最小値を取得します.
     *
     * @return Zoomの最小値
     */
    public int getMinZoom() {
        return getMinSettingData(CT_ZOOM_ABSOLUTE_CONTROL);
    }

    /**
     * オートフォーカスがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedAutoFocus() {
        return isSupported(CT_FOCUS_AUTO_CONTROL);
    }

    /**
     * オートフォーカスの有効・無効を取得します.
     *
     * @return オートフォーカスの有効・無効
     */
    public boolean isAutoFocus() {
        return isSettingData(CT_FOCUS_AUTO_CONTROL);
    }

    /**
     * オートフォーカスを設定します.
     *
     * @param autoFocus オートフォーカス
     */
    public void setAutoFocus(final boolean autoFocus) {
        setByteControl(CT_FOCUS_AUTO_CONTROL, autoFocus ? 1 : 0);
    }

    /**
     * フォーカスがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedFocus() {
        return isSupported(CT_FOCUS_ABSOLUTE_CONTROL);
    }

    /**
     * フォーカスを設定します.
     *
     * @param focus フォーカス
     */
    public void setFocus(final int focus) {
        if (focus < getMinFocus() || getMaxFocus() < focus) {
            throw new IllegalArgumentException("focus is invalid.");
        }
        setShortControl(CT_FOCUS_ABSOLUTE_CONTROL, focus);
    }

    /**
     * フォーカスを取得します.
     *
     * @return フォーカス
     */
    public int getFocus() {
        return getCurSettingData(CT_FOCUS_ABSOLUTE_CONTROL);
    }

    /**
     * フォーカスの最大値を取得します.
     *
     * @return フォーカスの最大値
     */
    public int getMaxFocus() {
        return getMaxSettingData(CT_FOCUS_ABSOLUTE_CONTROL);
    }

    /**
     * フォーカスの最小値を取得します.
     *
     * @return フォーカスの最小値
     */
    public int getMinFocus() {
        return getMinSettingData(CT_FOCUS_ABSOLUTE_CONTROL);
    }

    /**
     * アイリス (F値) がサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedIris() {
        return isSupported(CT_IRIS_ABSOLUTE_CONTROL);
    }

    /**
     * アイリス (F値) を設定します.
     * <p>
     * 明るさ調整.
     * </p>
     * @param iris アイリス値
     */
    public void setIris(final int iris) {
        if (iris < getMinIris() || getMaxIris() < iris) {
            throw new IllegalArgumentException("iris is invalid.");
        }
        setShortControl(CT_IRIS_ABSOLUTE_CONTROL, iris);
    }

    /**
     * アイリス (F値) を取得します.
     *
     * @return アイリス (F値)
     */
    public int getIris() {
        return getCurSettingData(CT_IRIS_ABSOLUTE_CONTROL);
    }

    /**
     * アイリス (F値) の最大値を取得します.
     *
     * @return アイリス (F値)の最大値
     */
    public int getMaxIris() {
        return getMaxSettingData(CT_IRIS_ABSOLUTE_CONTROL);
    }

    /**
     * アイリス (F値) の最小値を取得します.
     *
     * @return アイリス (F値)の最小値
     */
    public int getMinIris() {
        return getMinSettingData(CT_IRIS_ABSOLUTE_CONTROL);
    }

    /**
     * ロールがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedRoll() {
        return isSupported(CT_ROLL_ABSOLUTE_CONTROL);
    }

    /**
     * ロールを設定します.
     *
     * @param roll ロール
     */
    public void setRoll(final int roll) {
        if (roll < getMinRoll() || getMaxRoll() < roll) {
            throw new IllegalArgumentException("roll is invalid.");
        }
        setShortControl(CT_ROLL_ABSOLUTE_CONTROL, roll);
    }

    /**
     * ロールを取得します.
     *
     * @return 設定されているロール
     */
    public int getRoll() {
        return getCurSettingData(CT_ROLL_ABSOLUTE_CONTROL);
    }

    /**
     * ロールの最大値を取得します.
     *
     * @return ロールの最大値
     */
    public int getMaxRoll() {
        return getMaxSettingData(CT_ROLL_ABSOLUTE_CONTROL);
    }

    /**
     * ロールの最小値を取得します.
     *
     * @return ロールの最小値
     */
    public int getMinRoll() {
        return getMinSettingData(CT_ROLL_ABSOLUTE_CONTROL);
    }

    //// PU ////

    /**
     * ブライトネスがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedBrightness() {
        return isSupported(PU_BRIGHTNESS_CONTROL);
    }

    /**
     * ブライトネスを設定します.
     *
     * @param brightness ブライトネス
     */
    public void setBrightness(final int brightness) {
        if (brightness < getMinBrightness() || getMaxBrightness() < brightness) {
            throw new IllegalArgumentException("brightness is invalid.");
        }
        setShortControl(PU_BRIGHTNESS_CONTROL, brightness);
    }

    /**
     * ブライトネスを取得します.
     *
     * @return ブライトネス
     */
    public int getBrightness() {
        return getCurSettingData(PU_BRIGHTNESS_CONTROL);
    }

    /**
     * ブライトネスの最大値を取得します.
     *
     * @return ブライトネスの最大値
     */
    public int getMaxBrightness() {
        return getMaxSettingData(PU_BRIGHTNESS_CONTROL);
    }

    /**
     * ブライトネスの最小値を取得します.
     *
     * @return ブライトネスの最小値
     */
    public int getMinBrightness() {
        return getMinSettingData(PU_BRIGHTNESS_CONTROL);
    }

    /**
     * コントラストがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedContrast() {
        return isSupported(PU_BRIGHTNESS_CONTROL);
    }

    /**
     * コントラストを設定します.
     *
     * @param contrast コントラスト
     */
    public void setContrast(final int contrast) {
        if (contrast < getMinContrast() || getMaxContrast() < contrast) {
            throw new IllegalArgumentException("contrast is invalid");
        }
        setShortControl(PU_CONTRAST_CONTROL, contrast);
    }

    /**
     * コントラストを取得します.
     *
     * @return コントラスト
     */
    public int getContrast() {
        return getCurSettingData(PU_CONTRAST_CONTROL);
    }

    /**
     * コントラストの最大値を取得します.
     *
     * @return コントラストの最大値
     */
    public int getMaxContrast() {
        return getMaxSettingData(PU_CONTRAST_CONTROL);
    }

    /**
     * コントラストの最小値を取得します.
     *
     * @return コントラストの最小値
     */
    public int getMinContrast() {
        return getMinSettingData(PU_CONTRAST_CONTROL);
    }

    /**
     * ゲインがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedGain() {
        return isSupported(PU_GAIN_CONTROL);
    }

    /**
     * ゲインを設定します.
     *
     * @param gain ゲイン
     */
    public void setGain(final int gain) {
        if (gain < getMinGain() || getMaxGain() < gain) {
            throw new IllegalArgumentException("gain is invalid");
        }
        setShortControl(PU_GAIN_CONTROL, gain);
    }

    /**
     * ゲインを取得します.
     *
     * @return ゲイン
     */
    public int getGain() {
        return getCurSettingData(PU_GAIN_CONTROL);
    }

    /**
     * ゲインの最大値を取得します.
     *
     * @return ゲインの最大値
     */
    public int getMaxGain() {
        return getCurSettingData(PU_GAIN_CONTROL);
    }

    /**
     * ゲインの最小値を取得します.
     *
     * @return ゲインの最小値
     */
    public int getMinGain() {
        return getCurSettingData(PU_GAIN_CONTROL);
    }

    /**
     * Hue (色相)がサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedHue() {
        return isSupported(PU_HUE_CONTROL);
    }

    /**
     * Hue を設定します.
     *
     * @param hue Hueの値
     */
    public void setHue(final int hue) {
        if (hue < getMinHue() || getMaxHue() < hue) {
            throw new IllegalArgumentException("hue is invalid.");
        }
        setShortControl(PU_HUE_CONTROL, hue);
    }

    /**
     * Hue を取得します.
     *
     * @return Hue
     */
    public int getHue() {
        return getCurSettingData(PU_HUE_CONTROL);
    }

    /**
     * Hue の最大値を取得します.
     *
     * @return Hueの最大値
     */
    public int getMaxHue() {
        return getMaxSettingData(PU_HUE_CONTROL);
    }

    /**
     * Hue の最小値を取得します.
     *
     * @return Hueの最小値
     */
    public int getMinHue() {
        return getMinSettingData(PU_HUE_CONTROL);
    }

    /**
     * Saturation がサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedSaturation() {
        return isSupported(PU_SATURATION_CONTROL);
    }

    /**
     * Saturation (彩度)を設定します.
     *
     * @param saturation Saturation
     */
    public void setSaturation(final int saturation) {
        if (saturation < getMinSaturation() || getMaxSaturation() < saturation) {
            throw new IllegalArgumentException("saturation is invalid.");
        }
        setShortControl(PU_SATURATION_CONTROL, saturation);
    }

    /**
     * Saturation を取得します.
     *
     * @return Saturation
     */
    public int getSaturation() {
        return getCurSettingData(PU_SATURATION_CONTROL);
    }

    /**
     * Saturation の最大値を取得します.
     *
     * @return Saturationの最大値
     */
    public int getMaxSaturation() {
        return getMaxSettingData(PU_SATURATION_CONTROL);
    }

    /**
     * Saturation の最小値を取得します.
     *
     * @return Saturationの最小値
     */
    public int getMinSaturation() {
        return getMinSettingData(PU_SATURATION_CONTROL);
    }

    /**
     * シャープネスがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedSharpness() {
        return isSupported(PU_SHARPNESS_CONTROL);
    }

    /**
     * シャープネスを設定します.
     *
     * @param sharpness シャープネス
     */
    public void setSharpness(final int sharpness) {
        if (sharpness < getMinSharpness() || getMaxSharpness() < sharpness) {
            throw new IllegalArgumentException("sharpness is invalid.");
        }
        setShortControl(PU_SHARPNESS_CONTROL, sharpness);
    }

    /**
     * シャープネスを取得します.
     *
     * @return シャープネス
     */
    public int getSharpness() {
        return getCurSettingData(PU_SHARPNESS_CONTROL);
    }

    /**
     * シャープネスの最大値を取得します.
     *
     * @return シャープネスの最大値
     */
    public int getMaxSharpness() {
        return getMaxSettingData(PU_SHARPNESS_CONTROL);
    }

    /**
     * シャープネスの最小値を取得します.
     *
     * @return シャープネスの最小値
     */
    public int getMinSharpness() {
        return getMinSettingData(PU_SHARPNESS_CONTROL);
    }

    /**
     * ガンマがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedGamma() {
        return isSupported(PU_GAMMA_CONTROL);
    }

    /**
     * ガンマを設定します.
     *
     * @param gamma ガンマ
     */
    public void setGamma(final int gamma) {
        if (gamma < getMinGamma() || getMaxGamma() < gamma) {
            throw new IllegalArgumentException("gamma is invalid");
        }
        setShortControl(PU_GAMMA_CONTROL, gamma);
    }

    /**
     * ガンマを取得します.
     *
     * @return ガンマ
     */
    public int getGamma() {
        return getCurSettingData(PU_GAMMA_CONTROL);
    }

    /**
     * ガンマの最大値を取得します.
     *
     * @return ガンマの最大値
     */
    public int getMaxGamma() {
        return getMaxSettingData(PU_GAMMA_CONTROL);
    }

    /**
     * ガンマの最小値を取得します.
     *
     * @return ガンマの最小値
     */
    public int getMinGamma() {
        return getMinSettingData(PU_GAMMA_CONTROL);
    }

    /**
     * ホワイトバランステンパラチャーがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedWhiteBalanceTemperature() {
        return isSupported(PU_WHITE_BALANCE_TEMPERATURE_CONTROL);
    }

    /**
     * ホワイトバランステンパラチャーを設定します.
     *
     * @param whiteBalanceTemperature ホワイトバランステンパラチャー
     */
    public void setWhiteBalanceTemperature(final int whiteBalanceTemperature) {
        if (whiteBalanceTemperature < getMinWhiteBalanceTemperature() ||
                getMaxWhiteBalanceTemperature() < whiteBalanceTemperature) {
            throw new IllegalArgumentException("whiteBalanceTemperature is invalid.");
        }
        setShortControl(PU_WHITE_BALANCE_TEMPERATURE_CONTROL, whiteBalanceTemperature);
    }

    /**
     * ホワイトバランステンパラチャーを取得します.
     *
     * @return ホワイトバランステンパラチャー
     */
    public int getWhiteBalanceTemperature() {
        return getCurSettingData(PU_WHITE_BALANCE_TEMPERATURE_CONTROL);
    }

    /**
     * ホワイトバランステンパラチャーの最大値を取得します.
     *
     * @return ホワイトバランステンパラチャーの最大値
     */
    public int getMaxWhiteBalanceTemperature() {
        return getMaxSettingData(PU_WHITE_BALANCE_TEMPERATURE_CONTROL);
    }

    /**
     * ホワイトバランステンパラチャーの最小値を取得します.
     *
     * @return ホワイトバランステンパラチャーの最小値
     */
    public int getMinWhiteBalanceTemperature() {
        return getMinSettingData(PU_WHITE_BALANCE_TEMPERATURE_CONTROL);
    }

    /**
     * オートコントラストがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedAutoContrast() {
        return isSupported(PU_CONTRAST_AUTO_CONTROL);
    }

    /**
     * オートコントラストの有効・無効を取得します.
     *
     * @return trueの場合は有効、falseの場合は無効
     */
    public boolean isAutoContrast() {
        return isSettingData(PU_CONTRAST_AUTO_CONTROL);
    }

    /**
     * オートコントラストを設定します.
     *
     * @param autoContrast 有効にする場合はtrue、無効にする場合にはfalse
     */
    public void setAutoContrast(final boolean autoContrast) {
        setByteControl(PU_CONTRAST_AUTO_CONTROL, autoContrast ? 1 : 0);
    }

    /**
     * オート Hue がサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedAutoHue() {
        return isSupported(PU_HUE_AUTO_CONTROL);
    }

    /**
     * オート Hue の有効・無効を取得します.
     *
     * @return trueの場合は有効、falseの場合は無効
     */
    public boolean isAutoHue() {
        return isSettingData(PU_HUE_AUTO_CONTROL);
    }

    /**
     * オート Hue を設定します.
     *
     * @param autoHue 有効にする場合はtrue、無効にする場合にはfalse
     */
    public void setAutoHue(final boolean autoHue) {
        setByteControl(PU_HUE_AUTO_CONTROL, autoHue ? 1 : 0);
    }

    /**
     * オートホワイトバランステンパラチャーがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedAutoWhiteBalanceTemperature() {
        return isSupported(PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL);
    }

    /**
     * オートホワイトバランステンパラチャーの有効・無効を取得します.
     *
     * @return trueの場合は有効、falseの場合は無効
     */
    public boolean isAutoWhiteBalanceTemperature() {
        return isSettingData(PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL);
    }

    /**
     * オートホワイトバランステンパラチャーを設定します.
     *
     * @param autoWhiteBalanceTemperature 有効にする場合はtrue、無効にする場合にはfalse
     */
    public void setAutoWhiteBalanceTemperature(boolean autoWhiteBalanceTemperature) {
        setByteControl(PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL, autoWhiteBalanceTemperature ? 1 : 0);
    }

    /**
     * オートホワイトバランスコンポーネントがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedAutoWhiteBalanceComponent() {
        return isSupported(PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL);
    }

    /**
     * オートホワイトバランスコンポーネントの有効・無効を取得します.
     *
     * @return trueの場合は有効、falseの場合は無効
     */
    public boolean isAutoWhiteBalanceComponent() {
        return isSettingData(PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL);
    }

    /**
     * オートホワイトバランスコンポーネントを設定します.
     *
     * @param autoWhiteBalanceComponent 有効にする場合はtrue、無効にする場合にはfalse
     */
    public void setAutoWhiteBalanceComponent(final boolean autoWhiteBalanceComponent) {
        setByteControl(PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL, autoWhiteBalanceComponent ? 1 : 0);
    }

    /**
     * ホワイトバランスコンポーネントがサポートされているか確認します.
     *
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    public boolean isSupportedWhiteBalanceComponent() {
        return isSupported(PU_WHITE_BALANCE_COMPONENT_CONTROL);
    }

    /**
     * ホワイトバランスコンポーネントを設定します.
     *
     * @param component ホワイトバランスコンポーネント
     */
    public void setWhiteBalanceComponent(final WhiteBalanceComponent component) {
        Data data = mProcessingUnitControls.get(PU_WHITE_BALANCE_COMPONENT_CONTROL);
        if (data != null && data.isSupported()) {
            byte[] value = new byte[4];
            value[0] = (byte) (component.mBlue & 0xFF);
            value[1] = (byte) ((component.mBlue >> 8) & 0xFF);
            value[2] = (byte) (component.mRed & 0xFF);
            value[3] = (byte) ((component.mRed >> 8) & 0xFF);
            int result = setArrayControl(PU_WHITE_BALANCE_COMPONENT_CONTROL, value);
            if (result == UVCCamera.UVC_SUCCESS) {
                ((ArrayData) data).mCurrentValue[0] = component.mBlue;
                ((ArrayData) data).mCurrentValue[1] = component.mRed;
            }

            // TODO 値の設定に失敗した場合の処理

            return;
        }
        throw new UnsupportedOperationException(PU_WHITE_BALANCE_COMPONENT_CONTROL + " is not supported.");
    }

    /**
     * ホワイトバランスコンポーネントを取得します.
     *
     * @return ホワイトバランスコンポーネント
     */
    public WhiteBalanceComponent getWhiteBalanceComponent() {
        Data data = mProcessingUnitControls.get(PU_WHITE_BALANCE_COMPONENT_CONTROL);
        if (data != null && data.isSupported()) {
            int[] value = ((ArrayData) data).mCurrentValue;
            return new WhiteBalanceComponent(value[0], value[1]);
        }
        throw new UnsupportedOperationException(PU_WHITE_BALANCE_COMPONENT_CONTROL + " is not supported.");
    }

    /**
     * ホワイトバランスコンポーネントの最大値を取得します.
     *
     * @return ホワイトバランスコンポーネントの最大値
     */
    public WhiteBalanceComponent getMaxWhiteBalanceComponent() {
        Data data = mProcessingUnitControls.get(PU_WHITE_BALANCE_COMPONENT_CONTROL);
        if (data != null && data.isSupported()) {
            int[] value = ((ArrayData) data).mMaxValue;
            return new WhiteBalanceComponent(value[0], value[1]);
        }
        throw new UnsupportedOperationException(PU_WHITE_BALANCE_COMPONENT_CONTROL + " is not supported.");
    }

    /**
     * ホワイトバランスコンポーネントの最小値を取得します.
     *
     * @return ホワイトバランスコンポーネントの最小値
     */
    public WhiteBalanceComponent getMinWhiteBalanceComponent() {
        Data data = mProcessingUnitControls.get(PU_WHITE_BALANCE_COMPONENT_CONTROL);
        if (data != null && data.isSupported()) {
            int[] value = ((ArrayData) data).mMinValue;
            return new WhiteBalanceComponent(value[0], value[1]);
        }
        throw new UnsupportedOperationException(PU_WHITE_BALANCE_COMPONENT_CONTROL + " is not supported.");
    }


    /// CT

    /**
     * 指定されたカメラターミナルコントロールのサポート状況を取得します.
     *
     * @param control コントロール
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    private boolean isSupported(final CameraTerminalControl control) {
        Data result = mCameraTerminalControls.get(control);
        return result != null && result.isSupported();
    }

    /**
     * 指定されたカメラターミナルコントロールに Int 値(4byte)を設定します.
     *
     * @param ctrl コントロール
     * @param value 設定する値
     */
    private void setIntControl(final CameraTerminalControl ctrl, final int value) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null) {
            int result = setInt(ctrl, value);
            if (result == UVCCamera.UVC_SUCCESS) {
                ((SettingData) data).mCurrentValue = value;
            }

            // TODO 値の設定に失敗した場合の処理

            return;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたカメラターミナルコントロールに Short 値(2byte)を設定します.
     *
     * @param ctrl コントロール
     * @param value 設定する値
     */
    private void setShortControl(final CameraTerminalControl ctrl, final int value) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null) {
            int result = setShort(ctrl, value);
            if (result == UVCCamera.UVC_SUCCESS) {
                ((SettingData) data).mCurrentValue = value;
            }

            // TODO 値の設定に失敗した場合の処理

            return;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたカメラターミナルコントロールに Byte 値(1byte)を設定します.
     *
     * @param ctrl コントロール
     * @param value 設定する値
     */
    private void setByteControl(final CameraTerminalControl ctrl, final int value) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null && data.isSupported()) {
            int result = setByte(ctrl, value);
            if (result == UVCCamera.UVC_SUCCESS) {
                ((SettingData) data).mCurrentValue = value;
            }

            // TODO 値の設定に失敗した場合の処理

            return;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたカメラターミナルコントロールの値を取得します.
     *
     * @param ctrl コントロール
     * @return 設定されている値
     */
    private int getCurSettingData(final CameraTerminalControl ctrl) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mCurrentValue;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたカメラターミナルコントロールの最大値を取得します.
     *
     * @param ctrl コントロール
     * @return 最大値
     */
    private int getMaxSettingData(final CameraTerminalControl ctrl) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mMaxValue;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたカメラターミナルコントロールの最小値を取得します.
     *
     * @param ctrl コントロール
     * @return 最小値
     */
    private int getMinSettingData(final CameraTerminalControl ctrl) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mMinValue;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたカメラターミナルコントロールの設定値を boolean で取得します.
     *
     * @param ctrl コントロール
     * @return 1が設定されている場合はtrue、それ以外はfalse
     */
    private boolean isSettingData(CameraTerminalControl ctrl) {
        Data data = mCameraTerminalControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mCurrentValue == 1;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }


    // PU


    /**
     * 指定されたプロセシングユニットコントロールのサポート状況を取得します.
     *
     * @param control コントロール
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    private boolean isSupported(final ProcessingUnitControl control) {
        Data result = mProcessingUnitControls.get(control);
        return result != null && result.isSupported();
    }

    /**
     * 指定されたプロセシングユニットコントロールに Short 値(2byte)を設定します.
     *
     * @param ctrl コントロール
     * @param value 設定する値(short)
     */
    private void setShortControl(final ProcessingUnitControl ctrl, final int value) {
        Data data = mProcessingUnitControls.get(ctrl);
        if (data != null && data.isSupported()) {
            int result = setShort(ctrl, value);
            if (result == UVCCamera.UVC_SUCCESS) {
                ((SettingData) data).mCurrentValue = value;
            }

            // TODO 値の設定に失敗した場合の処理

            return;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたプロセシングユニットコントロールに Byte 値(1byte)を設定します.
     *
     * @param ctrl コントロール
     * @param value 設定する値(byte)
     */
    private void setByteControl(final ProcessingUnitControl ctrl, final int value) {
        Data data = mProcessingUnitControls.get(ctrl);
        if (data != null && data.isSupported()) {
            int result = setByte(ctrl, value);
            if (result == UVCCamera.UVC_SUCCESS) {
                ((SettingData) data).mCurrentValue = value;
            }

            // TODO 値の設定に失敗した場合の処理

            return;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたプロセシングユニットコントロールの値を取得します.
     *
     * @param ctrl コントロール
     * @return 設定されている値
     */
    private int getCurSettingData(final ProcessingUnitControl ctrl) {
        Data data = mProcessingUnitControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mCurrentValue;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたプロセシングユニットコントロールの最大値を取得します.
     *
     * @param ctrl コントロール
     * @return 最大値
     */
    private int getMaxSettingData(ProcessingUnitControl ctrl) {
        Data data = mProcessingUnitControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mMaxValue;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたプロセシングユニットコントロールの最小値を取得します.
     *
     * @param ctrl コントロール
     * @return 最小値
     */
    private int getMinSettingData(ProcessingUnitControl ctrl) {
        Data data = mProcessingUnitControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mMinValue;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }

    /**
     * 指定されたプロセシングユニットコントロールの最小値を取得します.
     *
     * @param ctrl コントロール
     * @return 最小値
     */
    private boolean isSettingData(ProcessingUnitControl ctrl) {
        Data data = mProcessingUnitControls.get(ctrl);
        if (data != null && data.isSupported()) {
            return ((SettingData) data).mCurrentValue == 1;
        }
        throw new UnsupportedOperationException(ctrl + " is not supported.");
    }


    //// 初期化処理


    /**
     * カメラターミナルコントロールのサポート状況の設定を行います.
     * <p>
     * MEMO: NDK側から呼び出されるので、変更する場合には十分に注意すること。
     * </p>
     * @param control コントロールID
     * @param support サポート状況
     */
    void putCameraTerminalControls(final int control, final boolean support) {
        Data data;
        CameraTerminalControl ct = CameraTerminalControl.valueOf(control);
        switch (ct) {
            case CT_EXPOSURE_TIME_ABSOLUTE_CONTROL: {
                SettingData d = new SettingData();
                d.mSupported = support;
                if (support) {
                    d.mMaxValue = getIntMax(ct);
                    d.mMinValue = getIntMin(ct);
                    d.mDefaultValue = getIntDef(ct);
                    d.mCurrentValue = getIntCur(ct);
                }
                data = d;
            }   break;

            case CT_ROLL_ABSOLUTE_CONTROL:
            case CT_IRIS_ABSOLUTE_CONTROL:
            case CT_FOCUS_ABSOLUTE_CONTROL:
            case CT_ZOOM_ABSOLUTE_CONTROL: {
                SettingData d = new SettingData();
                d.mSupported = support;
                if (support) {
                    d.mMaxValue = getShortMax(ct);
                    d.mMinValue = getShortMin(ct);
                    d.mDefaultValue = getShortDef(ct);
                    d.mCurrentValue = getShortCur(ct);
                }
                data = d;
            }   break;

            case CT_SCANNING_MODE_CONTROL:
            case CT_AE_MODE_CONTROL:
            case CT_AE_PRIORITY_CONTROL:
            case CT_FOCUS_AUTO_CONTROL: {
                SettingData d = new SettingData();
                d.mSupported = support;
                if (support) {
                    d.mDefaultValue = getByteDef(ct);
                    d.mCurrentValue = getByteCur(ct);
                }
                data = d;
            }   break;

            default:
                data = new Data();
                data.mSupported = support;
                break;
        }

        mCameraTerminalControls.put(ct, data);
    }

    /**
     * プロセシングユニットコントロールのサポート状況の設定を行います.
     * <p>
     * MEMO: NDK側から呼び出されるので、変更する場合には十分に注意すること。
     * </p>
     * @param control コントロールID
     * @param support サポート状況
     */
    void putProcessingUnitControls(final int control, final boolean support) {
        Data data;
        ProcessingUnitControl pu = ProcessingUnitControl.valueOf(control);
        switch (pu) {
            case PU_WHITE_BALANCE_TEMPERATURE_CONTROL:
            case PU_GAMMA_CONTROL:
            case PU_SHARPNESS_CONTROL:
            case PU_SATURATION_CONTROL:
            case PU_HUE_CONTROL:
            case PU_GAIN_CONTROL:
            case PU_CONTRAST_CONTROL:
            case PU_BRIGHTNESS_CONTROL: {
                SettingData d = new SettingData();
                d.mSupported = support;
                if (support) {
                    d.mMaxValue = getShortMax(pu);
                    d.mMinValue = getShortMin(pu);
                    d.mDefaultValue = getShortDef(pu);
                    d.mCurrentValue = getShortCur(pu);
                }
                data = d;
            }   break;

            case PU_WHITE_BALANCE_COMPONENT_AUTO_CONTROL:
            case PU_WHITE_BALANCE_TEMPERATURE_AUTO_CONTROL:
            case PU_HUE_AUTO_CONTROL:
            case PU_CONTRAST_AUTO_CONTROL: {
                SettingData d = new SettingData();
                d.mSupported = support;
                if (support) {
                    d.mDefaultValue = getByteDef(pu);
                    d.mCurrentValue = getByteCur(pu);
                }
                data = d;
            }   break;

            case PU_WHITE_BALANCE_COMPONENT_CONTROL: {
                ArrayData d = new ArrayData(2);
                d.mSupported = support;
                if (support) {
                    byte[] value = new byte[4];
                    getArrayMax(pu, value);
                    d.mMaxValue[0] = (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
                    d.mMaxValue[1] = (value[3] & 0xFF) << 8 | (value[2] & 0xFF);

                    getArrayMin(pu, value);
                    d.mMinValue[0] = (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
                    d.mMinValue[1] = (value[3] & 0xFF) << 8 | (value[2] & 0xFF);

                    getArrayDef(pu, value);
                    d.mDefaultValue[0] = (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
                    d.mDefaultValue[1] = (value[3] & 0xFF) << 8 | (value[2] & 0xFF);

                    getArrayDef(pu, value);
                    d.mCurrentValue[0] = (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
                    d.mCurrentValue[1] = (value[3] & 0xFF) << 8 | (value[2] & 0xFF);
                }
                data = d;
            }   break;

            default:
                data = new Data();
                data.mSupported = support;
                break;
        }

        mProcessingUnitControls.put(pu, data);
    }


    private int setArrayControl(Control control, byte[] value) {
        return mUVCCamera.setControl(control, value);
    }

    private int getArrayMax(Control control, byte[] value) {
        return getArray(control, RequestType.GET_MAX, value);
    }

    private int getArrayMin(Control control, byte[] value) {
        return getArray(control, RequestType.GET_MIN, value);
    }

    private int getArrayDef(Control control, byte[] value) {
        return getArray(control, RequestType.GET_DEF, value);
    }

    private int getArrayCur(Control control, byte[] value) {
        return getArray(control, RequestType.GET_CUR, value);
    }

    private int getArray(Control control, RequestType request, byte[] value) {
        return mUVCCamera.getControl(control, request, value);
    }



    private int getIntMax(Control control) {
        return getInt(control, RequestType.GET_MAX);
    }

    private int getIntMin(Control control) {
        return getInt(control, RequestType.GET_MIN);
    }

    private int getIntDef(Control control) {
        return getInt(control, RequestType.GET_DEF);
    }

    private int getIntCur(Control control) {
        return getInt(control, RequestType.GET_CUR);
    }

    private int getInt(Control control, RequestType request) {
        byte[] value = new byte[2];
        int ret = mUVCCamera.getControl(control, request, value);
        if (ret != UVCCamera.UVC_SUCCESS) {
            // TODO 取得に失敗した場合の処理
        }
        return ((value[3] & 0xFF) << 24) | ((value[2] & 0xFF) << 16) |
                ((value[1] & 0xFF) << 8) | (value[0] & 0xFF);
    }

    private int setInt(Control control, int value) {
        byte[] buf = new byte[4];
        buf[0] = (byte) (value & 0xFF);
        buf[1] = (byte) ((value >> 8) & 0xFF);
        buf[2] = (byte) ((value >> 16) & 0xFF);
        buf[3] = (byte) ((value >> 24) & 0xFF);
        return mUVCCamera.setControl(control, buf);
    }



    private int getShortMax(Control control ) {
        return getShort(control, RequestType.GET_MAX);
    }

    private int getShortMin(Control control) {
        return getShort(control, RequestType.GET_MIN);
    }

    private int getShortDef(Control control) {
        return getShort(control, RequestType.GET_DEF);
    }

    private int getShortCur(Control control) {
        return getShort(control, RequestType.GET_CUR);
    }

    private int getShort(Control control, RequestType request) {
        byte[] value = new byte[2];
        int ret = mUVCCamera.getControl(control, request, value);
        if (ret != UVCCamera.UVC_SUCCESS) {
            // TODO 取得に失敗した場合の処理
        }
        return (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
    }

    private int setShort(Control control, int value) {
        byte[] buf = new byte[2];
        buf[0] = (byte) (value & 0xFF);
        buf[1] = (byte) ((value >> 8) & 0xFF);
        return mUVCCamera.setControl(control, buf);
    }


    /**
     * デフォルトの値を取得します.
     *
     * @param control コントロール
     * @return 取得した値
     */
    private int getByteDef(final Control control) {
        return getByte(control, RequestType.GET_DEF);
    }

    /**
     * 現在の値を取得します.
     *
     * @param control コントロール
     * @return 取得した値
     */
    private int getByteCur(Control control) {
        return getByte(control, RequestType.GET_CUR);
    }

    /**
     * 値を取得します.
     *
     * @param control コントロール
     * @param request リクエスト
     * @return 取得した値
     */
    private int getByte(Control control, RequestType request) {
        byte[] value = new byte[1];
        int ret = mUVCCamera.getControl(control, request, value);
        if (ret != UVCCamera.UVC_SUCCESS) {
            // TODO 取得に失敗した場合の処理
        }
        return value[0] & 0xFF;
    }

    /**
     * 指定された値を設定します.
     *
     * @param control コントロール
     * @param value 設定する値
     * @return 0の場合は設定成功、それ以外は失敗
     */
    private int setByte(Control control, int value) {
        byte[] buf = new byte[1];
        buf[0] = (byte) (value & 0xFF);
        return mUVCCamera.setControl(control, buf);
    }


    @Override
    public String toString() {
        return "CameraTerminalControls: " + mCameraTerminalControls + "\n" +
                "ProcessingUnitControls: " + mProcessingUnitControls;
    }

    /**
     * ホワイトバランスコンポーネントの設定情報を格納するクラス.
     */
    public class WhiteBalanceComponent {
        /**
         * 青色成分.
         */
        int mBlue;

        /**
         * 赤色成分.
         */
        int mRed;

        /**
         * コンストラクタ.
         *
         * @param blue 青色成分
         * @param red 赤色成分
         */
        WhiteBalanceComponent(final int blue, final int red) {
            mBlue = blue;
            mRed = red;
        }

        /**
         * 青色成分を取得します.
         *
         * @return 青色成分
         */
        public int getBlue() {
            return mBlue;
        }

        /**
         * 青色成分を設定します
         * @param blue
         */
        public void setBlue(final int blue) {
            mBlue = blue;
        }

        /**
         * 赤色成分を取得します.
         *
         * @return 赤色成分
         */
        public int getRed() {
            return mRed;
        }

        /**
         * 赤色成分を設定します.
         *
         * @param red 赤色成分
         */
        public void setRed(final int red) {
            mRed = red;
        }
    }

    /**
     * UVCからの設定データを格納するクラス.
     */
    private class Data {
        /**
         * サポート状況.
         * <p>
         * サポートしている場合はtrue、それ以外はfalse
         * </p>
         */
        boolean mSupported;

        /**
         * サポート状況を取得します.
         *
         * @return サポートしている場合はtrue、それ以外はfalse
         */
        boolean isSupported() {
            return mSupported;
        }

        @Override
        public String toString() {
            return "{\n" +
                    "    Supported: " + mSupported + "\n" +
                    "}\n";
        }
    }

    /**
     * 設定情報を格納するクラス.
     */
    private class SettingData extends Data {
        /**
         * 設定できる最大値.
         */
        int mMaxValue;
        /**
         * 設定できる最小.
         */
        int mMinValue;

        /**
         * デフォルトで設定されている値.
         */
        int mDefaultValue;

        /**
         * 現在設定されている値.
         */
        int mCurrentValue;

        @Override
        public String toString() {
            return "{\n" +
                    "    Supported: " + mSupported + "\n" +
                    "    Max: " + mMaxValue + "\n" +
                    "    Min: " + mMinValue + "\n" +
                    "    Default: " + mDefaultValue + "\n" +
                    "    Current: " + mCurrentValue + "\n" +
                    "}\n";
        }
    }

    /**
     * 設定情報が配列で格納するクラス.
     */
    private class ArrayData extends Data {
        int[] mMaxValue;
        int[] mMinValue;
        int[] mDefaultValue;
        int[] mCurrentValue;

        ArrayData(int len) {
            mMaxValue = new int[len];
            mMinValue = new int[len];
            mDefaultValue = new int[len];
            mCurrentValue = new int[len];
        }

        @Override
        public String toString() {
            return "{\n" +
                    "    Supported: " + mSupported + "\n" +
                    "    Max: " + mMaxValue + "\n" +
                    "    Min: " + mMinValue + "\n" +
                    "    Default: " + mDefaultValue + "\n" +
                    "    Current: " + mCurrentValue + "\n" +
                    "}\n";
        }
    }
}
