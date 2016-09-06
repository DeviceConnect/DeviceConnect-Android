package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.DescribeEndpointRequest;
import com.amazonaws.services.iot.model.DescribeEndpointResult;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AWSIotを制御するクラス.
 */
public class AWSIotController {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS";

    /**
     * AWSIoTのClient
     */
    private AWSIotClient mIotClient;

    /**
     * AWSIoTのDataClient.
     */
    private AWSIotDataClient mIotDataClient;

    /**
     * AWSIoTのMqtt Manager.
     */
    private static AWSIotMqttManager mMqttManager;

    /**
     * 証明書のプロバイダ
     */
    private AWSCredentialsProvider mCredentialsProvider;

    /**
     * 接続フラグ
     */
    private boolean mIsConnected = false;

    /**
     * イベントリスナー
     */
    private EventListener mEventListener;

    /**
     * イベントリスナー
     */
    public interface EventListener {

        /**
         * 接続完了.
         *
         * @param err エラー
         */
        void onConnected(Exception err);

        /**
         * 再接続中.
         */
        void onReconnecting();

        /**
         * 切断完了.
         */
        void onDisconnected();

        /**
         * Shadow取得.
         *
         * @param thingName thing名
         * @param result    result
         * @param err       エラー
         */
        void onReceivedShadow(String thingName, String result, Exception err);

        /**
         * メッセージ受信.
         *
         * @param topic   Topic名
         * @param message メッセージ
         * @param err     エラー
         */
        void onReceivedMessage(String topic, String message, Exception err);
    }

    /**
     * イベントリスナーを設定
     *
     * @param listener リスナー
     */
    public void setEventListener(final EventListener listener) {
        mEventListener = listener;
    }

    /**
     * 接続
     *
     * @param accessKey アクセスキー
     * @param secretKey シークレットキー
     * @param region    リージョン
     */
    public void connect(final String accessKey, final String secretKey, final Regions region) {
        mIsConnected = false;

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        mCredentialsProvider = new StaticCredentialsProvider(credentials);

        mIotClient = new AWSIotClient(mCredentialsProvider);
        mIotClient.setRegion(Region.getRegion(region));
        mIotDataClient = new AWSIotDataClient(mCredentialsProvider);

        new DescribeEndpointTask().execute();
    }

    public void disconnect() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotController#disconnect");
        }

        // TODO 切断処理

        if (mMqttManager != null) {
            mMqttManager.disconnect();
        }
    }

    /**
     * Shadowの取得.
     *
     * @param name Shadow名
     */
    public void getShadow(final String name) {
        if (!mIsConnected) {
            if (mEventListener != null) {
                // TODO: エラー種類
                mEventListener.onReceivedShadow(name, null, new Exception());
            }
            return;
        }
        GetShadowTask getStatusShadowTask = new GetShadowTask(name);
        getStatusShadowTask.execute();
    }

    /**
     * Shadowの更新.
     *
     * @param name  Shadow名
     * @param key   キー
     * @param value 値
     */
    public void updateShadow(final String name, final String key, final Object value) {
        if (!mIsConnected) {
            return;
        }

        UpdateShadowTask updateShadowTask = new UpdateShadowTask();
        updateShadowTask.setThingName(name);
        String state = makeJson(key, value).toString();
        updateShadowTask.setState(state);
        updateShadowTask.execute();
    }

    /**
     * Shadowの更新.
     *
     * @param name Shadow名
     * @param keys 値
     */
    public void updateShadow(final String name, final HashMap<String, Object> keys) {
        if (!mIsConnected) {
            return;
        }

        UpdateShadowTask updateShadowTask = new UpdateShadowTask();
        updateShadowTask.setThingName(name);
        String state = makeJson(keys).toString();
        updateShadowTask.setState(state);
        updateShadowTask.execute();
    }

    /**
     * Shadowのクリア.
     *
     * @param name Shadow名
     */
    public void clearShadow(final String name) {
        if (!mIsConnected) {
            return;
        }

        UpdateShadowTask updateShadowTask = new UpdateShadowTask();
        updateShadowTask.setThingName(name);
        String state = "{\"state\":{\"desired\":null}}";
        updateShadowTask.setState(state);
        updateShadowTask.execute();
    }

    /**
     * AWSIotへ送信するjson作成
     *
     * @param key   キー
     * @param value 値
     * @return json
     */
    private JSONObject makeJson(final String key, final Object value) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonState = new JSONObject();
        JSONObject jsonDesired = new JSONObject();
        try {
            jsonDesired.put(key, value);
            jsonState.put("desired", jsonDesired);
            jsonRoot.put("state", jsonState);
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "json error.", e);
            }
            // TODO: エラー処理
        }
        return jsonRoot;
    }

    /**
     * AWSIotへ送信するjson作成
     *
     * @param keys 値
     * @return json
     */
    private JSONObject makeJson(final HashMap<String, Object> keys) {
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonState = new JSONObject();
        JSONObject jsonReported = new JSONObject();
        try {
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                jsonReported.put(entry.getKey(), entry.getValue());
            }
            jsonState.put("desired", jsonReported);
            jsonRoot.put("state", jsonState);
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "json error.", e);
            }
            // TODO: エラー処理
        }
        return jsonRoot;
    }

    /**
     * Endpoint取得Task
     */
    private class DescribeEndpointTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {
        /**
         * バックグラウンド処理.
         *
         * @param voids voids
         * @return 結果
         */
        @Override
        protected AsyncTaskResult<String> doInBackground(final Void... voids) {
            try {
                DescribeEndpointRequest request = new DescribeEndpointRequest();
                DescribeEndpointResult result = mIotClient.describeEndpoint(request);
                return new AsyncTaskResult<>(result.toString());
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Error on DescribeEndpointTask", e);
                }
                return new AsyncTaskResult<>(e);
            }
        }

        /**
         * 実行結果.
         *
         * @param result 結果
         */
        @Override
        protected void onPostExecute(final AsyncTaskResult<String> result) {
            Exception exception;
            if (result.getError() == null) {
                JSONObject json;
                try {
                    json = new JSONObject(result.getResult());
                    if (json.has("endpointAddress")) {
                        String endpoint = json.getString("endpointAddress");
                        mIotDataClient.setEndpoint(endpoint);
                        String clientId = UUID.randomUUID().toString();
                        // TODO AWS IoTの設定
                        mMqttManager = new AWSIotMqttManager(clientId, endpoint);
                        mMqttManager.setKeepAlive(120);
                        mMqttManager.setConnectionStabilityTime(180);
                        mMqttManager.setReconnectRetryLimits(1, 5);
                        mMqttManager.setAutoReconnect(true);
                        connectMQTT();
                        return;
                    } else {
                        // TODO: エラー種類
                        exception = new Exception();
                    }
                } catch (JSONException e) {
                    exception = e;
                }
            } else {
                exception = result.getError();
            }
            if (mEventListener != null) {
                mEventListener.onConnected(exception);
            }
        }
    }

    /**
     * Shadowを取得するAsyncTask.
     */
    private class GetShadowTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

        /**
         * Thing名
         */
        private final String thingName;

        /**
         * 名前を指定して初期化.
         *
         * @param name 名前
         */
        public GetShadowTask(final String name) {
            thingName = name;
        }

        /**
         * バックグラウンド処理.
         *
         * @param voids voids
         * @return 結果
         */
        @Override
        protected AsyncTaskResult<String> doInBackground(final Void... voids) {
            try {
                GetThingShadowRequest getThingShadowRequest = new GetThingShadowRequest().withThingName(thingName);
                GetThingShadowResult result = mIotDataClient.getThingShadow(getThingShadowRequest);
                byte[] bytes = new byte[result.getPayload().remaining()];
                result.getPayload().get(bytes);
                String resultString = new String(bytes);
                return new AsyncTaskResult<>(resultString);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Error on GetShadowTask", e);
                }
                return new AsyncTaskResult<>(e);
            }
        }

        /**
         * 実行結果.
         *
         * @param result 結果
         */
        @Override
        protected void onPostExecute(final AsyncTaskResult<String> result) {
            if (mEventListener != null) {
                mEventListener.onReceivedShadow(thingName, result.getResult(), result.getError());
            }
        }
    }

    /**
     * Shadowを更新するAsyncTask.
     */
    private class UpdateShadowTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {
        /**
         * Thing名
         */
        private String thingName;

        /**
         * 更新するState
         */
        private String updateState;

        /**
         * Thing名を設定.
         *
         * @param name Thing名
         */
        public void setThingName(final String name) {
            thingName = name;
        }

        /**
         * Stateを設定.
         *
         * @param state State
         */
        public void setState(final String state) {
            updateState = state;
        }

        /**
         * バックグラウンド処理.
         *
         * @param voids voids
         * @return 結果
         */
        @Override
        protected AsyncTaskResult<String> doInBackground(final Void... voids) {
            try {
                UpdateThingShadowRequest request = new UpdateThingShadowRequest();
                request.setThingName(thingName);

                ByteBuffer payloadBuffer = ByteBuffer.wrap(updateState.getBytes());
                request.setPayload(payloadBuffer);

                UpdateThingShadowResult result = mIotDataClient.updateThingShadow(request);

                byte[] bytes = new byte[result.getPayload().remaining()];
                result.getPayload().get(bytes);
                String resultString = new String(bytes);
                return new AsyncTaskResult<>(resultString);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Error on UpdateShadowTask", e);
                }
                return new AsyncTaskResult<>(e);
            }
        }

        /**
         * 実行結果.
         *
         * @param result 結果
         */
        @Override
        protected void onPostExecute(final AsyncTaskResult<String> result) {
            if (result.getError() == null) {
                if (DEBUG) {
                    Log.d(TAG, result.getResult());
                }
                // TODO: callback
            }
        }
    }

    /**
     * MQTTへの接続.
     */
    public void connectMQTT() {
        if (DEBUG) {
            Log.i(TAG, "connectMQTT");
        }

        if (mMqttManager == null) {
            return;
        }

        try {
            mMqttManager.connect(mCredentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                    if (DEBUG) {
                        Log.d(TAG, "MQTT Status = " + String.valueOf(status));
                        if (throwable != null) {
                            Log.e(TAG, "MQTT Error", throwable);
                        }
                    }
                    if (status == AWSIotMqttClientStatus.Connecting) {
                    } else if (status == AWSIotMqttClientStatus.Connected) {
                        // 接続済みとする
                        if (!mIsConnected) {
                            mIsConnected = true;
                            if (mEventListener != null) {
                                mEventListener.onConnected(null);
                            }
                        }
                    } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                        if (mEventListener != null) {
                            mEventListener.onReconnecting();
                        }
                    } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                        if (mEventListener != null) {
                            mEventListener.onDisconnected();
                        }
                    } else {
                    }
                }
            });
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Connection error.", e);
            }
        }
    }

    /**
     * MQTTのTopicを購読する.
     * <p>
     * 登録時のTopicのフォーマットは<a href="http://docs.aws.amazon.com/iot/latest/developerguide/thing-shadow-mqtt.html">Device Shadow MQTT Pub/Sub Messages</a>を参照。
     * </p>
     * @param topic topicのURI
     */
    public void subscribe(final String topic) {
        if (DEBUG) {
            Log.d(TAG, "********* subscribe: " + topic);
        }

        if (mMqttManager == null) {
            return;
        }

        mMqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                new AWSIotMqttNewMessageCallback() {
                    @Override
                    public void onMessageArrived(final String topic, final byte[] data) {
                        try {
                            String message = new String(data, "UTF-8");
                            if (mEventListener != null) {
                                mEventListener.onReceivedMessage(topic, message, null);
                            }
                        } catch (UnsupportedEncodingException e) {
                            if (DEBUG) {
                                Log.e(TAG, "Message encoding error.", e);
                            }
                            if (mEventListener != null) {
                                mEventListener.onReceivedMessage(topic, null, e);
                            }
                        }
                    }
                });
    }

    public void unsubscribe(final String topic) {
        if (DEBUG) {
            Log.d(TAG, "********* unsubscribe: " + topic);
        }

        if (mMqttManager == null) {
            return;
        }

        try {
            mMqttManager.unsubscribeTopic(topic);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "unsubscribe error.", e);
            }
        }
    }

    /**
     * MQTTでtopicへメッセージを発行する.
     *
     * @param topic topicのURI
     * @param msg   メッセージ
     */
    public void publish(final String topic, final String msg) {
        if (DEBUG) {
            Log.i(TAG, "publish topic:" + topic);
        }

        if (mMqttManager == null) {
            return;
        }

        try {
            mMqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0, new AWSIotMqttMessageDeliveryCallback() {
                @Override
                public void statusChanged(MessageDeliveryStatus status, Object userData) {
                    Log.e(TAG, "AWSIotController#publish: MessageDeliveryStatus=" + status);
                }
            }, null);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Publish error.", e);
            }
            // TODO: エラー処理
        }
    }
}
