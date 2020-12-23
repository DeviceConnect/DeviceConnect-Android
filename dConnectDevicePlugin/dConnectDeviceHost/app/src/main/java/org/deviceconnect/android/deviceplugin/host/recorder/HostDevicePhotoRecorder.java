/*
 HostDevicePhotoRecorder.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.os.Handler;
import androidx.annotation.NonNull;

/**
 * 静止画の撮影を行うためのインターフェース.
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
     * カメラのライトを ON にする.
     *
     * このメソッドは非同期的に実行される.
     *
     * @param listener 処理結果を受け取るためのリスナー.
     * @param handler リスナーを実行するハンドラー. リスナーを指定する場合は必須.
     * @throws IllegalArgumentException リスナーとハンドラーの一方または両方を指定していない場合
     */
    void turnOnFlashLight(@NonNull TurnOnFlashLightListener listener, @NonNull Handler handler);

    /**
     * カメラのライトを OFF にする.
     *
     * @param listener 処理結果を受け取るためのリスナー.
     * @param handler リスナーを実行するハンドラー.
     * @throws IllegalArgumentException リスナーとハンドラーの一方または両方を指定していない場合
     */
    void turnOffFlashLight(@NonNull TurnOffFlashLightListener listener, @NonNull Handler handler);

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

    interface OnEventListener {

        /**
         * 写真撮影が完了したタイミングで実行される.
         *
         * @param uri 写真が保存されている URI.
         * @param filePath 写真が保存されているファイルパス.
         * @param mimeType 写真のマイムタイプ
         */
        void onTakePhoto(String uri, String filePath, String mimeType);
    }

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
         * 実際にライトがOFFになったタイミングで実行される.
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
