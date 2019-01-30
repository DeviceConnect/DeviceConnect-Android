/*
 TagService.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services;

import org.deviceconnect.android.service.DConnectService;

import java.lang.ref.WeakReference;

/**
 * タグを管理するサービスの基底クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class TagService extends DConnectService implements TagServiceInterface {
    /**
     * コントロール用インターフェース.
     */
    private WeakReference<TagController> mQRController;

    /**
     * コンストラクタ.
     * @param serviceId サービスID
     */
    public TagService(final String serviceId) {
        super(serviceId);
    }

    /**
     * {@link TagController} のインスタンスを取得します.
     * <p>
     * インスタンスが設定されていない場合は null を返却します。
     * </p>
     * @return {@link TagController} のインスタンス
     */
    protected synchronized TagController getTagController() {
        return mQRController == null ? null : mQRController.get();
    }

    @Override
    public synchronized void setTagController(final TagController controller) {
        if (controller == null) {
            mQRController = null;
        } else {
            mQRController = new WeakReference<>(controller);
        }
    }

    @Override
    public void onTagReaderActivityResult(final String requestCode, final int result, final TagInfo tagInfo) {
        // do nothing.
    }

    @Override
    public void onTagWriterActivityResult(final String requestCode, final int result) {
        // do nothing.
    }
}
