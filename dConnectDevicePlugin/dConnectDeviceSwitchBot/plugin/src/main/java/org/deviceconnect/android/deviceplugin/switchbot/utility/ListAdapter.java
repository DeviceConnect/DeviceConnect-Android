package org.deviceconnect.android.deviceplugin.switchbot.utility;


import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;

import java.util.ArrayList;

public class ListAdapter<T> extends RecyclerView.Adapter<ListViewHolder<T>> implements ListViewHolder.EventListener {
    private static final String TAG = "ListViewHolder";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    private ArrayList<T> deviceList;
    private EventListener eventListener;
    private ArrayList<SwitchBotDevice> checkedList = new ArrayList<>();
    private final int layoutId;

    public ListAdapter(ArrayList<T> deviceList, int layoutId, EventListener eventListener) {
        if(DEBUG){
            Log.d(TAG, "ListAdapter()");
            Log.d(TAG, "deviceList:" + deviceList);
            Log.d(TAG, "eventListener:"  + eventListener);
        }
        this.deviceList = deviceList;
        this.eventListener = eventListener;
        this.layoutId = layoutId;
    }

    public void add(T item) {
        if (DEBUG) {
            Log.d(TAG, "add()");
        }
        if (item instanceof BluetoothDevice) {
            final String addDeviceAddress = ((BluetoothDevice) item).getAddress();
            if (DEBUG) {
                Log.d(TAG, "device address(add) : " + addDeviceAddress);
            }
            for (T it : deviceList) {
                final String itDeviceAddress = ((BluetoothDevice) it).getAddress();
                if (DEBUG) {
                    Log.d(TAG, "device address(it) : " + itDeviceAddress);
                }
                if (addDeviceAddress.equals(itDeviceAddress)) {
                    return;
                }
            }
            deviceList.add(item);
            notifyItemInserted(deviceList.size() - 1);
        }
    }

    @NonNull
    @Override
    public ListViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListViewHolder<>(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder<T> holder, int position) {
        holder.bind(deviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    @Override
    public void onItemClick(BluetoothDevice bluetoothDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device address : " + bluetoothDevice.getAddress());
        }
        eventListener.onItemClick(bluetoothDevice);
    }

    @Override
    public void onItemClick(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
        eventListener.onItemClick(switchBotDevice);
    }

    @Override
    public void onCheckedChange(SwitchBotDevice switchBotDevice, Boolean b) {
        if (DEBUG) {
            Log.d(TAG, "onCheckedChange()");
            Log.d(TAG, "switchBotDevice:" + switchBotDevice);
            Log.d(TAG, "b:" + b);
        }
        if (b) {
            checkedList.add(switchBotDevice);
        } else {
            checkedList.remove(switchBotDevice);
        }
    }

    public ArrayList<SwitchBotDevice> getCheckedList() {
        return checkedList;
    }

    public interface EventListener {
        void onItemClick(BluetoothDevice bluetoothDevice);
        void onItemClick(SwitchBotDevice switchBotDevice);
    }
}
