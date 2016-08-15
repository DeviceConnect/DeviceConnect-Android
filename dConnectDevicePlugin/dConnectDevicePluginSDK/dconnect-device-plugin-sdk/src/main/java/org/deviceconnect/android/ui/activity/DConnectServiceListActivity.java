package org.deviceconnect.android.ui.activity;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

/**
 * Device Connect Service List Window.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectServiceListActivity extends Activity {

    /**
     * デフォルトのタイトル文字列.
     */
    public static final String DEFAULT_TITLE = "CLOSE";

    private DConnectServiceProvider mProvider;

    private boolean mIsBound;

    private final Object mLock = new Object();

    private final Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            info("onServiceConnected: " + name);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
            });
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        if (mIsBound) {
                            mListAdapter.add(new ServiceContainer(service));
                        }
                    }
                }
            });
        }

        @Override
        public void onServiceRemoved(final DConnectService service) {
            info("onServiceRemoved: " + service.getId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        ServiceContainer container = mListAdapter.getServiceContainer(service.getId());
                        if (container != null) {
                            mListAdapter.remove(container);
                        }
                    }
                }
            });
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

    private Class<? extends Activity> mManualActivity;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(R.string.activity_service_list_title);
        }

        mListView = (ListView) findViewById(R.id.device_connect_service_list_view);

        mManualActivity = getSettingManualActivityClass();
        if (mManualActivity != null) {
            mNewServiceButton = (Button) findViewById(R.id.device_connect_service_list_button_new_service);
            mNewServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Intent intent = new Intent(getApplicationContext(), mManualActivity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        } else {
            mNewServiceButton.setVisibility(View.GONE);
        }
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract Class<? extends DConnectMessageService> getMessageServiceClass();

    protected abstract Class<? extends Activity> getSettingManualActivityClass();

    protected boolean canRemove(final String serviceId) {
        return true;
    }

    protected boolean dispatchServiceRemoval(final String serviceId) {
        return false;
    }

    protected void removeService(final String serviceId) {
        mProvider.removeService(serviceId);
    }

    protected DConnectService getService(final String serviceId) {
        return mProvider.getService(serviceId);
    }

    private void showRemovalConfirmation(final DConnectService service) {
        int messageId = service.isOnline() ?
            R.string.dialog_message_online_service_removal_confirmation :
            R.string.dialog_message_offline_service_removal_confirmation;
        String message = getString(messageId).replace("{name}", service.getName());

        new AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_button_service_removal_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        removeService(service.getId());
                    }
                })
            .setNegativeButton(R.string.dialog_button_service_removal_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        // NOP.
                    }
                })
            .create().show();
    }

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

            convertView.setBackgroundResource(service.isOnline() ?
                R.color.service_list_item_background_online :
                R.color.service_list_item_background_offline);

            TextView statusView = (TextView) convertView.findViewById(R.id.service_online_status);
            statusView.setText(service.isOnline() ?
                R.string.device_connect_service_is_online :
                R.string.device_connect_service_is_offline);

            TextView nameView = (TextView) convertView.findViewById(R.id.service_name);
            nameView.setText(service.getName());

            Button btnRemoveService = (Button) convertView.findViewById(R.id.btn_remove_service);
            btnRemoveService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ServiceContainer container = getItem(position);
                    if (container != null) {
                        if (!dispatchServiceRemoval(container.getId())) {
                            showRemovalConfirmation(getService(container.getId()));
                        }
                    }
                }
            });
            btnRemoveService.setVisibility(canRemove(service.getId()) ? View.VISIBLE : View.INVISIBLE);

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
