/*
 SecuritySettingDialogFragment.java
 Copyright (c) 2021 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.host.file.HostFileProvider;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * セキュリティ設定画面を開くダイアログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class SecuritySettingDialogFragment extends DialogFragment {
    private static final String TAG = "SecuritySettingDialogFragment";
    public static final String EXTRA_ROOT_CERT = "root_cert";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        }
        byte[] rootCert = getArguments().getByteArray(EXTRA_ROOT_CERT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_security_setting));
        builder.setMessage(getString(R.string.dialog_message_security_setting));
        builder.setPositiveButton(R.string.dialog_open_security_setting, (dialog, which) -> {
            final FileManager fileMgr = new FileManager(getContext(), HostFileProvider.class.getName());
            fileMgr.saveFile("manager.pem", rootCert, true, new FileManager.SaveFileCallback() {
                @Override
                public void onSuccess(@NonNull String s) {
                    shareCA(new File(fileMgr.getBasePath(), "manager.pem"));
                    Intent installIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(installIntent);
                }

                @Override
                public void onFail(@NonNull Throwable throwable) {
                    Toast.makeText(getContext(), R.string.dialog_error_message_not_export_ca, Toast.LENGTH_LONG).show();
                }
            });
        });
        builder.setNegativeButton(R.string.activity_launch_button_cancel, (dialog, which) -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });
        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();

        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void shareCA(final File fileName) {
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.TITLE, fileName.getName());
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName.getName());
        values.put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Downloads.MIME_TYPE, "application/x-pem-file");
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Log.e(TAG, "Failed to share ca: not inserted to ca store: path = " + fileName);
            return;
        }

        try (InputStream in = new FileInputStream(fileName);
             OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) {
                Log.e(TAG, "Failed to share photo: no output stream: path = " + fileName);
                return;
            }
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to share photo: I/O error: path = " + fileName, e);
            return;
        }

        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
    }
}
