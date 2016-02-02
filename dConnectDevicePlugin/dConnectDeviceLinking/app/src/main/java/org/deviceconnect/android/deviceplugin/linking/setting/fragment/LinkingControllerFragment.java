/*
 LinkingControllerFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.IlluminationData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment for show Linking Controller.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingControllerFragment extends Fragment {

    private ItemAdapter mAdapter;
    private TextView mDeviceNameView;
    private LinkingDevice mDevice;
    private View mRoot;

    public void setTargetDevice(LinkingDevice device) {
        mDevice = device;
        mDeviceNameView.setText(mDevice.getDisplayName());

        byte[] illumination = mDevice.getIllumination();
        final IlluminationData data = new IlluminationData(illumination);

        Map<String, Integer> map = PreferenceUtil.getInstance(getActivity().getApplicationContext()).getLightOffSetting();
        if (map == null) {
            return;
        }
        Integer patternId = map.get(mDevice.getBdAddress());
        if (patternId == null) {
            ((Button) mRoot.findViewById(R.id.select_light_off)).setText(getString(R.string.not_selected));
            return;
        }
        for (IlluminationData.Setting setting : data.mPattern.children) {
            if ((setting.id & 0xFF) == patternId) {
                ((Button) mRoot.findViewById(R.id.select_light_off)).setText(setting.names[0].name);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mDevice = null;
        final View root = inflater.inflate(R.layout.control_linking, container, false);
        mRoot = root;
        setupUI(root);
        return root;
    }

    private void setupUI(final View root) {
        mDeviceNameView = (TextView) root.findViewById(R.id.device_name);
        mDeviceNameView.setText(getString(R.string.device_name) + getString(R.string.not_selected));
        setInitButton(root);
        setListView(root);
    }

    private void setInitButton(final View root) {
        root.findViewById(R.id.select_light_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDevice()) {
                    return;
                }

                byte[] illumination = mDevice.getIllumination();
                final IlluminationData data = new IlluminationData(illumination);
                Log.i("LinkingSample", "illumination:" + data);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final String[] items = new String[data.mPattern.children.length];
                for (int i = 0; i < data.mPattern.children.length; i++) {
                    items[i] = data.mPattern.children[i].names[0].name;
                }
                builder.setTitle(getString(R.string.pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IlluminationData.Setting selectedPattern = data.mPattern.children[which];
                        ((Button) root.findViewById(R.id.select_light_off)).setText(selectedPattern.names[0].name);
                        updateLightOffSetting(selectedPattern.id & 0xFF);
                    }
                });
                builder.create().show();
            }
        });
    }

    private void updateLightOffSetting(Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getActivity().getApplicationContext());
        Map<String, Integer> map = util.getLightOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setLightOffSetting(map);
    }

    private void showDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("タイトル")
                .setMessage("メッセージ")
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    private void setListView(final View root) {
        ListView list = (ListView) root.findViewById(R.id.list);
        ItemAdapter adapter = new ItemAdapter(getActivity(), createItems());
        list.setAdapter(adapter);
        list.setItemsCanFocus(true);
        mAdapter = adapter;
    }

    private List<Item> createItems() {
        List<Item> items = new ArrayList<>();
        items.add(new Item(ITEM_TYPE.LED));
        return items;
    }

    private void onClickLED(boolean isOn) {
        if (!checkDevice()) {
            return;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendLEDCommand(mDevice, isOn);
    }

    private boolean checkDevice() {
        if (mDevice == null) {
            Toast.makeText(getActivity(), getString(R.string.device_not_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private class ItemAdapter extends ArrayAdapter<Item> {
        private LayoutInflater mInflater;

        public ItemAdapter(final Context context, final List<Item> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Item item = getItem(position);
            if (item.type == ITEM_TYPE.LED) {
                convertView = mInflater.inflate(R.layout.led_button_item, parent, false);
                convertView.findViewById(R.id.on).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickLED(true);
                    }
                });
                convertView.findViewById(R.id.off).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickLED(false);
                    }
                });
            }
            return convertView;
        }

    }

    enum ITEM_TYPE {
        LED
    }

    private class Item {
        ITEM_TYPE type;

        Item(ITEM_TYPE type) {
            this.type = type;
        }
    }

}
