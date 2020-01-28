package com.example.switchbotdemoapp.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


import androidx.annotation.NonNull;

import com.example.switchbotdemoapp.BuildConfig;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.DConnectSDKFactory;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;

import java.util.ArrayList;
import java.util.HashMap;

public class DConnectWrapper {
    private static final String TAG = "DConnectWrapper";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    private static final String SERVICE_DISCOVERY_THREAD_TAG = "dconnect_wrapper_service_discovery";
    private static final Long SERVICE_DISCOVERY_TASK_CYCLE = 10000L;
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static DConnectSDK mDConnectSDK;
    private static String[] mScopes;
    private static String mAppName;
    public static final String PARAM_SERVICE_ID = "serviceId";
    private static final ArrayList<ServiceDiscoveryCallback> mServiceDiscoveryCallbacks = new ArrayList<>();
    private static final HandlerThread mHandlerThread = new HandlerThread(SERVICE_DISCOVERY_THREAD_TAG);
    private static Handler mHandler;
    private static Runnable mServiceDiscoveryTask = new Runnable() {
        @Override
        public void run() {
            mDConnectSDK.serviceDiscovery(new DConnectSDK.OnResponseListener() {
                @Override
                public void onResponse(DConnectResponseMessage dConnectResponseMessage) {
                    if (DEBUG) {
                        Log.d(TAG, "onSuccess()");
                        Log.d(TAG, "result : " + dConnectResponseMessage.getResult());
                    }
                    if (dConnectResponseMessage.getResult() == DConnectResponseMessage.RESULT_OK) {
                        ArrayList<Service> serviceList = new ArrayList<>();
                        for(Object object : dConnectResponseMessage.getList("services")) {
                            final DConnectMessage service = (DConnectMessage)object;
                            final String serviceId = service.getString("id");
                            final String serviceName = service.getString("name");
                            if (DEBUG) {
                                Log.d(TAG, "serviceId : " + serviceId);
                                Log.d(TAG, "serviceName : " + serviceName);
                            }
                            if(service.getString("id").contains("SwitchBot.Device")) {
                                serviceList.add(new Service(serviceId, serviceName));
                            }
                        }
                        synchronized (mServiceDiscoveryCallbacks) {
                            for(ServiceDiscoveryCallback serviceDiscoveryCallback : mServiceDiscoveryCallbacks) {
                                serviceDiscoveryCallback.onSuccess(serviceList);
                            }
                        }
                        mHandler.postDelayed(mServiceDiscoveryTask, SERVICE_DISCOVERY_TASK_CYCLE);
                    } else {
                        final int errorCode = dConnectResponseMessage.getErrorCode();
                        final DConnectResponseMessage.ErrorCode error = DConnectResponseMessage.ErrorCode.getInstance(errorCode);
                        final String errorMessage = dConnectResponseMessage.getErrorMessage();
                        Log.e(TAG, "errorCode : " + errorCode);
                        Log.e(TAG, "errorMessage : " + errorMessage);
                        if (error == DConnectResponseMessage.ErrorCode.NOT_FOUND_CLIENT_ID ||
                                error == DConnectResponseMessage.ErrorCode.EMPTY_ACCESS_TOKEN ||
                                error == DConnectResponseMessage.ErrorCode.EXPIRED_ACCESS_TOKEN) {
                            getAccessToken();
                        }
                        synchronized (mServiceDiscoveryCallbacks) {
                            for(ServiceDiscoveryCallback serviceDiscoveryCallback : mServiceDiscoveryCallbacks) {
                                serviceDiscoveryCallback.onFailure(
                                        dConnectResponseMessage.getErrorCode(),
                                        dConnectResponseMessage.getErrorMessage()
                                );
                            }
                        }
                        mHandler.postDelayed(mServiceDiscoveryTask, SERVICE_DISCOVERY_TASK_CYCLE);
                    }
                }
            });
        }
    };

    public enum Method {
        POST("post");
        private final String method;

        Method(final String method) {
            this.method = method;
        }

        public String getString() {
            return this.method;
        }
    }

    public static void initialize(final Context context, final String appName, final String host, final int port, final String[] scopes) {
        if (DEBUG) {
            Log.d(TAG, "initialize()");
        }
        mContext = context;
        mDConnectSDK = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
        mDConnectSDK.setHost(host);
        mDConnectSDK.setPort(port);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mScopes = scopes;
        mAppName = appName;
        setAccessToken();
    }

    private static void getAccessToken() {
        if (DEBUG) {
            Log.d(TAG, "getAccessToken()");
        }
        mDConnectSDK.authorization(mAppName, mScopes, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(String clientId, String accessToken) {
                if (DEBUG) {
                    Log.d(TAG, "onSuccess()");
                    Log.d(TAG, "clientId : " + clientId);
                    Log.d(TAG, "accessToken : " + accessToken);
                }
                Settings.putString(mContext, KEY_CLIENT_ID, clientId);
                Settings.putString(mContext, KEY_ACCESS_TOKEN, accessToken);
                mDConnectSDK.setAccessToken(accessToken);
                mHandler.removeCallbacks(mServiceDiscoveryTask);
                mHandler.post(mServiceDiscoveryTask);
            }

            @Override
            public void onError(final int errorCode, final String errorMessage) {
                Log.e(TAG, "onFailure()");
                Log.e(TAG, "errorCode : " + errorCode);
                Log.e(TAG, "errorMessage : " + errorMessage);
            }
        });
    }

    private static void setAccessToken() {
        if (DEBUG) {
            Log.d(TAG,"setAccessToken()");
        }
        final String accessToken = Settings.getString(mContext, KEY_ACCESS_TOKEN, null);
        if (accessToken != null) {
            if (DEBUG) {
                Log.d(TAG, "accessToken : " + accessToken);
            }
            mDConnectSDK.setAccessToken(accessToken);
        }
    }

    public static void registerServiceDiscoveryCallback(@NonNull final ServiceDiscoveryCallback serviceDiscoveryCallback) {
        if (DEBUG) {
            Log.d(TAG,"registerServiceDiscoveryCallback()");
        }
        synchronized (mServiceDiscoveryCallbacks) {
            mServiceDiscoveryCallbacks.add(serviceDiscoveryCallback);
        }
    }

    public static void unregisterServiceDiscoveryCallback(@NonNull final ServiceDiscoveryCallback serviceDiscoveryCallback) {
        if (DEBUG) {
            Log.d(TAG,"unregisterServiceDiscoveryCallback()");
        }
        synchronized (mServiceDiscoveryCallbacks) {
            mServiceDiscoveryCallbacks.remove(serviceDiscoveryCallback);
        }
    }

    public static void startServiceDiscovery() {
        if (DEBUG) {
            Log.d(TAG,"startServiceDiscovery()");
        }
        mHandler.post(mServiceDiscoveryTask);
    }

    public static void stopServiceDiscovery() {
        if (DEBUG) {
            Log.d(TAG,"stopServiceDiscovery()");
        }
        mHandler.removeCallbacks(mServiceDiscoveryTask);
    }

    public static void post(final String profile, final String attribute, final String serviceId,
                     final HashMap<String, String> params, final RestApiCallback restApiCallback) {
        if (DEBUG) {
            Log.d(TAG, "post()");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                DConnectSDK.URIBuilder uriBuilder = mDConnectSDK.createURIBuilder();
                uriBuilder.setScheme("http");
                uriBuilder.setHost(mDConnectSDK.getHost());
                uriBuilder.setPort(mDConnectSDK.getPort());
                uriBuilder.setProfile(profile);
                uriBuilder.setAttribute(attribute);
                uriBuilder.setServiceId(serviceId);
                if (DEBUG) {
                    Log.d(TAG, "uri : " + uriBuilder.build().toString());
                }
                MultipartEntity entity = new MultipartEntity();
                for (String key : params.keySet()) {
                    String param = params.get(key);
                    if (param != null) {
                        entity.add(key, new StringEntity(param));
                    }
                }
                mDConnectSDK.post(uriBuilder.build(), entity, new DConnectSDK.OnResponseListener() {
                    @Override
                    public void onResponse(DConnectResponseMessage dConnectResponseMessage) {
                        if (dConnectResponseMessage.getResult() == DConnectResponseMessage.RESULT_OK) {
                            restApiCallback.onSuccess(profile, attribute, Method.POST, serviceId, dConnectResponseMessage);
                        } else {
                            restApiCallback.onFailure(profile, attribute, Method.POST, serviceId, dConnectResponseMessage.getErrorCode(), dConnectResponseMessage.getErrorMessage());
                        }
                    }
                });
            }
        }).start();
    }

    public static class Service {
        private final String mServiceId;
        private final String mServiceName;

        Service(final String serviceId, final String serviceName) {
            mServiceId = serviceId;
            mServiceName = serviceName;
        }

        public String getServiceId() {
            return mServiceId;
        }

        public String getServiceName() {
            return mServiceName;
        }
    }

    public interface ServiceDiscoveryCallback {
        void onSuccess(final ArrayList<Service> serviceList);
        void onFailure(final int errorCode, final String errorMessage);
    }

    public interface RestApiCallback {
        void onSuccess(final String profile, final String attribute, final Method method, final String serviceId, final DConnectResponseMessage dConnectResponseMessage);
        void onFailure(final String profile, final String attribute, final Method method, final String serviceId, final int errorCode, final String errorMessage);
    }
}