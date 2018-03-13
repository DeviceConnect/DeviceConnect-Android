package org.deviceconnect.android.deviceplugin.chromecast;

import android.app.Application;

import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastController;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;

/**
 * ChromeCas Applicationクラス.
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastApplication extends Application {
    /** Chromecast Discovery. */
    private ChromeCastDiscovery mDiscovery;
    /** Chromecast Application. */
    private ChromeCastController mController;

    public void initialize() {
        String appId = getString(R.string.application_id);

        if (mDiscovery == null) {
            mDiscovery = new ChromeCastDiscovery(this);

        }
        if (mController == null) {
            mController = new ChromeCastController(this, appId);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mController != null) {
            mController.teardown();
        }
    }

    /**
     * ChromeCastを制御するクラスを返す.
     * @return
     */
    public ChromeCastController getController() {
        return mController;
    }

    /**
     * ChromeCastを探索するクラスを返す.
     * @return
     */
    public ChromeCastDiscovery getDiscovery() {
        return mDiscovery;
    }
}
