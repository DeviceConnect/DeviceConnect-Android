/*
 TagMessageService.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag;

import android.content.pm.PackageManager;

import org.deviceconnect.android.deviceplugin.tag.profiles.TagSystemProfile;
import org.deviceconnect.android.deviceplugin.tag.services.nfc.NFCService;
import org.deviceconnect.android.deviceplugin.tag.services.qr.QRService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.provider.FileProvider;

/**
 * Tag を操作するためのサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class TagMessageService extends DConnectMessageService {
    /**
     * QRコード読み込み用クラス.
     */
    private QRService mQRService;

    /**
     * NFC読み込み用クラス.
     */
    private NFCService mNFCService;

    /**
     * ファイル管理クラス.
     */
    private FileManager mFileMgr;

    /**
     * プラグインの設定.
     */
    private TagSetting mSetting;

    @Override
    public void onCreate() {
        super.onCreate();

        mSetting = new TagSetting(this);
        mSetting.registerOnChangeListener(this::setUseLocalOAuth);

        mFileMgr = new FileManager(this, FileProvider.class.getName());

        mQRService = new QRService(this);
        getServiceProvider().addService(mQRService);

        // NFC がサポートされていない場合には登録しない
        if (checkNFCHardware()) {
            mNFCService = new NFCService(this);
            getServiceProvider().addService(mNFCService);
        }

        setUseLocalOAuth(mSetting.isUseOAuth());
    }

    @Override
    public void onDestroy() {
        if (mSetting != null) {
            mSetting.unregisterOnChangeListener();
        }
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new TagSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        EventManager.INSTANCE.removeAll();
    }

    @Override
    protected void onManagerTerminated() {
        EventManager.INSTANCE.removeAll();
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        if (origin != null) {
            EventManager.INSTANCE.removeEvents(origin);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        EventManager.INSTANCE.removeAll();
    }

    /**
     * NFCのサポート状況を取得します.
     *
     * @return サポートしている場合にはtrue、それ以外はfalse
     */
    private boolean checkNFCHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    /**
     * ファイル管理クラスを取得します.
     *
     * @return FileManager
     */
    public FileManager getFileManager() {
        return mFileMgr;
    }

    /**
     * NFC読み込み用クラスを取得します.
     *
     * @return NFC読み込み用クラス
     */
    public NFCService getNFCService() {
        return mNFCService;
    }

    /**
     * QR読み込み用クラスを取得します.
     *
     * @return QR読み込み用クラス
     */
    public QRService getQRService() {
        return mQRService;
    }
}