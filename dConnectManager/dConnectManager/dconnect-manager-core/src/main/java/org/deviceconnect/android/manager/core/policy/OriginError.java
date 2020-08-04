package org.deviceconnect.android.manager.core.policy;

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
     * 指定されたオリジンが許可されていない(許可リストに含まれていない)ことを示す定数.
     */
    NOT_ALLOWED
}
