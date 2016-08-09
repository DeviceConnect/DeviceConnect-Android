/*
 OriginValidator.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

import android.content.Context;
import android.content.Intent;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;

public final class OriginValidator {
    /** fileスキームのオリジン. */
    private static final String ORIGIN_FILE = "file://";

    /** 常に許可するオリジン一覧. */
    private static final String[] IGNORED_ORIGINS = {ORIGIN_FILE};

    /** ホワイトリスト管理クラス. */
    private Whitelist mWhiteList;

    /** オリジン要求フラグ. */
    private boolean mRequireOrigin;

    /** Originブロック機能の使用フラグ. */
    private boolean mBlockingOrigin;

    public OriginValidator(final Context context, final boolean requireOrigin, final boolean blockingOrigin) {
        mWhiteList = new Whitelist(context);
        mRequireOrigin = requireOrigin;
        mBlockingOrigin = blockingOrigin;
    }

    /**
     * オリジンの正当性をチェックする.
     * <p>
     * 設定画面上でオリジン要求フラグがOFFにされた場合は即座に「エラー無し」を返す。
     * </p>
     * @param request 送信元のリクエスト
     * @return チェック処理の結果
     */
    public OriginError checkOrigin(final Intent request) {
        if (!mRequireOrigin) {
            return OriginError.NONE;
        }
        String originParam = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (originParam == null) {
            return OriginError.NOT_SPECIFIED;
        }
        String[] origins = originParam.split(" ");
        if (origins.length != 1) {
            return OriginError.NOT_UNIQUE;
        }
        if (!allowsOrigin(request)) {
            return OriginError.NOT_ALLOWED;
        }
        return OriginError.NONE;
    }

    /**
     * 指定されたリクエストのオリジンが許可されるかどうかを返す.
     *
     * @param request 受信したリクエスト
     * @return 指定されたリクエストのオリジンが許可される場合は<code>true</code>、
     *      そうでない場合は<code>false</code>
     */
    private boolean allowsOrigin(final Intent request) {
        String originExp = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (originExp == null) {
            // NOTE: クライアント作成のためにオリジンが必要のため、
            // ホワイトリストが無効の場合でもオリジン指定のない場合はリクエストを許可しない.
            return false;
        }
        for (String origin : IGNORED_ORIGINS) {
            if (originExp.equals(origin)) {
                return true;
            }
        }
        return !mBlockingOrigin || mWhiteList.allows(OriginParser.parse(originExp));
    }

    /**
     * Originヘッダ解析時に検出したエラー.
     */
    public enum OriginError {
        /**
         * エラー無しを示す定数.
         */
        NONE,

        /**
         * オリジンが指定されていないことを示す定数.
         */
        NOT_SPECIFIED,

        /**
         * 2つ以上のオリジンが指定されていたことを示す定数.
         */
        NOT_UNIQUE,

        /**
         * 指定されたオリジンが許可されていない(ホワイトリストに含まれていない)ことを示す定数.
         */
        NOT_ALLOWED
    }
}
