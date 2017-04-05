/*
 MediaPlayerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.MediaPlayerProfileConstants;

/**
 * MediaPlayer プロファイル.
 * 
 * <p>
 * スマートデバイス上のメディアの再生状態の変更要求を通知するAPI.<br>
 * メディア操作を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class MediaPlayerProfile extends DConnectProfile implements MediaPlayerProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    // Setter

    /**
     * レスポンスにメディア数を設定する.
     * 
     * @param response レスポンスデータ
     * @param count メディア数
     */
    public static final void setCount(final Intent response, final int count) {
        response.putExtra(PARAM_COUNT, count);
    }

    /**
     * レスポンスにメディアを設定する.
     * 
     * @param response レスポンスデータ
     * @param media メディア
     */
    public static final void setMedia(final Intent response, final Bundle[] media) {
        response.putExtra(PARAM_MEDIA, media);
    }

    /**
     * レスポンスにメディアIDを設定する.
     * 
     * @param response レスポンスデータ
     * @param mediaId メディアID
     */
    public static final void setMediaId(final Intent response, final String mediaId) {
        response.putExtra(PARAM_MEDIA_ID, mediaId);
    }

    /**
     * 再生情報にメディアIDを設定する.
     * 
     * @param playStatus 再生情報
     * @param mediaId メディアID
     */
    public static final void setMediaId(final Bundle playStatus, final String mediaId) {
        playStatus.putString(PARAM_MEDIA_ID, mediaId);
    }

    /**
     * メッセージにメディアプレイヤー情報を設定する.
     * 
     * @param message メッセージ
     * @param mediaPlayer メディアプレイヤー
     */
    public static final void setMediaPlayer(final Intent message, final Bundle mediaPlayer) {
        message.putExtra(PARAM_MEDIA_PLAYER, mediaPlayer);
    }

    /**
     * レスポンスにコンテンツの再生状態を設定する.
     * 
     * @param response レスポンスデータ
     * @param playStatus コンテンツの再生状態
     */
    public static final void setStatus(final Intent response, final PlayStatus playStatus) {
        response.putExtra(PARAM_STATUS, playStatus.getValue());
    }

    /**
     * レスポンスに再生状態を設定する.
     * 
     * @param response レスポンスデータ
     * @param status 再生状態
     */
    public static final void setStatus(final Intent response, final Status status) {
        response.putExtra(PARAM_STATUS, status.getValue());
    }

    /**
     * メディア情報に再生状態を設定する.
     * 
     * @param media メディア情報
     * @param status 再生状態
     */
    public static final void setStatus(final Bundle media, final Status status) {
        setStatus(media, status.getValue());
    }

    /**
     * メディア情報に再生状態を設定する.
     * 
     * @param media メディア情報
     * @param status 再生状態
     */
    public static final void setStatus(final Bundle media, final String status) {
        media.putString(PARAM_STATUS, status);
    }

    /**
     * レスポンスに再生位置を設定する.
     * 
     * @param response レスポンスデータ
     * @param pos 再生位置
     */
    public static final void setPos(final Intent response, final int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos is negative.");
        }
        response.putExtra(PARAM_POS, pos);
    }

    /**
     * 再生情報に再生位置を設定する.
     * 
     * @param playStatus 再生情報
     * @param pos 再生位置
     */
    public static final void setPos(final Bundle playStatus, final int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos is negative.");
        }
        playStatus.putInt(PARAM_POS, pos);
    }

    /**
     * レスポンスにMIMEタイプを設定する.
     * 
     * @param response レスポンスデータ
     * @param mimeType MIMEタイプ
     */
    public static final void setMIMEType(final Intent response, final String mimeType) {
        response.putExtra(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * メディア情報にMIMEタイプを設定する.
     * 
     * @param media メディア情報
     * @param mimeType MIMEタイプ
     */
    public static final void setMIMEType(final Bundle media, final String mimeType) {
        media.putString(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * レスポンスにタイトルを設定する.
     * 
     * @param response レスポンスデータ
     * @param title タイトル
     */
    public static final void setTitle(final Intent response, final String title) {
        response.putExtra(PARAM_TITLE, title);
    }

    /**
     * メディア情報にタイトルを設定する.
     * 
     * @param media メディア情報
     * @param title タイトル
     */
    public static final void setTitle(final Bundle media, final String title) {
        media.putString(PARAM_TITLE, title);
    }

    /**
     * レスポンスにタイプ名を設定する.
     * 
     * @param response レスポンスデータ
     * @param type タイプ名
     */
    public static final void setType(final Intent response, final String type) {
        response.putExtra(PARAM_TYPE, type);
    }

    /**
     * メディア情報にタイプ名を設定する.
     * 
     * @param media メディア情報
     * @param type タイプ名
     */
    public static final void setType(final Bundle media, final String type) {
        media.putString(PARAM_TYPE, type);
    }

    /**
     * レスポンスに言語を設定する.
     * 
     * @param response レスポンスデータ
     * @param language 言語
     */
    public static final void setLanguage(final Intent response, final String language) {
        response.putExtra(PARAM_LANGUAGE, language);
    }

    /**
     * メディア情報に言語を設定する.
     * 
     * @param media メディア情報
     * @param language 言語
     */
    public static final void setLanguage(final Bundle media, final String language) {
        media.putString(PARAM_LANGUAGE, language);
    }

    /**
     * レスポンスに画像へのURIを設定する.
     * 
     * @param response レスポンスデータ
     * @param uri 画像へのURI
     */
    public static final void setImageUri(final Intent response, final String uri) {
        response.putExtra(PARAM_IMAGE_URI, uri);
    }

    /**
     * メディア情報に画像へのURIを設定する.
     * 
     * @param media メディア情報
     * @param uri 画像へのURI
     */
    public static final void setImageUri(final Bundle media, final String uri) {
        media.putString(PARAM_IMAGE_URI, uri);
    }

    /**
     * レスポンスに説明文を設定する.
     * 
     * @param response レスポンスデータ
     * @param description 説明文
     */
    public static final void setDescription(final Intent response, final String description) {
        response.putExtra(PARAM_DESCRIPTION, description);
    }

    /**
     * メディア情報に説明文を設定する.
     * 
     * @param media メディア情報
     * @param description 説明文
     */
    public static final void setDescription(final Bundle media, final String description) {
        media.putString(PARAM_DESCRIPTION, description);
    }

    /**
     * レスポンスに曲の長さを設定する.
     * 
     * @param response レスポンスデータ
     * @param duration 曲の長さ
     */
    public static final void setDuration(final Intent response, final int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration is invalid.");
        }
        response.putExtra(PARAM_DURATION, duration);
    }

    /**
     * メディア情報に曲の長さを設定する.
     * 
     * @param media メディア情報
     * @param duration 曲の長さ
     */
    public static final void setDuration(final Bundle media, final int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration is invalid.");
        }
        media.putInt(PARAM_DURATION, duration);
    }

    /**
     * レスポンスに制作者情報一覧を設定する.
     * 
     * @param response レスポンスデータ
     * @param creators 制作者情報一覧
     */
    public static final void setCreators(final Intent response, final Bundle[] creators) {
        response.putExtra(PARAM_CREATORS, creators);
    }

    /**
     * メディア情報に制作者情報一覧を設定する.
     * 
     * @param media メディア情報
     * @param creators 制作者情報一覧
     */
    public static final void setCreators(final Bundle media, final Bundle[] creators) {
        media.putParcelableArray(PARAM_CREATORS, creators);
    }

    /**
     * 制作者情報に制作者名を設定する.
     * 
     * @param bundle 制作者情報
     * @param creator 制作者名
     */
    public static final void setCreator(final Bundle bundle, final String creator) {
        bundle.putString(PARAM_CREATOR, creator);
    }

    /**
     * 制作者情報に役割を設定する.
     * 
     * @param bundle 制作者情報
     * @param role 役割
     */
    public static final void setRole(final Bundle bundle, final String role) {
        bundle.putString(PARAM_ROLE, role);
    }

    /**
     * レスポンスデータにキーワード一覧を設定する.
     * 
     * @param response レスポンスデータ
     * @param keywords キーワード一覧 
     */
    public static final void setKeywords(final Intent response, final String[] keywords) {
        response.putExtra(PARAM_KEYWORDS, keywords);
    }

    /**
     * メディア情報にキーワード一覧を設定する.
     * 
     * @param media メディア情報
     * @param keywords キーワード一覧 
     */
    public static final void setKeywords(final Bundle media, final String[] keywords) {
        media.putStringArray(PARAM_KEYWORDS, keywords);
    }

    /**
     * レスポンスデータにジャンル一覧を設定する.
     * 
     * @param response レスポンスデータ
     * @param genres ジャンル一覧
     */
    public static final void setGenres(final Intent response, final String[] genres) {
        response.putExtra(PARAM_GENRES, genres);
    }

    /**
     * メディア情報にジャンル一覧を設定する.
     * 
     * @param media メディア情報
     * @param genres ジャンル一覧
     */
    public static final void setGenres(final Bundle media, final String[] genres) {
        media.putStringArray(PARAM_GENRES, genres);
    }

    /**
     * レスポンスにボリュームを設定する.
     * 
     * @param response レスポンスデータ
     * @param volume ボリューム(0.0 〜 1.0)
     */
    public static final void setVolume(final Intent response, final double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("volume is invalid. volume=" + volume);
        }
        response.putExtra(PARAM_VOLUME, volume);
    }

    /**
     * メディアプレイヤー情報にボリュームを設定する.
     * 
     * @param mediaPlayer メディアプレイヤー
     * @param volume ボリューム(0.0 〜 1.0)
     */
    public static final void setVolume(final Bundle mediaPlayer, final double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("volume is invalid. volume=" + volume);
        }
        mediaPlayer.putDouble(PARAM_VOLUME, volume);
    }

    /**
     * レスポンスにボリュームを設定する.
     * 
     * @param response レスポンスデータ
     * @param mute ミュート有りはtrue、ミュート無しはfalse
     */
    public static final void setMute(final Intent response, final boolean mute) {
        response.putExtra(PARAM_MUTE, mute);
    }

    // Getter

    /**
     * リクエストからstatusを取得する.
     * <p>
     * 以下の属性以外の値が設定されていた場合にもnullを返却する。
     * <ul>
     * <li>play</li>
     * <li>stop</li>
     * <li>resume</li>
     * </ul>
     * </p>
     * @param request リクエストデータ
     * @return status。無い場合はnullを返す。
     */
    public static final PlayStatus getPlayStatus(final Intent request) {
        String value = request.getStringExtra(PARAM_STATUS);
        return PlayStatus.getInstance(value);
    }

    /**
     * リクエストからメディアIDを取得する.
     * 
     * @param request リクエストデータ
     * @return メディアID。無い場合はnullを返す。
     */
    public static final String getMediaId(final Intent request) {
        return request.getStringExtra(PARAM_MEDIA_ID);
    }

    /**
     * リクエストから再生位置を取得する.
     * 
     * @param request リクエストデータ
     * @return 再生位置。無い場合は0を返す。
     */
    public static Integer getPos(final Intent request) {
        return parseInteger(request, PARAM_POS);
    }

    /**
     * リクエストから再生状態を取得する.
     * 
     * @param request リクエストデータ
     * @return 再生状態。無い場合はnullを返す。
     */
    public static final Status getStatus(final Intent request) {
        String statusStr = request.getStringExtra(PARAM_STATUS);
        return Status.getInstance(statusStr);
    }

    /**
     * リクエストからボリュームを取得する.
     * 
     * @param request リクエストデータ
     * @return ボリューム。無い場合は-1を返す。
     */
    public static Double getVolume(final Intent request) {
        return parseDouble(request, PARAM_VOLUME);
    }

    /**
     * リクエストからクエリーを取得する.
     * 
     * 
     * @param request リクエストデータ
     * @return クエリー。無い場合はnullを返す。
     */
    public static final String getQuery(final Intent request) {
        String query = request.getStringExtra(PARAM_QUERY);
        return query;
    }

    /**
     * リクエストからマイムタイプを取得する.
     * @param request リクエストデータ
     * @return マイムタイプ。無い場合はnullを返す。
     */
    public static final String getMIMEType(final Intent request) {
        String mimeType = request.getStringExtra(PARAM_MIME_TYPE);
        return mimeType;
    }

    /**
     * リクエストからオーダーを取得する.
     * 
     * @param request リクエストデータ
     * @return オーダー。無い場合はnullを返す。
     */
    public static final String[] getOrder(final Intent request) {
        String order = request.getStringExtra(PARAM_ORDER);
        if (order == null) {
            return null;
        }
        return order.split(",");
    }

    /**
     * リクエストからオフセットを取得する.
     * 
     * @param request リクエストデータ
     * @return オフセット。無い場合には0を返す。
     */
    public static Integer getOffset(final Intent request) {
        return parseInteger(request, PARAM_OFFSET);
    }

    /**
     * リクエストからリミットを取得する.
     * 
     * @param request リクエストデータ
     * @return リミット。無い場合には0を返す。
     */
    public static Integer getLimit(final Intent request) {
        return parseInteger(request, PARAM_LIMIT);
    }
}
