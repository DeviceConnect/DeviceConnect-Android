package org.deviceconnect.android.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.R;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
import org.deviceconnect.android.service.DConnectServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class DConnectServiceListActivity extends Activity {

    private DConnectServiceProvider mProvider;

    private boolean mIsBound;

    private final Object mLock = new Object();

    private final Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            info("onServiceConnected: " + name);
            synchronized (mLock) {
                mProvider = ((DConnectMessageService.LocalBinder) service).getMessageService()
                    .getServiceProvider();
                mProvider.addServiceListener(mServiceListener);

                List<ServiceContainer> containers = new ArrayList<ServiceContainer>();
                for (DConnectService entity : mProvider.getServiceList()) {
                    containers.add(new ServiceContainer(entity));
                }
                mListAdapter = new ServiceListAdapter(DConnectServiceListActivity.this, containers);
                mListView.setAdapter(mListAdapter);
                mListView.setItemsCanFocus(true);

                mIsBound = true;
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            info("onServiceDisconnected: " + name);
            mIsBound = false;
        }
    };

    private DConnectServiceListener mServiceListener = new DConnectServiceListener() {
        @Override
        public void onServiceAdded(final DConnectService service) {
            info("onServiceAdded: " + service.getId());
            synchronized (mLock) {
                if (mIsBound) {
                    mListAdapter.add(new ServiceContainer(service));
                }
            }
        }

        @Override
        public void onServiceRemoved(final DConnectService service) {
            info("onServiceRemoved: " + service.getId());
            synchronized (mLock) {
                ServiceContainer container = mListAdapter.getServiceContainer(service.getId());
                if (container != null) {
                    mListAdapter.remove(container);
                }
            }
        }

        @Override
        public void onStatusChange(final DConnectService service) {
            info("onStatusChange: " + service.getId());
            // NOP.
        }
    };

    private ServiceListAdapter mListAdapter;

    private ListView mListView;

    private Button mNewServiceButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        mListView = (ListView) findViewById(R.id.device_connect_service_list_view);
        mNewServiceButton = (Button) findViewById(R.id.device_connect_service_list_button_new_service);
        mNewServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onServiceRegistration();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsBound) {
            Intent intent = new Intent(getApplicationContext(), getMessageServiceClass());
            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsBound) {
            getApplicationContext().unbindService(mConnection);
            mProvider.removeServiceListener(mServiceListener);
            mListAdapter.clear();
            mListAdapter = null;
            mIsBound = false;
        }
    }

    protected abstract Class<? extends DConnectMessageService> getMessageServiceClass();

    protected void onServiceRegistration() {}

    private void info(final String message) {
        mLogger.info(message);
    }

    private class ServiceContainer {

        final DConnectService mEntity;

        ServiceContainer(final DConnectService service) {
            mEntity = service;
        }

        String getId() {
            return mEntity.getId();
        }

        String getName() {
            return mEntity.getName();
        }

        boolean isOnline() {
            return mEntity.isOnline();
        }
    }

    private class ServiceListAdapter extends ArrayAdapter<ServiceContainer> {
        private LayoutInflater mInflater;

        public ServiceListAdapter(final Context context, final List<ServiceContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_device_connect_service, null);
            }

            final ServiceContainer service = getItem(position);

            TextView nameView = (TextView) convertView.findViewById(R.id.service_name);
            nameView.setText(service.getName());

            Button btnRemoveService = (Button) convertView.findViewById(R.id.btn_remove_service);
            btnRemoveService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ServiceContainer container = getItem(position);
                    if (container != null) {
                        mProvider.removeService(container.getId());
                    }
                }
            });

            return convertView;
        }

        public ServiceContainer getServiceContainer(final String serviceId) {
            for (int i = 0; i < getCount(); i++) {
                ServiceContainer container = getItem(i);
                if (container.getId().equals(serviceId)) {
                    return container;
                }
            }
            return null;
        }
    }
}
