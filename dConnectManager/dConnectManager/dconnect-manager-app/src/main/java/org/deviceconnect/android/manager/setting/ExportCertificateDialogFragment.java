package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;

import java.io.IOException;
import java.util.logging.Logger;

public class ExportCertificateDialogFragment extends DialogFragment {

    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String fileDirsStr = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_export_certificate, null);

        final EditText exportPathEdit = view.findViewById(R.id.export_certificate_path);
        exportPathEdit.setText(fileDirsStr, TextView.BufferType.EDITABLE);

        builder.setView(view)
                .setTitle(R.string.activity_settings_export_server_keystore_dialog_path_title)
                .setPositiveButton(R.string.activity_settings_export_server_keystore_dialog_button_export, (dialogInterface, i) -> {
                    Activity activity = getActivity();
                    if (activity != null && activity instanceof SettingActivity) {
                        final String exportPath = exportPathEdit.getText().toString();
                        DConnectService service = ((SettingActivity) activity).getManagerService();
                        if (service != null) {
                            try {
                                service.exportKeyStore(exportPath);
                                showExportSuccessMessage();
                            } catch (IOException e) {
                                mLogger.severe("Failed to export server certificate: " + e.getMessage());
                                showExportErrorMessage();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.activity_settings_export_server_keystore_dialog_button_cancel, (dialogInterface, i) ->{
                        // NOP.
                });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void showExportSuccessMessage() {
        Toast.makeText(getContext(), getString(R.string.activity_settings_export_server_keystore_success), Toast.LENGTH_LONG).show();
    }

    private void showExportErrorMessage() {
        Toast.makeText(getContext(), getString(R.string.activity_settings_export_server_keystore_error), Toast.LENGTH_LONG).show();
    }
}
