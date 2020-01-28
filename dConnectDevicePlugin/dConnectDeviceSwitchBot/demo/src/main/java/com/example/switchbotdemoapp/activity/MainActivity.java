package com.example.switchbotdemoapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.switchbotdemoapp.BuildConfig;
import com.example.switchbotdemoapp.R;
import com.example.switchbotdemoapp.profile.ButtonProfile;
import com.example.switchbotdemoapp.profile.SwitchProfile;
import com.example.switchbotdemoapp.utility.DConnectWrapper;

import org.deviceconnect.message.DConnectResponseMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private Spinner mSpinner;
    private RadioGroup mRadioGroup;
    private DConnectWrapper.ServiceDiscoveryCallback mServiceDiscoveryCallback = new DConnectWrapper.ServiceDiscoveryCallback() {
        @Override
        public void onSuccess(final ArrayList<DConnectWrapper.Service> serviceList) {
            if (DEBUG) {
                Log.d(TAG, "onSuccess()");
                for (DConnectWrapper.Service service : serviceList) {
                    Log.d(TAG, "serviceId : " + service.getServiceId());
                    Log.d(TAG, "serviceName : " + service.getServiceName());
                }
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ServiceAdapter serviceAdapter = new ServiceAdapter(MainActivity.this, serviceList);
                    mSpinner.setAdapter(serviceAdapter);
                }
            });
        }

        @Override
        public void onFailure(int errorCode, String errorMessage) {
            Log.e(TAG, "onFailure()");
            Log.e(TAG, "errorCode : " + errorCode);
            Log.e(TAG, "errorMessage : " + errorMessage);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (DEBUG) {
            Log.d(TAG, "onCreate()");
            Log.d(TAG, "savedInstanceState : " + savedInstanceState);
        }
        mSpinner = findViewById(R.id.device_list);
        mRadioGroup = findViewById(R.id.operation_radio_group);
        findViewById(R.id.execute_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.execute_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        execute();
                    }
                });
            }
        });
        DConnectWrapper.startServiceDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(TAG, "onDestroy()");
        }
        DConnectWrapper.stopServiceDiscovery();
    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            Log.d(TAG, "onStart()");
        }
        super.onStart();
        DConnectWrapper.registerServiceDiscoveryCallback(mServiceDiscoveryCallback);
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.d(TAG, "onStop()");
        }
        super.onStop();
        DConnectWrapper.unregisterServiceDiscoveryCallback(mServiceDiscoveryCallback);
    }

    private void execute() {
        DConnectWrapper.Service service = (DConnectWrapper.Service) mSpinner.getSelectedItem();
        if (service != null) {
            final String serviceId = service.getServiceId();
            final String serviceName = service.getServiceName();
            if (DEBUG) {
                Log.d(TAG, "serviceId : " + serviceId);
                Log.d(TAG, "serviceName : " + serviceName);
            }
            HashMap<String, String> params = new HashMap<>();
            params.put(DConnectWrapper.PARAM_SERVICE_ID, serviceId);
            switch (mRadioGroup.getCheckedRadioButtonId()) {
                case R.id.push:
                    execute(ButtonProfile.PROFILE_NAME, ButtonProfile.AT_PUSH, serviceId, params);
                    break;
                case R.id.down:
                    execute(ButtonProfile.PROFILE_NAME, ButtonProfile.AT_DOWN, serviceId, params);
                    break;
                case R.id.up:
                    execute(ButtonProfile.PROFILE_NAME, ButtonProfile.AT_UP, serviceId, params);
                    break;
                case R.id.turn_off:
                    execute(SwitchProfile.PROFILE_NAME, SwitchProfile.AT_TURN_OFF, serviceId, params);
                    break;
                case R.id.turn_on:
                    execute(SwitchProfile.PROFILE_NAME, SwitchProfile.AT_TURN_ON, serviceId, params);
                    break;
                default:
                    break;
            }
        }
    }

    private void execute(final String profile, final String attribute, final String serviceId, final HashMap<String, String> params) {
        findViewById(R.id.execute_button).setClickable(false);
        DConnectWrapper.post(profile, attribute, serviceId, params, new DConnectWrapper.RestApiCallback() {
            @Override
            public void onSuccess(String profile, String attribute, DConnectWrapper.Method method, String serviceId, DConnectResponseMessage dConnectResponseMessage) {
                if (DEBUG) {
                    Log.d(TAG, "onSuccess()");
                    Log.d(TAG, "profile : " + profile);
                    Log.d(TAG, "attribute : " + attribute);
                    Log.d(TAG, "method : " + method);
                    Log.d(TAG, "serviceId : " + serviceId);
                    Log.d(TAG, "dConnectResponseMessage : " + dConnectResponseMessage);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_LONG).show();
                        findViewById(R.id.execute_button).setEnabled(true);
                    }
                });
                findViewById(R.id.execute_button).setClickable(true);
            }

            @Override
            public void onFailure(final String profile, final String attribute, final DConnectWrapper.Method method, final String serviceId, final int errorCode, final String errorMessage) {
                Log.e(TAG, "onSuccess()");
                Log.e(TAG, "profile : " + profile);
                Log.e(TAG, "attribute : " + attribute);
                Log.e(TAG, "method : " + method);
                Log.e(TAG, "serviceId : " + serviceId);
                Log.e(TAG, "errorCode : " + errorCode);
                Log.e(TAG, "errorMessage : " + errorMessage);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, String.format(Locale.US, "errorCode : %d, errorMessage : %s", errorCode, errorMessage), Toast.LENGTH_LONG).show();
                        findViewById(R.id.execute_button).setClickable(true);
                    }
                });
            }
        });
    }

    class ServiceAdapter extends ArrayAdapter<DConnectWrapper.Service> {
        ServiceAdapter(Context context, ArrayList<DConnectWrapper.Service> serviceList) {
            super(context, R.layout.spinner, serviceList);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public @NonNull View getView(int position, View contentView, @NonNull ViewGroup parent) {
            TextView textView = (TextView)super.getView(position, contentView, parent);
            DConnectWrapper.Service service = getItem(position);
            if(service != null) {
                textView.setText(service.getServiceName());
            }
            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView textView = (TextView)super.getDropDownView(position, convertView, parent);
            DConnectWrapper.Service service = getItem(position);
            if(service != null) {
                textView.setText(service.getServiceName());
            }
            return textView;
        }
    }
}
