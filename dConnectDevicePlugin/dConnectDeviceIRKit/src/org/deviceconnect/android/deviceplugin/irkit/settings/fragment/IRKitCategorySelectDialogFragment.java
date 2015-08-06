package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.irkit.R;

/**
 * カテゴリー選択用のダイアログ.
 */
public class IRKitCategorySelectDialogFragment extends DialogFragment {

    /**
     * サービスID.
     */
    private String mServiceId;

    /**
     * ダイアログの作成.
     * @return ダイアログ
     */
    public static IRKitCategorySelectDialogFragment newInstance() {
        return new IRKitCategorySelectDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_virtual_profile
                                    , null);
        Button registerLightBtn = (Button) dialogView.findViewById(R.id.select_light);
        registerLightBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                IRKitCreateVirtualDeviceDialogFragment irkitDialog =
                        IRKitCreateVirtualDeviceDialogFragment.newInstance();
                irkitDialog.setVirtualDeviceData(mServiceId, "ライト");
                irkitDialog.show(getActivity().getFragmentManager(),
                        "fragment_dialog");

                IRKitCategorySelectDialogFragment.this.dismiss();
            }
        });
        Button registerTVBtn = (Button) dialogView.findViewById(R.id.select_tv);
        registerTVBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                IRKitCreateVirtualDeviceDialogFragment irkitDialog =
                        IRKitCreateVirtualDeviceDialogFragment.newInstance();
                irkitDialog.setVirtualDeviceData(mServiceId, "テレビ");
                irkitDialog.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
                IRKitCategorySelectDialogFragment.this.dismiss();
            }
        });
        Button closeBtn = (Button) dialogView.findViewById(R.id.close);
        closeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                IRKitCategorySelectDialogFragment.this.dismiss();
            }
        });
        builder.setView(dialogView);

        return builder.create();
    }

    /**
     * サービスID の設定.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }
}
