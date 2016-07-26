package org.deviceconnect.android.manager.policy;

import android.content.Context;

public class OriginValidator {
    /** fileスキームのオリジン. */
    private static final String ORIGIN_FILE = "file://";
    /** 常に許可するオリジン一覧. */
    private static final String[] IGNORED_ORIGINS = {ORIGIN_FILE};

    private boolean mRequireOrigin;
    private boolean mWhiteListEnabled;

    /** ホワイトリスト管理クラス. */
    private Whitelist mWhiteList;

    public OriginValidator(Context context, boolean requireOrigin, boolean whiteListEnabled) {
        mRequireOrigin = requireOrigin;
        mWhiteListEnabled = whiteListEnabled;
        mWhiteList = new Whitelist(context);
    }

    public OriginError checkOrigin(final String originParam)  {
        if (!mRequireOrigin) {
            return OriginError.NONE;
        }
        if (originParam == null) {
            return OriginError.NOT_SPECIFIED;
        }
        String[] origins = originParam.split(" ");
        if (origins.length != 1) {
            return OriginError.NOT_UNIQUE;
        }
        if (!allowsOrigin(originParam)) {
            return OriginError.NOT_ALLOWED;
        }
        return OriginError.NONE;
    }

    private boolean allowsOrigin(final String originExp) {
        if (originExp == null) {
            // NOTE: クライアント作成のためにオリジンが必要のため、
            // ホワイトリストが無効の場合でもオリジン指定のない場合はリクエストを許可しない.
            return false;
        }
        for (int i = 0; i < IGNORED_ORIGINS.length; i++) {
            if (originExp.equals(IGNORED_ORIGINS[i])) {
                return true;
            }
        }
        if (!mWhiteListEnabled) {
            return true;
        }
        return mWhiteList.allows(OriginParser.parse(originExp));
    }
}
