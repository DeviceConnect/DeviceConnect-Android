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
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.data.DataManager;
import org.deviceconnect.android.app.simplebot.data.DataManager.Data;
import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.android.app.simplebot.utils.DConnectHelper;
import org.deviceconnect.android.app.simplebot.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *　コマンド詳細画面
 */
public class CommandDetailsFragment extends Fragment implements View.OnClickListener {

    /** キーワードEditText */
    private EditText editTextKeyword;
    /** ServiceIDボタン */
    private Button buttonServiceId;
    /** パスボタン */
    private Button buttonPath;
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

    // TODO: 仮実装
    private Button buttonMethod;

    /** コマンドデータ */
    private Data commandData = new Data();

    /** サービス一覧 */
    private List<DConnectHelper.ServiceInfo> services;
    /** 選択されたサービス*/
    private DConnectHelper.ServiceInfo selectedService = null;

    /** 画面を戻すフラグ */
    private boolean popBackFlg = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command_details, container, false);
        Context context = view.getContext();
        Bundle bundle = getArguments();
        if (bundle == null) {
            return view;
        }
        Button buttonDelete = (Button)view.findViewById(R.id.buttonDelete);
        Button buttonUpdate = (Button)view.findViewById(R.id.buttonUpdate);
        Button buttonAdd = (Button)view.findViewById(R.id.buttonAdd);
        editTextKeyword = (EditText)view.findViewById(R.id.editTextKeyword);
        buttonServiceId = (Button)view.findViewById(R.id.buttonServiceId);
        buttonPath = (Button)view.findViewById(R.id.buttonPath);
        buttonBody = (Button)view.findViewById(R.id.buttonBody);
        buttonAccept = (Button)view.findViewById(R.id.buttonAccept);
        buttonAcceptUri = (Button)view.findViewById(R.id.buttonAcceptUri);
        buttonSuccess = (Button)view.findViewById(R.id.buttonSuccess);
        buttonSuccessUri = (Button)view.findViewById(R.id.buttonSuccessUri);
        buttonError = (Button)view.findViewById(R.id.buttonError);
        buttonErrorUri = (Button)view.findViewById(R.id.buttonErrorUri);
        // TODO: 仮実装
        buttonMethod = (Button)view.findViewById(R.id.buttonMethod);
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
        buttonPath.setOnClickListener(this);
        buttonBody.setOnClickListener(this);
        buttonAccept.setOnClickListener(this);
        buttonAcceptUri.setOnClickListener(this);
        buttonSuccess.setOnClickListener(this);
        buttonSuccessUri.setOnClickListener(this);
        buttonError.setOnClickListener(this);
        buttonErrorUri.setOnClickListener(this);
        // TODO: 仮実装
        buttonMethod.setOnClickListener(this);

        // プログレスダイアログを表示
        final ProgressDialog dialog = Utils.showProgressDialog(context);
        // サービス一覧を取得
        Utils.fetchServices(context, new DConnectHelper.FinishCallback<List<DConnectHelper.ServiceInfo>>() {
            @Override
            public void onFinish(List<DConnectHelper.ServiceInfo> serviceInfos, Exception error) {
                // プログレスダイアログを閉じる
                dialog.dismiss();
                // エラーチェック
                if (error != null) {
                    Utils.showErrorDialog(getActivity(), error);
                }
                services = serviceInfos;
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
        buttonPath.setText(checkUnset(commandData.path));
        buttonBody.setText(checkUnset(commandData.body));
        buttonAccept.setText(checkUnset(commandData.accept));
        buttonAcceptUri.setText(checkUnset(commandData.acceptUri));
        buttonSuccess.setText(checkUnset(commandData.success));
        buttonSuccessUri.setText(checkUnset(commandData.successUri));
        buttonError.setText(checkUnset(commandData.error));
        buttonErrorUri.setText(checkUnset(commandData.errorUri));
        // TODO: 仮実装
        buttonMethod.setText(checkUnset(commandData.method));
    }

    /**
     * 未設定チェック
     * @param str 文字列
     * @return 文字列がnullの場合に未設定の文字列を返す
     */
    private String checkUnset(String str) {
        if (str == null) {
            return getString(R.string.unset);
        }
        return str;
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
            case R.id.buttonPath:
                // TODO: 選択ダイアログにする
                onClickTextButton("パス入力", id);
                break;
            case R.id.buttonBody:
                // TODO: 選択ダイアログにする
                onClickTextButton("データ入力", id);
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
            // TODO: 仮実装
            case R.id.buttonMethod:
                final String items[] = new String[]{"GET", "POST", "PUT", "DELETE"};
                Utils.showSelectDialog(getActivity(), "メソッド選択（仮実装）", items, new DConnectHelper.FinishCallback<Integer>() {
                    @Override
                    public void onFinish(Integer integer, Exception error) {
                        commandData.method = items[integer];
                        // View更新
                        updateViews();
                    }
                });
                break;
        }
    }

    /**
     * サービスID選択ボタンイベント
     */
    private void onClickServiceId() {
        // サービス存在チェック
        if (services == null || services.size() == 0) {
            Utils.showAlertDialog(getActivity(), getString(R.string.service_not_found));
            return;
        }
        // 選択ダイアログ表示
        String items[] = new String[services.size()];
        for (int i = 0; i < services.size(); i++) {
            items[i] = services.get(i).name;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select_service))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 情報取得
                        selectedService = services.get(which);
                        commandData.serviceId = selectedService.id;
                        commandData.serviceName = selectedService.name;
                        // View更新
                        updateViews();
                    }
                })
                .show();
    }

    /**
     * テキスト入力ボタンイベント
     */
    private void onClickTextButton(String title, final int button) {
        // サービス選択チェック
        if (commandData.serviceId == null) {
            Utils.showAlertDialog(getActivity(), getString(R.string.service_not_selected));
            return;
        }
        int inputType = InputType.TYPE_CLASS_TEXT;
        String text = null;
        switch (button) {
            case R.id.buttonPath:
                text = commandData.path;
                inputType = inputType | InputType.TYPE_TEXT_VARIATION_URI;
                break;
            case R.id.buttonBody:
                text = commandData.body;
                break;
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
                    case R.id.buttonPath:
                        commandData.method = "GET";
                        commandData.path = s;
                        break;
                    case R.id.buttonBody:
                        commandData.body = s;
                        break;
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
            commandData.path == null ||
            commandData.body == null ||
            commandData.success == null ||
            commandData.error == null) {
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

}
