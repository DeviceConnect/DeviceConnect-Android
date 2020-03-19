/*
 SystemProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import org.deviceconnect.android.R;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.List;

/**
 * System プロファイル.
 * 
 * <p>
 * システム情報を提供するAPI.<br>
 * システム情報を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 * 
 * @author NTT DOCOMO, INC.
 */
public abstract class SystemProfile extends DConnectProfile implements SystemProfileConstants {

    /**
     * 設定画面起動用IntentのパラメータオブジェクトのExtraキー.
     */
    public static final String SETTING_PAGE_PARAMS = "org.deviceconnect.profile.system.setting_params";
    /** Notification Id */
    private final int NOTIFICATION_ID = 3518;


    /**
     * 遷移先の設定画面用のActivityのクラス.
     * 
     * @param request リクエストパラメータ
     * @param param 
     *            Activity起動用Intentのパラメータ。設定画面用のActivityを呼び出すときにIntentのExtra要素として付加される
     *            。Extraのキーは {@link SystemProfile#SETTING_PAGE_PARAMS} となる。
     * 
     * @return ActivityのClassクラス
     */
    protected abstract Class<? extends Activity> getSettingPageActivity(Intent request, Bundle param);

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    public SystemProfile() {
        addApi(mPutWakeUpApi);
    }

    private final DConnectApi mPutWakeUpApi = new DConnectApi() {

        @Override
        public String getInterface() {
            return INTERFACE_DEVICE;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WAKEUP;
        }

        @Override
        public Method getMethod() {
            return Method.PUT;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle param = new Bundle();
            Class<? extends Activity> clazz = getSettingPageActivity(request, param);
            if (clazz == null) {
                setUnsupportedError(response);
            } else {
                Intent i = new Intent(getContext(), clazz);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(SETTING_PAGE_PARAMS, param);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    getContext().startActivity(i);
                } else {
                    NotificationUtils.createNotificationChannel(getContext());
                    NotificationUtils.notify(getContext(),  NOTIFICATION_ID, 0, i,
                            getContext().getString(R.string.notification_warnning));
                }
                setResult(response, DConnectMessage.RESULT_OK);
            }
            return true;
        }
    };

    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにバージョンを格納する.
     * 
     * @param response レスポンスパラメータ
     * @param version バージョン
     */
    public static void setVersion(final Intent response, final String version) {
        response.putExtra(PARAM_VERSION, version);
    }

    /**
     * レスポンスにサポートしているI/Fの一覧を格納する.
     * 
     * @param response レスポンスパラメータ
     * @param supports サポートしているI/F一覧
     */
    public static void setSupports(final Intent response, final String[] supports) {
        response.putExtra(PARAM_SUPPORTS, supports);
    }

    /**
     * レスポンスにサポートしているI/Fの一覧を格納する.
     * 
     * @param response レスポンスパラメータ
     * @param supports サポートしているI/F一覧
     */
    public static void setSupports(final Intent response, final List<String> supports) {
        setSupports(response, supports.toArray(new String[supports.size()]));
    }

    /**
     * レスポンスにサポートしているI/Fの一覧を格納する.
     *
     * @param response レスポンスパラメータ
     * @param supports サポートしているI/F一覧
     */
    public static void setSupports(final Bundle response, final String[] supports) {
        response.putStringArray(PARAM_SUPPORTS, supports);
    }

    /**
     * レスポンスにサポートしているI/Fの一覧を格納する.
     *
     * @param response レスポンスパラメータ
     * @param supports サポートしているI/F一覧
     */
    public static void setSupports(final Bundle response, final List<String> supports) {
        setSupports(response, supports.toArray(new String[supports.size()]));
    }

    /**
     * リクエストからpluginIdを取得する.
     * 
     * @param request リクエストパラメータ
     * @return プラグインのID
     */
    public static String getPluginID(final Intent request) {
        String pluginId = request.getStringExtra(PARAM_PLUGIN_ID);
        return pluginId;
    }

    /**
     * レスポンスにnameを設定する.
     * @param response レスポンスパラメータ
     * @param name Managerの名前
     */
    public static void setName(final Intent response, final String name) {
        response.putExtra(PARAM_NAME, name);
    }

    /**
     * レスポンスにUUIDを設定する.
     * @param response レスポンスパラメータ
     * @param uuid UUID
     */
    public static void setUuid(final Intent response, final String uuid) {
        response.putExtra(PARAM_UUID, uuid);
    }
}
