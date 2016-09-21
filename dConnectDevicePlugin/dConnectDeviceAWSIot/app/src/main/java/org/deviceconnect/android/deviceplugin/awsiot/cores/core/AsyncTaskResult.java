package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

/**
 * AsyncTaskの結果
 * @param <T> 型
 */
public class AsyncTaskResult<T> {
    /** 結果 */
    private T result;
    /** エラー */
    private Exception error;

    /**
     * 結果を取得.
     * @return Result
     */
    public T getResult() {
        return result;
    }

    /**
     * エラーを取得.
     * @return Error
     */
    public Exception getError() {
        return error;
    }

    /**
     * 結果を指定して初期化.
     * @param result 結果
     */
    public AsyncTaskResult(T result) {
        super();
        this.result = result;
    }

    /**
     * 結果を指定して初期化.
     * @param error エラー
     */
    public AsyncTaskResult(Exception error) {
        super();
        this.error = error;
    }
}