/*
 AsyncTaskResult.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.core;

/**
 * AsyncTaskの結果
 * @param <T> 型
 */
class AsyncTaskResult<T> {
    /** 結果 */
    private T result;
    /** エラー */
    private Exception error;

    /**
     * 結果を取得.
     * @return Result
     */
    T getResult() {
        return result;
    }

    /**
     * エラーを取得.
     * @return Error
     */
    Exception getError() {
        return error;
    }

    /**
     * 結果を指定して初期化.
     * @param result 結果
     */
    AsyncTaskResult(T result) {
        super();
        this.result = result;
    }

    /**
     * 結果を指定して初期化.
     * @param error エラー
     */
    AsyncTaskResult(Exception error) {
        super();
        this.error = error;
    }
}