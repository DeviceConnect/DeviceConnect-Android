/*
 UnitTestService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.service;


import android.content.ComponentName;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.test.profile.TestSystemProfile;
import org.deviceconnect.android.deviceplugin.test.profile.Util;
import org.deviceconnect.android.deviceplugin.test.profile.unique.TestAllGetControlProfile;
import org.deviceconnect.android.deviceplugin.test.profile.unique.TestDataProfile;
import org.deviceconnect.android.deviceplugin.test.profile.unique.TestJSONConversionProfile;
import org.deviceconnect.android.deviceplugin.test.profile.unique.TestUniqueProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * ユニットテスト用サービス.
 * <p>
 * 各プロファイルの疎通テストに使用する.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class UnitTestService extends DConnectService {

    private static final long DEFAULT_DELAY = 500;

    private Map<String, EventSender> mEventSenders = new HashMap<String, EventSender>();

    public UnitTestService(final String id, final String name) {
        super(id);
        setName(name);
        setOnline(true);

        addProfile(new TestSystemProfile());
        addProfile(new TestUniqueProfile());
        addProfile(new TestJSONConversionProfile());
        addProfile(new TestAllGetControlProfile());
        addProfile(new TestDataProfile());
    }

    /**
     * 指定したミリ秒後に別スレッドでインテントをブロードキャストする.
     *
     * @param intent インテント
     * @param delay 遅延設定 (単位はミリ秒)
     */
    public void sendBroadcast(final Intent intent, final long delay) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    intent.setComponent(Util.DEFAULT_MESSAGE_RECEIVER);
                    getContext().sendBroadcast(intent);
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }
        })).start();
    }

    /**
     * {@value #DEFAULT_DELAY}ミリ秒後に別スレッドでインテントをブロードキャストする.
     *
     * @param intent インテント
     */
    public void sendBroadcast(final Intent intent) {
        sendBroadcast(intent, DEFAULT_DELAY);
    }


    private EventSender getEventSender(final String path) {
        return mEventSenders.get(path);
    }

    private EventSender removeEventSender(final String path) {
        return mEventSenders.remove(path);
    }

    private void putEventSender(final String path, final EventSender thread) {
        mEventSenders.put(path, thread);
    }

    private void startEventBroadcast(final Intent request) {
        EventSender sender = new EventSender(request);
        EventSender cache = getEventSender(sender.getPath());
        if (cache != null) {
            return;
        }
        putEventSender(sender.getPath(), sender);
        new Thread(sender).start();
    }

    private void stopEventSender(final Intent request) {
        EventSender cache = removeEventSender(createPath(request));
        if (cache == null) {
            return;
        }
        cache.stop();
    }

    private String createPath(final Intent request) {
        String profileName = DConnectProfile.getProfile(request);
        String attributeName = DConnectProfile.getAttribute(request);
        return "/" + profileName + "/" + attributeName;
    }

    private class EventSender implements Runnable {

        private final String mPath;
        private final String mAccessToken;
        private final Intent mEvent;
        private boolean mIsSending = true;
        private int mCount = 10;

        EventSender(final Intent request) {
            String profileName = DConnectProfile.getProfile(request);
            String attributeName = DConnectProfile.getAttribute(request);
            String accessToken = DConnectProfile.getAccessToken(request);
            String serviceId = DConnectProfile.getServiceID(request);
            Intent message = MessageUtils.createEventIntent();
            DConnectProfile.setAccessToken(message, accessToken);
            DConnectProfile.setServiceID(message, serviceId);
            DConnectProfile.setProfile(message, profileName);
            DConnectProfile.setAttribute(message, attributeName);
            message.setComponent((ComponentName) request.getParcelableExtra(DConnectMessage.EXTRA_RECEIVER));
            mEvent = message;
            mPath = createPath(request);
            mAccessToken = accessToken;
        }

        String getPath() {
            return mPath + mAccessToken;
        }

        void stop() {
            mIsSending = false;
        }

        @Override
        public void run() {
            try {
                while (mIsSending && mCount > 0) {
                    sendBroadcast(mEvent);
                    Thread.sleep(DEFAULT_DELAY);
                    mCount--;
                }
            } catch (InterruptedException e) {
                mIsSending = false;
            } finally {
                removeEventSender(getPath());
            }
        }
    }

}
