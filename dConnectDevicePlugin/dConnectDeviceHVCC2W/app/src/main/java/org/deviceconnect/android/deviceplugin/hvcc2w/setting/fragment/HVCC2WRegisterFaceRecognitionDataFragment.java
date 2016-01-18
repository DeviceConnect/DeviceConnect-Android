/*
 HVCC2WRegisterFaceRecognitionDataFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.R;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCStorage;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionDataModel;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionObject;

import java.util.List;

/**
 * HVC-C2W Settings Fragment Page 5.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WRegisterFaceRecognitionDataFragment extends Fragment {

    /** Face Recognition  data ListView. */
    private ListView mListView;
    /** Face Recognition data name list adapter. */
    private ListAdapter mAdapter;
    /** Face Recognition data name. */
    private EditText mName;
    /** Face Recognition data register button. */
    private Button mRegister;
    /** Face Recognition data unregister button. */
    private Button mUnregister;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        mAdapter = new ListAdapter(getActivity(), -1);
        View root = inflater.inflate(R.layout.setting_recognize, null);
        mName = (EditText) root.findViewById(R.id.input_name);
        mRegister = (Button) root.findViewById(R.id.register_face_recognize);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerFaceRecognitionData();
            }
        });
        mUnregister = (Button) root.findViewById(R.id.unregister_face_recognize);
        mUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregisterFaceRecognitionData();
            }
        });
        mListView = (ListView) root.findViewById(R.id.recoglist);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String name = (String) parent.getItemAtPosition(position);
                mName.setText(name);
            }
        });
        initListView();
        return root;
    }

    /** Initial List View. */
    private void initListView() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        List<FaceRecognitionObject> objects = HVCStorage.INSTANCE.getFaceRecognitionDatas(null);
        for (FaceRecognitionObject object: objects) {
            mAdapter.add(object.getName());
        }
        mListView.setAdapter(mAdapter);
    }

    /** Register Face Recognition Data. */
    private void registerFaceRecognitionData() {
        String name = mName.getText().toString();
        if (!name.isEmpty()) {
            List<FaceRecognitionObject> recogs = HVCStorage.INSTANCE.getFaceRecognitionDatas(null);
            int userId = 0;
            for (int i = 0; i < recogs.size(); i++, userId++) {
                FaceRecognitionObject recog = recogs.get(i);
                if (recog.getUserId() > userId) {
                    Log.d("ABC", "rec:" + recog.getUserId() + ":" + userId);
                    break;
                }
            }
            if (BuildConfig.DEBUG) {
                Log.d("ABC", "result userId:" + userId);
            }
            if (userId > 500) {
                HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_fail_register_over), null);
                return;
            }
            FaceRecognitionObject registerObject = new FaceRecognitionDataModel(name, userId, 0);
            HVCManager.INSTANCE.registerAlbum(name, userId, 0, null);
            long ret = HVCStorage.INSTANCE.registerFaceRecognitionData(registerObject);
            Log.d("ABC", "ret:" + ret); // TODO Comment out
            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_success_register), null);
            initListView();
        } else {
            Toast.makeText(getContext(), getString(R.string.c2w_setting_message_6_1), Toast.LENGTH_LONG).show();
        }
    }

    /** Unregister Face Recognition Data. */
    private void unregisterFaceRecognitionData() {
        String name = mName.getText().toString();
        if (!name.isEmpty()) {
            List<FaceRecognitionObject> recogs = HVCStorage.INSTANCE.getFaceRecognitionDatas(name);
            if (recogs.size() == 0) {
                HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_fail_unregister_nothing), null);
                return;
            }
            long ret = HVCStorage.INSTANCE.removeFaceRecognitionData(name);
            HVCManager.INSTANCE.deleteAlbum(name, null);
            Log.d("ABC", "ret:" + ret); // TODO Comment out
            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_success_unregister), null);
            initListView();
            mName.setText("");
        } else {
            Toast.makeText(getContext(), getString(R.string.c2w_setting_message_6_2), Toast.LENGTH_LONG).show();
        }
    }

    /** Face Recognition Data List Adapter. */
    private class ListAdapter extends ArrayAdapter<String> {

        public ListAdapter(Context context, int textViewId) {
            super(context, textViewId);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.text, null);
            }
            String item = getItem(position);
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(item);
            convertView.setTag(item);
            return convertView;
        }
    }
}
