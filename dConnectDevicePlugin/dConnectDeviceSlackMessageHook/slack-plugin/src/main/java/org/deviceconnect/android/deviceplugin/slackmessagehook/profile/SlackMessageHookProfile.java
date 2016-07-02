/*
 SlackMessageHookProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.slackmessagehook.SlackMessageHookDeviceService;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MessageHookProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * MessageHook プロファイルの実装クラス.
 * @author docomo
 */
public class SlackMessageHookProfile extends MessageHookProfile {

    /**
     * ServiceIDチェック.
     * @param serviceId サービスID
     * @param response レスポンス
     * @return エラー時はtrue
     */
    private boolean checkServiceId(String serviceId, Intent response) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        if (!SlackMessageHookServiceDiscoveryProfile.SERVICE_ID.equals(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onGetMessage(Intent request, final Intent response, String serviceId) {

        // ServiceIDチェック
        if (checkServiceId(serviceId, response)) {
            return true;
        }

        // 接続チェック
        if (!SlackManager.INSTANCE.isConnected()) {
            MessageUtils.setUnknownError(response, "Not connected to the Slack server");
            return true;
        }

        new Thread() {
            @Override
            public void run() {
                // 履歴を返す
                SlackMessageHookDeviceService service = (SlackMessageHookDeviceService) getContext();
                List<Bundle> history = service.getHistory();
                Bundle[] bundles = new Bundle[history.size()];
                history.toArray(bundles);
                response.putExtra(PARAM_MESSAGES, bundles);
                setResult(response, DConnectMessage.RESULT_OK);
                service.sendResponse(response);
            }
        }.start();

        return false;
    }

    @Override
    protected boolean onGetChannel(Intent request, final Intent response, String serviceId) {

        // ServiceIDチェック
        if (checkServiceId(serviceId, response)) {
            return true;
        }

        // 接続チェック
        if (!SlackManager.INSTANCE.isConnected()) {
            MessageUtils.setUnknownError(response, "Not connected to the Slack server");
            return true;
        }

        new Thread() {
            @Override
            public void run() {
                final CountDownLatch latch = new CountDownLatch(3);
                final HashMap<String, ArrayList<SlackManager.ListInfo>> resMap = new HashMap<>();

                // Channelリスト取得
                SlackManager.INSTANCE.getChannelList(new SlackManager.FinishCallback<ArrayList<SlackManager.ListInfo>>() {
                    @Override
                    public void onFinish(ArrayList<SlackManager.ListInfo> listInfos, Exception error) {
                        if (error == null) {
                            resMap.put("channel", listInfos);
                        } else {
                            Log.e("slack", "err", error);
                        }
                        latch.countDown();
                    }
                });

                // IMリスト取得
                SlackManager.INSTANCE.getIMList(new SlackManager.FinishCallback<ArrayList<SlackManager.ListInfo>>() {
                    @Override
                    public void onFinish(ArrayList<SlackManager.ListInfo> listInfos, Exception error) {
                        if (error == null) {
                            resMap.put("im", listInfos);
                        } else {
                            Log.e("slack", "err", error);
                        }
                        latch.countDown();
                    }
                });

                // ユーザーリスト取得
                SlackManager.INSTANCE.getUserList(new SlackManager.FinishCallback<ArrayList<SlackManager.ListInfo>>() {
                    @Override
                    public void onFinish(ArrayList<SlackManager.ListInfo> listInfos, Exception error) {
                        if (error == null) {
                            resMap.put("user", listInfos);
                        } else {
                            Log.e("slack", "err", error);
                        }
                        latch.countDown();
                    }
                });

                // 処理終了を待つ
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 各情報を詰め替え
                ArrayList<SlackManager.ListInfo> channels = resMap.get("channel");
                ArrayList<SlackManager.ListInfo> ims = resMap.get("im");
                ArrayList<SlackManager.ListInfo> users = resMap.get("user");
                if (channels != null && ims != null && users != null) {
                    List<Bundle> bundleList = new ArrayList<>();
                    // Channel
                    for (SlackManager.ListInfo info : channels) {
                        Bundle bundle = new Bundle();
                        bundle.putString(PARAM_ID, info.id);
                        bundle.putString(PARAM_NAME, info.name);
                        bundle.putString(PARAM_TYPE, "slack");
                        bundleList.add(bundle);
                    }
                    // UserをHashMapへ
                    HashMap<String, SlackManager.ListInfo> userMap = new HashMap<>();
                    for (SlackManager.ListInfo info : users) {
                        userMap.put(info.id, info);
                    }
                    // IM
                    for (SlackManager.ListInfo info : ims) {
                        Bundle bundle = new Bundle();
                        bundle.putString(PARAM_ID, info.id);
                        // UserIDからUserNameを取得
                        SlackManager.ListInfo user = userMap.get(info.name);
                        if (user == null) {
                            bundle.putString(PARAM_NAME, info.name);
                        } else {
                            bundle.putString(PARAM_NAME, user.name);
                        }
                        bundle.putString(PARAM_TYPE, "slack");
                        bundleList.add(bundle);
                    }
                    Bundle[] bundles = new Bundle[bundleList.size()];
                    bundleList.toArray(bundles);
                    response.putExtra(PARAM_CHANNELS, bundles);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                    setResult(response, DConnectMessage.RESULT_ERROR);
                }
                SlackMessageHookDeviceService service = (SlackMessageHookDeviceService) getContext();
                service.sendResponse(response);

            }
        }.start();

        return false;
    }

    @Override
    protected boolean onPostMessage(Intent request, final Intent response, String serviceId, String channel, String text, String resource, String mimeType) {

        // ServiceIDチェック
        if (checkServiceId(serviceId, response)) {
            return true;
        }

        // channelIDチェック
        if (channel == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Needs to have a \"channel\" parameter");
            return true;
        }

        // 接続チェック
        if (!SlackManager.INSTANCE.isConnected()) {
            MessageUtils.setUnknownError(response, "Not connected to the Slack server");
            return true;
        }

        if (resource == null) {
            if (text == null) {
                // 最低限textパラメータは必要
                MessageUtils.setInvalidRequestParameterError(response, "Needs to have a \"text\" parameter");
            } else {
                // メッセージ送信
                SlackManager.INSTANCE.sendMessage(text, channel, new SlackManager.FinishCallback<String>() {
                    @Override
                    public void onFinish(String s, Exception error) {
                        if (error == null) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setUnknownError(response);
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                        SlackMessageHookDeviceService service = (SlackMessageHookDeviceService) getContext();
                        service.sendResponse(response);
                    }
                });
                return false;
            }
        } else {
            try {
                // ファイルアップロード
                URL url = new URL(resource);
                String origin = getContext().getPackageName();
                SlackManager.INSTANCE.uploadFile(text, channel, url, origin, new SlackManager.FinishCallback<JSONObject>() {
                    @Override
                    public void onFinish(JSONObject jsonObject, Exception error) {
                        if (error == null) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setUnknownError(response);
                            setResult(response, DConnectMessage.RESULT_ERROR);
                        }
                        SlackMessageHookDeviceService service = (SlackMessageHookDeviceService) getContext();
                        service.sendResponse(response);
                    }
                });
                return false;
            } catch (MalformedURLException e) {
                MessageUtils.setInvalidRequestParameterError(response, "Invalid resource url");
            }
        }

        return true;
    }

    @Override
    protected boolean onPutOnMessageReceived(Intent request, Intent response, String serviceId, String sessionKey) {
        EventError error = EventManager.INSTANCE.addEvent(request);
        switch (error) {
            case NONE:
                setResult(response, DConnectMessage.RESULT_OK);
                break;
            case INVALID_PARAMETER:
                MessageUtils.setInvalidRequestParameterError(response);
                break;
            default:
                MessageUtils.setUnknownError(response);
                break;
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnMessageReceived(Intent request, Intent response, String serviceId, String sessionKey) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        switch (error) {
            case NONE:
                setResult(response, DConnectMessage.RESULT_OK);
                break;
            case INVALID_PARAMETER:
                MessageUtils.setInvalidRequestParameterError(response);
                break;
            default:
                MessageUtils.setUnknownError(response);
                break;
        }
        return true;
    }
}
