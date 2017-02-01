package org.deviceconnect.android.uiapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.data.DCProfile;

import java.util.ArrayList;
import java.util.List;

public class ServiceActivity extends BasicActivity {

    private ProfileAdapter mProfileAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        Intent intent = getIntent();
        if (intent != null) {
            String serviceId = intent.getStringExtra("serviceId");
            String name = intent.getStringExtra("name");
            if (serviceId != null) {
                getServiceInformation(serviceId, new OnReceivedServiceInformationListener() {
                    @Override
                    public void onReceived(final List<DCProfile> profiles) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProfileAdapter.mProfileList = profiles;
                                mProfileAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
            if (name != null) {
                setTitle(name);
            }
        }

        mProfileAdapter = new ProfileAdapter();

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mProfileAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                openProfileActivity(mProfileAdapter.mProfileList.get(position));
            }
        });
    }

    private void openProfileActivity(final DCProfile profile) {
        Intent intent = new Intent();
        intent.setClass(this, ProfileActivity.class);
        intent.putExtra("serviceId", getIntent().getStringExtra("serviceId"));
        intent.putExtra("profileName", profile.getName());
        startActivity(intent);
    }

    private class ProfileAdapter extends BaseAdapter {

        private List<DCProfile> mProfileList = new ArrayList<>();

        @Override
        public int getCount() {
            return mProfileList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mProfileAdapter.getItem(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_profile_list, null);
            }

            DCProfile profile = mProfileList.get(position);

            TextView textView = (TextView) view.findViewById(R.id.item_name);
            if (textView != null) {
                textView.setText(profile.getName());
            }

            return view;
        }
    }
}
