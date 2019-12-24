/*
 HitoeManager
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import java.util.ArrayList;

/**
 * Temporary holding the Exdata.
 * @author NTT DOCOMO, INC.
 */
public class TempExData {
    /** EX data's key. */
    private String mKey;
    /** EX data's list. */
    private ArrayList<String> mDataList;

    /**
     * Consutructor.
     * @param key Key
     * @param dataList Ex data list
     */
    public TempExData(final String key, final ArrayList<String> dataList) {
        setKey(key);
        setDataList(new ArrayList<>());
        for (int i = 0; i < dataList.size(); i++) {
            getDataList().add(dataList.get(i));
        }
    }

    /**
     * Get Ex data's key.
     * @return Ex data's key
     */
    public String getKey() {
        return mKey;
    }

    /**
     * Set Ex data's key.
     * @param key Ex data's key
     */
    public void setKey(final String key) {
        mKey = key;
    }

    /**
     * Get Ex data list.
     * @return Ex data list
     */
    public ArrayList<String> getDataList() {
        return mDataList;
    }

    /**
     * Set Ex data list.
     * @param dataList Ex data list
     */
    public void setDataList(final ArrayList<String> dataList) {
        mDataList = dataList;
    }
}
