package org.deviceconnect.android.deviceplugin.uvc.fragment.preference;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class SeekBarDialogFragment extends PreferenceDialogFragmentCompat {
    private static final int MAX = 1000;
    private EditText mValueEditText;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBarDialogPreference pref = (SeekBarDialogPreference) getPreference();
        int min = pref.getMinValue();
        int max = pref.getMaxValue();
        long value = min;
        try {
            value = Long.parseLong(pref.getText());
        } catch (Exception e) {
            // ignore.
        }

        mValueEditText = view.findViewById(R.id.number_edit_text);
        if (mValueEditText != null) {
            mValueEditText.setText(String.valueOf(value));
        }

        SeekBar seekBar = view.findViewById(R.id.seekbar);
        if (seekBar != null) {
            seekBar.setMax(MAX);
            seekBar.setProgress((int) (MAX * ((value - min) / (float) (max - min))));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    long value = min + (long) ((max - min) * (progress / (float) MAX));
                    mValueEditText.setText(String.valueOf(value));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        EditTextPreference f = (EditTextPreference) getPreference();
        if (positiveResult) {
            f.setText(mValueEditText.getText().toString());
        }
    }

    public static SeekBarDialogFragment newInstance(String key) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);

        SeekBarDialogFragment fragment = new SeekBarDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}