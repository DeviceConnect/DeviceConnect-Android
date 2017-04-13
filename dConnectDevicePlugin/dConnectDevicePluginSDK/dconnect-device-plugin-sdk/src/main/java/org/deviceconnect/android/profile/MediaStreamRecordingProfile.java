/*
 MediaStreamRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;

import java.util.List;

/**
 * MediaStream Recording プロファイル.
 * 
 * <p>
 * スマートデバイスによる写真撮影、動画録画、音声録音などの機能を提供するAPI.<br>
 * スマートデバイスによる写真撮影、動画録画、音声録音などの機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class MediaStreamRecordingProfile extends DConnectProfile implements MediaStreamRecordingProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストからレコーダーの識別IDを取得する.
     * 
     * @param request リクエストパラメータ
     * @return レコーダーの識別ID。無い場合はnullを返す。
     */
    public static String getTarget(final Intent request) {
        String target = request.getStringExtra(PARAM_TARGET);
        return target;
    }

    /**
     * リクエストからタイムスライスを取得する.
     * 
     * @param request リクエストパラメータ
     * @return タイムスライス。無い、または不正値の場合は-1を返す。
     */
    public static Long getTimeSlice(final Intent request) {
        Bundle b = request.getExtras();
        if (b == null) {
            return null;
        }
        try {
            String slice = b.getString(PARAM_TIME_SLICE);
            if (slice == null) {
                return null;
            }
            Long res = Long.valueOf(slice);
            return res;
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    /**
     * リクエストから撮影時の横幅を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 撮影時の横幅。無い、不正値の場合は<code>null</code>。
     */
    public static Integer getImageWidth(final Intent request) {
        return parseInteger(request, PARAM_IMAGE_WIDTH);
    }

    /**
     * リクエストから撮影時の縦幅を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 撮影時の縦幅。無い、不正値の場合は<code>null</code>。
     */
    public static Integer getImageHeight(final Intent request) {
        return parseInteger(request, PARAM_IMAGE_HEIGHT);
    }

    /**
     * リクエストからプレビュー時の横幅を取得する.
     *
     * @param request リクエストパラメータ
     * @return プレビュー時の横幅。無い、不正値の場合は<code>null</code>。
     */
    public static Integer getPreviewWidth(final Intent request) {
        return parseInteger(request, PARAM_PREVIEW_WIDTH);
    }

    /**
     * リクエストからプレビュー時の縦幅を取得する.
     *
     * @param request リクエストパラメータ
     * @return プレビュー時の縦幅。無い、不正値の場合は<code>null</code>。
     */
    public static Integer getPreviewHeight(final Intent request) {
        return parseInteger(request, PARAM_PREVIEW_HEIGHT);
    }

    /**
     * リクエストからプレビューの最大プレームレートを取得する.
     *
     * @param request リクエストパラメータ
     * @return プレビューの最大プレームレート。無い、不正値の場合は<code>null</code>。
     */
    public static Double getPreviewMaxFrameRate(final Intent request) {
        return parseDouble(request, PARAM_PREVIEW_MAX_FRAME_RATE);
    }


    /**
     * リクエストからMIMEタイプを取得する.
     * 
     * @param request リクエストパラメータ
     * @return MIMEタイプ
     */
    public static String getMIMEType(final Intent request) {
        String mime = request.getStringExtra(PARAM_MIME_TYPE);
        return mime;
    }
    
    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにレコーダーデータを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param recorders レコーダーデータ
     */
    public static void setRecorders(final Intent response, final Bundle[] recorders) {
        response.putExtra(PARAM_RECORDERS, recorders);
    }
    
    /**
     * レスポンスにファイルパスを設定する.
     * 
     * @param response レスポンスデータ
     * @param path ファイルパス
     */
    public static void setPath(final Intent response, final String path) {
        response.putExtra(PARAM_PATH, path);
    }
    
    /**
     * 写真情報にファイルパスを設定する.
     * 
     * @param photo 写真情報 
     * @param path ファイルパス
     */
    public static void setPath(final Bundle photo, final String path) {
        photo.putString(PARAM_PATH, path);
    }

    /**
     * レスポンスにレコーダーデータを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param recorders レコーダーデータ
     */
    public static void setRecorders(final Intent response, final List<Bundle> recorders) {
        setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
    }

    /**
     * レコーダーデータにレコーダーIDを設定する.
     * 
     * @param recorder レコーダーデータ
     * @param id レコーダーID
     */
    public static void setRecorderId(final Bundle recorder, final String id) {
        recorder.putString(PARAM_ID, id);
    }

    /**
     * レコーダーデータにレコーダー名を設定する.
     * 
     * @param recorder レコーダーデータ
     * @param name レコーダー名
     */
    public static void setRecorderName(final Bundle recorder, final String name) {
        recorder.putString(PARAM_NAME, name);
    }

    /**
     * レコーダーデータにレコーダーの状態を設定する.
     * 
     * @param recorder レコーダーデータ
     * @param state レコーダーの状態
     */
    public static void setRecorderState(final Bundle recorder, final RecorderState state) {
        recorder.putString(PARAM_STATE, state.getValue());
    }

    /**
     * レコーダーデータに撮影時の横幅を設定する.
     * 
     * @param recorder レコーダーデータ
     * @param imageWidth 撮影時の横幅
     */
    public static void setRecorderImageWidth(final Bundle recorder, final int imageWidth) {
        recorder.putInt(PARAM_IMAGE_WIDTH, imageWidth);
    }

    /**
     * レコーダーデータに撮影時の縦幅を設定する.
     * 
     * @param recorder レコーダーデータ
     * @param imageHeight 撮影時の縦幅
     */
    public static void setRecorderImageHeight(final Bundle recorder, final int imageHeight) {
        recorder.putInt(PARAM_IMAGE_HEIGHT, imageHeight);
    }

    /**
     * レコーダーデータにプレビュー時の横幅を設定する.
     *
     * @param recorder レコーダーデータ
     * @param previewWidth プレビュー時の横幅
     */
    public static void setRecorderPreviewWidth(final Bundle recorder, final int previewWidth) {
        recorder.putInt(PARAM_PREVIEW_WIDTH, previewWidth);
    }

    /**
     * レコーダーデータにプレビュー時の縦幅を設定する.
     *
     * @param recorder レコーダーデータ
     * @param previewHeight プレビュー時の縦幅
     */
    public static void setRecorderPreviewHeight(final Bundle recorder, final int previewHeight) {
        recorder.putInt(PARAM_PREVIEW_HEIGHT, previewHeight);
    }

    /**
     * レコーダーデータにプレビューの最大フレームレートを設定する.
     *
     * @param recorder レコーダーデータ
     * @param maxFrameRate プレビューの最大フレームレート
     */
    public static void setRecorderPreviewMaxFrameRate(final Bundle recorder, final double maxFrameRate) {
        recorder.putDouble(PARAM_PREVIEW_MAX_FRAME_RATE, maxFrameRate);
    }

    /**
     * レコーダーデータに音声情報を設定する.
     *
     * @param recorder レコーダーデータ
     * @param audio 音声情報
     */
    public static void setRecorderAudio(final Bundle recorder, final Bundle audio) {
        recorder.putBundle(PARAM_AUDIO, audio);
    }

    /**
     * 音声情報にチャンネル数を設定する.
     *
     * @param audio 音声情報
     * @param channels チャンネル数
     */
    public static void setAudioChannels(final Bundle audio, final int channels) {
        audio.putInt(PARAM_CHANNELS, channels);
    }

    /**
     * 音声情報にサンプルレートを設定する.
     *
     * @param audio 音声情報
     * @param sampleRate サンプルレート
     */
    public static void setAudioSampleRate(final Bundle audio, final int sampleRate) {
        audio.putInt(PARAM_SAMPLE_RATE, sampleRate);
    }

    /**
     * 音声情報にサンプルサイズを設定する.
     *
     * @param audio 音声情報
     * @param sampleSize サンプルサイズ
     */
    public static void setAudioSampleSize(final Bundle audio, final int sampleSize) {
        audio.putInt(PARAM_SAMPLE_SIZE, sampleSize);
    }

    /**
     * 音声情報にブロックサイズを設定する.
     *
     * @param audio 音声情報
     * @param blockSize ブロックサイズ
     */
    public static void setAudioBlockSize(final Bundle audio, final int blockSize) {
        audio.putInt(PARAM_BLOCK_SIZE, blockSize);
    }

    /**
     * メッセージに音声情報を設定する.
     *
     * @param message メッセージ
     * @param audio 音声情報
     */
    public static void setAudio(final Intent message, final Bundle audio) {
        message.putExtra(PARAM_AUDIO, audio);
    }

    /**
     * 音声情報に音声配信URIを設定する.
     *
     * @param audio 音声情報
     * @param uri 音声配信URI
     */
    public static void setAudioUri(final Bundle audio, final String uri) {
        audio.putString(PARAM_URI, uri);
    }

    /**
     * レコーダーデータにMIMEタイプを設定する.
     * 
     * @param recorder レコーダーデータ
     * @param mime MIMEタイプ
     */
    public static void setRecorderMIMEType(final Bundle recorder, final String mime) {
        recorder.putString(PARAM_MIME_TYPE, mime);
    }

    /**
     * レコーダーデータに設定情報を設定する.
     * 
     * @param recorder レコーダーデータ
     * @param config 設定情報
     */
    public static void setRecorderConfig(final Bundle recorder, final String config) {
        recorder.putString(PARAM_CONFIG, config);
    }

    /**
     * メッセージに写真データを設定する.
     * 
     * @param message メッセージパラメータ
     * @param photo 写真データ
     */
    public static void setPhoto(final Intent message, final Bundle photo) {
        message.putExtra(PARAM_PHOTO, photo);
    }

    /**
     * メディアデータを設定する.
     * 
     * @param message メッセージパラメータ
     * @param media メディアデータ
     */
    public static void setMedia(final Intent message, final Bundle media) {
        message.putExtra(PARAM_MEDIA, media);
    }

    /**
     * レスポンスにファイルのURIを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param uri ファイルのURI
     */
    public static void setUri(final Intent response, final String uri) {
        response.putExtra(PARAM_URI, uri);
    }

    /**
     * メディアデータにURIを設定する.
     * 
     * @param media メディアデータ
     * @param uri ファイルのURI
     */
    public static void setUri(final Bundle media, final String uri) {
        media.putString(PARAM_URI, uri);
    }
    
    /**
     * メディアデータにエラーメッセージを設定する.
     * 
     * @param media メディアデータ
     * @param errorMessage エラーメッセージ
     */
    public static void setErrorMessage(final Bundle media, final String errorMessage) {
        media.putString(PARAM_ERROR_MESSAGE, errorMessage);
    }
    
    /**
     * メディアデータに状態を設定する.
     * 
     * @param media メディアデータ
     * @param state 状態
     */
    public static void setStatus(final Bundle media, final RecordingState state) {
        media.putString(PARAM_STATUS, state.getValue());
    }
    
    /**
     * メディアデータに状態を設定する.
     * 
     * @param media メディアデータ
     * @param state 状態
     */
    public static void setStatus(final Bundle media, final String state) {
        media.putString(PARAM_STATUS, state);
    }

    /**
     * レスポンスに撮影時の解像度一覧を設定する.
     *
     * @param response レスポンスパラメータ
     * @param imageSizes 撮影時の解像度一覧
     */
    public static void setImageSizes(final Intent response, final Bundle[] imageSizes) {
        response.putExtra(PARAM_IMAGE_SIZES, imageSizes);
    }

    /**
     * レスポンスに撮影時の解像度一覧を設定する.
     *
     * @param response レスポンスパラメータ
     * @param imageSizes 撮影時の解像度一覧
     */
    public static void setImageSizes(final Intent response, final List<Bundle> imageSizes) {
        response.putExtra(PARAM_IMAGE_SIZES, imageSizes.toArray(new Bundle[imageSizes.size()]));
    }

    /**
     * レスポンスにプレビュー時の解像度一覧を設定する.
     *
     * @param response レスポンスパラメータ
     * @param previewSizes プレビュー時の解像度一覧
     */
    public static void setPreviewSizes(final Intent response, final Bundle[] previewSizes) {
        response.putExtra(PARAM_PREVIEW_SIZES, previewSizes);
    }

    /**
     * レスポンスにプレビュー時の解像度一覧を設定する.
     *
     * @param response レスポンスパラメータ
     * @param previewSizes プレビュー時の解像度一覧
     */
    public static void setPreviewSizes(final Intent response, final List<Bundle> previewSizes) {
        response.putExtra(PARAM_PREVIEW_SIZES, previewSizes.toArray(new Bundle[previewSizes.size()]));
    }

    /**
     * サイズデータに横幅を設定する.
     * @param size サイズデータ
     * @param width 横幅
     */
    public static void setWidth(final Bundle size, final int width) {
        size.putInt(PARAM_WIDTH, width);
    }

    /**
     * サイズデータに縦幅を設定する.
     * @param size サイズデータ
     * @param height 縦幅
     */
    public static void setHeight(final Bundle size, final int height) {
        size.putInt(PARAM_HEIGHT, height);
    }

    /**
     * サイズデータに最小値と最大値を設定する.
     * @param size サイズデータ
     * @param min 最小値
     * @param max 最大値
     * @deprecated
     */
    public static void setSize(final Bundle size, final int min, final int max) {
        size.putInt(PARAM_MIN, min);
        size.putInt(PARAM_MAX, max);
    }

    /**
     * レスポンスに横幅サイズの最小値と最大値を設定する.
     * @param response レスポンス
     * @param min 最小値
     * @param max 最大値
     * @deprecated
     */
    public static void setImageWidth(final Intent response, final int min, final int max) {
        Bundle size = new Bundle();
        setSize(size, min, max);
        setImageWidth(response, size);
    }

    /**
     * レスポンスに横幅サイズを設定する.
     * @param response レスポンス
     * @param size サイズデータ
     * @deprecated
     */
   public static void setImageWidth(final Intent response, final Bundle size) {
        response.putExtra(PARAM_IMAGE_WIDTH, size);
    }

   /**
    * レスポンスに縦幅サイズの最小値と最大値を設定する.
    * @param response レスポンス
    * @param min 最小値
    * @param max 最大値
    * @deprecated
    */
    public static void setImageHeight(final Intent response, final int min, final int max) {
        Bundle size = new Bundle();
        setSize(size, min, max);
        setImageHeight(response, size);
    }

    /**
     * レスポンスに縦幅サイズを設定する.
     * @param response レスポンス
     * @param size サイズデータ
     * @deprecated 
     */
    public static void setImageHeight(final Intent response, final Bundle size) {
        response.putExtra(PARAM_IMAGE_HEIGHT, size);
    }

    /**
     * MIMEタイプを設定する.
     * 
     * @param response レスポンス
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Intent response, final String[] mimeType) {
        response.putExtra(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * MIMEタイプを設定する.
     * 
     * @param param パラメータ
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Bundle param, final String mimeType) {
        param.putString(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * MIMEタイプを設定する.
     * 
     * @param param パラメータ
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Bundle param, final String[] mimeType) {
        param.putStringArray(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * MIMEタイプを設定する.
     * 
     * @param param パラメータ
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Bundle param, final List<String> mimeType) {
        param.putStringArray(PARAM_MIME_TYPE, mimeType.toArray(new String[mimeType.size()]));
    }
}
