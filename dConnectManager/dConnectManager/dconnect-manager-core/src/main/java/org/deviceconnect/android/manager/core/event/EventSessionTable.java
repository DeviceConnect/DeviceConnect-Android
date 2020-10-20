package org.deviceconnect.android.manager.core.event;

import org.deviceconnect.android.manager.core.plugin.DevicePlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * イベントのセッションを保持するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class EventSessionTable {

    /**
     * セッションのリスト.
     */
    private final List<EventSession> mEventSessions = new ArrayList<>();

    /**
     * セッションのリストを取得します.
     *
     * @return セッションのリスト
     */
    public List<EventSession> getAll() {
        synchronized (mEventSessions) {
            return new ArrayList<>(mEventSessions);
        }
    }

    /**
     * 指定されたプラグインが持っているイベントセッションを取得します.
     * <p>
     * イベントセッションが存在しない場合は、空のリストを返却します。
     * </p>
     * @param plugin プラグイン
     * @return イベントセッションのリスト
     */
    List<EventSession> findEventSessionsForPlugin(final DevicePlugin plugin) {
        List<EventSession> result = new ArrayList<>();
        synchronized (mEventSessions) {
            for (EventSession session : mEventSessions) {
                if (plugin.getPluginId().equals(session.getPluginId())) {
                    result.add(session);
                }
            }
        }
        return result;
    }

    /**
     * イベントセッションを追加します.
     *
     * @param session 追加するイベントセッション
     */
    void add(final EventSession session) {
        synchronized(mEventSessions) {
            mEventSessions.add(session);
        }
    }

    /**
     * イベントセッションを削除します.
     *
     * @param session イベントセッション
     */
    void remove(final EventSession session) {
        synchronized (mEventSessions) {
            mEventSessions.remove(session);
        }
    }

    /**
     * アクセストークンを更新します.
     *
     * @param pluginId プラグインID
     * @param newAccessToken アクセストークン
     */
    void updateAccessTokenForPlugin(final String pluginId, final String newAccessToken) {
        synchronized (mEventSessions) {
            for (EventSession session : mEventSessions) {
                if (session.getPluginId() != null && session.getPluginId().equals(pluginId)) {
                    session.setAccessToken(newAccessToken);
                }
            }
        }
    }

    /**
     * 指定されたプラグインIDのイベントセッションを削除します.
     *
     * @param pluginId プラグインID
     */
    void removeForPlugin(final String pluginId) {
        synchronized (mEventSessions) {
            for (Iterator<EventSession> it = mEventSessions.iterator(); it.hasNext(); ) {
                EventSession session = it.next();
                if (session.getPluginId() != null && session.getPluginId().equals(pluginId)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 指定されたレシーバーIDのイベントセッションを削除します.
     *
     * @param receiverId レシーバーID
     */
    void removeForReceiverId(final String receiverId) {
        synchronized (mEventSessions) {
            for (Iterator<EventSession> it = mEventSessions.iterator(); it.hasNext(); ) {
                if (it.next().getReceiverId().equals(receiverId)) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public String toString() {
        return mEventSessions.toString();
    }
}
