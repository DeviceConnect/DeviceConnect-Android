/*
 TestMediaStreamRecordingProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.test.plugin.profile;

import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;


/**
 * JUnit用テストデバイスプラグイン、MediaStreamRecordingプロファイル.
 * @author NTT DOCOMO, INC.
 */
public interface TestMediaStreamRecordingProfileConstants {

    /**
     * カメラID.
     */
    String ID = "test_camera_id";

    /**
     * カメラの名前.
     */
    String NAME = "test_camera_name";

    /**
     * カメラの状態.
     */
    String STATE = MediaStreamRecordingProfileConstants.RecorderState.INACTIVE.getValue();

    /**
     * 撮影時の横幅.
     */
    int IMAGE_WIDTH = 1920;

    /**
     * 撮影時の縦幅.
     */
    int IMAGE_HEIGHT = 1080;

    /**
     * プレビュー時の横幅.
     */
    int PREVIEW_WIDTH = 640;

    /**
     * プレビュー時の縦幅.
     */
    int PREVIEW_HEIGHT = 480;

    /**
     * プレビューの最大フレームレート.
     */
    double PREVIEW_MAX_FRAME_RATE = 30.0d;

    /**
     * レコーダの状態.
     */
    String STATUS = "recording";

    /**
     * レコーダのエンコードするMIMEタイプ.
     */
    String MIME_TYPE = "video/mp4";

    /**
     * カメラ設定.
     */
    String CONFIG = "test_config";

    /**
     * 撮影した写真のURI.
     */
    String URI = "content://test/test.mp4";

    /**
     * プレビュー動画配信URI.
     */
    String PREVIEW_URI = "http://localhost:9000/preview";

    /**
     * 音声配信URI.
     */
    String AUDIO_URI = "http://localhost:9000/audio";

    /**
     * メディアID.
     */
    String PATH = "test.mp4";

    /**
     * タイムスライス.
     */
    long TIME_SLICE = 3600L;

}
