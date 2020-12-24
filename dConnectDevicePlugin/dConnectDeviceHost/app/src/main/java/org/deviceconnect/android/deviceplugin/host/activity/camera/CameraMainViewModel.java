package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.deviceconnect.android.deviceplugin.host.BR;
import org.deviceconnect.android.deviceplugin.host.R;

public class CameraMainViewModel extends BaseObservable {
    private String mNetworkType = "";
    private String mBitRate = "";
    private String mTemperature = "";
    private String mBatteryLevel = "";

    private int mToggleStartResId = R.drawable.ic_baseline_play_arrow_24;
    private int mMuteResId = R.drawable.ic_baseline_mic_24;
    private int mRotationResId = R.drawable.ic_baseline_sync_24;

    private int mParamVisibility = View.GONE;
    private int mSurfaceVisibility = View.GONE;

    @Bindable
    public String getNetworkType() {
        return mNetworkType;
    }

    public void setNetworkType(String type) {
        mNetworkType = type;
        notifyPropertyChanged(BR.networkType);
    }

    @Bindable
    public String getBitRate() {
        return mBitRate;
    }

    public void setBitRate(String bitRate) {
        mBitRate = bitRate;
        notifyPropertyChanged(BR.bitRate);
    }

    @Bindable
    public String getTemperature() {
        return mTemperature;
    }

    public void setTemperature(String temperature) {
        mTemperature = temperature;
        notifyPropertyChanged(BR.temperature);
    }

    @Bindable
    public String getBatteryLevel() {
        return mBatteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        mBatteryLevel = batteryLevel;
        notifyPropertyChanged(BR.batteryLevel);
    }

    @Bindable
    public int getToggleStartResId() {
        return mToggleStartResId;
    }

    public void setToggleStartResId(int resId) {
        mToggleStartResId = resId;
        notifyPropertyChanged(BR.toggleStartResId);
    }

    @Bindable
    public int getMuteResId() {
        return mMuteResId;
    }

    public void setMuteResId(int resId) {
        mMuteResId = resId;
        notifyPropertyChanged(BR.muteResId);
    }

    @Bindable
    public int getRotationResId() {
        return mRotationResId;
    }

    public void setRotationResId(int resId) {
        mRotationResId = resId;
        notifyPropertyChanged(BR.rotationResId);
    }

    @Bindable
    public int getParamVisibility() {
        return mParamVisibility;
    }

    public void setParamVisibility(int visibility) {
        mParamVisibility = visibility;
        notifyPropertyChanged(BR.paramVisibility);
    }

    @Bindable
    public int getSurfaceVisibility() {
        return mSurfaceVisibility;
    }

    public void setSurfaceVisibility(int visibility) {
        mSurfaceVisibility = visibility;
        notifyPropertyChanged(BR.surfaceVisibility);
    }
}
