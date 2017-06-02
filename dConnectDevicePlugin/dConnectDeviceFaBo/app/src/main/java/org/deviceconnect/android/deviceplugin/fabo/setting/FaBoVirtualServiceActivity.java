package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.Activity;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.R;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ServiceData;

/**
 * 仮装サービスの設定を行うActivity.
 */
public class FaBoVirtualServiceActivity extends Activity {

    /**
     * 新規作成フラグ.
     * <p>
     * 新規作成の仮装サービスの場合はtrue、それ以外はfalse。
     * </p>
     */
    private boolean mNewCrateFlag;

    /**
     * 仮装サービスデータ.
     */
    private ServiceData mServiceData;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_virtual_service);
    }


}
