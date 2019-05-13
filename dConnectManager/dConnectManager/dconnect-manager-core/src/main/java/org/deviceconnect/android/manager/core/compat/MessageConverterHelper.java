package org.deviceconnect.android.manager.core.compat;

import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;

/**
 * 旧 Device Connect プロファイルとの互換性を保つための変換クラス.
 */
public class MessageConverterHelper {

    /**
     * リクエストのパスを変換するクラス群.
     */
    private MessageConverter[] mRequestConverters;

    /**
     * レスポンスのパスを変換するクラス群.
     */
    private MessageConverter[] mResponseConverters;

    /**
     * コンストラクタ.
     * @param pluginMgr プラグイン管理クラス
     */
    public MessageConverterHelper(DevicePluginManager pluginMgr) {
        mRequestConverters = new MessageConverter[]{
                new CompatibleRequestConverter(pluginMgr)
        };

        mResponseConverters = new MessageConverter[]{
                new ServiceDiscoveryConverter(),
                new ServiceInformationConverter()
        };
    }

    /**
     * リクエストを変換します.
     *
     * @param request 変換するリクエスト
     */
    public void convertRequestMessage(Intent request) {
        for (MessageConverter converter : mRequestConverters) {
            converter.convert(request);
        }
    }

    /**
     * レスポンスを変換します.
     *
     * @param response 変換するレスポンス
     */
    public void convertResponseMessage(Intent response) {
        for (MessageConverter converter : mResponseConverters) {
            converter.convert(response);
        }
    }
}
