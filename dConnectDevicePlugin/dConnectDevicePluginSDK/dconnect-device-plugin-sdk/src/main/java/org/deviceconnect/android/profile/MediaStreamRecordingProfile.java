/*
 MediaStreamRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;

import java.util.List;

/**
 * MediaStream Recording プロファイル.
 * 
 * <p>
 * スマートデバイスによる写真撮影、動画録画、音声録音などの機能を提供するAPI.<br/>
 * スマートデバイスによる写真撮影、動画録画、音声録音などの機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * MediaStream Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>MediaStreamRecording MediaRecorder API [GET] :
 * {@link MediaStreamRecordingProfile#onGetMediaRecorder(Intent, Intent, String)}
 * </li>
 * <li>MediaStreamRecording Take Photo API [POST] :
 * {@link MediaStreamRecordingProfile#onPostTakePhoto(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Record API [POST] :
 * {@link MediaStreamRecordingProfile#onPostRecord(Intent, Intent, String, String, Long)}
 * </li>
 * <li>MediaStreamRecording Pause API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutPause(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Resume API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutResume(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Stop API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutStop(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Mute Track API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutMuteTrack(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Unmute Track API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutUnmuteTrack(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Options API [GET] :
 * {@link MediaStreamRecordingProfile#onGetOptions(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Options API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutOptions(Intent, Intent, String, String, Integer, Integer, Integer, Integer, Double, String)}
 * </li>
 * <li>MediaStreamRecording Preview API [PUT] :
 * {@link MediaStreamRecordingProfile#onPutPreview(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Preview API [DELETE] :
 * {@link MediaStreamRecordingProfile#onPutPreview(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Take a Picture Event API [Register] :
 * {@link MediaStreamRecordingProfile#onPutOnPhoto(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Take a Picture Event API [Unregister] :
 * {@link MediaStreamRecordingProfile#onDeleteOnPhoto(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Recording Change Event API [Register] :
 * {@link MediaStreamRecordingProfile#onPutOnRecordingChange(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Recording Change Event API [Unregister] :
 * {@link MediaStreamRecordingProfile#onDeleteOnRecordingChange(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Data Available Event API [Register] :
 * {@link MediaStreamRecordingProfile#onPutOnDataAvailable(Intent, Intent, String, String)}
 * </li>
 * <li>MediaStreamRecording Data Available Event API [Unregister] :
 * {@link MediaStreamRecordingProfile#onDeleteOnDataAvailable(Intent, Intent, String, String)}
 * </li>
 * </ul>
 * @author NTT DOCOMO, INC.
 */
public class MediaStreamRecordingProfile extends DConnectProfile implements MediaStreamRecordingProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (attribute == null) {
            setUnsupportedError(response);
        } else {
            String serviceId = getServiceID(request);
            if (attribute.equals(ATTRIBUTE_MEDIARECORDER)) {
                result = onGetMediaRecorder(request, response, serviceId);
            } else if (attribute.equals(ATTRIBUTE_OPTIONS)) {
                String target = getTarget(request);
                result = onGetOptions(request, response, serviceId, target);
            } else {
                setUnsupportedError(response);
            }
        }

        return result;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (attribute == null) {
            setUnsupportedError(response);
        } else {
            String target = getTarget(request);
            String serviceId = getServiceID(request);

            if (attribute.equals(ATTRIBUTE_TAKE_PHOTO)) {
                result = onPostTakePhoto(request, response, serviceId, target);
            } else if (attribute.equals(ATTRIBUTE_RECORD)) {
                try {
                    Long timeslice = getTimeSlice(request);
                    result = onPostRecord(request, response, serviceId, target, timeslice);
                } catch (NumberFormatException e) {
                    MessageUtils.setInvalidRequestParameterError(response);
                }
            } else {
                setUnsupportedError(response);
            }
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (attribute == null) {
            MessageUtils.setUnknownAttributeError(response);
        } else {
            String serviceId = getServiceID(request);
            String target = getTarget(request);
            String sessionKey = getSessionKey(request);

            if (attribute.equals(ATTRIBUTE_PAUSE)) {
                result = onPutPause(request, response, serviceId, target);
            } else if (attribute.equals(ATTRIBUTE_RESUME)) {
                result = onPutResume(request, response, serviceId, target);
            } else if (attribute.equals(ATTRIBUTE_STOP)) {
                result = onPutStop(request, response, serviceId, target);
            } else if (attribute.equals(ATTRIBUTE_MUTETRACK)) {
                result = onPutMuteTrack(request, response, serviceId, target);
            } else if (attribute.equals(ATTRIBUTE_UNMUTETRACK)) {
                result = onPutUnmuteTrack(request, response, serviceId, target);
            } else if (attribute.equals(ATTRIBUTE_OPTIONS)) {
                Integer imageWidth = getImageWidth(request);
                Integer imageHeight = getImageHeight(request);
                Integer previewWidth = getPreviewWidth(request);
                Integer previewHeight = getPreviewHeight(request);
                Double previewMaxFrameRate = getPreviewMaxFrameRate(request);
                String mimeType = getMIMEType(request);
                result = onPutOptions(request, response, serviceId, target, imageWidth, imageHeight,
                    previewWidth, previewHeight, previewMaxFrameRate, mimeType);
            } else if (attribute.equals(ATTRIBUTE_ON_PHOTO)) {
                result = onPutOnPhoto(request, response, serviceId, sessionKey);
            } else if (attribute.equals(ATTRIBUTE_ON_RECORDING_CHANGE)) {
                result = onPutOnRecordingChange(request, response, serviceId, sessionKey);
            } else if (attribute.equals(ATTRIBUTE_ON_DATA_AVAILABLE)) {
                result = onPutOnDataAvailable(request, response, serviceId, sessionKey);
            } else if (attribute.equals(ATTRIBUTE_PREVIEW)) {
                result = onPutPreview(request, response, serviceId, target);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (attribute == null) {
            MessageUtils.setUnknownAttributeError(response);
        } else {

            String serviceId = getServiceID(request);
            String target = getTarget(request);
            String sessionKey = getSessionKey(request);

            if (attribute.equals(ATTRIBUTE_ON_PHOTO)) {
                result = onDeleteOnPhoto(request, response, serviceId, sessionKey);
            } else if (attribute.equals(ATTRIBUTE_ON_RECORDING_CHANGE)) {
                result = onDeleteOnRecordingChange(request, response, serviceId, sessionKey);
            } else if (attribute.equals(ATTRIBUTE_ON_DATA_AVAILABLE)) {
                result = onDeleteOnDataAvailable(request, response, serviceId, sessionKey);
            } else if (attribute.equals(ATTRIBUTE_PREVIEW)) {
                result = onDeletePreview(request, response, serviceId, target);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        }
        return result;
    }

    // ------------------------------------
    // GET
    // ------------------------------------

    /**
     * 使用可能レコーダー情報取得リクエストハンドラー.<br/>
     * 使用可能なレコーダーの情報を提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetMediaRecorder(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * サポートオプション一覧取得リクエストハンドラー.<br/>
     * サポートしているオプションの一覧を提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target レコーダーを識別するID。省略された場合はnull。
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetOptions(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // POST
    // ------------------------------------

    /**
     * 写真撮影依頼リクエストハンドラー.<br/>
     * 写真の撮影を実行し、その結果をレスポンスパラメータに格納する。 レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target 撮影するレコーダーを識別するID。省略された場合はnull。
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostTakePhoto(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 動画撮影、音声録音依頼リクエストハンドラー.<br/>
     * 動画撮影、音声録音を実行し、その結果をレスポンスパラメータに格納する。 レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target レコーダーを識別するID
     * @param timeslice タイムスライス
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostRecord(final Intent request, final Intent response, final String serviceId,
            final String target, final Long timeslice) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // PUT
    // ------------------------------------

    /**
     * 動画撮影、音声録音の一時停止依頼リクエストハンドラー.<br/>
     * 動画撮影、音声録音を一時停止し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target 一時停止するレコーダーを識別するID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutPause(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 動画撮影、音声録音の再開依頼リクエストハンドラー.<br/>
     * 動画撮影、音声録音を再開し、その結果をレスポンスパラメータに格納する。 レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target 一時停止するレコーダーを識別するID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutResume(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 動画撮影、音声録音の停止依頼リクエストハンドラー.<br/>
     * 動画撮影、音声録音を停止し、その結果をレスポンスパラメータに格納する。 レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target 一時停止するレコーダーを識別するID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutStop(final Intent request, final Intent response, final String serviceId, 
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 動画撮影、音声録音のミュート依頼リクエストハンドラー.<br/>
     * 動画撮影、音声録音をミュートし、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target 一時停止するレコーダーを識別するID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutMuteTrack(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 動画撮影、音声録音のミュート解除リクエストハンドラー.<br/>
     * 動画撮影、音声録音をミュートを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target 一時停止するレコーダーを識別するID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutUnmuteTrack(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * オプション設定リクエストハンドラー.<br/>
     * オプションを設定し、その結果をレスポンスパラメータに格納する。 レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target レコーダーの識別ID
     * @param imageWidth 撮影時の画像の横幅
     * @param imageHeight 撮影時の画像の縦幅
     * @param previewWidth プレビュー時の画像の横幅
     * @param previewHeight プレビュー時の画像の縦幅
     * @param previewMaxFrameRate プレビューの最大フレームレート
     * @param mimeType MIMEタイプ
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOptions(final Intent request, final Intent response, final String serviceId,
            final String target, final Integer imageWidth, final Integer imageHeight,
            final Integer previewWidth, final Integer previewHeight, final Double previewMaxFrameRate,
            final String mimeType) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onphotoコールバック登録リクエストハンドラー.<br/>
     * onphotoコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onrecordingchangeコールバック登録リクエストハンドラー.<br/>
     * onrecordingchangeコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnRecordingChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ondataavailableコールバック登録リクエストハンドラー.<br/>
     * ondataavailableコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @deprecated This method is deprecated.
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnDataAvailable(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * プレビューを開始する.
     * <p>
     * プレビュー送信用のサーバを起動し、そのURIをレスポンスパラメータに格納する。<br/>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target レコーダーID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutPreview(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------

    /**
     * onphotoコールバック解除リクエストハンドラー.<br/>
     * onphotoコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onrecordingchangeコールバック解除リクエストハンドラー.<br/>
     * onPutOnRecordingChangeコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnRecordingChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ondataavailableコールバック解除リクエストハンドラー.<br/>
     * ondataavailableコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @deprecated This method is deprecated.
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnDataAvailable(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * プレビューを停止する.
     * <p>
     * プレビュー送信用のサーバを停止する。<br/>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param target レコーダーID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeletePreview(final Intent request, final Intent response, final String serviceId,
            final String target) {
        setUnsupportedError(response);
        return true;
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
