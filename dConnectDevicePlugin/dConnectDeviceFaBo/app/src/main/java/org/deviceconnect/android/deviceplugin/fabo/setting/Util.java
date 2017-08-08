package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.DConnectMessageServiceProvider;

import java.lang.reflect.Method;

final class Util {
    private Util() {
    }

    /**
     * デバイスプラグインに格納されるメタタグ名.
     */
    private static final String PLUGIN_META_DATA = "org.deviceconnect.android.deviceplugin";

    /**
     * DConnectMessageServiceのClassを取得します.
     * <p>
     * Classが見つからなかった場合にはnullを返却します。
     * </p>
     * @param context コンテキスト
     * @return DConnectMessageServiceのClass
     */
    static Class<? extends DConnectMessageService> getDConnectMessageServiceClass(final Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS);
            ActivityInfo[] receivers = packageInfo.receivers;
            if (receivers != null) {
                for (ActivityInfo receiver : receivers) {
                    String packageName = receiver.packageName;
                    String className = receiver.name;

                    ComponentName component = new ComponentName(packageName, className);
                    ActivityInfo receiverInfo = pm.getReceiverInfo(component, PackageManager.GET_META_DATA);
                    if (receiverInfo.metaData != null) {
                        Object value = receiverInfo.metaData.get(PLUGIN_META_DATA);
                        if (value != null) {
                            try {
                                Class<?> clazz = context.getClassLoader().loadClass(className);
                                DConnectMessageServiceProvider obj = (DConnectMessageServiceProvider) clazz.newInstance();
                                Method method = clazz.getDeclaredMethod("getServiceClass");
                                method.setAccessible(true);
                                return (Class<? extends DConnectMessageService>) method.invoke(obj);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
