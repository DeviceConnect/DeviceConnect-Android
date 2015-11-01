/*
 IRKitCategorySelectDialogFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.irkit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * カテゴリー選択用のダイアログ.
 */
public class IRKitCategorySelectDialogFragment extends DialogFragment {

    /**
     * サービスIDのキー名.
     */
    private static final String KEY_SERVICE_ID = "serviceId";

    /**
     * ダイアログの作成.
     * @return ダイアログ
     */
    public static IRKitCategorySelectDialogFragment newInstance(final String serviceId) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SERVICE_ID, serviceId);

        IRKitCategorySelectDialogFragment d = new IRKitCategorySelectDialogFragment();
        d.setArguments(bundle);
        return d;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final ArrayList<String> categories = new ArrayList<String>();
        categories.add(getString(R.string.select_light));
        categories.add(getString(R.string.select_tv));

        CustomAdapter adapter = new CustomAdapter(getActivity(), 0, categories);
        ListView listView = new ListView(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                openCreateVirtualDevice(categories.get(position));
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_category);
        builder.setView(listView);
        builder.setPositiveButton(R.string.alert_btn_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private void openCreateVirtualDevice(final String category) {
        IRKitCreateVirtualDeviceDialogFragment dialog =
                IRKitCreateVirtualDeviceDialogFragment.newInstance(
                        getArguments().getString(KEY_SERVICE_ID), category);
        dialog.show(getActivity().getFragmentManager(),
                "fragment_dialog");
    }

    private class CustomAdapter extends ArrayAdapter<String> {
        private LayoutInflater mInflater;

        public CustomAdapter(final Context context, final int resource, final List<String> objects) {
            super(context, resource, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(final int position, final View v, final ViewGroup parent) {
            View view = v;
            if (view == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
            return view;
        }
    }
}
