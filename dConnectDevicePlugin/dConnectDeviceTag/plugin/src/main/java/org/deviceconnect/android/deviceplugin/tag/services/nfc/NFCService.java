/*
 NFCService.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;

import org.deviceconnect.android.deviceplugin.tag.activity.NFCReaderActivity;
import org.deviceconnect.android.deviceplugin.tag.activity.NFCWriterActivity;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;
import org.deviceconnect.android.deviceplugin.tag.services.TagService;
import org.deviceconnect.android.deviceplugin.tag.services.nfc.profiles.NFCTagProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * NFCのタグを操作するためのサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class NFCService extends TagService {
    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * リクエストのカウンター.
     */
    private int mCounter;

    /**
     * 書き込み用コールバックを格納するマップ.
     */
    private Map<String, WriterCallback> mWriterCallbackMap = new HashMap<>();

    /**
     * 読み込み用コールバックを格納するマップ.
     */
    private Map<String, ReaderCallback> mReaderCallbackMap = new HashMap<>();

    /**
     * NFCの読み取り結果を通知するコールバック.
     */
    private ReaderCallback mCallback;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public NFCService(final Context context) {
        super("nfc_service_id");
        mContext = context;
        setName("NFC Service");
        setOnline(true);
        setNetworkType(NetworkType.NFC);
        addProfile(new NFCTagProfile());
    }

    /**
     * NFCにデータを書き込みます.
     *
     * @param data 書き込むデータ
     * @param callback 書き込み結果を受け取るコールバック
     */
    public void writeNFC(final Map<String, String> data, final WriterCallback callback) {
        String requestCode = createRequestCode();
        mWriterCallbackMap.put(requestCode, callback);
        startNFCWriterActivity(requestCode, data);
    }

    /**
     * NFC の読み込み通知を行う先のコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void readNFC(final ReaderCallback callback) {
        mCallback = callback;
    }

    /**
     * 一度だけ NFC の読み込みを行います.
     *
     * @param callback コールバック
     */
    public void readNFCOnce(final ReaderCallback callback) {
        String requestCode = createRequestCode();
        mReaderCallbackMap.put(requestCode, callback);
        startNFCReaderActivity(requestCode, true);
    }

    @Override
    public void onTagReaderActivityResult(final String requestCode, final int result, final TagInfo tagInfo) {
        TagController ctr = getTagController();
        if (ctr != null) {
            ctr.finishActivity();
        }

        if (requestCode != null) {
            ReaderCallback callback = mReaderCallbackMap.remove(requestCode);
            if (callback != null) {
                callback.onResult(result, tagInfo);
            }
        }

        if (mCallback != null) {
            mCallback.onResult(result, tagInfo);
        }
    }

    @Override
    public void onTagWriterActivityResult(String requestCode, int result) {
        if (requestCode != null) {
            WriterCallback cb = mWriterCallbackMap.remove(requestCode);
            if (cb != null) {
                cb.onResult(result);
            }
        }

        TagController ctr = getTagController();
        if (ctr != null) {
            ctr.finishActivity();
        }
    }

    /**
     * リクエストコードを作成します.
     *
     * @return リクエストコード
     */
    private String createRequestCode() {
        return "nfc_write_" + (mCounter++);
    }

    /**
     * {@link NFCReaderActivity} を起動します.
     *
     * @param requestCode リクエストコード
     * @param once 一度フラグ
     */
    private void startNFCReaderActivity(final String requestCode, final boolean once) {
        Intent intent = new Intent();
        intent.setClass(mContext, NFCReaderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(TagConstants.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(TagConstants.EXTRA_ONCE, once);
        mContext.startActivity(intent);
    }

    /**
     * {@link NFCWriterActivity} を起動します.
     *
     * @param requestCode リクエストコード
     * @param data 書き込むデータ
     */
    private void startNFCWriterActivity(final String requestCode, final Map<String, String> data) {
        Intent intent = new Intent();
        intent.setClass(mContext, NFCWriterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(TagConstants.EXTRA_REQUEST_CODE, requestCode);
        for (String key : data.keySet()) {
            intent.putExtra(key, data.get(key));
        }
        mContext.startActivity(intent);
    }

    /**
     * NFC の ID を文字列にして取得します.
     *
     * @param tag NFC タグ
     * @return 文字列に変換されたタグID
     */
    public static String tagToString(final Tag tag) {
        return bytesToString(tag.getId());
    }

    /**
     * バイト配列を文字列に変換します.
     *
     * @param bytes バイト配列
     * @return バイト配列を文字列
     */
    private static String bytesToString(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte bt : bytes) {
            sb.append(String.format("%02x", bt));
        }
        return sb.toString();
    }
}
