package org.deviceconnect.android.deviceplugin.host.recorder;

import android.graphics.Rect;

public interface CropInterface {

    /**
     * PreviewSurfaceView で枠を表示する時に表示する名前を取得します.
     *
     * @return 名前
     */
    String getName();

    /**
     * start で指定された矩形から end で指定された矩形に移動します.
     *
     * @param start 開始する矩形
     * @param end 停止する矩形
     * @param duration 移動する時間(ミリ秒)
     */
    void moveCropRect(Rect start, Rect end, int duration);

    /**
     * クロップ範囲を設定します.
     *
     * @param rect クロップする範囲の矩形
     */
    void setCropRect(Rect rect);

    /**
     * クロップ範囲を取得します.
     *
     * @return クロップする範囲の矩形
     */
    Rect getCropRect();

    /**
     * イベントリスナーを追加します.
     *
     * @param listener 追加するリスナー
     */
    void addOnEventListener(OnEventListener listener);

    /**
     * イベントリスナーを削除します.
     *
     * @param listener 削除するリスナー
     */
    void removeOnEventListener(OnEventListener listener);

    interface OnEventListener {
        /**
         * クロップする矩形が追加されたことを通知します.
         */
        void onAdded(CropInterface crop, Rect cropRect);

        /**
         * クロップする矩形が削除されたことを通知します.
         */
        void onRemoved(CropInterface crop);

        /**
         * クロップする矩形が移動したことを通知します.
         */
        void onMoved(CropInterface crop, Rect cropRect);
    }
}
