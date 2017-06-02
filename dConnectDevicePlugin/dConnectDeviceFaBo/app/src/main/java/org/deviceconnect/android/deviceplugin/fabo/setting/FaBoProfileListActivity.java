package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.fabo.R;

/**
 * プロファイル一覧を表示するActivity.
 */
public class FaBoProfileListActivity extends Activity {

    private ProfileAdapter mProfileAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_profile_list);

        mProfileAdapter = new ProfileAdapter();

        ListView listView = (ListView) findViewById(R.id.activity_fabo_profile_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
            }
        });
        listView.setAdapter(mProfileAdapter);
    }

    /**
     * プロファイルを格納するアダプタ.
     */
    private class ProfileAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int id) {
            return id;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_fabo_profile, null);
            }
            return convertView;
        }
    }
}
