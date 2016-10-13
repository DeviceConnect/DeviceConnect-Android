package org.deviceconnect.android.deviceplugin.alljoyn.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.alljoyn.R;

import java.util.Arrays;
import java.util.List;

/**
 * Setting activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynSettingActivity extends Activity {

    private static final List<SettingData> SETTING_LIST = Arrays.asList(
            new SettingData(R.layout.aj_settings_lifx_list_item, AllJoynSettingLIFXActivity.class)
    );

    private LayoutInflater mInflator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aj_settings_master);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(getString(R.string.alljoyn_settings_master_textViewTopBar_text));
        }

        mInflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new SettingListAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingData settingData = SETTING_LIST.get(position);
                Intent intent = new Intent(AllJoynSettingActivity.this,
                        settingData.detailActivityClass);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SettingListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return SETTING_LIST.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                Integer tagPosition = (Integer) convertView.getTag();
                if (tagPosition == position) {
                    return convertView;
                }
            }

            SettingData settingData = SETTING_LIST.get(position);
            View view = mInflator.inflate(settingData.listItemLayoutId, null);
            view.setTag(position);
            return view;
        }
    }

    private static class SettingData {
        public final int listItemLayoutId;
        public final Class detailActivityClass;

        public SettingData(int listItemLayoutId, Class detailActivityClass) {
            this.listItemLayoutId = listItemLayoutId;
            this.detailActivityClass = detailActivityClass;
        }
    }
}
