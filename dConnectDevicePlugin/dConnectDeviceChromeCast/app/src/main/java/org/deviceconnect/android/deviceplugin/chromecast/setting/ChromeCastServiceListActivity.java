package org.deviceconnect.android.deviceplugin.chromecast.setting;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastApplication;
import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.R;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
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
        toolbar.setNavigationIcon(R.drawable.close_icon);
        toolbar.setNavigationOnClickListener((view) -> {
            finish();
        });
        LinearLayout layout = findViewById(R.id.fragment_container);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        layout.setPadding(0, actionBarSize, 0, 0);
    }

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return ChromeCastService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return ChromeCastSettingFragmentActivity.class;
    }
    @Override
    public void onServiceRemoved(final DConnectService service) {
        super.onServiceRemoved(service);
        ChromeCastApplication application = (ChromeCastApplication) getApplication();
        if (application != null) {
            //ChromeCastのサービスが削除されたタイミングでChromeCastControllerを初期化する
            application.getController().reconnect();

        }
    }
}
