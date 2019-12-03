/*
HueFragment04
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.exception.PHHueInvalidAPIException;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;

import org.deviceconnect.android.deviceplugin.hue.db.HueManager;
import org.deviceconnect.android.deviceplugin.hue.BuildConfig;
import org.deviceconnect.android.deviceplugin.hue.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hue setting fragment (4).
 */
public class HueFragment04 extends Fragment {
    /** Hue access point. */
    private PHAccessPoint mAccessPoint;

    /** List adapter. */
    private ListAdapter mListAdapter;

    /** Progress dialog. */
    private AlertDialog mProgressBar;

    /**
     * newInstance.
     * 
     * @param accessPoint Access point.
     * @return fragment Fragment instance.
     */
    public static HueFragment04 newInstance(final PHAccessPoint accessPoint) {
        HueFragment04 fragment = new HueFragment04();
        fragment.setPHAccessPoint(accessPoint);
        return fragment;
    }

    /**
     * Set PHAccessPoint.
     *
     * @param accessPoint Access point.
     */
    private void setPHAccessPoint(final PHAccessPoint accessPoint) {
        mAccessPoint = accessPoint;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hue_fragment_04, container, false);

        List<PHLight> lights = HueManager.INSTANCE.getCacheLights();
        if (lights != null) {
            mListAdapter = new ListAdapter(getActivity(), lights);
        } else {
            mListAdapter = new ListAdapter(getActivity(), new ArrayList<PHLight>());
        }

        ListView listView = view.findViewById(R.id.light_list_view);
        listView.setAdapter(mListAdapter);

        Button autoBtn =  view.findViewById(R.id.btn_auto_add);
        autoBtn.setOnClickListener((v) -> {
            searchLightAutomatic();
        });

        Button manualBtn = view.findViewById(R.id.btn_manual_add);
        manualBtn.setOnClickListener((v) -> {
            editSerial();
        });

        return view;
    }

    /**
     * Update ListView.
     */
    private void updateListView() {
        mListAdapter.setLights(HueManager.INSTANCE.getCacheLights());
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * Edit serial.
     */
    private void editSerial() {
        final EditText editText = new EditText(getActivity());
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.frag04_serial_number_title)
                .setMessage(R.string.frag04_serial_number_message)
                .setView(editText)
                .setPositiveButton(R.string.frag04_serial_ok, (dialogs, whichButton) -> {
                    String serial = editText.getText().toString();
                    searchLightManually(serial);
                })
                .setNegativeButton(R.string.frag04_serial_cancel, (dialogs, whichButton) -> {
                })
                .show();
        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        // Input limit of the serial number
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (source.toString().matches("[0-9a-fA-F]+")) {
                return source;
            } else {
                return "";
            }
        };
        InputFilter[] filters = new InputFilter[] {
                inputFilter, new InputFilter.LengthFilter(6)
        };
        editText.setFilters(filters);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                                          final int count, final int after) {
            }
            @Override
            public void onTextChanged(final CharSequence s, final int start,
                                      final int before, final int count) {
                positiveButton.setEnabled(editText.length() == 6);
            }
            @Override
            public void afterTextChanged(final Editable s) {
            }
        });
        editText.setHint(R.string.frag04_serial_number_hint);
    }

    /**
     * Search light automatic.
     */
    private void searchLightAutomatic() {
        openProgressBar();

        try {
            HueManager.INSTANCE.searchLightAutomatic(new PHLightListenerImpl());
        } catch (PHHueInvalidAPIException e) {
            if (BuildConfig.DEBUG) {
                Log.e("Hue", "error", e);
            }
            closeProgressBar();
        }
    }

    /**
     * Search light manually.
     * 
     * @param serial Serial number.
     */
    private void searchLightManually(final String serial) {
        openProgressBar();

        try {
            HueManager.INSTANCE.searchLightManually(serial, new PHLightListenerImpl());
        } catch (PHHueInvalidAPIException e) {
            if (BuildConfig.DEBUG) {
                Log.e("Hue", "error", e);
            }
            closeProgressBar();
        }
    }

    /**
     * UIスレッド上で指定されたRunnableを実行します.
     * @param run 実行するRunnable
     */
    private void runOnUiThread(final Runnable run) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(run);
        }
    }

    /**
     * Open progress bar. 
     */
    private void openProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View v = inflater.inflate(R.layout.dialog_progress, null);
                    TextView titleView = v.findViewById(R.id.title);
                    TextView messageView = v.findViewById(R.id.message);
                    titleView.setText(getString(R.string.message_light_searching));
                    messageView.setText(getString(R.string.frag04_serial_search));
                    mProgressBar = builder.setView(v).create();
                    mProgressBar.show();
                }
            }
        });
    }

    /**
     * Close progress bar. 
     */
    private void closeProgressBar() {
        runOnUiThread(() -> {
            if (mProgressBar != null) {
                mProgressBar.dismiss();
                mProgressBar = null;
            }
        });
    }

    /**
     * Show toast.
     * 
     * @param message Show message.
     */
    private void showToast(final String message) {
        runOnUiThread(() -> {
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 認証に失敗したことを通知するダイアログを表示します.
     * <p>
     * 通知後に、最初のブリッジ検索画面に遷移します。
     * </p>
     */
    private void showAuthenticationFailed() {
        runOnUiThread(() -> {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            new AlertDialog.Builder(activity)
                    .setTitle(R.string.frag02_failed)
                    .setMessage(R.string.frag04_unauthorized_bridge)
                    .setPositiveButton(R.string.hue_dialog_ok, (dialog, which) -> {
                        moveFirstFragment();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    /**
     * 最初のフラグメントに移動します.
     */
    private void moveFirstFragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
        transaction.replace(R.id.fragment_frame, new HueFragment01());
        transaction.commit();
    }

    /**
     * List adapter class.
     */
    private class ListAdapter extends BaseAdapter {
        /** Layout inflater. */
        private LayoutInflater mInflater;

        /** Light list. */
        private List<PHLight> mLights;

        /**
         * Constructor.
         * 
         * @param context Context.
         * @param lights Light list.
         */
        ListAdapter(final Context context, final List<PHLight> lights) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            setLights(lights);
        }

        @Override
        public int getCount() {
            return mLights.size();
        }

        @Override
        public Object getItem(final int position) {
            return mLights.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.hue_access_point, parent, false);
            }

            PHLight light = mLights.get(position);

            TextView titleView = view.findViewById(R.id.title);
            titleView.setText(light.getName());

            return view;
        }

        /**
         * Set lights.
         * 
         * @param lights Light list.
         */
        private void setLights(final List<PHLight> lights) {
            mLights = lights;
        }
    }

    /**
     * Light Listener.
     */
    private class PHLightListenerImpl implements PHLightListener {
        /** Bridge resource list. */
        private final List<PHBridgeResource> mLightHeaders = new ArrayList<PHBridgeResource>();

        @Override
        public void onError(final int code, final String message) {
            closeProgressBar();

            if (code == PHHueError.AUTHENTICATION_FAILED) {
                showAuthenticationFailed();
            } else {
                showToast(message);
            }
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onReceivingLights(final List<PHBridgeResource> lightHeaders) {
            for (PHBridgeResource header : lightHeaders) {
                boolean duplicated = false;
                for (PHBridgeResource cache : mLightHeaders) {
                    if (cache.getIdentifier().equals(header.getIdentifier())) {
                        duplicated = true;
                        break;
                    }
                }
                if (!duplicated) {
                    mLightHeaders.add(header);
                }
            }
        }

        @Override
        public void onSearchComplete() {
            if (mLightHeaders.size() == 0) {
                showToast(getString(R.string.frag04_not_found_new_light));
            } else {
                String message = getString(R.string.frag04_light_result1);
                message += mLightHeaders.size() + getString(R.string.frag04_light_result2);
                showToast(message);
            }
            closeProgressBar();

            View view = getView();
            if (view != null) {
                view.postDelayed(() -> {
                    updateListView();
                }, 400);
            }
        }

        @Override
        public void onStateUpdate(final Map<String, String> list, final  List<PHHueError> error) {
        }

        @Override
        public void onReceivingLightDetails(final PHLight light) {
        }
    }
}
