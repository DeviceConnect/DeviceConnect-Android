package org.deviceconnect.android.manager.core.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.message.DevicePluginContext;

import java.lang.reflect.Constructor;

/**
 * プラグインに直接接続を行うクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DirectConnection extends AbstractConnection {

    /**
     * 接続先プラグインのコンポーネント名.
     */
    private final ComponentName mPluginName;

    /**
     * 通知先のコールバック.
     */
    private final IDConnectCallback mCallback;

    /**
     * プラグインのコンテキスト.
     */
    private DevicePluginContext mPluginContext;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param pluginId プラグインID
     * @param target コンポーネント名
     * @param callback コールバック
     */
    public DirectConnection(final Context context, final String pluginId,
                            final ComponentName target, final IDConnectCallback callback) {
        super(context, pluginId);
        mPluginName = target;
        mCallback = callback;
        setSuspendedState(null);
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.DIRECT;
    }

    @Override
    public void connect() throws ConnectingException {
        String className = mPluginName.getClassName();

        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (Exception e) {
            setSuspendedState(ConnectionError.INTERNAL_ERROR);
            throw new ConnectingException(e);
        }

        Class<?>[] types = { Context.class };
        Constructor<DevicePluginContext> constructor;
        try {
            constructor = (Constructor<DevicePluginContext>) clazz.getConstructor(types);
        } catch (Exception e) {
            setSuspendedState(ConnectionError.INTERNAL_ERROR);
            throw new ConnectingException(e);
        }

        Object[] args = { mContext };

        try {
            mPluginContext = constructor.newInstance(args);
            mPluginContext.setIDConnectCallback(mCallback);
            setConnectedState();
        } catch (Exception e) {
            setSuspendedState(ConnectionError.INTERNAL_ERROR);
            throw new ConnectingException(e);
        }
    }

    @Override
    public void disconnect() {
        if (mPluginContext != null) {
            try {
                mPluginContext.release();
            } catch (Exception e) {
                // ignore.
            }
            mPluginContext = null;
        }
        setDisconnectedState();
    }

    @Override
    public void send(Intent message) throws MessagingException {
        if (mPluginContext == null) {
            throw new MessagingException(MessagingException.Reason.NOT_CONNECTED);
        }

        try {
            mPluginContext.handleMessage(message);
        } catch (Exception e) {
            throw new MessagingException(e, MessagingException.Reason.OTHER);
        }
    }
}
