/*
 ConnectionErrorView.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.CommunicationHistory;
import org.deviceconnect.android.manager.plugin.ConnectionError;
import org.deviceconnect.android.manager.plugin.DevicePlugin;

import java.util.List;

/**
 * プラグインとの接続に関するエラーの表示.
 *
 * @author NTT DOCOMO, INC.
 */
public class ConnectionErrorView extends LinearLayout {

    private static final int DEFAULT_VISIBILITY = View.GONE;

    private final TextView mErrorView;

    public ConnectionErrorView(final Context context,
                               final @Nullable AttributeSet attrs) {
        super(context, attrs);
        setVisibility(DEFAULT_VISIBILITY);

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_plugin_connection_error, this);
        mErrorView = (TextView) layout.findViewById(R.id.plugin_connection_error_message);
    }

    public void showErrorMessage(final DevicePlugin plugin) {
        ConnectionError error = plugin.getCurrentConnectionError();
        if (error != null) {
            showErrorMessage(error);
            return;
        }

        CommunicationHistory history = plugin.getHistory();
        List<CommunicationHistory.Info> timeouts = history.getNotRespondedCommunications();
        List<CommunicationHistory.Info> responses = history.getRespondedCommunications();
        if (timeouts.size() > 0) {
            CommunicationHistory.Info timeout = timeouts.get(timeouts.size() - 1);
            boolean showsWarning;
            if (responses.size() > 0) {
                CommunicationHistory.Info response = responses.get(responses.size() - 1);
                showsWarning = response.getStartTime() < timeout.getStartTime();
            } else {
                showsWarning = true;
            }
            // NOTE: 最も直近のリクエストについて応答タイムアウトが発生した場合のみ下記のエラーを表示.
            if (showsWarning) {
                showErrorMessage(R.string.dconnect_error_response_timeout);
            }
            return;
        }

        setVisibility(DEFAULT_VISIBILITY);
        mErrorView.setText(null);
    }

    public void showErrorMessage(final ConnectionError error) {
        if (error != null) {
            int messageId = -1;
            switch (error) {
                case NOT_PERMITTED:
                    messageId = R.string.dconnect_error_connection_not_permitted;
                    break;
                case NOT_RESPONDED:
                    messageId = R.string.dconnect_error_connection_timeout;
                    break;
                case TERMINATED:
                    messageId = R.string.dconnect_error_connection_terminated;
                    break;
                case INTERNAL_ERROR:
                    messageId = R.string.dconnect_error_connection_internal_error;
                    break;
                default:
                    break;
            }
            showErrorMessage(messageId);
        }
    }

    private void showErrorMessage(final int messageId) {
        if (messageId != -1) {
            String message = getContext().getString(messageId);
            mErrorView.setText(message);
            setVisibility(View.VISIBLE);
        }
    }
}
