package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.R;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * Chromecastサービス一覧画面.
 * @author NTT DOCOMO, INC.
 */

public class ChromeCastServiceListActivity extends DConnectServiceListActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle(R.string.activity_service_list_title);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setBackgroundColor(Color.parseColor("#00a0e9"));
        addContentView(toolbar, new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        toolbar.setNavigationIcon(R.drawable.ic_close_light);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        LinearLayout layout = (LinearLayout) findViewById(R.id.fragment_container);
        layout.setPadding(0, 200, 0, 0);
    }

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return ChromeCastService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return ChromeCastSettingFragmentActivity.class;
    }
}
