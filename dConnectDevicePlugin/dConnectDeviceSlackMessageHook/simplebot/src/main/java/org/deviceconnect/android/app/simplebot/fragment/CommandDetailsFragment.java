/*
 CommandDetailsFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.data.DataManager;
import org.deviceconnect.android.app.simplebot.data.DataManager.Data;
import org.deviceconnect.android.app.simplebot.utils.DConnectHelper;
import org.deviceconnect.android.app.simplebot.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *　コマンド詳細画面
 */
public class CommandDetailsFragment extends Fragment implements View.OnClickListener, ShowMenuFragment {

    /** メニュー */
    private Menu mainMenu;

    /** キーワードEditText */
    private EditText editTextKeyword;
    /** ServiceIDボタン */
    private Button buttonServiceId;
    /** APIボタン */
    private Button buttonApi;
    /** データボタン */
    private Button buttonBody;
    /** 受付レスポンスボタン */
    private Button buttonAccept;
    /** 受付レスポンスURIボタン */
    private Button buttonAcceptUri;
    /** 成功レスポンスボタン */
    private Button buttonSuccess;
    /** 成功レスポンスURIボタン */
    private Button buttonSuccessUri;
    /** 失敗レスポンスボタン */
    private Button buttonError;
    /** 失敗レスポンスURIボタン */
    private Button buttonErrorUri;

    /** コマンドデータ */
    private Data commandData = new Data();

    /** サービス一覧 */
    private List<DConnectHelper.ServiceInfo> services;
    /** 選択されたサービス*/
    private DConnectHelper.ServiceInfo selectedService = null;
    /** API一覧 */
    private List<DConnectHelper.APIInfo> apiInfoList;
    /** 選択されたAPI */
    private DConnectHelper.APIInfo selectedApi;


    /** 画面を戻すフラグ */
    private boolean popBackFlg = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_command_details, container, false);
        final Context context = view.getContext();
        Bundle bundle = getArguments();
        if (bundle == null) {
            return view;
        }
        Button buttonDelete = (Button)view.findViewById(R.id.buttonDelete);
        Button buttonUpdate = (Button)view.findViewById(R.id.buttonUpdate);
        Button buttonAdd = (Button)view.findViewById(R.id.buttonAdd);
        editTextKeyword = (EditText)view.findViewById(R.id.editTextKeyword);
        buttonServiceId = (Button)view.findViewById(R.id.buttonServiceId);
        buttonApi = (Button)view.findViewById(R.id.buttonApi);
        buttonBody = (Button)view.findViewById(R.id.buttonBody);
        buttonAccept = (Button)view.findViewById(R.id.buttonAccept);
        buttonAcceptUri = (Button)view.findViewById(R.id.buttonAcceptUri);
        buttonSuccess = (Button)view.findViewById(R.id.buttonSuccess);
        buttonSuccessUri = (Button)view.findViewById(R.id.buttonSuccessUri);
        buttonError = (Button)view.findViewById(R.id.buttonError);
        buttonErrorUri = (Button)view.findViewById(R.id.buttonErrorUri);
        // ボタン表示切り替え
        if ("add".equals(bundle.getString("mode"))) {
            buttonDelete.setVisibility(View.GONE);
            buttonUpdate.setVisibility(View.GONE);
        } else {
            buttonAdd.setVisibility(View.GONE);
        }
        // データ処理
        final DataManager dm = new DataManager(context);
        long id = bundle.getLong("id");
        if (id > 0) {
            // データ反映
            Data d = dm.getData(id);
            if (d == null) {
                commandData = new Data();
            } else {
                commandData = d;
            }
            updateViews();
            // KeywordはupdateViewsで更新しないので、ここで更新
            editTextKeyword.setText(commandData.keyword);
        }
        // リスナー登録
        buttonAdd.setOnClickListener(this);
        buttonUpdate.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonServiceId.setOnClickListener(this);
        buttonApi.setOnClickListener(this);
        buttonBody.setOnClickListener(this);
        buttonAccept.setOnClickListener(this);
        buttonAcceptUri.setOnClickListener(this);
        buttonSuccess.setOnClickListener(this);
        buttonSuccessUri.setOnClickListener(this);
        buttonError.setOnClickListener(this);
        buttonErrorUri.setOnClickListener(this);

        // プログレスダイアログを表示
        final ProgressDialog dialog = Utils.showProgressDialog(context);
        // サービス一覧を取得
        Utils.fetchServices(context, new DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>>() {
            @Override
            public void onFinish(List<DConnectHelper.ServiceInfo> serviceInfos, Exception error) {
                // エラーチェック
                if (error != null) {
                    Utils.showErrorDialog(getActivity(), error);
                    // プログレスダイアログを閉じる
                    dialog.dismiss();
                    return;
                }
                services = serviceInfos;
                if (services != null) {
                    // 選択中のサービスを再設定
                    for(DConnectHelper.ServiceInfo info: services) {
                        if (info.id.equals(commandData.serviceId)) {
                            selectedService = info;
                            // プログレスダイアログを表示
                            fetchServiceInformation(context, dialog);
                            break;
                        }
                    }
                }
                if (selectedService == null) {
                    // プログレスダイアログを閉じる
                    dialog.dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 画面を戻す
        if (popBackFlg) {
            getFragmentManager().popBackStack();
        } else {
            getActivity().setTitle(getString(R.string.app_name) + " [コマンド詳細]");
        }
    }

    /**
     * 画面更新
     */
    private void updateViews() {
        buttonServiceId.setText(checkUnset(commandData.serviceName));
        buttonApi.setText(checkUnset(commandData.api));
        buttonBody.setText(checkUnset(commandData.body));
        buttonAccept.setText(checkUnset(commandData.accept));
        buttonAcceptUri.setText(checkUnset(commandData.acceptUri));
        buttonSuccess.setText(checkUnset(commandData.success));
        buttonSuccessUri.setText(checkUnset(commandData.successUri));
        buttonError.setText(checkUnset(commandData.error));
        buttonErrorUri.setText(checkUnset(commandData.errorUri));
    }

    /**
     * 未設定チェック
     * @param str 文字列
     * @return 文字列がnullの場合に未設定の文字列を返す
     */
    private String checkUnset(String str) {
        if (str == null || str.length() == 0) {
            return getString(R.string.unset);
        }
        return str;
    }


    /**
     * ServiceInformationを取得
     * @param context Context
     */
    private void fetchServiceInformation(final Context context, final ProgressDialog dialog) {
        // 選択状態初期化
        apiInfoList = null;
        selectedApi = null;
        Utils.fetchServiceInformation(context, selectedService.id, new DConnectHelper.FinishCallback<Map<String, List<DConnectHelper.APIInfo>>>() {
            @Override
            public void onFinish(Map<String, List<DConnectHelper.APIInfo>> apiInfos, Exception error) {
                // プログレスダイアログを閉じる
                dialog.dismiss();
                // エラーチェック
                if (error != null) {
                    Utils.showErrorDialog(context, error);
                } else if (apiInfos == null) {
                    Utils.showAlertDialog(context, context.getString(R.string.service_not_supported));
                } else {
                    // Profile単位を除外して一律のリストに格納
                    apiInfoList = new ArrayList<>();
                    for (Map.Entry<String, List<DConnectHelper.APIInfo>> apis: apiInfos.entrySet()) {
                        apiInfoList.addAll(apis.getValue());
                    }
                    // 選択中のAPIを再設定
                    if (commandData.path != null && commandData.method != null) {
                        for (DConnectHelper.APIInfo info: apiInfoList) {
                            String path = "/gotapi/" + info.path;
                            if (path.equals(commandData.path) && info.method.equals(commandData.method)) {
                                selectedApi = info;
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    //---------------------------------------------------------------------------------------
    //region Click Events

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonServiceId:
                onClickServiceId();
                break;
            case R.id.buttonAdd:
            case R.id.buttonUpdate:
                onClickUpdate();
                break;
            case R.id.buttonDelete:
                onClickDelete();
                break;
            case R.id.buttonApi:
                onClickApi();
                break;
            case R.id.buttonBody:
                onClickData();
                break;
            case R.id.buttonAccept:
                onClickTextButton("受付レスポンス入力", id);
                break;
            case R.id.buttonAcceptUri:
                onClickTextButton("受付レスポンスリソースURI入力", id);
                break;
            case R.id.buttonSuccess:
                onClickTextButton("成功レスポンス入力", id);
                break;
            case R.id.buttonSuccessUri:
                onClickTextButton("成功レスポンスリソースURI入力", id);
                break;
            case R.id.buttonError:
                onClickTextButton("失敗レスポンス入力", id);
                break;
            case R.id.buttonErrorUri:
                onClickTextButton("失敗レスポンスリソースURI入力", id);
                break;
        }
    }

    /**
     * サービスID選択ボタンイベント
     */
    private void onClickServiceId() {
        final Context context = getActivity();
        // サービス存在チェック
        if (services == null || services.size() == 0) {
            Utils.showAlertDialog(context, getString(R.string.service_not_found));
            return;
        }
        // 選択ダイアログ表示
        String items[] = new String[services.size()];
        for (int i = 0; i < services.size(); i++) {
            items[i] = services.get(i).name;
        }
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.select_service))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 情報取得
                        selectedService = services.get(which);
                        commandData.serviceId = selectedService.id;
                        commandData.serviceName = selectedService.name;
                        commandData.api = null;
                        commandData.method = null;
                        commandData.path = null;
                        commandData.body = null;
                        // ServiceInformationを取得
                        // プログレスダイアログを表示
                        final ProgressDialog progressDialog = Utils.showProgressDialog(context);
                        fetchServiceInformation(context, progressDialog);
                        // View更新
                        updateViews();
                    }
                })
                .show();
    }

    /**
     * API選択ボタンイベント
     */
    private void onClickApi() {
        // サービス選択チェック
        if (selectedService == null) {
            Utils.showAlertDialog(getActivity(), getString(R.string.service_not_selected));
            return;
        }
        // サービス対応チェック
        if (apiInfoList == null || apiInfoList.size() == 0) {
            Utils.showAlertDialog(getActivity(), getString(R.string.service_not_supported));
            return;
        }

        // 選択ダイアログ表示
        String items[] = new String[apiInfoList.size()];
        for (int i = 0; i < apiInfoList.size(); i++) {
            items[i] = apiInfoList.get(i).name;
        }
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select_api))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DConnectHelper.APIInfo info = apiInfoList.get(which);
                        commandData.api = info.name;
                        commandData.path = "/gotapi/" + info.path;
                        commandData.method = info.method;
                        commandData.body = null;
                        // 選択中のAPI
                        selectedApi = info;
                        // View更新
                        updateViews();
                    }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ListView listView = ((AlertDialog) dialogInterface).getListView();
                final ListAdapter originalAdapter = listView.getAdapter();

                listView.setAdapter(new ListAdapter() {
                    @Override
                    public boolean areAllItemsEnabled() {
                        return originalAdapter.areAllItemsEnabled();
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        return originalAdapter.isEnabled(position);
                    }

                    @Override
                    public void registerDataSetObserver(DataSetObserver observer) {
                        originalAdapter.registerDataSetObserver(observer);
                    }

                    @Override
                    public void unregisterDataSetObserver(DataSetObserver observer) {
                        originalAdapter.unregisterDataSetObserver(observer);
                    }

                    @Override
                    public int getCount() {
                        return originalAdapter.getCount();
                    }

                    @Override
                    public Object getItem(int position) {
                        return originalAdapter.getItem(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return originalAdapter.getItemId(position);
                    }

                    @Override
                    public boolean hasStableIds() {
                        return originalAdapter.hasStableIds();
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = originalAdapter.getView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        textView.setTextSize(16);
                        return textView;
                    }

                    @Override
                    public int getItemViewType(int position) {
                        return originalAdapter.getItemViewType(position);
                    }

                    @Override
                    public int getViewTypeCount() {
                        return originalAdapter.getViewTypeCount();
                    }

                    @Override
                    public boolean isEmpty() {
                        return originalAdapter.isEmpty();
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * Data選択ボタンイベント
     */
    private void onClickData() {
        // API選択チェック
        if (selectedApi == null) {
            Utils.showAlertDialog(getActivity(), getString(R.string.api_not_selected));
            return;
        }

        // 設定済みのBodyデータをパース
        Map<String, Object> bodyJson = Utils.jsonToMap(commandData.body);

        // 入力ダイアログ作成
        final Context context = getActivity();
        float dp = context.getResources().getDisplayMetrics().density;
        ScrollView scrollView = new ScrollView(context);
        LinearLayout rootLayout = new LinearLayout(context);
        int p = (int) (10 * dp);
        rootLayout.setPadding(p, 0, p, 0);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(rootLayout);
        final Map<String, EditText> editMap = new HashMap<>();
        for (DConnectHelper.APIParam param: selectedApi.params) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            // サービスIDは自動入力なので無視
            if (param.name.equals(DataManager.COLUMN_SERVICE_ID)) {
                continue;
            }

            // 説明テキスト
            TextView textView = new TextView(context);
            if (param.required) {
                textView.setText(param.name + "*");
            } else {
                textView.setText(param.name);
            }
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (100 * dp),
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(textView);

            // 入力エリア
            EditText editView = new EditText(context);
            editView.setHint(param.name);
            // 入力制限
            if ("number".endsWith(param.type) || "integer".endsWith(param.type)) {
                editView.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            editView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(editView);
            editMap.put(param.name, editView);

            // 設定済みのBodyデータを反映
            if (bodyJson != null && bodyJson.containsKey(param.name)) {
                editView.setText((String) bodyJson.get(param.name));
            }

            rootLayout.addView(layout);
        }

        // データなし
        if (editMap.size() == 0) {
            Utils.showAlertDialog(context, context.getString(R.string.no_data_to_set));
            return;
        }

        // ダイアログ表示
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.data_set))
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JSONObject jsonObject = new JSONObject();
                        for (DConnectHelper.APIParam param: selectedApi.params) {
                            EditText editText = editMap.get(param.name);
                            if (editText == null) {
                                continue;
                            }
                            if (editText.getText().length() > 0) {
                                try {
                                    jsonObject.put(param.name, editText.getText().toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // 入力必須チェック
                                if (param.required) {
                                    Toast ts = Toast.makeText(context, getString(R.string.not_input_all), Toast.LENGTH_SHORT);
                                    ts.setGravity(Gravity.CENTER, 0, 0);
                                    ts.show();
                                    return;
                                }
                            }
                        }
                        commandData.body = jsonObject.toString();
                        // View更新
                        updateViews();
                        // 閉じる
                        dialogInterface.dismiss();
                    }
                });
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    /**
     * テキスト入力ボタンイベント
     */
    private void onClickTextButton(String title, final int button) {
        int inputType = InputType.TYPE_CLASS_TEXT;
        String text = null;
        switch (button) {
            case R.id.buttonAccept:
                text = commandData.accept;
                break;
            case R.id.buttonAcceptUri:
                text = commandData.acceptUri;
                break;
            case R.id.buttonSuccess:
                text = commandData.success;
                break;
            case R.id.buttonSuccessUri:
                text = commandData.successUri;
                break;
            case R.id.buttonError:
                text = commandData.error;
                break;
            case R.id.buttonErrorUri:
                text = commandData.errorUri;
                break;
        }
        Utils.showInputDialog(getActivity(), title, text, inputType, new DConnectHelper.FinishCallback<String>() {
            @Override
            public void onFinish(String s, Exception error) {
                switch (button) {
                    case R.id.buttonAccept:
                        commandData.accept = s;
                        break;
                    case R.id.buttonAcceptUri:
                        commandData.acceptUri = s;
                        break;
                    case R.id.buttonSuccess:
                        commandData.success = s;
                        break;
                    case R.id.buttonSuccessUri:
                        commandData.successUri = s;
                        break;
                    case R.id.buttonError:
                        commandData.error = s;
                        break;
                    case R.id.buttonErrorUri:
                        commandData.errorUri = s;
                        break;
                }
                // View更新
                updateViews();
            }
        });
    }

    /**
     * 追加/更新ボタン選択イベント
     */
    private void onClickUpdate() {
        final Context context = getActivity();
        // Keywordは変更イベントで更新していないので、ここで更新
        commandData.keyword = editTextKeyword.getText().toString();
        // 入力チェック
        if (commandData.keyword.length() == 0 ||
            commandData.path == null) {
            Utils.showAlertDialog(context, getString(R.string.not_input_all));
            return;
        }
        // scopeチェック
        List<String> scopes = null;
        if (selectedService != null) {
            scopes = selectedService.scopes;
        }
        Utils.checkScopes(context, scopes, new DConnectHelper.FinishCallback<Boolean>() {
            @Override
            public void onFinish(Boolean aBoolean, Exception error) {
                if (error == null) {
                    // データ保存
                    final DataManager dm = new DataManager(context);
                    if (dm.upsert(commandData)) {
                        // 画面を戻す
                        if (isResumed()) {
                            getFragmentManager().popBackStack();
                        } else {
                            // 画面が表示されていないのでonResumeで戻す。
                            popBackFlg = true;
                        }
                    } else {
                        Utils.showAlertDialog(context, getString(R.string.err_add_data));
                    }
                } else {
                    // 認証に失敗したらエラー
                    Utils.showAlertDialog(context, getString(R.string.err_add_data));
                }
            }
        });
    }

    /**
     * 削除ボタン選択イベント
     */
    private void onClickDelete() {
        Utils.showConfirmDialog(getActivity(), getString(R.string.del_command), getString(R.string.confirm_del_command), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DataManager dm = new DataManager(getActivity());
                if (dm.delete(commandData.id)) {
                    getFragmentManager().popBackStack();
                } else {
                    Utils.showAlertDialog(getActivity(), getString(R.string.err_del_data));
                }
            }
        });
    }

    //endregion
    //---------------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.help_menu, menu);
        mainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                Fragment fragment = new CommandDetailsHelpFragment();
                Utils.transition(fragment, getFragmentManager(), true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * メニューを表示
     */
    public void showMenu() {
        mainMenu.performIdentifierAction(R.id.overflow_options, 0);
    }
}
