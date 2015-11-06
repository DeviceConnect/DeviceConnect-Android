package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaFeatureActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * THETA Device's Gallery Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaGalleryFragment extends Fragment {

    /** Theta's Gallery. */
    private ThetaGalleryAdapter mGalleryAdapter;

    /** Theta disconnect warning view. */
    private RelativeLayout mRecconectLayout;

    /** Singleton. */
    public static ThetaGalleryFragment newInstance() {
        return new ThetaGalleryFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO beta
        Intent intent = new Intent();
        intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                ThetaFeatureActivity.MODE_VR);
        intent.setClass(getActivity(), ThetaFeatureActivity.class);
        startActivity(intent);
        getActivity().finish();
//        getActivity().getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
//        WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
//        String ssId = wifiInfo.getSSID().replace("\"", "");
//        String message;
//        if (isTheta(ssId)) {
//            message = getString(R.string.camera_search_message_found);
//            message = message.replace("$NAME$", ssId);
//            getActivity().getActionBar().setTitle(ssId);
//            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//        } else {  //Open THETA Settings.
//            Toast.makeText(getActivity(), R.string.camera_must_connect, Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent();
//            intent.setClass(getActivity(), ThetaDeviceSettingsActivity.class);
////            startActivity(intent);
//        }
//        setRetainInstance(true);
//        mGalleryAdapter = new ThetaGalleryAdapter(getActivity(), createDataList(100)); // TODO new List<ThetaObject>();
    }


    /**
     * isTheta?
     *
     * @param ssId SSID
     * @return true:theta false:other
     */
    private boolean isTheta(final String ssId) {
        if (ssId == null) {
            return false;
        }
        return ssId.startsWith(getString(R.string.theta_ssid_prefix));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // list setting
        View rootView = inflater.inflate(R.layout.theta_gallery, container, false);
        mRecconectLayout = (RelativeLayout) rootView.findViewById(R.id.theta_reconnect_layout);
        rootView.findViewById(R.id.theta_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ThetaDeviceSettingsActivity.class);
                startActivity(intent);
            }
        });
        rootView.findViewById(R.id.theta_shutter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                        ThetaFeatureActivity.MODE_SHOOTING);
                intent.setClass(getActivity(), ThetaFeatureActivity.class);
                startActivity(intent);
            }
        });

        AbsListView list = (AbsListView) rootView.findViewById(R.id.theta_list);
        list.setAdapter(mGalleryAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                                ThetaFeatureActivity.MODE_VR);
                intent.setClass(getActivity(), ThetaFeatureActivity.class);
                startActivity(intent);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                return false;
            }
        });
        return rootView;
    }

    // TODO delete debug code
    private static List<String> createDataList(int counts) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < counts; i++) {
            list.add("i=" + i);
        }
        return list;
    }

    /**
     * ThetaGalleryAdapter.
     */
    private class ThetaGalleryAdapter extends ArrayAdapter<String> {
        /** LayoutInflater. */
        private LayoutInflater mInflater;

        /**
         * コンストラクタ.
         *
         * @param context Context.
         * @param objects ThetaGalleryList.
         */
        public ThetaGalleryAdapter(final Context context, final List<String> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View cv = convertView;
            if (cv == null) {
                cv = mInflater.inflate(R.layout.theta_gallery_adapter, parent, false);
            } else {
                cv = convertView;
            }
            ImageView thumb = (ImageView) cv.findViewById(R.id.theta_thumb_data);
            ImageView type = (ImageView) cv.findViewById(R.id.data_type);
            TextView date = (TextView) cv.findViewById(R.id.data_date);
            ProgressBar progress = (ProgressBar) cv.findViewById(R.id.theta_thumb_progress);

            String dateText = getItem(position);
            date.setText(dateText);
            type.setImageResource(R.drawable.ic_action_labels);
            return cv;
        }
    }
}