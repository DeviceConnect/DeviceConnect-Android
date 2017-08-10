/*
 SlackMessageHookProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.slackmessagehook.SlackMessageHookDeviceService;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MessageHookProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * MessageHook プロファイルの実装クラス.
 * @author docomo
 */
public class SlackMessageHookProfile extends MessageHookProfile {

    /**
     * コンストラクタ
     */
    public SlackMessageHookProfile() {
        addApi(mGetMessageApi);
        addApi(mGetChannelsApi);
        addApi(mPostMessageApi);
        addApi(mGetOnMessageApi);
        addApi(mOnPutOnMessageReceivedApi);
        addApi(mOnDeleteOnMessageReceivedApi);
    }


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
        if (!SlackMessageHookDeviceService.SERVICE_ID.equals(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        return false;
    }

    /**
     * メッセージ取得API
     */
    private final DConnectApi mGetMessageApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_MESSAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getMessage(request, response);
        }
    };



    /**
     * Channelリスト取得API
     */
    private final DConnectApi mGetChannelsApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_CHANNEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {

            String serviceId = getServiceID(request);

            // ServiceIDチェック
            if (checkServiceId(serviceId, response)) {
                return true;
            }

            // 接続チェック
            if (!SlackManager.INSTANCE.isConnected()) {
                MessageUtils.setUnknownError(response, "Not connected to the Slack server");
                return true;
            }

            // Channelリストを取得
            SlackManager.INSTANCE.getAllChannelList(new SlackManager.FinishCallback<List<SlackManager.ListInfo>>() {
                @Override
                public void onFinish(List<SlackManager.ListInfo> listInfos, Exception error) {
                    if (error == null) {
                        List<Bundle> bundleList = new ArrayList<>();
                        for (SlackManager.ListInfo info : listInfos) {
                            Bundle bundle = new Bundle();
                            bundle.putString(PARAM_ID, info.id);
                            bundle.putString(PARAM_NAME, info.name);
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
            });
            return false;
        }
    };

    /**
     * メッセージ投稿API
     */
    private final DConnectApi mPostMessageApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_MESSAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {

            String serviceId = getServiceID(request);
            String channelid = request.getStringExtra(PARAM_CHANNELID);
            String text = request.getStringExtra(PARAM_TEXT);
            String resource = request.getStringExtra(PARAM_RESOURCE);

            // ServiceIDチェック
            if (checkServiceId(serviceId, response)) {
                return true;
            }

            // channelIDチェック
            if (channelid == null) {
                MessageUtils.setInvalidRequestParameterError(response, "Needs to have a \"channelId\" parameter");
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
                    SlackManager.INSTANCE.sendMessage(text, channelid, new SlackManager.FinishCallback<String>() {
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
                    SlackManager.INSTANCE.uploadFile(text, channelid, url, origin, new SlackManager.FinishCallback<JSONObject>() {
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
    };
    /**
     * メッセージ取得API.
     * メッセージ履歴の最新の一件を返却する.
     */
    private final DConnectApi mGetOnMessageApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONMESSAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // ServiceIDチェック
            String serviceId = getServiceID(request);
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
                    if (history.size() > 0) {
                        response.putExtra(PARAM_MESSAGE, history.get(history.size() - 1));
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                    service.sendResponse(response);
                }
            }.start();
            return false;
        }
    };
    /**
     * メッセージイベント登録API
     */
    private final DConnectApi mOnPutOnMessageReceivedApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONMESSAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (!SlackManager.INSTANCE.isConnected()) {
                MessageUtils.setUnknownError(response, "Not connected to the Slack server");
                return true;
            }
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
    };

    /**
     * メッセージイベント解除API
     */
    private final DConnectApi mOnDeleteOnMessageReceivedApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONMESSAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (!SlackManager.INSTANCE.isConnected()) {
                MessageUtils.setUnknownError(response, "Not connected to the Slack server");
                return true;
            }

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
    };
    private boolean getMessage(Intent request, final Intent response) {
        // ServiceIDチェック
        String serviceId = getServiceID(request);
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
}
