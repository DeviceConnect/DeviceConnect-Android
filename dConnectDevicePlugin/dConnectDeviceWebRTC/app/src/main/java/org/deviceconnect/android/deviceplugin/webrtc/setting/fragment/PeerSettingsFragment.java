package org.deviceconnect.android.deviceplugin.webrtc.setting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;

import java.util.List;

public class PeerSettingsFragment extends Fragment {

    private SettingsAdapter mSettingsAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        WebRTCApplication app = (WebRTCApplication) getActivity().getApplication();
        mSettingsAdapter = new SettingsAdapter(app.getPeerConfig());

        View root = inflater.inflate(R.layout.settings_peer, null);
        ListView listView = (ListView) root.findViewById(R.id.listView);
        listView.setAdapter(mSettingsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openDialog(position);
            }
        });

        return root;
    }



    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void openDialog(final int position) {
        String title = getString(R.string.settings_dialog_peer_list_title);
        String message = getString(R.string.settings_dialog_peer_list_message);
        ConfirmationDialogFragment dialog = ConfirmationDialogFragment.create(title, message);
        dialog.setOnConfirmationListener(new ConfirmationDialogFragment.OnConfirmationListener() {
            @Override
            public void onPositive() {
                WebRTCApplication app = (WebRTCApplication) getActivity().getApplication();
                PeerConfig config = (PeerConfig) mSettingsAdapter.getItem(position);
                app.destroyPeer(config);
                mSettingsAdapter.setConfigs(app.getPeerConfig());
            }
            @Override
            public void onNegative() {
            }
        });
        dialog.show(getFragmentManager(), "test");
    }

    public void reload() {
        if (mSettingsAdapter != null) {
            WebRTCApplication app = (WebRTCApplication) getActivity().getApplication();
            mSettingsAdapter.setConfigs(app.getPeerConfig());
        }
    }

    private class SettingsAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<PeerConfig> mConfigs;

        private SettingsAdapter(final List<PeerConfig> configs) {
            mInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mConfigs = configs;
        }

        @Override
        public int getCount() {
            return mConfigs.size();
        }

        @Override
        public Object getItem(final int position) {
            return mConfigs.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
            }

            WebRTCApplication app = (WebRTCApplication) getActivity().getApplication();

            String peerId = "";
            PeerConfig config = (PeerConfig) getItem(position);
            Peer peer = app.getPeer(config);
            if (peer != null) {
                peerId = peer.getMyAddressId();
                if (peerId == null) {
                    peerId = "";
                }
            }
            setViewText(view, config.getDomain(), peerId);
            return view;
        }

        public void setConfigs(List<PeerConfig> configs) {
            mConfigs = configs;
            notifyDataSetChanged();
        }

        private void setViewText(final View view, final String title, final String value) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(title);
            }

            TextView valueView = (TextView) view.findViewById(R.id.value);
            if (valueView != null) {
                valueView.setText(value);
            }
        }
    }
}
