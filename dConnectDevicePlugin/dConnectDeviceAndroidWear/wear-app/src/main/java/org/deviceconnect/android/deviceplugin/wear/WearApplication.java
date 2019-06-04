/*
DataLayerListenerService.java
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * このアプリで共有するGoogleApiClientを保持するアプリケーションクラス.
 */
public class WearApplication extends Application {

    /**
     * WearのID.
     */
    private String mSelfId;
    /**
     * スレッド管理用クラス.
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        destroy();
    }

    /**
     * WearのIDを設定する.
     * @param self WearのID
     */
    public synchronized void setSelfId(final String self) {
        mSelfId = self;
    }

    /**
     * WearのIDを返す.
     * @return WearのID
     */
    public synchronized String getSelfId() {
        return mSelfId;
    }

    /**
     * 後始末を行う.
     */
    public synchronized void destroy() {
        mExecutorService.shutdown();
    }

    /**
     * Phone側にメッセージを送る.
     * @param destinationId phone側のID
     * @param path メッセージのパス
     * @param data メッセージのデータ
     */
    public void sendMessage(final String destinationId, final String path, final String data) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(getApplicationContext())
                                .sendMessage(destinationId, path, data.getBytes());
                sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        if (BuildConfig.DEBUG) {
                            Log.d("WEAR", "Sent result:" + integer);
                        }
                    }
                });

                sendMessageTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (BuildConfig.DEBUG) {
                            Log.e("WEAR", "Sent result:" + e.getLocalizedMessage());
                        }
                    }
                });
            }
        });
    }
}
