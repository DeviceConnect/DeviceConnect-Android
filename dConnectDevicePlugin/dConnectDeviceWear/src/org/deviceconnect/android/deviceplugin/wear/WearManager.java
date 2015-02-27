package org.deviceconnect.android.deviceplugin.wear;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Android Wearを管理するクラス.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearManager implements ConnectionCallbacks, OnConnectionFailedListener {
    /**
     * PNGのクオリティを定義する.
     */
    private static final int PNG_QUALITY = 100;

    /**
     * Android Wearの画像制限は100KBまで.
     */
    private static final int LIMIT_IMAGE_SIZE = 100 * 1024 * 1024;

    /**
     * Google Play Service.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * スレッド管理用クラス.
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * コンストラクタ.
     * @param context このクラスが属するコンテキスト
     */
    public WearManager(final Context context) {
        mContext = context;
        init();
    }

    /**
     * このクラスを初期化する.
     */
    public void init() {
        if (mGoogleApiClient != null) {
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * 後始末処理を行う.
     */
    public void destory() {
        mExecutorService.shutdown();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * GoogleApiClientのインスタンスを取得する.
     * @return GoogleApiClientのインスタンス
     */
    public GoogleApiClient getApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Wear nodeを取得.
     * 
     * @return WearNode
     */
    public Collection<String> getNodes() {
        List<String> results = new ArrayList<String>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
    }


    @Override
    public void onConnected(final Bundle bundle) {
    }


    @Override
    public void onConnectionSuspended(final int state) {
    }

    /**
     * BitmapからPNGのAssetを作成する.
     * @param bitmap Assetの元画像
     * @return Asset, エラーが発生した場合にはnull
     */
    private Asset createAssetFromBitmap(final Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        if (bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, byteStream)) {
            if (byteStream.size() >= LIMIT_IMAGE_SIZE) {
                return null;
            }
            return Asset.createFromBytes(byteStream.toByteArray());
        } else {
            return null;
        }
    }

    /**
     * PutDataRequestを作成する.
     * @param bitmap requestに格納する画像
     * @return PutDataRequestのインスタンス
     */
    private PutDataRequest createPutDataRequest(final Bitmap bitmap) {
        Asset asset = createAssetFromBitmap(bitmap);
        if (asset == null) {
            return null;
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
        dataMap.getDataMap().putAsset("profileImage", asset);
        PutDataRequest request = dataMap.asPutDataRequest();
        return request;
    }
    public boolean sendImage(final Bitmap bitmap) {
//        PutDataRequest request = createPutDataRequest(bitmap);
//        if (request == null) {
//            return false;
//        }
//        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
//        return true;
        PutDataRequest request = createPutDataRequest(bitmap);
        if (request == null) {
            return false;
        }
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
        return true;
    }
}
