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

    private static final String END_POINT_ADDRESS = "endpointAddress";

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
     * endpoint情報
     */
    private String mAWSIotEndPoint = "Not Connected";

    public interface ConnectCallback {
        void onConnected(Exception err);
    }

    public interface GetShadowCallback {
        void onReceivedShadow(String thingName, String result, Exception err);
    }

    public interface UpdateShadowCallback {
        void onUpdateShadow(String result, Exception err);
    }

    public interface MessageCallback {
        void onReceivedMessage(String topic, String message, Exception err);
    }

    /**
     * 接続
     *
     * @param accessKey アクセスキー
     * @param secretKey シークレットキー
     * @param region    リージョン
     */
    public void connect(final String accessKey, final String secretKey, final Regions region, final ConnectCallback callback) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        mCredentialsProvider = new StaticCredentialsProvider(credentials);

        mIotClient = new AWSIotClient(mCredentialsProvider);
        mIotClient.setRegion(Region.getRegion(region));
        mIotDataClient = new AWSIotDataClient(mCredentialsProvider);

        new DescribeEndpointTask() {
            @Override
            protected void onPostExecute(final AsyncTaskResult<String> result) {
                Exception exception;
                if (result.getError() == null) {
                    JSONObject json;
                    try {
                        json = new JSONObject(result.getResult());
                        if (json.has(END_POINT_ADDRESS)) {
                            String endpoint = json.getString(END_POINT_ADDRESS);
                            mAWSIotEndPoint = endpoint;
                            mIotDataClient.setEndpoint(endpoint);
                            connectMQTT(callback);
                            return;
                        } else {
                            exception = new Exception("Not found endpointAddress.");
                        }
                    } catch (JSONException e) {
                        exception = e;
                    }
                } else {
                    exception = result.getError();
                }
                callback.onConnected(exception);
            }
        }.execute();
    }

    public void disconnect() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotController#disconnect");
        }

        mAWSIotEndPoint = null;
        mIotClient = null;
        mIotDataClient = null;
        mCredentialsProvider = null;

        disconnectMQTT();
    }

    public void getShadow(final String name, final GetShadowCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback is null.");
        }

        if (mAWSIotEndPoint == null) {
            callback.onReceivedShadow(name, null, new RuntimeException("No Connect to the AWS IoT Server."));
            return;
        }

        GetShadowTask task = new GetShadowTask(name) {
            @Override
            protected void onPostExecute(final AsyncTaskResult<String> result) {
                callback.onReceivedShadow(name, result.getResult(), result.getError());
            }
        };
        task.execute();
    }

    public void updateShadow(final String name, final String key, final Object value, final UpdateShadowCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback is null.");
        }

        if (mAWSIotEndPoint == null) {
            callback.onUpdateShadow(null, new RuntimeException("No Connect to the AWS IoT Server."));
            return;
        }

        UpdateShadowTask updateShadowTask = new UpdateShadowTask() {
            @Override
            protected void onPostExecute(final AsyncTaskResult<String> result) {
                callback.onUpdateShadow(result.getResult(), result.getError());
            }
        };
        updateShadowTask.setThingName(name);
        updateShadowTask.setState(makeJson(key, value).toString());
        updateShadowTask.execute();
    }

    /**
     * Shadowのクリア.
     *
     * @param name Shadow名
     */
    public void clearShadow(final String name) {
        UpdateShadowTask updateShadowTask = new UpdateShadowTask();
        updateShadowTask.setThingName(name);
        updateShadowTask.setState("{\"state\":{\"reported\":null}}");
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
            jsonState.put("reported", jsonDesired);
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
            jsonState.put("reported", jsonReported);
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
    }

    /**
     * Shadowを取得するAsyncTask.
     */
    private class GetShadowTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

        /**
         * Thing名.
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
                return new AsyncTaskResult<>("");
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
    }

    /**
     * MQTTへの接続.
     */
    public void connectMQTT(final ConnectCallback callback) {
        if (DEBUG) {
            Log.i(TAG, "connectMQTT");
        }

        if (mMqttManager != null) {
            if (callback != null) {
                callback.onConnected(null);
            }
            return;
        }

        String clientId = UUID.randomUUID().toString();
        mMqttManager = new AWSIotMqttManager(clientId, mAWSIotEndPoint);
        mMqttManager.setKeepAlive(120);
        mMqttManager.setConnectionStabilityTime(180);
        mMqttManager.setReconnectRetryLimits(1, 5);
        mMqttManager.setMaxAutoReconnectAttepts(-1);
        mMqttManager.setAutoReconnect(true);

        try {
            mMqttManager.connect(mCredentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
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
                            if (callback != null) {
                                callback.onConnected(null);
                            }
                        }
                    } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                    } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
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

    public void disconnectMQTT() {
        if (mMqttManager != null) {
            mMqttManager.disconnect();
            mMqttManager = null;
        }
        mIsConnected = false;
    }

    public void subscribe(final String topic, final MessageCallback callback) {
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
                            callback.onReceivedMessage(topic, new String(data, "UTF-8"), null);
                        } catch (UnsupportedEncodingException e) {
                            callback.onReceivedMessage(topic, null, e);
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
                    if (DEBUG) {
                        Log.i(TAG, "AWSIotController#publish: MessageDeliveryStatus=" + status);
                    }
                }
            }, null);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Publish error.", e);
            }
            // TODO: エラー処理
        }
    }

    /**
     * 接続状態を返す.
     * @return true(接続中) / false(切断中)
     */
    public Boolean isConnected() {
        return mIsConnected;
    }

    /**
     * 接続状態をリセット.
     */
    public void resetConnected() {
        mIsConnected = false;
    }

    /** endpoint情報取得関数 */
    public String getAWSIotEndPoint() {
        return mAWSIotEndPoint;
    }
}
