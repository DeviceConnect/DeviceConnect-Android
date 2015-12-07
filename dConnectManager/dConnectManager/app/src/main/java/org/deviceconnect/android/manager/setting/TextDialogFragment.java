package org.deviceconnect.android.manager.setting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.deviceconnect.android.manager.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * プライバシーポリシーフラグメント.
 */
public class TextDialogFragment extends DialogFragment {

    /**
     * バッファサイズ.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("dconnect.uiapp");

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_privacypolicy, null);
        TextView text = (TextView) view.findViewById(android.R.id.text1);

        InputStream is = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            is = getActivity().getResources().openRawResource(getArguments().getInt(Intent.EXTRA_TEXT));
            byte[] buf = new byte[BUFFER_SIZE];
            while (true) {
                int len = is.read(buf);
                if (len < 0) {
                    break;
                }
                os.write(buf, 0, len);
            }
            text.setText(new String(os.toByteArray(), "UTF_8"));
        } catch (IOException e) {
            mLogger.warning(e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    mLogger.fine(e.toString());
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(getArguments().getInt(Intent.EXTRA_TITLE));
        builder.setPositiveButton(R.string.activity_settings_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

}
