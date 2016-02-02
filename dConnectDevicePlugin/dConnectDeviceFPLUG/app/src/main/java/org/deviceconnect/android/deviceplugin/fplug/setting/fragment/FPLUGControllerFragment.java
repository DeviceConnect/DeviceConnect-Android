/*
 FPLUGControllerFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.setting.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.fplug.R;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGRequestCallback;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGResponse;
import org.deviceconnect.android.deviceplugin.fplug.fplug.WattHour;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for Control F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGControllerFragment extends Fragment {

    private FPLUGController mController;
    private TextView mAddressView;
    private ItemAdapter mAdapter;

    public void setTargetFPlugAddress(FPLUGController controller) {
        mController = controller;
        mAddressView.setText(getString(R.string.setting_controller_mac_address, controller.getAddress()));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mController = null;
        final View root = inflater.inflate(R.layout.control_fplug, container, false);
        setupUI(root);
        return root;
    }

    private void setupUI(final View root) {
        mAddressView = (TextView) root.findViewById(R.id.mac_address);
        setInitButton(root);
        setListView(root);
    }

    private void setListView(final View root) {
        ListView list = (ListView) root.findViewById(R.id.list);
        ItemAdapter adapter = new ItemAdapter(getActivity(), createItems());
        list.setAdapter(adapter);
        list.setItemsCanFocus(true);
        mAdapter = adapter;
    }

    private void setInitButton(final View root) {
        root.findViewById(R.id.init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkController()) {
                    return;
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.setting_controller_init_dialog_title))
                        .setMessage(getString(R.string.setting_controller_init_dialog_message))
                        .setPositiveButton(getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mController.requestInitPlug(new FPLUGRequestCallback() {
                                            @Override
                                            public void onSuccess(FPLUGResponse response) {
                                                if (getActivity() != null) {
                                                    updateText(root, R.id.init_res, getString(R.string.success));
                                                }
                                            }

                                            @Override
                                            public void onError(String message) {
                                                if (getActivity() != null) {
                                                    updateText(root, R.id.init_res, getString(R.string.failed));
                                                }
                                            }

                                            @Override
                                            public void onTimeout() {
                                                if (getActivity() != null) {
                                                    updateText(root, R.id.init_res, getString(R.string.timeout));
                                                }
                                            }
                                        });
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
        });
    }

    private void requestRealtime(Item item) {
        if (!checkController()) {
            return;
        }
        mController.requestRealtimeWatt(new SimpleRequestCallback(item) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                getItem().value = "" + response.getRealtimeWatt();
                updateValue();
            }
        });
    }

    private void requestIntegrated(Item item) {
        if (!checkController()) {
            return;
        }
        mController.requestWattHour(new SimpleRequestCallback(item) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                int total = 0;
                for (WattHour wh : response.getWattHourList()) {
                    total += wh.getWatt();
                }
                getItem().value = "" + total;
                updateValue();
            }
        });
    }

    private void requestTemperature(Item item) {
        if (!checkController()) {
            return;
        }
        mController.requestTemperature(new SimpleRequestCallback(item) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                getItem().value = "" + response.getTemperature();
                updateValue();
            }
        });
    }

    private void requestHumidity(Item item) {
        if (!checkController()) {
            return;
        }
        mController.requestHumidity(new SimpleRequestCallback(item) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                getItem().value = "" + response.getHumidity();
                updateValue();
            }
        });
    }

    private void requestIlluminance(Item item) {
        if (!checkController()) {
            return;
        }
        mController.requestIlluminance(new SimpleRequestCallback(item) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                getItem().value = "" + response.getIlluminance();
                updateValue();
            }
        });
    }

    private void updateText(final View root, final int resId, final String text) {
        if (!isResumed()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) root.findViewById(resId)).setText(text);
            }
        });
    }

    private void updateValue() {
        if (!isResumed()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean checkController() {
        if (mController == null) {
            Toast.makeText(getActivity(), getString(R.string.setting_controller_unselected_fplug), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private List<Item> createItems() {
        List<Item> items = new ArrayList<>();
        items.add(new Item(getString(R.string.realtime), "0", "w", ITEM_TYPE.REALTIME));
        items.add(new Item(getString(R.string.integrated), "0", "w", ITEM_TYPE.INTEGRATED));
        items.add(new Item(getString(R.string.temperature), "0", "℃", ITEM_TYPE.TEMPERATURE));
        items.add(new Item(getString(R.string.humidity), "0", "%", ITEM_TYPE.HUMIDITY));
        items.add(new Item(getString(R.string.illuminance), "0", "lx", ITEM_TYPE.ILLUMINANCE));
        items.add(new Item(ITEM_TYPE.LED));
        return items;
    }

    private void onClickButton(Item item) {
        switch (item.type) {
            case REALTIME:
                requestRealtime(item);
                break;
            case INTEGRATED:
                requestIntegrated(item);
                break;
            case TEMPERATURE:
                requestTemperature(item);
                break;
            case HUMIDITY:
                requestHumidity(item);
                break;
            case ILLUMINANCE:
                requestIlluminance(item);
                break;
        }
    }

    private void onClickLED(boolean isOn) {
        if (!checkController()) {
            return;
        }
        mController.requestLEDControl(isOn, new FPLUGRequestCallback() {
            @Override
            public void onSuccess(FPLUGResponse response) {
            }

            @Override
            public void onError(String message) {
            }

            @Override
            public void onTimeout() {
            }
        });
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
            } else {
                convertView = mInflater.inflate(R.layout.getable_item, parent, false);
                convertView.findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(item);
                    }
                });
                ((TextView) convertView.findViewById(R.id.name)).setText(item.name);
                ((TextView) convertView.findViewById(R.id.value)).setText(item.value);

                TextView unitTextView = (TextView) convertView.findViewById(R.id.unit);
                if (!isFailedValue(item.value)) {
                    unitTextView.setText(item.unit);
                    unitTextView.setVisibility(View.VISIBLE);
                } else {
                    unitTextView.setVisibility(View.INVISIBLE);
                }
            }
            return convertView;
        }

        private boolean isFailedValue(String value) {
            return getString(R.string.get_failed).equals(value) || getString(R.string.timeout).equals(value);
        }
    }

    enum ITEM_TYPE {
        REALTIME, INTEGRATED,
        LED, TEMPERATURE, HUMIDITY, ILLUMINANCE
    }

    private class Item {
        ITEM_TYPE type;
        String name;
        String value;
        String unit;

        Item(ITEM_TYPE type) {
            this.type = type;
        }

        Item(String name, String value, String unit, ITEM_TYPE type) {
            this.name = name;
            this.value = value;
            this.unit = unit;
            this.type = type;
        }
    }

    private abstract class SimpleRequestCallback implements FPLUGRequestCallback {

        Item mItem;

        public SimpleRequestCallback(Item item) {
            mItem = item;
        }

        @Override
        public void onError(String message) {
            mItem.value = getString(R.string.get_failed);
            updateValue();
        }

        @Override
        public void onTimeout() {
            mItem.value = getString(R.string.timeout);
            updateValue();
        }

        protected Item getItem() {
            return mItem;
        }
    }

}
