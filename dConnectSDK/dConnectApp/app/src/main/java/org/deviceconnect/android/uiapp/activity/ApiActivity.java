package org.deviceconnect.android.uiapp.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.deviceconnect.android.uiapp.DConnectApplication;
import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.data.DCApi;
import org.deviceconnect.android.uiapp.data.DCParam;
import org.deviceconnect.android.uiapp.data.DCProfile;
import org.deviceconnect.android.uiapp.utils.MessageParser;
import org.deviceconnect.android.uiapp.utils.Settings;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                executeApi(mApi);
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

    private void setView(final DCApi api) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.content);

        List<DCParam> paramList = api.getParameters();
        if (paramList != null) {
            for (final DCParam param : paramList) {
                if ("string".equalsIgnoreCase(param.getType())) {
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

                    layout.addView(view);

                    if ("serviceId".equals(param.getName())) {
                        String serviceId = getIntent().getStringExtra("serviceId");
                        editText.setText(serviceId);
                        param.setValue(serviceId);
                    }
                } else if ("boolean".equalsIgnoreCase(param.getType())) {

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

                    layout.addView(view);
                } else if ("integer".equalsIgnoreCase(param.getType())) {
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

                    layout.addView(view);
                } else if ("number".equalsIgnoreCase(param.getType())) {
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

                    layout.addView(view);
                } else if ("array".equalsIgnoreCase(param.getType())) {
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

                    layout.addView(view);
                } else if ("file".equalsIgnoreCase(param.getType())) {
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

                    layout.addView(view);
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

    private void onTest(final DConnectResponseMessage response) {
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

        Map<String, String> data = new HashMap<>();
        List<DCParam> paramList = api.getParameters();
        for (DCParam param :paramList) {
            if (param.isSend()) {
                data.put(param.getName(), param.getValue());
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
        if (api == null) {
            return;
        }

        TextView textView = (TextView) findViewById(R.id.response);
        textView.setText("");

        DConnectSDK.OnResponseListener listener = new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onTest(response);
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
                                    Log.e("ABC", "error");
                                }
                            });
                            break;
                        case AUTHORIZATION:
                            break;
                        default:
                            // TODO: エラー処理
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

    private class TempData {
        TextView mTextView;
        DCParam mParam;
    }
}
