package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.deviceplugin.uvc.databinding.FragmentUvcRecorderListBinding;
import org.deviceconnect.android.deviceplugin.uvc.databinding.ItemUvcRecorderBinding;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.service.DConnectService;

import java.util.ArrayList;
import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class UVCRecorderListFragment extends UVCDevicePluginBindFragment {
    private RecorderAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentUvcRecorderListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_uvc_recorder_list, container, false);
        binding.setPresenter(this);

        mAdapter = new RecorderAdapter(getContext(), new ArrayList<>());

        View rootView = binding.getRoot();
        ListView listView = rootView.findViewById(R.id.recorder_list_view);
        listView.setAdapter(mAdapter);
        listView.setItemsCanFocus(true);
        return rootView;
    }

    @Override
    public void onBindService() {
        List<RecorderContainer> containers = new ArrayList<>();

        UVCDeviceService deviceService = getUVCDeviceService();
        if (deviceService != null) {
            String serviceId = getServiceId();
            if (serviceId != null) {
                UVCService service = deviceService.findUVCServiceById(serviceId);
                for (MediaRecorder recorder : service.getUvcRecorderList()) {
                    if (recorder instanceof UvcRecorder) {
                        containers.add(new RecorderContainer(service, (UvcRecorder) recorder));
                    }
                }
                setTitle(service.getName());
            }
        }

        mAdapter.setContainers(containers);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RecorderContainer container = mAdapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putString("service_id", container.getServiceId());
        bundle.putString("recorder_id", container.getId());
        bundle.putString("settings_name", container.getSettingsName());
        findNavController(this).navigate(R.id.action_recorder_to_main, bundle);
    }

    private String getServiceId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString("service_id");
        }
        return null;
    }

    public static class RecorderContainer {
        private final DConnectService mService;
        private final MediaRecorder mRecorder;
        private final String mSettingsName;

        RecorderContainer(DConnectService service, UvcRecorder recorder) {
            mService = service;
            mRecorder = recorder;
            mSettingsName = recorder.getSettingsName();
        }

        public String getName() {
            return mRecorder.getName();
        }

        public String getId() {
            return mRecorder.getId();
        }

        public String getSettingsName() {
            return mSettingsName;
        }

        public String getServiceId() {
            return mService.getId();
        }
    }

    private static class RecorderAdapter extends ArrayAdapter<RecorderContainer> {
        RecorderAdapter(final Context context, final List<RecorderContainer> objects) {
            super(context, 0, objects);
        }

        void setContainers(List<RecorderContainer> containers) {
            clear();
            addAll(containers);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemUvcRecorderBinding binding;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                binding = DataBindingUtil.inflate(inflater, R.layout.item_uvc_recorder, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ItemUvcRecorderBinding) convertView.getTag();
            }
            binding.setContainer(getItem(position));
            return convertView;
        }
    }
}
