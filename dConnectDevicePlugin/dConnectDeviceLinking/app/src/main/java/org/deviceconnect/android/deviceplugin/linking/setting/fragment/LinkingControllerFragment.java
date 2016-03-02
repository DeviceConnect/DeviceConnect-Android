/*
 LinkingControllerFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nttdocomo.android.sdaiflib.ControlSensorData;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.IlluminationData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.VibrationData;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.Map;

/**
 * Fragment for show Linking Controller.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingControllerFragment extends Fragment {

    private LinkingDevice mDevice;
    private ControlSensorData mController;
    private TextView mDeviceNameView;
    private View mRoot;
    private int mCurrentRequestType = 0;

    private ControlSensorData.SensorDataInterface mSensorInterface = new ControlSensorData.SensorDataInterface() {
        @Override
        public void onStopSensor(String bd, int type, int reason) {
            updateDataText(0, 0, 0, 0, 0);
            updateDataText(1, 0, 0, 0, 0);
            updateDataText(2, 0, 0, 0, 0);
        }

        @Override
        public void onSensorData(String bd, int type, float x, float y, float z, byte[] originalData, long time) {
            updateDataText(type, x, y, z, time);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 4) {
            return;
        }
        //RESULT_OK, RESULT_SENSOR_UNSUPPORTED
        if (resultCode != -1 && resultCode != 8) {
            mCurrentRequestType = 0;
            return;
        }
        if (mCurrentRequestType == 2) {
            mCurrentRequestType = 0;
            return;
        }
        if (mController == null) {
            mCurrentRequestType = 0;
            return;
        }
        mCurrentRequestType += 1;
        mController.setType(mCurrentRequestType);
        int result = mController.start();
        printResult(result);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mDevice = null;
        View root = inflater.inflate(R.layout.control_linking, container, false);
        mRoot = root;
        setupUI(root);
        return root;
    }

    public void setTargetDevice(LinkingDevice device) {
        mDevice = device;
        mDeviceNameView.setText(mDevice.getDisplayName());
        setupLightOffSetting();
        setupVibrationOffSetting();
    }

    private void setupLightOffSetting() {
        byte[] illumination = mDevice.getIllumination();
        if (illumination == null) {
            ((Button) mRoot.findViewById(R.id.select_light_off)).setText(getString(R.string.not_selected));
            return;
        }
        IlluminationData data = new IlluminationData(illumination);
        Map<String, Integer> map = PreferenceUtil.getInstance(getActivity().getApplicationContext()).getLightOffSetting();
        if (map == null) {
            return;
        }
        Integer patternId = map.get(mDevice.getBdAddress());
        if (patternId == null) {
            ((Button) mRoot.findViewById(R.id.select_light_off)).setText(getString(R.string.not_selected));
            return;
        }
        for (IlluminationData.Setting setting : data.mPattern.children) {
            if ((setting.id & 0xFF) == patternId) {
                ((Button) mRoot.findViewById(R.id.select_light_off)).setText(setting.names[0].name);
            }
        }
    }

    private void setupVibrationOffSetting() {
        byte[] vibration = mDevice.getVibration();
        if (vibration == null) {
            ((Button) mRoot.findViewById(R.id.select_vibration_off)).setText(getString(R.string.not_selected));
            return;
        }
        VibrationData data = new VibrationData(vibration);
        Map<String, Integer> map = PreferenceUtil.getInstance(getActivity().getApplicationContext()).getVibrationOffSetting();
        if (map == null) {
            return;
        }
        Integer patternId = map.get(mDevice.getBdAddress());
        if (patternId == null) {
            ((Button) mRoot.findViewById(R.id.select_vibration_off)).setText(getString(R.string.not_selected));
            return;
        }
        for (VibrationData.Setting setting : data.mPattern.children) {
            if ((setting.id & 0xFF) == patternId) {
                ((Button) mRoot.findViewById(R.id.select_vibration_off)).setText(setting.names[0].name);
            }
        }
    }

    private void setupUI(final View root) {
        String deviceName = getString(R.string.device_name) + getString(R.string.not_selected);
        mDeviceNameView = (TextView) root.findViewById(R.id.device_name);
        mDeviceNameView.setText(deviceName);
        setLightButton(root);
        setVibrationButton(root);
        setSensorButton(root);
    }

    private void setLightButton(final View view) {
        view.findViewById(R.id.select_light_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDevice()) {
                    return;
                }

                byte[] illumination = mDevice.getIllumination();
                if (illumination == null) {
                    Toast.makeText(getContext(), getString(R.string.device_not_support_led), Toast.LENGTH_SHORT).show();
                    return;
                }

                final IlluminationData data = new IlluminationData(illumination);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final String[] items = new String[data.mPattern.children.length];
                for (int i = 0; i < data.mPattern.children.length; i++) {
                    items[i] = data.mPattern.children[i].names[0].name;
                }
                builder.setTitle(getString(R.string.pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IlluminationData.Setting selectedPattern = data.mPattern.children[which];
                        ((Button) view.findViewById(R.id.select_light_off)).setText(selectedPattern.names[0].name);
                        updateLightOffSetting(selectedPattern.id & 0xFF);
                    }
                });
                builder.create().show();
            }
        });
        view.findViewById(R.id.on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLED(true);
            }
        });
        view.findViewById(R.id.off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLED(false);
            }
        });
    }

    private void setVibrationButton(final View view) {
        view.findViewById(R.id.select_vibration_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDevice()) {
                    return;
                }

                byte[] vibration = mDevice.getVibration();
                if (vibration == null) {
                    Toast.makeText(getContext(), getString(R.string.device_not_support_vibration), Toast.LENGTH_SHORT).show();
                    return;
                }

                final VibrationData data = new VibrationData(vibration);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final String[] items = new String[data.mPattern.children.length];
                for (int i = 0; i < data.mPattern.children.length; i++) {
                    items[i] = data.mPattern.children[i].names[0].name;
                }
                builder.setTitle(getString(R.string.pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VibrationData.Setting selectedPattern = data.mPattern.children[which];
                        ((Button) view.findViewById(R.id.select_vibration_off)).setText(selectedPattern.names[0].name);
                        updateVibrationOffSetting(selectedPattern.id & 0xFF);
                    }
                });
                builder.create().show();
            }
        });
    }

    private void setSensorButton(View view) {
        view.findViewById(R.id.sensor_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSensor(true);
            }
        });
        view.findViewById(R.id.sensor_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSensor(false);
            }
        });
    }

    private void updateLightOffSetting(Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getActivity().getApplicationContext());
        Map<String, Integer> map = util.getLightOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setLightOffSetting(map);
    }

    private void updateVibrationOffSetting(Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getActivity().getApplicationContext());
        Map<String, Integer> map = util.getVibrationOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setVibrationOffSetting(map);
    }


    private void updateDataText(int type, float x, float y, float z, long time) {
        switch (type) {
            case 0:
                ((TextView) mRoot.findViewById(R.id.gyro_text)).setText(makeParamText(x, y, z, time));
                break;
            case 1:
                ((TextView) mRoot.findViewById(R.id.acceleration_text)).setText(makeParamText(x, y, z, time));
                break;
            case 2:
                ((TextView) mRoot.findViewById(R.id.orientation_text)).setText(makeParamText(x, y, z, time));
                break;
        }
    }

    private String makeParamText(float x, float y, float z, long time) {
        return "x:" + x + "\ny:" + y + "\nz:" + z + "\ntime:" + time;
    }

    private void onClickLED(boolean isOn) {
        if (!checkDevice()) {
            return;
        }
        if (mDevice.getIllumination() == null) {
            Toast.makeText(getContext(), getString(R.string.device_not_support_led), Toast.LENGTH_SHORT).show();
            return;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        manager.sendLEDCommand(mDevice, isOn);
    }

    private void onClickSensor(boolean isOn) {
        if (!checkDevice()) {
            return;
        }

        //TODO:check device's feature

        if (isOn) {
            if (mController == null) {
                mController = new ControlSensorData(getActivity(), mSensorInterface);
                mController.setInterval(100);
                mController.setType(0);
            } else {
                int result = mController.stop();
                printResult(result);
                updateDataText(0, 0, 0, 0, 0);
                updateDataText(1, 0, 0, 0, 0);
                updateDataText(2, 0, 0, 0, 0);
            }
            mController.setBDaddress(mDevice.getBdAddress());
            mCurrentRequestType = 0;
            int result = mController.start();
            printResult(result);
        } else {
            if (mController != null) {
                int result = mController.stop();
                printResult(result);
                updateDataText(0, 0, 0, 0, 0);
                updateDataText(1, 0, 0, 0, 0);
                updateDataText(2, 0, 0, 0, 0);
                mController = null;
            }
        }
    }

    private boolean checkDevice() {
        if (mDevice == null) {
            Toast.makeText(getActivity(), getString(R.string.device_not_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void printResult(int result) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        switch (result) {
            case -1:
                Log.i("LinkingPlugIn", "RESULT_OK(-1)");
                break;
            case 1:
                Log.i("LinkingPlugIn", "RESULT_CANCEL(1)");
                break;
            case 4:
                Log.i("LinkingPlugIn", "RESULT_DEVICE_OFF(4)");
                break;
            case 5:
                Log.i("LinkingPlugIn", "RESULT_CONNECT_FAILURE(5)");
                break;
            case 6:
                Log.i("LinkingPlugIn", "RESULT_CONFLICT(6)");
                break;
            case 7:
                Log.i("LinkingPlugIn", "RESULT_PARAM_ERROR(7)");
                break;
            case 8:
                Log.i("LinkingPlugIn", "RESULT_SENSOR_UNSUPPORTED(8)");
                break;
            case 0:
                Log.i("LinkingPlugIn", "RESULT_OTHER_ERROR(0)");
                break;
            default:
                Log.i("LinkingPlugIn", "UNKNOWN_ERROR(" + result + ")");
        }
    }

}
