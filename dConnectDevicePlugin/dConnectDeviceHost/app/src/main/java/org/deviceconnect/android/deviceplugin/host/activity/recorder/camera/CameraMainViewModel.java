package org.deviceconnect.android.deviceplugin.host.activity.recorder.camera;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.deviceconnect.android.deviceplugin.host.BR;
import org.deviceconnect.android.deviceplugin.host.R;

public class CameraMainViewModel extends BaseObservable {
    private String mBitRate = "";
    private String mTemperature = "";

    private int mTogglePreviewResId = R.drawable.ic_baseline_tap_and_play_48;
    private int mToggleBroadcastResId = R.drawable.ic_baseline_cloud_upload_48;
    private int mToggleRecordingResId = R.drawable.ic_baseline_videocam_48;
    private int mPhotoResId = R.drawable.ic_baseline_camera_48;
    private int mMuteResId = R.drawable.ic_baseline_mic_24;
    private int mRotationResId = R.drawable.ic_baseline_sync_24;

    private int mParamVisibility = View.GONE;
    private int mSurfaceVisibility = View.GONE;
    private int mPhotoVisibility = View.GONE;
    private boolean mCameraSwitchClickable = true;
    private int mRunningVisibility = View.GONE;


    @Bindable
    public boolean isCameraSwitchClickable() {
        return mCameraSwitchClickable;
    }

    public void setCameraSwitchClickable(boolean clickable) {
        mCameraSwitchClickable = clickable;
        notifyPropertyChanged(BR.cameraSwitchClickable);
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
    public int getTogglePreviewResId() {
        return mTogglePreviewResId;
    }

    public void setTogglePreviewResId(int resId) {
        mTogglePreviewResId = resId;
        notifyPropertyChanged(BR.togglePreviewResId);
    }

    @Bindable
    public int getToggleBroadcastResId() {
        return mToggleBroadcastResId;
    }

    public void setToggleBroadcastResId(int resId) {
        mToggleBroadcastResId = resId;
        notifyPropertyChanged(BR.toggleBroadcastResId);
    }

    @Bindable
    public int getToggleRecordingResId() {
        return mToggleRecordingResId;
    }

    public void setToggleRecordingResId(int resId) {
        mToggleRecordingResId = resId;
        notifyPropertyChanged(BR.toggleRecordingResId);
    }

    @Bindable
    public int getPhotoResId() {
        return mPhotoResId;
    }

    public void setPhotoResId(int resId) {
        mPhotoResId = resId;
        notifyPropertyChanged(BR.photoResId);
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

    @Bindable
    public int getPhotoVisibility() {
        return mPhotoVisibility;
    }

    public void setPhotoVisibility(int visibility) {
        mPhotoVisibility = visibility;
        notifyPropertyChanged(BR.photoVisibility);
    }

    @Bindable
    public int getRunningVisibility() {
        return mRunningVisibility;
    }

    public void setRunningVisibility(int visibility) {
        mRunningVisibility = visibility;
        notifyPropertyChanged(BR.runningVisibility);
    }
}
