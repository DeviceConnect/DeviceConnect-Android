/*
 DConnectFilesProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.request.DConnectRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.DConnectProfileConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ファイルにアクセスするためのプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class DConnectFilesProfile extends DConnectProfile {
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** プロファイル名. */
    public static final String PROFILE_NAME = "files";

    /** 属性: {@value}. */
    public static final String PARAM_MIME_TYPE = "mimetype";

    /** 属性: {@value}. */
    public static final String PARAM_DATA = "data";

    /** 拡張子とMimetypeを持つマップ. */
    private final Map<String, String> mExtMap = new HashMap<>();

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public DConnectFilesProfile(final Context context) {
        loadMimeType(context);
        addApi(mGetRequest);
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    private final DConnectApi mGetRequest = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            DConnectRequest req = new DConnectRequest() {
                @Override
                public boolean hasRequestCode(final int requestCode) {
                    return false;
                }
                @Override
                public void run() {
                    String uri = request.getStringExtra(DConnectProfileConstants.PARAM_URI);
                    byte[] buf = getContentData(uri);
                    if (buf == null) {
                        MessageUtils.setInvalidRequestParameterError(response);
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                        response.putExtra(PARAM_DATA, buf);
                        response.putExtra(PARAM_MIME_TYPE, getExtension(uri));
                    }
                    sendResponse(response);
                }
            };
            req.setContext(getContext());
            req.setRequest(request);
            ((DConnectMessageService) getContext()).addRequest(req);

            // 各デバイスプラグインに送信する場合にはfalseを返却、
            // dConnectManagerで止める場合にはtrueを返却する
            // ここでは、各デバイスには渡さないのでtrueを返却する。
            return true;
        }
    };

    /**
     * mimetype一覧を読み込む.
     * @param context コンテキスト
     */
    private void loadMimeType(final Context context) {
        BufferedReader br = null;
        try {
            AssetManager assetManager = context.getResources().getAssets();
            br = new BufferedReader(new InputStreamReader(assetManager.open("mimetype.csv")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(",");
                if (tmp.length == 2) {
                    mExtMap.put(tmp[0].trim(), tmp[1].trim());
                }
            }
        } catch (IOException e) {
            mLogger.warning("Exception in DConnectFilesProfile.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    mLogger.warning("Exception in DConnectFilesProfile.");
                }
            }
        }
    }

    /**
     * 拡張子からMime Typeを判別する.
     * @param path ファイルパス
     * @return MimeType
     */
    private String getExtension(final String path) {
        String mimeType = "application/octet-stream";
        int idx = path.lastIndexOf(".");
        if (idx > 0) {
            String ext = path.substring(idx + 1);
            if (mExtMap.containsKey(ext)) {
                return mExtMap.get(ext);
            }
        }
        return mimeType;
    }
}
