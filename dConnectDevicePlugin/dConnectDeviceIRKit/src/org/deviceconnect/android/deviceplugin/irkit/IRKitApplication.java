package org.deviceconnect.android.deviceplugin.irkit;

import android.app.Application;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class IRKitApplication extends Application {
    /**
     * 検知したデバイス群.
     */
    private List<IRKitDevice> mDevices;

    /**
     * IRKitのListViewの位置.
     */
    private SparseArray<ListViewPosition> mListViewPosition = new SparseArray<ListViewPosition>();
    /**
     * IRKiのデバイスを保持する.
     */
    public void setIRKitDevices(final ConcurrentHashMap<String, IRKitDevice> devices) {
        if (mDevices != null) {
            mDevices.clear();
            mDevices = null;
        }
        mDevices = new ArrayList<IRKitDevice>();
        for (Map.Entry<String, IRKitDevice> device : devices.entrySet()) {
            mDevices.add(device.getValue());
        }

    }

    /**
     * IRKiのデバイスを返す.
     * @return 検知したデバイス群
     */
    public List<IRKitDevice> getIRKitDevices() {
        return mDevices;
    }


    /**
     * 設定画面のページごとのListViewの位置を保持する.
     * @param page 設定画面のページ
     * @param pos ListViewのposition
     * @param offset ListViewのoffset
     */
    public void setListViewPosition(final int page, final int pos, final int offset) {
        mListViewPosition.put(page, new ListViewPosition(pos, offset));
    }

    /**
     * 設定画面のページごとのListViewの位置を返す.
     * @param page 設定画面のページ
     * @return
     */
    public ListViewPosition getListViewPosition(final int page) {
        return mListViewPosition.get(page);
    }


    /**
     * ListViewの位置情報を削除する.
     */
    public void removeAllListViewPostion() {
        mListViewPosition.clear();
    }
    /**
     * ListViewの位置を保持するクラス.
     */
    public class ListViewPosition {
        /**
         * ListViewのPosition.
         */
        private int mPos;
        /**
         * ListViewのOffset.
         */
        private int mOffset;

        /**
         * コンストラクタ.
         * @param pos Position
         * @param offset Offset
         */
        public ListViewPosition(final int pos, final int offset) {
            mPos = pos;
            mOffset = offset + 10;
        }

        /**
         * positionの取得.
         * @return position
         */
        public int getPosition() {
            return mPos;
        }

        /**
         * offsetの取得.
         * @return Offset
         */
        public int getOffset() {
            return mOffset;
        }
    }
}
