package org.deviceconnect.android.libmedia.streaming.camera2;

public class Camera2WrapperException extends RuntimeException {
    /**
     * パーミッションに許可が無い場合のエラーコード.
     */
    public static final int ERROR_CODE_NO_PERMISSION = -1;

    /**
     * セッションの作成に失敗した場合のエラーコード.
     */
    public static final int ERROR_CODE_FAILED_CREATE_SESSION = -2;

    /**
     * ステートの変更に失敗した場合のエラーコード.
     */
    public static final int ERROR_CODE_FAILED_CHANGE_STATE = -3;

    /**
     * エラーコード.
     */
    private int mErrorCode;

    public Camera2WrapperException(int errorCode) {
        mErrorCode = errorCode;
    }

    public Camera2WrapperException(String message, int errorCode) {
        super(message);
        mErrorCode = errorCode;
    }

    public Camera2WrapperException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        mErrorCode = errorCode;
    }

    public Camera2WrapperException(Throwable cause, int errorCode) {
        super(cause);
        mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}
