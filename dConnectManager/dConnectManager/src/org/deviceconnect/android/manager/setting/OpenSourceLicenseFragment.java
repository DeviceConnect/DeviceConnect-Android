package org.deviceconnect.android.manager.setting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.protocol.HTTP;
import org.deviceconnect.android.manager.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * オープンソースライセンスフラグメント.
 */
public class OpenSourceLicenseFragment extends DialogFragment {

    /**
     * 属性:open_source_software.
     */
    public static final String EXTRA_OSS = "open_source_software";

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("dconnect.uiapp");

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        FrameLayout lframe = new FrameLayout(getActivity());

        ListView lv = new ListView(getActivity());
        lv.setId(android.R.id.list);
        lv.setDrawSelectorOnTop(false);
        lframe.addView(lv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        lframe.setBackgroundColor(getResources().getColor(android.R.color.background_light));

        ArrayAdapter<Parcelable> adapter =
                new SoftwareArrayAdapter(getActivity(), R.layout.item_open_source_licenses);
        if (getArguments() != null && getArguments().containsKey(EXTRA_OSS)) {
            adapter.addAll(getArguments().getParcelableArrayList(EXTRA_OSS));
        }

        ListView listView = (ListView) lframe.findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.activity_settings_open_source_licenses);
        builder.setView(lframe);
        builder.setPositiveButton(R.string.activity_settings_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    /**
     * OpenSourceSoftwareインスタンスを作成する.
     * @param software ソフトウェア名
     * @param resId リソースID
     * @return OpenSourceSoftwareインスタンス
     */
    public static OpenSourceSoftware createOpenSourceSoftware(
            final String software, final int resId) {
        return new OpenSourceSoftware(software, resId);
    }

    /**
     * オープンソースライセンス.
     */
    public static class OpenSourceSoftware implements Parcelable {

        /**
         * バッファサイズ.
         */
        private static final int BUFFER_SIZE = 1024;

        /**
         * ソフトウェア名.
         */
        private String mSoftware;

        /**
         * ライセンスリソース.
         */
        private String mLicenseText;

        /**
         * ライセンスリソースID.
         */
        private int mLicenseResource;

        /**
         * Parcelableクリエイター.
         */
        public static final Parcelable.Creator<OpenSourceSoftware> CREATOR =
                new Parcelable.Creator<OpenSourceSoftware>() {
            public OpenSourceSoftware createFromParcel(final Parcel in) {
                return new OpenSourceSoftware(in);
            }
            public OpenSourceSoftware[] newArray(final int size) {
                return new OpenSourceSoftware[size];
            }
        };

        /**
         * コンストラクタ.
         * @param software ソフトウェア名
         * @param resId リソースID
         */
        public OpenSourceSoftware(final String software, final int resId) {
            mSoftware = software;
            mLicenseResource = resId;
        }

        /**
         * Parcelableコンストラクタ.
         * @param in 入力
         */
        private OpenSourceSoftware(final Parcel in) {
            mSoftware = in.readString();
            mLicenseResource = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(mSoftware);
            dest.writeInt(mLicenseResource);
        }

        /**
         * ソフトウェア名を取得する.
         * @return ソフトウェア名
         */
        public String getSoftwareName() {
            return mSoftware;
        }

        /**
         * ライセンステキストを取得する.
         * @return ライセンステキスト
         */
        public String getLicenseText() {
            return mLicenseText;
        }

        /**
         * ライセンステキストを読み込む.
         * @param context コンテキスト
         * @throws IOException I/Oエラーが発生した場合
         */
        public void loadLicenseText(final Context context) throws IOException {
            if (mLicenseText != null) {
                return;
            }

            if (mLicenseResource <= 0) {
                return;
            }

            InputStream is = null;
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                is = context.getResources().openRawResource(mLicenseResource);
                byte[] buf = new byte[BUFFER_SIZE];
                while (true) {
                    int len = is.read(buf);
                    if (len < 0) {
                        break;
                    }
                    os.write(buf, 0, len);
                }
                mLicenseText = new String(os.toByteArray(), HTTP.UTF_8);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    /**
     * サービス配列アダプタ.
     */
    private class SoftwareArrayAdapter extends ArrayAdapter<Parcelable> {

        /**
         * レイアウトリソースID.
         */
        private int mResourceId;

        /**
         * コンストラクタ.
         * @param context コンテキスト
         * @param resource レイアウトリソースID
         */
        public SoftwareArrayAdapter(final Context context, final int resource) {
            super(context, resource);
            mLogger.entering(getClass().getName(), "SoftwareArrayAdapter",
                    new Object[] {context, resource});

            mResourceId = resource;

            mLogger.exiting(getClass().getName(), "SoftwareArrayAdapter");
        }

        /**
         * コンストラクタ.
         * @param context コンテキスト
         * @param resource レイアウトリソースID
         * @param objects サービスリスト
         */
        public SoftwareArrayAdapter(
                final Context context, final int resource, final List<Parcelable> objects) {
            super(context, resource, objects);
            mLogger.entering(getClass().getName(), "SoftwareArrayAdapter",
                    new Object[] {context, resource, objects});

            mResourceId = resource;

            mLogger.exiting(getClass().getName(), "SoftwareArrayAdapter");
        }

        /**
         * コンストラクタ.
         * @param context コンテキスト
         * @param resource レイアウトリソースID
         * @param objects サービスリスト
         */
        public SoftwareArrayAdapter(
                final Context context, final int resource, final OpenSourceSoftware[] objects) {
            super(context, resource, objects);
            mLogger.entering(getClass().getName(), "SoftwareArrayAdapter",
                    new Object[] {context, resource, objects});

            mResourceId = resource;

            mLogger.exiting(getClass().getName(), "SoftwareArrayAdapter");
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            mLogger.entering(getClass().getName(), "getView",
                    new Object[] {position, convertView, parent});

            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(mResourceId, null);
            }

            OpenSourceSoftware item = (OpenSourceSoftware) getItem(position);
            if (item.getLicenseText() == null) {
                try {
                    item.loadLicenseText(getContext());
                } catch (IOException e) {
                    mLogger.warning(e.toString());
                }
            }

            TextView titleView = (TextView) view.findViewById(android.R.id.text1);
            titleView.setText(item.getSoftwareName());

            TextView bodyView = (TextView) view.findViewById(android.R.id.text2);
            bodyView.setVisibility(View.VISIBLE);
            bodyView.setText(item.getLicenseText());

            mLogger.exiting(getClass().getName(), "getView", view);
            return view;
        }

    }

}
