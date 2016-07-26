package org.deviceconnect.android.manager.setting;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import org.deviceconnect.android.manager.R;

public class ServiceDiscoveryDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String msg = getString(R.string.activity_service_list_service_discovery);
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);
        return progressDialog;
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }

}
