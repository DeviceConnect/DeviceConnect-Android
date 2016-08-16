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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.R;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
import org.deviceconnect.android.service.DConnectServiceProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Device Connect Service List Window.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectServiceListActivity extends FragmentActivity {

    private DConnectServiceProvider mProvider;

    private boolean mIsBound;

    private final Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            info("onServiceConnected: " + name);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProvider = ((DConnectMessageService.LocalBinder) service).getMessageService()
                        .getServiceProvider();
                    mIsBound = true;

                    showServiceViewer();
                }
            });
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            info("onServiceDisconnected: " + name);
            mIsBound = false;
        }
    };

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

        mManualActivity = getSettingManualActivityClass();
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

    private List<ServiceContainer> getServiceContainers() {
        List<ServiceContainer> containers = new ArrayList<ServiceContainer>();
        if (mProvider != null) {
            for (DConnectService entity : mProvider.getServiceList()) {
                containers.add(new ServiceContainer(entity));
            }
        }
        return containers;
    }

    private void showSettingActivity() {
        Intent intent = new Intent(getApplicationContext(), mManualActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showServiceViewer() {
        FragmentManager mgr = getSupportFragmentManager();
        FragmentTransaction transaction = mgr.beginTransaction();
        Fragment fragment = new ViewerFragment();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showServiceRemover() {
        FragmentManager mgr = getSupportFragmentManager();
        FragmentTransaction transaction = mgr.beginTransaction();
        Fragment fragment = new RemoverFragment();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void info(final String message) {
        mLogger.info(message);
    }

    private static class ServiceContainer {

        private final String mId;
        private String mName;
        private boolean mIsOnline;

        public ServiceContainer(final DConnectService service) {
            mId = service.getId();
            setName(service.getName());
            setOnline(service.isOnline());
        }

        public String getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        public void setName(final String name) {
            mName = name;
        }

        public void setOnline(final boolean online) {
            mIsOnline = online;
        }

        boolean isOnline() {
            return mIsOnline;
        }
    }

    public static abstract class AbstractViewerFragment extends Fragment implements DConnectServiceListener {

        protected ServiceListAdapter mListAdapter;

        protected DConnectServiceProvider mProvider;

        private final Object mLock = new Object();

        protected List<ServiceContainer> getServiceContainers() {
            DConnectServiceListActivity activity = (DConnectServiceListActivity) getActivity();
            if (activity == null) {
                return new ArrayList<ServiceContainer>();
            }
            return activity.getServiceContainers();
        }

        @Override
        public void onActivityCreated(final Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            DConnectServiceListActivity activity = (DConnectServiceListActivity) getActivity();
            mProvider = activity.mProvider;
            mProvider.addServiceListener(this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mProvider.removeServiceListener(this);
            mProvider = null;
        }

        @Override
        public void onServiceAdded(final DConnectService service) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        mListAdapter.add(new ServiceContainer(service));
                    }
                }
            });
        }

        @Override
        public void onServiceRemoved(final DConnectService service) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
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
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLock) {
                        ServiceContainer container = mListAdapter.getServiceContainer(service.getId());
                        if (container != null) {
                            mListAdapter.onStatusChange(service);
                        }
                    }
                }
            });
        }
    }

    public static class ViewerFragment extends AbstractViewerFragment {

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_service_list_viewer, container, false);

            mListAdapter = new ServiceListAdapter(getContext(), getServiceContainers(), false);

            ListView listView = (ListView) root.findViewById(R.id.device_connect_service_list_view);
            listView.setAdapter(mListAdapter);
            listView.setItemsCanFocus(true);
            listView.setClickable(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view,
                                        final int position, final long id) {
                    Log.d("AAA", "AAA");
                }
            });

            Button newServiceButton = (Button) root.findViewById(R.id.device_connect_service_list_button_add_service);
            newServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((DConnectServiceListActivity) getActivity()).showSettingActivity();
                }
            });

            Button removeServiceButton = (Button) root.findViewById(R.id.device_connect_service_list_button_remove_service);
            removeServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((DConnectServiceListActivity) getActivity()).showServiceRemover();
                }
            });

            return root;
        }

    }

    public static class RemoverFragment extends AbstractViewerFragment
        implements OnCheckServiceListener {

        private Button mRemoveServiceButton;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_service_list_remover, container, false);

            mListAdapter = new ServiceListAdapter(getContext(), getServiceContainers(), true);
            mListAdapter.mOnCheckServiceListener = this;

            ListView listView = (ListView) root.findViewById(R.id.device_connect_service_list_view);
            listView.setAdapter(mListAdapter);

            Button cancelButton = (Button) root.findViewById(R.id.device_connect_service_list_button_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((DConnectServiceListActivity) getActivity()).showServiceViewer();
                }
            });

            mRemoveServiceButton = (Button) root.findViewById(R.id.device_connect_service_list_button_remove_service);
            mRemoveServiceButton.setClickable(false);
            mRemoveServiceButton.setEnabled(false);
            mRemoveServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    showRemovalConfirmation();
                }
            });

            return root;
        }

        private void showRemovalConfirmation() {
            new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_title_service_removal_confirmation)
                .setMessage(R.string.dialog_message_service_removal_confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_button_service_removal_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            for (ServiceContainer container : mListAdapter.getCheckedServiceList()) {
                                mProvider.removeService(container.getId());
                            }
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

        @Override
        public void onCheckStateChange(final ServiceContainer checkedService, final boolean isChecked) {
            boolean hasCheckedService = mListAdapter.getCheckedServiceList().size() > 0;
            mRemoveServiceButton.setClickable(hasCheckedService);
            mRemoveServiceButton.setEnabled(hasCheckedService);
        }
    }

    private static class ServiceListAdapter extends ArrayAdapter<ServiceContainer> {

        private final LayoutInflater mInflater;

        private final boolean mHasCheckbox;

        private final List<ServiceContainer> mCheckedServiceList =
            new ArrayList<ServiceContainer>();

        private OnCheckServiceListener mOnCheckServiceListener;

        public ServiceListAdapter(final Context context, final List<ServiceContainer> objects,
                                  final boolean hasCheckbox) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHasCheckbox = hasCheckbox;
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

            CheckBox checkBox =
                (CheckBox) convertView.findViewById(R.id.device_connect_service_removal_checkbox);
            checkBox.setVisibility(mHasCheckbox ? View.VISIBLE : View.GONE);
            if (!service.isOnline()) {
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton buttonView,
                                                 final boolean isChecked) {
                        setChecked(service, isChecked);
                    }
                });
            } else {
                checkBox.setEnabled(false);
                checkBox.setClickable(false);
                checkBox.setChecked(false);
            }


            return convertView;
        }

        boolean isChecked(final ServiceContainer service) {
            for (ServiceContainer s : mCheckedServiceList) {
                if (s.getId().equals(service.getId())) {
                    return true;
                }
            }
            return false;
        }

        void setChecked(final ServiceContainer service, final boolean isChecked) {
            if (isChecked) {
                if (!isChecked(service)) {
                    mCheckedServiceList.add(service);
                }
            } else {
                for (Iterator<ServiceContainer> it = mCheckedServiceList.iterator(); ; it.hasNext()) {
                    if (it.next().getId().equals(service.getId())) {
                        it.remove();
                        break;
                    }
                }
            }
            if (mOnCheckServiceListener != null) {
                mOnCheckServiceListener.onCheckStateChange(service, isChecked);
            }
        }

        ServiceContainer getServiceContainer(final String serviceId) {
            for (int i = 0; i < getCount(); i++) {
                ServiceContainer container = getItem(i);
                if (container.getId().equals(serviceId)) {
                    return container;
                }
            }
            return null;
        }

        List<ServiceContainer> getCheckedServiceList() {
            return new ArrayList<ServiceContainer>(mCheckedServiceList);
        }

        void onStatusChange(final DConnectService service) {
            ServiceContainer container = getServiceContainer(service.getId());
            container.setName(service.getName());
            container.setOnline(service.isOnline());
            notifyDataSetChanged();
        }
    }

    private interface OnCheckServiceListener {

        void onCheckStateChange(ServiceContainer checkedService, boolean isChecked);

    }
}
