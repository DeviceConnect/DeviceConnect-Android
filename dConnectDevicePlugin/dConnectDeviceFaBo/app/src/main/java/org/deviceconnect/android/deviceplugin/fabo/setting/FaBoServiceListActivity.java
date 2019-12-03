package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.core.R;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceListener;
import org.deviceconnect.android.service.DConnectServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * FaBoプラグインが管理するサービスのリストを表示するActivity.
 */
public class FaBoServiceListActivity extends Activity {

    /**
     * プラグインのサービス.
     */
    private DConnectMessageService mMessageService;

    /**
     * DConnectServiceの状態を監視・管理するクラス.
     */
    private DConnectServiceProvider mProvider;

    /**
     * サービスとのバインド状態.
     */
    private boolean mIsBound;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(R.string.activity_service_list_title);
        }

        bindMessageService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceList();
    }

    @Override
    protected void onDestroy() {
        unbindMessageService();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * DConnectMessageServiceとバインドします.
     */
    private void bindMessageService() {
        if (!mIsBound) {
            Intent intent = new Intent(getApplicationContext(), Util.getDConnectMessageServiceClass(this));
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * DConnectMessageServiceとアンバインドします.
     */
    private void unbindMessageService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * FaBoが持っているサービスを表示するフラグメントを表示します.
     */
    private void showViewerFragment() {
        FragmentManager mgr = getFragmentManager();
        FragmentTransaction transaction = mgr.beginTransaction();
        ViewerFragment fragment = new ViewerFragment();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * FaBoが持っているサービスを削除するフラグメントを表示します.
     */
    private void showRemoverFragment() {
        FragmentManager mgr = getFragmentManager();
        FragmentTransaction transaction = mgr.beginTransaction();
        RemoverFragment fragment = new RemoverFragment();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * FaBoが持っているサービスを管理するクラスを取得します.
     * <p>
     * DConnectMessageServiceとバインドされていない場合にはnullを返却します.
     * </p>
     * @return DConnectServiceProviderのインスタンス
     */
    private DConnectServiceProvider getProvider() {
        return mProvider;
    }

    /**
     * 指定されたリストを削除します.
     * @param services 削除するサービスリスト
     */
    private void removeServices(final List<DConnectService> services) {
        for (DConnectService service : services) {
            mProvider.removeService(service.getId());

            if (service instanceof VirtualService) {
                FaBoDeviceService faBo = (FaBoDeviceService) mMessageService;
                faBo.removeServiceData(((VirtualService) service).getServiceData());
            }
        }
    }

    /**
     * VirtualServiceActivityを開きます.
     *
     * @param service 仮想サービス
     */
    private void openVirtualServiceActivity(final VirtualService service) {
        Intent intent = new Intent();
        intent.setClass(this, FaBoVirtualServiceActivity.class);
        if (service != null) {
            intent.putExtra("service", service.getServiceData());
        }
        startActivity(intent);
    }

    /**
     * DConnectMessageServiceとのバインドを状態のイベントを受け取るクラス.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            runOnUiThread(() -> {
                mMessageService = ((DConnectMessageService.LocalBinder) service).getMessageService();
                mProvider = mMessageService.getServiceProvider();
                mProvider.addServiceListener(mDConnectServiceListener);
                mIsBound = true;

                showViewerFragment();
            });
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mIsBound = false;
            mProvider.removeServiceListener(mDConnectServiceListener);
            mMessageService = null;
        }
    };

    /**
     * DConnectServiceの状態通知を受け取るリスナー.
     */
    private DConnectServiceListener mDConnectServiceListener = new DConnectServiceListener() {
        @Override
        public void onServiceAdded(final DConnectService service) {
            updateServiceList();
        }

        @Override
        public void onServiceRemoved(final DConnectService service) {
            updateServiceList();
        }

        @Override
        public void onStatusChange(final DConnectService service) {
            updateServiceList();
        }
    };

    /**
     * DConnectServiceのリスト更新を行います.
     */
    private void updateServiceList() {
        runOnUiThread(() -> {
            FragmentManager mgr = getFragmentManager();
            Fragment f = mgr.findFragmentById(R.id.fragment_container);
            if (f != null && f instanceof BaseFragment) {
                ((BaseFragment) f).notifyServiceStatusChange();
            }
        });
    }

    /**
     * DConnectServiceを表示するための基底フラグメント.
     */
    public static class BaseFragment extends Fragment {
        /**
         * 共通で使用するDConnectServiceのリストを管理するアダプタ.
         */
        protected ServiceListAdapter mListAdapter;

        /**
         * DConnectServiceの状態が変わったこと通知します.
         */
        protected void notifyServiceStatusChange() {
            mListAdapter.notifyDataSetChanged();
        }

        /**
         * DConnectServiceProviderのインスタンスを取得します.
         * <p>
         * DConnectMessageServiceとバインドされていない場合にはnullを返却します.
         * </p>
         * @return DConnectServiceProviderのインスタンス
         */
        protected DConnectServiceProvider getProvider() {
            return ((FaBoServiceListActivity) getActivity()).getProvider();
        }
    }

    /**
     * DConnectServiceを表示するためのフラグメント.
     */
    public static class ViewerFragment extends BaseFragment {

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            setHasOptionsMenu(true);

            View root = inflater.inflate(R.layout.fragment_service_list_viewer, container, false);

            mListAdapter = new ServiceListAdapter(getActivity(), getProvider(), false);

            ListView listView = root.findViewById(R.id.activity_fabo_service_list_view);
            listView.setAdapter(mListAdapter);
            listView.setItemsCanFocus(true);
            listView.setClickable(true);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Object service = mListAdapter.getItem(position);
                if (service instanceof VirtualService) {
                    openVirtualServiceActivity((VirtualService) service);
                }
            });

            Button newServiceButton = root.findViewById(R.id.activity_fabo_service_add_btn);
            newServiceButton.setOnClickListener((v) -> {
                openVirtualServiceActivity(null);
            });

            return root;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_fabo_service_list_viewer, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.menu_fabo_service_remove) {
                showRemoverFragment();
            }
            return true;
        }

        /**
         * 仮想サービス管理Activityを表示します.
         * @param service 表示するサービス
         */
        private void openVirtualServiceActivity(final VirtualService service) {
            ((FaBoServiceListActivity) getActivity()).openVirtualServiceActivity(service);
        }

        /**
         * 仮想サービス削除用フラグメントを表示します.
         */
        private void showRemoverFragment() {
            ((FaBoServiceListActivity) getActivity()).showRemoverFragment();
        }
    }

    /**
     * DConnectServiceを削除するためのフラグメント.
     */
    public static class RemoverFragment extends BaseFragment implements ServiceListAdapter.OnStatusChangeListener {
        /**
         * 削除ボタン.
         */
        private Button mRemoveBtn;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_service_list_remover, container, false);

            mListAdapter = new ServiceListAdapter(getActivity(), getProvider(), true);
            mListAdapter.setOnStatusChangeListener(this);

            ListView listView = root.findViewById(R.id.activity_fabo_service_list_view);
            listView.setAdapter(mListAdapter);
            listView.setItemsCanFocus(true);
            listView.setClickable(true);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Object service = mListAdapter.getItem(position);
                if (service instanceof VirtualService) {
                    VirtualService vs = (VirtualService) service;
                    if (vs.isOnline()) {
                        showOnlineDialog();
                    } else {
                        mListAdapter.toggleCheckBox((DConnectService) service);
                        CheckBox checkBox = view.findViewById(R.id.activity_fabo_service_removal_checkbox);
                        if (checkBox != null) {
                            checkBox.toggle();
                        }
                    }
                } else {
                    showImmortalDialog();
                }
            });

            Button cancelButton = root.findViewById(R.id.activity_fabo_service_cancel_btn);
            cancelButton.setOnClickListener((v) -> {
                showViewerFragment();
            });

            mRemoveBtn = (Button) root.findViewById(R.id.activity_fabo_service_remove_btn);
            mRemoveBtn.setOnClickListener((v) -> {
                showRemovalConfirmation();
            });
            mRemoveBtn.setEnabled(false);

            return root;
        }

        /**
         * 削除確認用ダイアログを表示します.
         */
        private void showRemovalConfirmation() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.activity_fabo_service_dialog_title_service_removal_confirmation)
                    .setMessage(R.string.activity_fabo_service_dialog_message_service_removal_confirmation)
                    .setCancelable(false)
                    .setPositiveButton(R.string.activity_fabo_service_dialog_button_service_removal_ok,
                            (dialog, which) -> {
                                removeService();
                            })
                    .setNegativeButton(R.string.activity_fabo_service_dialog_button_service_removal_cancel, null)
                    .create().show();
        }

        /**
         * オンラインのために削除できないことを通知するダイアログを表示します.
         */
        private void showOnlineDialog() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.activity_fabo_service_dialog_title_online)
                    .setMessage(R.string.activity_fabo_service_dialog_message_online)
                    .setCancelable(true)
                    .setPositiveButton(R.string.activity_fabo_service_dialog_button_service_removal_ok, null)
                    .create().show();
        }

        /**
         * FaBoServiceは削除できないので、エラーダイアログを表示します.
         */
        private void showImmortalDialog() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.activity_fabo_service_dialog_title_immortal)
                    .setMessage(R.string.activity_fabo_service_dialog_message_immortal)
                    .setCancelable(true)
                    .setPositiveButton(R.string.activity_fabo_service_dialog_button_service_removal_ok, null)
                    .create().show();
        }

        /**
         * DConnectServiceを削除します.
         */
        private void removeService() {
            ((FaBoServiceListActivity) getActivity()).removeServices(mListAdapter.getRemoveServices());
            mListAdapter.getRemoveServices().clear();
            showViewerFragment();
        }

        /**
         * DConnectService表示用フラグメントを表示します.
         */
        private void showViewerFragment() {
            ((FaBoServiceListActivity) getActivity()).showViewerFragment();
        }

        @Override
        public void onStatusChange(boolean flag) {
            mRemoveBtn.setEnabled(flag);
        }
    }

    /**
     * サービスリストを管理するアダプタ.
     */
    private static class ServiceListAdapter extends BaseAdapter {
        /**
         * DConnectServiceを管理するクラス.
         */
        private DConnectServiceProvider mProvider;

        /**
         * インフレータ.
         */
        private LayoutInflater mInflater;

        /**
         * CheckBoxの表示フラグ.
         * <p>
         * trueの場合はCheckBoxを表示します。
         * </p>
         */
        private boolean mHasCheckbox;

        /**
         * 削除用のCheckBoxの状態が変更されたことを通知するためのリスナー.
         */
        private OnStatusChangeListener mOnStatusChangeListener;

        /**
         * 削除するDConnectServiceを保持するリスト.
         */
        private final List<DConnectService> mRemoveServices = new ArrayList<>();

        /**
         * コンストラクタ.
         * @param context コンテキスト
         * @param provider DConnectServiceを管理するクラス
         * @param hasCheckbox CheckBox表示の有無
         */
        ServiceListAdapter(final Context context, final DConnectServiceProvider provider, final boolean hasCheckbox) {
            mProvider = provider;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHasCheckbox = hasCheckbox;
        }

        /**
         * CheckBox状態変更通知リスナーを設定します.
         * @param listener リスナー
         */
        void setOnStatusChangeListener(final OnStatusChangeListener listener) {
            mOnStatusChangeListener = listener;
        }

        @Override
        public int getCount() {
            if (mProvider == null) {
                return 0;
            } else {
                return mProvider.getServiceList().size();
            }
        }

        @Override
        public Object getItem(final int position) {
            return mProvider.getServiceList().get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_fabo_service_list, null);
            }

            final DConnectService service = (DConnectService) getItem(position);
            convertView.setTag(service);
            convertView.setBackgroundResource(service.isOnline() ?
                    R.color.service_list_item_background_online :
                    R.color.service_list_item_background_offline);

            TextView statusView = convertView.findViewById(R.id.service_online_status);
            statusView.setText(service.isOnline() ?
                    R.string.activity_fabo_service_online :
                    R.string.activity_fabo_service_offline);

            TextView nameView = convertView.findViewById(R.id.service_name);
            nameView.setText(service.getName());

            CheckBox checkBox = convertView.findViewById(R.id.activity_fabo_service_removal_checkbox);
            checkBox.setVisibility(hasCheckbox(service) ? View.VISIBLE : View.GONE);
            checkBox.setChecked(mRemoveServices.contains(service));

            ImageView imageView = (ImageView) convertView.findViewById(R.id.activity_fabo_service_edit);
            imageView.setVisibility(hasImageView(service) ? View.VISIBLE : View.GONE);

            return convertView;
        }

        /**
         * 削除するDConnectServiceのリストを取得します.
         * @return 削除するDConnectServiceのリスト
         */
        private List<DConnectService> getRemoveServices() {
            return mRemoveServices;
        }

        /**
         * 削除するDConnectServiceを切り替えます.
         * @param service 変更するDConnectService
         */
        private void toggleCheckBox(final DConnectService service) {
            if (!mRemoveServices.contains(service)) {
                mRemoveServices.add(service);
            } else {
                mRemoveServices.remove((service));
            }

            if (mOnStatusChangeListener != null) {
                mOnStatusChangeListener.onStatusChange(!mRemoveServices.isEmpty());
            }
        }

        /**
         * DConnectServiceのカラムにCheckBoxを表示するかを確認します.
         * @param service 確認するDConnectMessage
         * @return CheckBoxを表示する場合はtrue、それ以外はfalse
         */
        private boolean hasCheckbox(final DConnectService service) {
            return mHasCheckbox && !service.isOnline() && service instanceof VirtualService;
        }

        private boolean hasImageView(final DConnectService service) {
            return !mHasCheckbox && service instanceof VirtualService;
        }

        /**
         * CheckBoxの状態変更を通知するリスナー.
         */
        interface OnStatusChangeListener {
            /**
             * 状態変更があったことを通知します.
             * @param flag 削除するDConnectServiceがある場合にはtrue、ない場合にはfalse
             */
            void onStatusChange(final boolean flag);
        }
    }
}
