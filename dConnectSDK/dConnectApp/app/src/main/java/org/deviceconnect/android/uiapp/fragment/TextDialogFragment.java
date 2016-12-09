package org.deviceconnect.android.uiapp.fragment;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.deviceconnect.android.uiapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextDialogFragment extends DialogFragment {

    /**
     * バッファサイズ.
     */
    private static final int BUFFER_SIZE = 1024;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_dialog, null);
        TextView text = (TextView) view.findViewById(android.R.id.text1);

        InputStream is = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            is = getActivity().getResources().openRawResource(
                    getArguments().getInt(Intent.EXTRA_TEXT));
            byte[] buf = new byte[BUFFER_SIZE];
            while (true) {
                int len = is.read(buf);
                if (len < 0) {
                    break;
                }
                os.write(buf, 0, len);
            }
            text.setText(new String(os.toByteArray(), "UTF-8"));
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        getDialog().setTitle(getArguments().getInt(Intent.EXTRA_TITLE));

        return view;
    }
}
