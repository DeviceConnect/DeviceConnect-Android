/*
 BindServiceActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;

import org.deviceconnect.android.deviceplugin.tag.TagMessageService;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;
import org.deviceconnect.android.deviceplugin.tag.services.TagService;
import org.deviceconnect.android.deviceplugin.tag.services.TagServiceInterface;

/**
 * {@link TagMessageService} とバインドを行う Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BindServiceActivity extends AppCompatActivity implements TagServiceInterface.TagController {
    /**
     * バインドした {@link TagMessageService} のインスタンス.
     * <p>
     * バインドされていない場合は null.
     * </p>
     */
    private TagMessageService mBoundService;

    /**
     * バインド状態.
     * <p>
     * バインドされている場合はtrue、それ以外はfalse
     * </p>
     */
    private boolean mIsBound;

    /**
     * レスポンス返却フラグ.
     */
    private boolean mReturnedResponse;

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }

    @Override
    protected void onPause() {
        TagService recorder = getTagService();
        if (recorder != null) {
            recorder.setTagController(null);
        }
        unbindService();
        super.onPause();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    /**
     * {@link TagMessageService} にバインドされた時に呼び出します.
     */
    protected void onServiceConnected() {
    }

    /**
     * {@link TagMessageService} からアンバインドされた時に呼び出します.
     */
    protected void onServiceDisconnected() {
    }

    /**
     * {@link TagService} のインスタンスを取得します.
     * <p>
     * バインドされていない場合には null を返却します。
     * </p>
     * @return {@link TagService} のインスタンス
     */
    protected abstract TagService getTagService();

    /**
     * バインドしている {@link TagMessageService} のインスタンスを取得します.
     * <p>
     * バインドされていない場合には null を返却します。
     * </p>
     * @return {@link TagMessageService} のインスタンス
     */
    protected TagMessageService getBoundService() {
        return mBoundService;
    }

    /**
     * すでにレスポンスを返却している確認します.
     *
     * @return レスポンスを返却している場合はtrue、それ以外はfalse
     */
    protected boolean isReturnedResponse() {
        return mReturnedResponse;
    }

    /**
     * QRコードの結果を通知します.
     *
     * @param result 結果
     * @param tagInfo QRコードの文字列
     */
    protected void postTagReaderActivityResult(final int result, final TagInfo tagInfo) {
        TagServiceInterface tagService = getTagService();
        if (tagService != null) {
            mReturnedResponse = true;
            tagService.onTagReaderActivityResult(getRequestCode(), result, tagInfo);
        }
    }

    /**
     * 書き込み結果を通知します.
     *
     * @param result 結果
     */
    protected void postTagWriterActivityResult(final int result) {
        TagServiceInterface tagService = getTagService();
        if (tagService != null) {
            mReturnedResponse = true;
            tagService.onTagWriterActivityResult(getRequestCode(), result);
        }
    }

    /**
     * リクエストコードを取得します.
     * @return リクエストコード
     */
    private String getRequestCode() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getStringExtra(TagConstants.EXTRA_REQUEST_CODE);
        }
        return null;
    }

    /**
     * {@link TagMessageService} にバインドします.
     */
    private void bindService() {
        Intent intent = new Intent(getApplicationContext(), TagMessageService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * {@link TagMessageService} からアンバインドします.
     */
    private void unbindService() {
        if (mIsBound) {
            unbindService(mConnection);
        }
    }

    /**
     * {@link TagMessageService} とのコネクション.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mBoundService = (TagMessageService) ((TagMessageService.LocalBinder) service).getMessageService();

            mReturnedResponse = false;

            TagService recorder = getTagService();
            if (recorder != null) {
                recorder.setTagController(BindServiceActivity.this);
            }

            BindServiceActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            mBoundService = null;
            mIsBound = false;
            BindServiceActivity.this.onServiceDisconnected();
        }
    };
}
