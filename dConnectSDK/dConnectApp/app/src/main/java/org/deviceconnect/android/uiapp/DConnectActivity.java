/*
 DConnectActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.android.client.activity.FragmentPagerActivity;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.uiapp.activity.PluginListActivity;
import org.deviceconnect.android.uiapp.device.SmartDevice;
import org.deviceconnect.android.uiapp.fragment.ServiceListFragment;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectMessage.ErrorCode;
import org.deviceconnect.message.client.DConnectClient;
import org.deviceconnect.message.http.event.CloseHandler;
import org.deviceconnect.message.http.event.HttpEventManager;
import org.deviceconnect.message.http.impl.client.HttpDConnectClient;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;
import org.deviceconnect.utils.AuthProcesser;
import org.deviceconnect.utils.AuthProcesser.AuthorizationHandler;
import org.deviceconnect.utils.URIBuilder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.widget.Toast;


/**
 * Device Connectアクティビティ.
 */
public class DConnectActivity extends FragmentPagerActivity {

    /**
     * Device Connectクライアント.
     */
    private DConnectClient mDConnectClient;

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("deviceconnect.uiapp");
    
    /**
     * ローディングフラグメント.
     */
    private DialogFragment mLoadingFragment;

    /**
     * LocalOAuthのエラー結果を保持する.
     * nullの場合はエラーなしを表す。
     */
    private ErrorCode mError;

    /**
     * Local OAuthに使用するスコープ一覧.
     */
    private String[] mScopes = {
        AuthorizationProfileConstants.PROFILE_NAME,
        BatteryProfileConstants.PROFILE_NAME,
        ConnectProfileConstants.PROFILE_NAME,
        DeviceOrientationProfileConstants.PROFILE_NAME,
        FileDescriptorProfileConstants.PROFILE_NAME,
        FileProfileConstants.PROFILE_NAME,
        MediaPlayerProfileConstants.PROFILE_NAME,
        MediaStreamRecordingProfileConstants.PROFILE_NAME,
        ServiceDiscoveryProfileConstants.PROFILE_NAME,
        ServiceInformationProfileConstants.PROFILE_NAME,
        NotificationProfileConstants.PROFILE_NAME,
        PhoneProfileConstants.PROFILE_NAME,
        ProximityProfileConstants.PROFILE_NAME,
        SettingsProfileConstants.PROFILE_NAME,
        SystemProfileConstants.PROFILE_NAME,
        VibrationProfileConstants.PROFILE_NAME,

        // 独自プロファイル
        "light",
        "camera",
        "temperature",
        "dice",
        "sphero",
        "drive_controller",
        "remote_controller",
        "mhealth",

        // テスト用
        "*"
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler("deviceconnect.uiapp");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }

        mLogger.entering(getClass().getName(), "onCreate", savedInstanceState);
        super.onCreate(savedInstanceState);

        mDConnectClient = new HttpDConnectClient();
        HttpEventManager.INSTANCE.setOrigin(getPackageName());

        (new ServiceDiscoveryTask()).execute();

        mLogger.exiting(getClass().getName(), "onCreate");
    }

    
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        // エラーがあった場合には、ダイアログを表示する
        if (mError != null) {
            clearFragmentList();
            mLoadingFragment = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(final Bundle savedInstanceState) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(DConnectActivity.this);
                    builder.setTitle(R.string.activity_failed_to_get_accesstoken);
                    builder.setMessage(mError.toString());
                    builder.setCancelable(true);
                    mError = null;
                    return builder.create();
                }
            };
            mLoadingFragment.show(getSupportFragmentManager(), "test");
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        mLogger.entering(getClass().getName(), "onOptionsItemSelected", item);

        boolean result;
        if (super.onOptionsItemSelected(item)) {
            result = true;
        } else {
            switch (item.getItemId()) {
            case R.id.action_refresh:
                getSupportFragmentManager().popBackStack();
                (new ServiceDiscoveryTask()).execute();
                result = true;
                break;
            case R.id.action_access_token:
                authrize();
                result = true;
                break;
            case R.id.action_open_websocket:
                openWebsocket();
                result = true;
                break;
            case R.id.action_plugins:
                openPluginList();
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
            }
        }

        mLogger.exiting(getClass().getName(), "onOptionsItemSelected", result);
        return result;
    }

    /**
     * アクセストークンを取得する.
     * アクセストークンがない場合にはnullを返却する。
     * @return アクセストークン
     */
    private String getAccessToken() {
        return ((DConnectApplication) getApplication()).getAccessToken();
    }

    /**
     * クライアントIDを取得する.
     * @return クライアントID
     */
    private String getClientId() {
        return ((DConnectApplication) getApplication()).getClientId();
    }

    /**
     * SSLフラグを取得する.
     * @return SSLを使用する場合はtrue、それ以外はfalse
     */
    private boolean isSSL() {
        return ((DConnectApplication) getApplication()).isSSL();
    }

    /**
     * ホスト名を取得する.
     * @return ホスト名
     */
    private String getHost() {
        return ((DConnectApplication) getApplication()).getHostName();
    }

    /**
     * ホートを取得する.
     * @return ポート番号
     */
    private int getPort() {
        return ((DConnectApplication) getApplication()).getPort();
    }

    /**
     * Local OAuthの認証を行う.
     */
    private void authrize() {
        boolean isSSL = isSSL();
        String host = getHost();
        int port = getPort();
        String appName = getResources().getString(R.string.app_name);
        String clientId = getClientId();
        if (!isStringEmpty(clientId)) {
            AuthProcesser.asyncRefreshToken(host, port, isSSL, clientId,
                    getPackageName(), appName, mScopes, mAuthHandler);
        } else {
            AuthProcesser.asyncAuthorize(host, port, isSSL, getPackageName(), appName, mScopes, mAuthHandler);
        }
    }

    /**
     * プラグインリスト画面を表示する.
     */
    private void openPluginList() {
        Intent intent = new Intent();
        intent.setClass(this, PluginListActivity.class);
        startActivity(intent);
    }

    /**
     * 空文字列のチェックを行う.
     * @param str チェックを行う文字列
     * @return 空文字の場合はtrue、それ以外はfalse
     */
    private boolean isStringEmpty(final String str) {
        if (str == null) {
            return true;
        }
        if (str.length() <= 0) {
            return true;
        }
        return false;
    }

    /**
     * Websocketを開く.
     */
    private void openWebsocket() {
        boolean isSSL = isSSL();
        String host = getHost();
        int port = getPort();

        String sessionKey = getClientId();
        if (sessionKey == null) {
            Toast.makeText(DConnectActivity.this, "Client is not created.", Toast.LENGTH_LONG).show();
            return;
        }
        boolean result = HttpEventManager.INSTANCE.connect(host, port, isSSL, sessionKey, new CloseHandler() {
            @Override
            public void onClosed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DConnectActivity.this, "Websocket Close.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        if (result) {
            Toast.makeText(DConnectActivity.this, "Websocket open.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(DConnectActivity.this, "Failed to open websocket.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Local OAuthのリスナー.
     */
    private AuthorizationHandler mAuthHandler =  new AuthorizationHandler() {
        @Override
        public void onAuthorized(final String clientId, final String accessToken) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(getString(R.string.key_settings_dconn_client_id), clientId);
            edit.putString(getString(R.string.key_settings_dconn_access_token), accessToken);
            edit.commit();
            mError = null;
            DConnectActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DConnectActivity.this, "Success.", Toast.LENGTH_LONG).show();
                }
            });
        }
        @Override
        public void onAuthFailed(final ErrorCode error) {
            mError = error;

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = prefs.edit();
            edit.remove(getString(R.string.key_settings_dconn_client_id));
            edit.remove(getString(R.string.key_settings_dconn_access_token));
            edit.commit();

            DConnectActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DConnectActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    /**
     * サービス検索タスク.
     */
    private class ServiceDiscoveryTask extends AsyncTask<Void, Integer, List<SmartDevice>> {
        @Override
        protected void onPreExecute() {
            clearFragmentList();
            mLoadingFragment = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(final Bundle savedInstanceState) {
                    final ProgressDialog dialog = new ProgressDialog(getActivity());
                    dialog.setCancelable(false);
                    dialog.setMessage(getString(R.string.searching_device));
                    return dialog;
                }
            };
            mLoadingFragment.show(getSupportFragmentManager(), null);
        }

        @Override
        protected List<SmartDevice> doInBackground(final Void... params) {
            mLogger.entering(getClass().getName(), "doInBackground", params);

            List<SmartDevice> devices = new ArrayList<SmartDevice>();

            DConnectMessage message = null;

            try {
                URIBuilder builder = new URIBuilder();
                builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);

                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                if (prefs.getBoolean(getString(R.string.key_settings_dconn_ssl), false)) {
                    builder.setScheme("https");
                } else {
                    builder.setScheme("http");
                }

                builder.setHost(prefs.getString(
                        getString(R.string.key_settings_dconn_host),
                        getString(R.string.default_host)));

                builder.setPort(Integer.parseInt(prefs.getString(
                        getString(R.string.key_settings_dconn_port),
                        getString(R.string.default_port))));

                builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());

                HttpUriRequest request = new HttpGet(builder.build());
                request.addHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, getPackageName());
                mLogger.info(request.getMethod() + " " + request.getURI());
                HttpResponse response = mDConnectClient.execute(request);
                message = (new HttpMessageFactory()).newDConnectMessage(response);
            } catch (URISyntaxException e) {
                e.printStackTrace();

                mLogger.exiting(getClass().getName(), "doInBackground", devices);
                return devices;
            } catch (IOException e) {
                e.printStackTrace();

                mLogger.exiting(getClass().getName(), "doInBackground", devices);
                return devices;
            }

            if (message == null) {
                mLogger.exiting(getClass().getName(), "doInBackground", devices);
                return devices;
            }

            int result = message.getInt(DConnectMessage.EXTRA_RESULT);
            if (result == DConnectMessage.RESULT_ERROR) {
                mLogger.exiting(getClass().getName(), "doInBackground", devices);
                return devices;
            }

            List<Object> services = message.getList(
                    ServiceDiscoveryProfileConstants.PARAM_SERVICES);
            if (services != null) {
                for (Object object: services) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> service = (Map<String, Object>) object;
                    SmartDevice device = new SmartDevice(
                        service.get(ServiceDiscoveryProfileConstants.PARAM_ID).toString(),
                        service.get(ServiceDiscoveryProfileConstants.PARAM_NAME).toString());
                    devices.add(device);
                    mLogger.info("Found smart device: " + device.getId());
                }
            }

            mLogger.exiting(getClass().getName(), "doInBackground", devices);
            return devices;
        }

        @Override
        protected void onPostExecute(final List<SmartDevice> result) {
            mLogger.entering(getClass().getName(), "onPostExecute", result);

            if (isFinishing()) {
                mLogger.fine("activity is finishing");
                mLogger.exiting(getClass().getName(), "onPostExecute");
                return;
            }

            if (mLoadingFragment != null) {
                mLoadingFragment.dismiss();
            }
            List<Fragment> fragments = new ArrayList<Fragment>();
            for (SmartDevice device: result) {
                ServiceListFragment serviceListFragment = new ServiceListFragment();

                Bundle args = new Bundle();

                args.putParcelable("device", device);
                args.putString(Intent.EXTRA_TITLE, device.getName());

                serviceListFragment.setArguments(args);

                fragments.add(serviceListFragment);
            }
            setFragmentList(fragments);

            mLogger.exiting(getClass().getName(), "onPostExecute");

        }

    }

}
