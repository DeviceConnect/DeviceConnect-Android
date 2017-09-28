/*
 ConfirmAuthFramgment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.R;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.LocalOAuth2Service;
import org.deviceconnect.android.localoauth.LocalOAuth2Settings;
import org.deviceconnect.android.localoauth.ScopeUtil;
import org.deviceconnect.android.localoauth.activity.ConfirmAuthActivity;
import org.restlet.ext.oauth.internal.AbstractTokenManager;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 認可ダイアログ.
 * @author NTT DOCOMO, INC.
 */
@SuppressLint("HandlerLeak")
public class ConfirmAuthFragment extends Fragment {
    /** 自動クリック処理を実行するまでの時間[msec]. テスト用。 */
    private static final long AUTO_CLICK_WAIT_TIME = 1000;

    /** デフォルトのタイムアウト時間(msec)を定義. */
    private static final int DEFAULT_TIMEOUT = 60 * 1000;

    /** 自動クリック用タイマー. テスト用。 */
    private Timer mAutoClickTimer;

    /** 呼び出し元のスレッドID. */
    private long mThreadId;

    /** Messenger for communicating with the service. */
    private Messenger mService;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /** 受信用メッセンジャー設定. */
    private Messenger mSelfMessenger;

    /** Flag indicating whether we have done response. */
    private boolean mDoneResponse;

    /** タイムアウト監視. */
    private Timer mTimeoutTimer;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        mThreadId = intent.getLongExtra(ConfirmAuthActivity.EXTRA_THREAD_ID, -1);
        String applicationName = intent.getStringExtra(ConfirmAuthActivity.EXTRA_APPLICATION_NAME);
        String packageName = intent.getStringExtra(ConfirmAuthActivity.EXTRA_PACKAGE_NAME);
        String keyword = intent.getStringExtra(ConfirmAuthActivity.EXTRA_KEYWORD);
        String[] displayScopes = intent.getStringArrayExtra(ConfirmAuthActivity.EXTRA_DISPLAY_SCOPES);
        String expirePeriod = toStringExpiredPeriod();
        boolean isForPlugin = intent.getBooleanExtra(ConfirmAuthActivity.EXTRA_IS_FOR_DEVICEPLUGIN, true);
        long requestTime = intent.getLongExtra(ConfirmAuthActivity.EXTRA_REQUEST_TIME, System.currentTimeMillis());
        long timeout = DEFAULT_TIMEOUT - (System.currentTimeMillis() - requestTime);

        int layoutId;
        if (isForPlugin) {
            layoutId = R.layout.confirm_auth_activity_plugin;
        } else {
            layoutId = R.layout.confirm_auth_activity_manager;
        }
        View view = inflater.inflate(layoutId, container, false);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    notApprovalProc();
                    return true;
                }
                return false;
            }
        });

        // 有効期限
        TextView textViewExpirePeriod = (TextView) view.findViewById(R.id.textViewExpirePeriod);
        textViewExpirePeriod.setText(expirePeriod);

        // アプリ名
        TextView textViewApplicationName = (TextView) view.findViewById(R.id.textViewAccessToken);
        textViewApplicationName.setText(applicationName);

        // スコープ一覧表示
        ListView listViewScopes = (ListView) view.findViewById(R.id.listViewScopes);
        listViewScopes.setAdapter(new ArrayAdapter<>(getActivity(),
               R.layout.confirm_auth_scopes_list_item, R.id.textViewScope,
                displayScopes));
        
        // 承認ボタン
        Button buttonApproval = (Button) view.findViewById(R.id.buttonApproval);
        buttonApproval.setOnClickListener(mOnButtonApprovalClickListener);
        buttonApproval.setEnabled(false);

        // 拒否ボタン
        Button buttonReject = (Button) view.findViewById(R.id.buttonReject);
        buttonReject.setOnClickListener(mOnButtonApprovalClickListener);
        buttonReject.setEnabled(false);

        if (!isForPlugin) {
            // アプリのアクセス先(=マネージャ)のパッケージ名
            TextView textViewPackageName = (TextView) view.findViewById(R.id.textPackageName);
            textViewPackageName.setText(packageName);
            // キーワード
            TextView textViewKeyword = (TextView) view.findViewById(R.id.textKeyword);
            textViewKeyword.setText(keyword);
        }

        // 受信用メッセンジャー設定
        mSelfMessenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                if (msg.what == LocalOAuth2Main.MSG_CHECK_THREAD_ID_RESULT) {
                    // threadIdはキューに存在しないのでActivityを閉じる(残ってしまったアプリ履歴からの起動の場合を想定)
                    if (msg.arg1 == 0 && getActivity() != null) {
                        getActivity().finish();
                    }
                } 
            }
        });

        if (timeout > 0) {
            startTimeoutTimer(timeout);
        } else {
            // タイムアウトになっているので、Activityを閉じる
            getActivity().finish();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ServiceConnectionを渡してServiceとBindする
        Intent intent = new Intent();
        intent.setClass(getActivity(), LocalOAuth2Service.class);
        intent.setPackage(getActivity().getPackageName());
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        cancelAutoClickTimer();
        stopTimeoutTimer();
        notApprovalProc();
        try {
            getActivity().unbindService(mConnection);
        } catch (Exception e) {
            // ignore
        }
        super.onPause();
    }

    /** Class for interacting with the main interface of the service. */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = new Messenger(service);
            mBound = true;

            // Bindが接続されたらthreadId有効チェック
            sendMessageCheckThreadId(mThreadId);
            startAutoClickTimer();

            if (getView() != null) {
                getView().findViewById(R.id.buttonApproval).setEnabled(true);
                getView().findViewById(R.id.buttonReject).setEnabled(true);
            }
        }
        @Override
        public void onServiceDisconnected(final ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

    /**
     * Message通知処理.
     * 
     * @param isApproval true: 許可 / false: 拒否
     */
    private void sendMessage(final Boolean isApproval) {
        if (!mBound) {
            return;
        }

        // Messageを使って許可・拒否のステータスをServiceに送る
        int approval = ConfirmAuthActivity.DISAPPROVAL;
        if (isApproval) {
            approval = ConfirmAuthActivity.APPROVAL;
        }
        Message msg = Message.obtain(null, LocalOAuth2Main.MSG_CONFIRM_APPROVAL,
                (int) mThreadId, approval);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            // サービスがDeadObjectになっている
            getActivity().finish();
        }
    }

    /**
     * threadId有効チェックメッセージ送信.
     * @param threadId 判定するスレッドID
     */
    private void sendMessageCheckThreadId(final long threadId) {
        Message msg = Message.obtain(null, LocalOAuth2Main.MSG_CONFIRM_CHECK_THREAD_ID,
                (int) threadId, 0);
        msg.replyTo = mSelfMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            // サービスがDeadObjectになっている
            getActivity().finish();
        }
    }

    /**
     * 許可ボタンをタップしたときの処理を行うリスナー.
     */
    private OnClickListener mOnButtonApprovalClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            int id = v.getId();
            if (id == R.id.buttonApproval) {
                // 自動クリックタイマーが起動中なら停止する
                cancelAutoClickTimer();
                approvalProc();
            } else if (id == R.id.buttonReject) {
                cancelAutoClickTimer();
                notApprovalProc();
            }
        }
    };

    /**
     * 承認ボタンを押下されたときの処理.
     * <p>
     * 既にレスポンスを返却している場合には何もしない。
     * </p>
     */
    public synchronized void approvalProc() {
        if (mDoneResponse) {
            return;
        }
        sendMessage(true);
        getActivity().finish();
        mDoneResponse = true;
    }

    /**
     * 拒否ボタンを押下されたときの処理.
     * <p>
     * 既にレスポンスを返却している場合には何もしない。
     * </p>
     */
    public synchronized void notApprovalProc() {
        if (mDoneResponse) {
            return;
        }
        sendMessage(false);
        getActivity().finish();
        mDoneResponse = true;
    }

    /**
     * 有効期限日を文字列で返す.
     * @param expirePeriodSec 有効期限[秒]
     * @return 有効期限日を文字列
     */
    private String toExpirePeriodDateString(final long expirePeriodSec) {
        Calendar now = Calendar.getInstance();
        long expirePeriodDateMSec = now.getTimeInMillis() + expirePeriodSec * LocalOAuth2Settings.MSEC;
        return ScopeUtil.getDisplayExpirePeriodDate(expirePeriodDateMSec);
    }

    /**
     * 有効期限の文字列を取得する.
     * @return 有効期限の文字列
     */
    private String toStringExpiredPeriod() {
        String expirePeriodFormat = getString(R.string.expire_period_format);
        String expirePeriodValue = toExpirePeriodDateString(AbstractTokenManager.DEFAULT_TOKEN_EXPIRE_PERIOD);
        return String.format(expirePeriodFormat, expirePeriodValue);
    }

    /**
     * 自動クリックタイマーを開始する.
     * <p>
     * デバッグ用メソッド.<br>
     * {@link LocalOAuth2Main#isAutoTestMode()}がfalseの場合には開始しない。
     * </p>
     */
    private void startAutoClickTimer() {
        // 自動テストモードの場合は、タイマーを開始する
        if (LocalOAuth2Main.isAutoTestMode()) {
            if (mAutoClickTimer == null) {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    approvalProc();
                                }
                            });
                        }
                    }
                };
                mAutoClickTimer = new Timer(true);
                mAutoClickTimer.schedule(timerTask, AUTO_CLICK_WAIT_TIME);
            }
        }
    }

    /**
     * 自動クリックタイマーを停止する.
     * <p>
     * デバッグ用メソッド.<br>
     * </p>
     */
    private void cancelAutoClickTimer() {
        if (mAutoClickTimer != null) {
            mAutoClickTimer.cancel();
            mAutoClickTimer = null;
        }
    }

    /**
     * リクエストが実行するまでのタイムアウトを開始する.
     * @param timeout タイムアウト時間(ms)
     */
    private void startTimeoutTimer(final long timeout) {
        if (mTimeoutTimer == null) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Activity a = getActivity();
                    if (a != null) {
                        a.finish();
                    }
                }
            };
            mTimeoutTimer = new Timer(true);
            mTimeoutTimer.schedule(timerTask, timeout);
        }
    }

    /**
     * タイムアウト用のタイマーを停止する.
     */
    private void stopTimeoutTimer() {
        if (mTimeoutTimer != null) {
            mTimeoutTimer.cancel();
            mTimeoutTimer = null;
        }
    }
}
