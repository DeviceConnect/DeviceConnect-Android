package org.deviceconnect.android.uiapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.uiapp.DConnectApplication;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.data.DCApi;
import org.deviceconnect.android.uiapp.data.DCParam;
import org.deviceconnect.android.uiapp.data.DCProfile;
import org.deviceconnect.android.uiapp.utils.MessageParser;
import org.deviceconnect.android.uiapp.utils.Settings;
import org.deviceconnect.message.DConnectEventMessage;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.entity.BinaryEntity;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.deviceconnect.android.uiapp.R.id.response;

public class ApiActivity extends BasicActivity {

    private static final String TYPE_IMAGE = "image/*";
    private DCApi mApi;
    private Map<Integer, TempData> mDataMap = new HashMap<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        Intent intent = getIntent();
        if (intent != null) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                getServiceInformation(serviceId, new OnReceivedServiceInformationListener() {
                    @Override
                    public void onReceived(final List<DCProfile> profiles) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DCProfile profile = getProfile(profiles);
                                if (profile != null) {
                                    mApi = getApi(profile);
                                    setView(mApi);
                                }
                            }
                        });
                    }
                });
            }

            String method = intent.getStringExtra("method");
            String path = intent.getStringExtra("path");
            setTitle(method.toUpperCase() + " " + path);
        }

        Button btn = (Button) findViewById(R.id.button_send);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mApi != null) {
                    try {
                        if ("event".equalsIgnoreCase(mApi.getXType())) {
                            executeEvent(mApi);
                        } else {
                            executeApi(mApi);
                        }
                    } catch (Exception e) {
                        showToast();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        getSDK().disconnectWebSocket();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSDK().connectWebSocket(new DConnectSDK.OnWebSocketListener() {
            @Override
            public void onOpen() {
            }

            @Override
            public void onClose() {
            }

            @Override
            public void onError(final Exception e) {
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        final TempData tempData = mDataMap.get(requestCode);
        if (data != null && resultCode == RESULT_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String dataString = data.getDataString();
                        tempData.mTextView.setText(dataString);
                        tempData.mParam.setValue(dataString);
                    } else {
                        String dataString = data.getData().toString();
                        tempData.mTextView.setText(dataString);
                        tempData.mParam.setValue(dataString);
                    }
                }
            });
        } else {
            super.onActivityResult(requestCode,resultCode, data);
        }
    }

    private void showToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ApiActivity.this, R.string.activity_api_parameter_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View createTextField(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_text_field, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        EditText editText = (EditText) view.findViewById(R.id.value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                param.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setHint(param.getType());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        if ("serviceId".equals(param.getName())) {
            String serviceId = getIntent().getStringExtra("serviceId");
            editText.setText(serviceId);
            param.setValue(serviceId);
        }

        return view;
    }

    private View createSwitch(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_switch, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        Switch sw = (Switch) view.findViewById(R.id.value);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setValue(String.valueOf(isChecked));
            }
        });

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private View createSpinner(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_spinner, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        List e = param.getEnum();
        for (Object o : e) {
            adapter.add("" + o);
        }

        Spinner spinner = (Spinner) view.findViewById(R.id.value);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                param.setValue(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private View createIntegerTextField(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_text_field, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        EditText editText = (EditText) view.findViewById(R.id.value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                param.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setHint(param.getType());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private View createNumberTextField(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_text_field, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        EditText editText = (EditText) view.findViewById(R.id.value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                param.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setHint(param.getType());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private View createArrayTextField(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_text_field, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        EditText editText = (EditText) view.findViewById(R.id.value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                param.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setHint(param.getType());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private View createFile(final DCParam param) {
        View view = getLayoutInflater().inflate(R.layout.item_file, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        final TextView value = (TextView) view.findViewById(R.id.value);

        Button btn = (Button) view.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                openSelectFile(param, value);
            }
        });

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private View createSeekBar(final DCParam param, final boolean isFloat) {
        View view = getLayoutInflater().inflate(R.layout.item_seek_bar, null);

        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(param.getName());

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.value);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                Number min = param.getMin();
                Number max = param.getMax();
                if (isFloat) {
                    float result = (max.floatValue() - min.floatValue()) * (progress / 100.0f);
                    param.setValue(String.valueOf(result));
                } else {
                    Number result = (max.intValue() - min.intValue()) * (progress / 100.0f);
                    param.setValue(String.valueOf(result.intValue()));
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(param.isRequired());
        checkBox.setEnabled(!param.isRequired());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                param.setSend(isChecked);
            }
        });

        param.setSend(param.isRequired());

        return view;
    }

    private void setView(final DCApi api) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.content);

        List<DCParam> paramList = api.getParameters();
        if (paramList != null) {
            for (final DCParam param : paramList) {
                if ("string".equalsIgnoreCase(param.getType())) {
                    layout.addView(createTextField(param));
                } else if ("boolean".equalsIgnoreCase(param.getType())) {
                    layout.addView(createSwitch(param));
                } else if ("integer".equalsIgnoreCase(param.getType())) {
                    if (param.getEnum() != null) {
                        layout.addView(createSpinner(param));
                    } else if (param.getMax() != null && param.getMin() != null) {
                        layout.addView(createSeekBar(param, false));
                    } else {
                        layout.addView(createIntegerTextField(param));
                    }
                } else if ("number".equalsIgnoreCase(param.getType())) {
                    if (param.getMax() != null && param.getMin() != null) {
                        layout.addView(createSeekBar(param, true));
                    } else {
                        layout.addView(createNumberTextField(param));
                    }
                } else if ("array".equalsIgnoreCase(param.getType())) {
                    layout.addView(createArrayTextField(param));
                } else if ("file".equalsIgnoreCase(param.getType())) {
                    layout.addView(createFile(param));
                }
            }
        }
    }

    private void openSelectFile(final DCParam param, final TextView textView) {
        int requestCode = UUID.randomUUID().hashCode();
        TempData data = new TempData();
        data.mParam = param;
        data.mTextView = textView;

        mDataMap.put(requestCode, data);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(TYPE_IMAGE);
        startActivityForResult(intent, requestCode);
    }

    private void openImageActivity(final String uri) {
        Intent intent = new Intent();
        intent.setClass(this, ImageActivity.class);
        intent.putExtra("uri", uri);
        startActivity(intent);
    }

    private void showResponseText(final DConnectResponseMessage response) {
        View view = findViewById(R.id.response_layout);
        view.setVisibility(View.VISIBLE);

        final MessageParser parser = new MessageParser();
        parser.setClickLinkListener(new MessageParser.OnClickLinkListener() {
            @Override
            public void onClick(String uri) {
                openImageActivity(uri);
            }
        });

        MovementMethod method = LinkMovementMethod.getInstance();

        TextView textView = (TextView) findViewById(R.id.response);
        textView.setText(parser.parse(response, 4));
        textView.setMovementMethod(method);
    }

    private void showErrorText(final int errorCode, final String errorMessage) {
        DConnectResponseMessage response = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        showResponseText(response);
    }

    private void showEventText(final DConnectEventMessage event) {
        View view = findViewById(R.id.event_layout);
        view.setVisibility(View.VISIBLE);

        final MessageParser parser = new MessageParser();
        parser.setClickLinkListener(new MessageParser.OnClickLinkListener() {
            @Override
            public void onClick(String uri) {
                openImageActivity(uri);
            }
        });

        MovementMethod method = LinkMovementMethod.getInstance();

        TextView textView = (TextView) findViewById(R.id.event);
        textView.setText(parser.parse(event, 4));
        textView.setMovementMethod(method);
    }

    private byte[] getContent(final String uri) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = getContentResolver().openInputStream(Uri.parse(uri));
            int len;
            byte[] buf = new byte[4096];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return out.toByteArray();
    }

    private void executeNotBodyApi(final DCApi api, final DConnectSDK.OnResponseListener listener) {
        DConnectSDK.URIBuilder builder = getSDK().createURIBuilder();
        builder.setPath(api.getPath());

        List<DCParam> paramList = api.getParameters();
        for (DCParam param :paramList) {
            if (param.isSend()) {
                builder.addParameter(param.getName(), param.getValue());
            }
        }

        switch (api.getMethod()) {
            case GET:
                getSDK().get(builder.build(), listener);
                break;
            case DELETE:
                getSDK().delete(builder.build(), listener);
                break;
        }
    }

    private void executeBodyApi(final DCApi api, final DConnectSDK.OnResponseListener listener) {
        DConnectSDK.URIBuilder builder = getSDK().createURIBuilder();
        builder.setPath(api.getPath());

        MultipartEntity data = new MultipartEntity();
        List<DCParam> paramList = api.getParameters();
        for (DCParam param :paramList) {
            if (param.isSend()) {
                if ("file".equals(param.getType())) {
                    data.add(param.getName(), new BinaryEntity(getContent(param.getValue())));
                } else {
                    data.add(param.getName(), new StringEntity(param.getValue()));
                }
            }
        }

        switch (api.getMethod()) {
            case PUT:
                getSDK().put(builder.build(), data, listener);
                break;
            case POST:
                getSDK().post(builder.build(), data, listener);
                break;
        }
    }

    private void executeApi(final DCApi api) {
        View responseView = findViewById(R.id.response_layout);
        responseView.setVisibility(View.GONE);

        TextView textView = (TextView) findViewById(R.id.response);
        textView.setText("");

        DConnectSDK.OnResponseListener listener = new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResponseText(response);
                        }
                    });
                } else {
                    int errorCode = response.getErrorCode();
                    switch (DConnectMessage.ErrorCode.getInstance(errorCode)) {
                        case SCOPE:
                            DConnectApplication.SCOPES.add(api.getProfile());
                        case EXPIRED_ACCESS_TOKEN:
                        case EMPTY_ACCESS_TOKEN:
                        case NOT_FOUND_CLIENT_ID:
                            String[] profiles = new String[DConnectApplication.SCOPES.size()];
                            DConnectApplication.SCOPES.toArray(profiles);
                            String appName = getString(R.string.app_name);
                            getSDK().authorization(appName, profiles, new DConnectSDK.OnAuthorizationListener() {
                                @Override
                                public void onResponse(final String clientId, final String accessToken) {
                                    Settings.getInstance().setClientId(clientId);
                                    Settings.getInstance().setAccessToken(accessToken);
                                    getSDK().setAccessToken(accessToken);
                                    executeApi(api);
                                }

                                @Override
                                public void onError(final int errorCode, final String errorMessage) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showErrorText(errorCode, errorMessage);
                                        }
                                    });
                                }
                            });
                            break;
                        case AUTHORIZATION:
                        default:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showResponseText(response);
                                }
                            });
                            break;
                    }
                }
            }
        };

        switch (api.getMethod()) {
            case GET:
            case DELETE:
                executeNotBodyApi(api, listener);
                break;
            case PUT:
            case POST:
                executeBodyApi(api, listener);
                break;
        }
    }

    private void executeEvent(final DCApi api) {
        TextView textView = (TextView) findViewById(response);
        textView.setText("");

        DConnectSDK.URIBuilder builder = getSDK().createURIBuilder();
        builder.setPath(api.getPath());

        List<DCParam> paramList = api.getParameters();
        for (DCParam param :paramList) {
            if (param.isSend()) {
                builder.addParameter(param.getName(), param.getValue());
            }
        }

        switch (api.getMethod()) {
            case DELETE:
                getSDK().removeEventListener(builder.build());
                break;
            case PUT:
                getSDK().addEventListener(builder.build(), new DConnectSDK.OnEventListener() {
                    @Override
                    public void onMessage(final DConnectEventMessage message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showEventText(message);
                            }
                        });
                    }

                    @Override
                    public void onResponse(final DConnectResponseMessage response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResponseText(response);
                            }
                        });
                    }
                });
                break;
        }
    }

    private class TempData {
        TextView mTextView;
        DCParam mParam;
    }
}
