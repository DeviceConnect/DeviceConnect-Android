/*
 QRService.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.qr;

import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;
import org.deviceconnect.android.deviceplugin.tag.services.TagService;
import org.deviceconnect.android.deviceplugin.tag.activity.QRReaderActivity;
import org.deviceconnect.android.deviceplugin.tag.services.qr.profiles.QRTagProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * QRコードのタグを操作するためのサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class QRService extends TagService {
    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 読み込み用コールバックを格納するマップ.
     */
    private Map<String, ReaderCallback> mReaderCallbackMap = new HashMap<>();

    /**
     * カウンター.
     */
    private int mCounter;

    /**
     * コールバック.
     */
    private ReaderCallback mCallback;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public QRService(final Context context) {
        super("qr_service_id");
        mContext = context;
        setName("QRCode Service");
        setOnline(true);
        setNetworkType("QR");
        addProfile(new QRTagProfile());
    }

    /**
     * QRコードを読み込みます.
     * <p>
     * このメソッドを呼び出すと Activity が起動して QR コードを読み込みを開始します。
     * 読み込んだ結果はコールバックに返却されます。
     * </p>
     * @param callback コールバック
     */
    public void readQRCode(final ReaderCallback callback) {
        String requestCode = createRequestCode();
        mReaderCallbackMap.put(requestCode, callback);
        startQRReaderActivity(requestCode, true);
    }

    /**
     * QRコードの読み込みを開始します.
     *
     * @param callback コールバック
     */
    public void startReadQRCode(final ReaderCallback callback) {
        String requestCode = createRequestCode();
        mCallback = callback;
        startQRReaderActivity(requestCode, false);
    }

    /**
     * QRコードの読み込みを停止します.
     */
    public void stopReadQRCode() {
        mCallback = null;
        stopQRReaderActivity();
    }

    /**
     * リクエストコードを作成します.
     *
     * @return リクエストコード
     */
    private String createRequestCode() {
        return "qr_code_" + (mCounter++);
    }

    /**
     * {@link QRReaderActivity} を起動します.
     *
     * @param requestCode リクエストコード
     * @param once
     */
    private void startQRReaderActivity(final String requestCode, final boolean once) {
        Intent intent = new Intent();
        intent.setClass(mContext, QRReaderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(TagConstants.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(TagConstants.EXTRA_ONCE, once);
        mContext.startActivity(intent);
    }

    /**
     * {@link QRReaderActivity} を停止します.
     */
    private void stopQRReaderActivity() {
        TagController ctr = getTagController();
        if (ctr != null) {
            ctr.finishActivity();
        }
    }

    @Override
    public void onTagReaderActivityResult(final String requestCode, final int result, final TagInfo tagInfo) {
        if (requestCode != null) {
            ReaderCallback cb = mReaderCallbackMap.remove(requestCode);
            if (cb != null) {
                cb.onResult(result, tagInfo);
            }
        }

        if (mCallback != null) {
            mCallback.onResult(result, tagInfo);
        } else if (mReaderCallbackMap.isEmpty()) {
            stopQRReaderActivity();
        }
    }
}
