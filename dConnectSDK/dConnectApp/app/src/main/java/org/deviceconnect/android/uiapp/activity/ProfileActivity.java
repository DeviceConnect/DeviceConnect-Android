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
import org.deviceconnect.android.uiapp.data.DCApi;
import org.deviceconnect.android.uiapp.data.DCProfile;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BasicActivity {

    private ApiAdapter mApiAdapter = new ApiAdapter();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mApiAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                openApiActivity(mApiAdapter.mApiList.get(position));
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                getServiceInformation(serviceId, new OnReceivedServiceInformationListener() {
                    @Override
                    public void onReceived(final List<DCProfile> profiles) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DCProfile profile = getProfile(profiles);
                                if (profile != null) {
                                    mApiAdapter.mApiList = profile.getApiList();
                                    mApiAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void openApiActivity(final DCApi api) {
        Intent intent =new Intent();
        intent.setClass(this, ApiActivity.class);
        intent.putExtra("serviceId", getIntent().getStringExtra("serviceId"));
        intent.putExtra("profileName", getIntent().getStringExtra("profileName"));
        intent.putExtra("method", api.getMethod().getValue());
        intent.putExtra("path", api.getPath());
        startActivity(intent);
    }

    private class ApiAdapter extends BaseAdapter {

        private List<DCApi> mApiList = new ArrayList<>();

        @Override
        public int getCount() {
            return mApiList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mApiList.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_api, null);
            }

            DCApi api = mApiList.get(position);

            TextView methodView = (TextView) view.findViewById(R.id.item_method);
            if (methodView != null) {
                methodView.setText(api.getMethod().getValue());
            }

            TextView pathView = (TextView) view.findViewById(R.id.item_path);
            if (pathView != null) {
                pathView.setText(api.getPath());
            }

            return view;
        }
    }
}
