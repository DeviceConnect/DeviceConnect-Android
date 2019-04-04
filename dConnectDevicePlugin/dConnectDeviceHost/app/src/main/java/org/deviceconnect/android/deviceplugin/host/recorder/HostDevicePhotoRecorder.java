/*
 HostDevicePhotoRecorder.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.os.Handler;

/**
 * 写真撮影機能を持つレコーダー.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDevicePhotoRecorder {

    /**
     * 写真撮影を実行する.
     *
     * @param listener 写真撮影の結果を受け取るためのリスナー
     */
    void takePhoto(OnPhotoEventListener listener);

    /**
     * バックカメラを使用するかどうかをチェックする.
     *
     * @return バックカメラの場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    boolean isBack();

    /**
     * カメラのライトを ON にする.
     *
     * このメソッドは非同期的に実行される.
     *
     * @param listener 処理結果を受け取るためのリスナー.
     * @param handler リスナーを実行するハンドラー. リスナーを指定する場合は必須.
     * @throws IllegalArgumentException リスナーを指定しているのに、ハンドラーを指定していない場合
     */
    void turnOnFlashLight(TurnOnFlashLightListener listener, Handler handler);

    /**
     * カメラのライトを ON にする.
     *
     * このメソッドは非同期的に実行される.
     * 以下の処理と同じ処理が実行される.
     * turnOnFlashLight(null, null);
     */
    void turnOnFlashLight();

    /**
     * カメラのライトを OFF にする.
     *
     * @param listener 処理結果を受け取るためのリスナー.
     * @param handler リスナーを実行するハンドラー. リスナーを指定する場合は必須.
     * @throws IllegalArgumentException リスナーを指定しているのに、ハンドラーを指定していない場合
     */
    void turnOffFlashLight(TurnOffFlashLightListener listener, Handler handler);

    /**
     * カメラのライトを OFF にする.
     *
     * このメソッドは非同期的に実行される.
     * 以下の処理と同じ処理が実行される.
     * turnOffFlashLight(null, null);
     */
    void turnOffFlashLight();

    /**
     * カメラのライトがONかどうかをチェックする.
     *
     * @return カメラのライトがONの場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    boolean isFlashLightState();

    /**
     * カメラのライトが使用中かどうかをチェックする.
     *
     * @return カメラのライトが使用中の場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    boolean isUseFlashLight();

    /**
     * 写真撮影の結果を受け取るためのリスナー.
     */
    interface OnPhotoEventListener {

        /**
         * 写真撮影が完了したタイミングで実行される.
         *
         * @param uri 写真が保存されている URI.
         * @param filePath 写真が保存されているファイルパス.
         * @param mimeType 写真のマイムタイプ
         */
        void onTakePhoto(String uri, String filePath, String mimeType);

        /**
         * 写真撮影に失敗した場合に実行される.
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailedTakePhoto(String errorMessage);
    }

    /**
     * カメラのライトをONするリクエストの結果を受け取るためのリスナー.
     */
    interface TurnOnFlashLightListener {

        /**
         * カメラに対して正常にリクエストされた場合に実行される.
         * この段階ではまだライトはONになっていない.
         */
        void onRequested();

        /**
         * 実際にライトがONになったタイミングで実行される.
         */
        void onTurnOn();

        /**
         * ライトをONにできなかった場合.
         * @param error エラー内容
         */
        void onError(Error error);
    }

    /**
     * カメラのライトをOFFするリクエストの結果を受け取るためのリスナー.
     */
    interface TurnOffFlashLightListener {

        /**
         * カメラに対して正常にリクエストされた場合に実行される.
         * この段階ではまだライトはOFFになっていない.
         */
        void onRequested();

        /**
         * 実際にライトがONになったタイミングで実行される.
         */
        void onTurnOff();

        /**
         * ライトをOFFにできなかった場合.
         * @param error エラー内容
         */
        void onError(Error error);
    }

    /**
     * 写真撮影用レコーダーについて発生するエラー.
     */
    enum Error {

        /**
         * 要求された機能をサポートしていない.
         */
        UNSUPPORTED,

        /**
         * 致命的なエラーにより機能を実行できなかった.
         */
        FATAL_ERROR
    }
}
