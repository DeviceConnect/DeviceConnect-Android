package org.deviceconnect.android.deviceplugin.uvc.fragment.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class SeekBarDialogPreference extends EditTextPreference {
    private int mMinValue;
    private int mMaxValue;

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarDialogPreference(Context context) {
        super(context);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_preference_seek_bar;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }
}
