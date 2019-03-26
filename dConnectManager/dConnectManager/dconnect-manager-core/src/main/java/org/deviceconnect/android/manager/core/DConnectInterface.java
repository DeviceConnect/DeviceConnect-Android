package org.deviceconnect.android.manager.core;

import android.app.Activity;
import android.content.BroadcastReceiver;

/**
 * dconnect-manager-core側で設定が必要なクラスを実装させるためのインターフェース.
 */
public interface DConnectInterface {
    Class<? extends BroadcastReceiver> getDConnectBroadcastReceiverClass();
    Class<? extends Activity> getSettingActivityClass();
    Class<? extends Activity> getKeywordActivityClass();
}
