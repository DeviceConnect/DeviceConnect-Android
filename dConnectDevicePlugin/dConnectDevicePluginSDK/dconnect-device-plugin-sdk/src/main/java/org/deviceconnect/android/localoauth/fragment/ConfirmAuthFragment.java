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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
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
    /** デフォルトのタイムアウト時間(msec)を定義. */
    private static final int DEFAULT_TIMEOUT = 60 * 1000;

    /** 呼び出し元のスレッドID. */
    private long mThreadId;

    /** Flag indicating whether we have done response. */
    private boolean mDoneResponse;

    /** タイムアウト監視. */
    private Timer mTimeoutTimer;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            getActivity().finish();
            return null;
        }
        mThreadId = intent.getLongExtra(ConfirmAuthActivity.EXTRA_THREAD_ID, -1);
        if (mThreadId == -1) {
            getActivity().finish();
            return null;
        }

        String applicationName = intent.getStringExtra(ConfirmAuthActivity.EXTRA_APPLICATION_NAME);
        String packageName = intent.getStringExtra(ConfirmAuthActivity.EXTRA_PACKAGE_NAME);
        String keyword = intent.getStringExtra(ConfirmAuthActivity.EXTRA_KEYWORD);
        String[] displayScopes = intent.getStringArrayExtra(ConfirmAuthActivity.EXTRA_DISPLAY_SCOPES);
        String expirePeriod = toStringExpiredPeriod();
        boolean isForPlugin = intent.getBooleanExtra(ConfirmAuthActivity.EXTRA_IS_FOR_DEVICEPLUGIN, true);
        boolean isAutoFlag= intent.getBooleanExtra(ConfirmAuthActivity.EXTRA_AUTO_FLAG, false);
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

        // 拒否ボタン
        Button buttonReject = (Button) view.findViewById(R.id.buttonReject);
        buttonReject.setOnClickListener(mOnButtonApprovalClickListener);

        if (!isForPlugin) {
            // アプリのアクセス先(=マネージャ)のパッケージ名
            TextView textViewPackageName = (TextView) view.findViewById(R.id.textPackageName);
            textViewPackageName.setText(packageName);
            // キーワード
            TextView textViewKeyword = (TextView) view.findViewById(R.id.textKeyword);
            textViewKeyword.setText(keyword);
        }

        if (timeout > 0) {
            startTimeoutTimer(timeout);
        } else {
            // タイムアウトになっているので、Activityを閉じる
            getActivity().finish();
        }

        if (isAutoFlag) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    approvalProc();
                }
            });
        }

        return view;
    }

    @Override
    public void onPause() {
        stopTimeoutTimer();
        notApprovalProc();
        super.onPause();
    }

    /**
     * Message通知処理.
     * 
     * @param isApproval true: 許可 / false: 拒否
     */
    private void sendMessage(final Boolean isApproval) {
        Intent intent = new Intent();
        intent.setAction(LocalOAuth2Main.ACTION_TOKEN_APPROVAL);
        intent.putExtra(LocalOAuth2Main.EXTRA_THREAD_ID, mThreadId);
        intent.putExtra(LocalOAuth2Main.EXTRA_APPROVAL, isApproval);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    /**
     * 許可ボタンをタップしたときの処理を行うリスナー.
     */
    private OnClickListener mOnButtonApprovalClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            int id = v.getId();
            if (id == R.id.buttonApproval) {
                approvalProc();
            } else if (id == R.id.buttonReject) {
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
