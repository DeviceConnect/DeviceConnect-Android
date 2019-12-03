package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.fabo.core.R;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileData;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.db.ProfileDataUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * プロファイル一覧を表示するActivity.
 */
public class FaBoProfileListActivity extends Activity {
    /**
     * リクエストコード.
     */
    private static final int REQUEST_CODE = 201;

    /**
     * プロファイルを表示するアダプタ.
     */
    private ProfileAdapter mProfileAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabo_profile_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.activity_fabo_virtual_service_profile_title);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        mProfileAdapter = new ProfileAdapter(getProfileTypes());

        ListView listView = findViewById(R.id.activity_fabo_profile_list_view);
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            ProfileData.Type type = (ProfileData.Type) mProfileAdapter.getItem(position);
            if (type.getValue() < 100) {
                openPinActivity(type.getValue());
            } else {
                selectedProfileData(type.getValue(), null);
            }
        });
        listView.setAdapter(mProfileAdapter);

        // バックキーを押下された時を考慮してキャンセルを設定しておく
        setResult(RESULT_CANCELED, new Intent());
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ProfileData profileData = data.getParcelableExtra("profile");
                    selectedProfileData(profileData);
                }
                break;
        }
    }

    /**
     * 追加できるプロファイルのリストを取得します.
     * @return 追加できるプロファイルリスト
     */
    private List<ProfileData.Type> getProfileTypes() {
        ArrayList<ProfileData> profileDatas = getIntent().getParcelableArrayListExtra("profile");
        List<ProfileData.Type> typeList = new ArrayList<>();
        for (ProfileData.Type type : ProfileData.Type.values()) {
            if (!containsProfileData(profileDatas, type)) {
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * プロファイルのリストに指定されたプロファイルが存在するか確認します.
     * @param list プロファイルのリスト
     * @param type 確認するプロファイル
     * @return 存在する場合にはtrue、それ以外はfalse
     */
    private boolean containsProfileData(final List<ProfileData> list, final ProfileData.Type type) {
        if (list == null) {
            return true;
        }

        for (ProfileData p : list) {
            if (p.getType().getProfileName().equals(type.getProfileName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 選択されたプロファイルデータを返却して、Activityを終了します.
     * @param profileType プロファイルタイプ
     * @param pins プロファイルが使用するピンのリスト
     */
    private void selectedProfileData(final int profileType, final List<Integer> pins) {
        ProfileData profileData = new ProfileData();
        profileData.setType(ProfileData.Type.getType(profileType));
        if (pins != null) {
            profileData.setPinList(pins);
        }
        selectedProfileData(profileData);
    }

    /**
     * プロファイルデータを選択し、Activityを終了します.
     *
     * @param profileData 選択するプロファイルデータ
     */
    private void selectedProfileData(final ProfileData profileData) {
        Intent intent = new Intent();
        intent.putExtra("profile", profileData);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * GPIOのピン選択画面を開きます.
     *
     * @param profileType 送信するプロファイルデータ
     */
    private void openPinActivity(final int profileType) {
        ProfileData data = new ProfileData();
        data.setType(ProfileData.Type.getType(profileType));

        Intent intent = new Intent();
        intent.setClass(this, FaBoPinListActivity.class);
        intent.putExtra("profile", data);
        intent.putExtra("pins", getIntent().getIntegerArrayListExtra("pins"));
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * プロファイルを格納するアダプタ.
     */
    private class ProfileAdapter extends BaseAdapter {

        private List<ProfileData.Type> mTypeList;

        ProfileAdapter(final List<ProfileData.Type> typeList) {
            mTypeList = typeList;
        }

        @Override
        public int getCount() {
            return mTypeList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mTypeList.get(position);
        }

        @Override
        public long getItemId(final int id) {
            return id;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_fabo_profile, null);
            }

            ProfileData.Type type = (ProfileData.Type) getItem(position);

            TextView nameTV = (TextView) convertView.findViewById(R.id.item_fabo_profile_name);
            nameTV.setText(ProfileDataUtil.getProfileName(FaBoProfileListActivity.this, type));

            return convertView;
        }
    }
}
