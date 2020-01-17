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
    private ArrayList<T> mList;
    private EventListener mEventListener;
    private ArrayList<SwitchBotDevice> mCheckedList = new ArrayList<>();
    private final int mLayoutId;

    public ListAdapter(ArrayList<T> list, int layoutId, EventListener eventListener) {
        if (DEBUG) {
            Log.d(TAG, "ListAdapter()");
            Log.d(TAG, "list:" + list);
            Log.d(TAG, "mEventListener:" + eventListener);
        }
        mList = list;
        mEventListener = eventListener;
        mLayoutId = layoutId;
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
            for (T it : mList) {
                final String itDeviceAddress = ((BluetoothDevice) it).getAddress();
                if (DEBUG) {
                    Log.d(TAG, "device address(it) : " + itDeviceAddress);
                }
                if (addDeviceAddress.equals(itDeviceAddress)) {
                    return;
                }
            }
            mList.add(item);
            notifyItemInserted(mList.size() - 1);
        }
    }

    @NonNull
    @Override
    public ListViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListViewHolder<>(LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder<T> holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onItemClick(BluetoothDevice bluetoothDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device address : " + bluetoothDevice.getAddress());
        }
        mEventListener.onItemClick(bluetoothDevice);
    }

    @Override
    public void onItemClick(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
        mEventListener.onItemClick(switchBotDevice);
    }

    @Override
    public void onCheckedChange(SwitchBotDevice switchBotDevice, Boolean b) {
        if (DEBUG) {
            Log.d(TAG, "onCheckedChange()");
            Log.d(TAG, "switchBotDevice:" + switchBotDevice);
            Log.d(TAG, "b:" + b);
        }
        if (b) {
            mCheckedList.add(switchBotDevice);
        } else {
            mCheckedList.remove(switchBotDevice);
        }
    }

    public ArrayList<SwitchBotDevice> getCheckedList() {
        return mCheckedList;
    }

    public interface EventListener {
        void onItemClick(BluetoothDevice bluetoothDevice);

        void onItemClick(SwitchBotDevice switchBotDevice);
    }
}
