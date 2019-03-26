/*
 TagInfo.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * タグ情報を格納するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class TagInfo {
    /**
     * タグID.
     */
    private String mTagId;

    /**
     * タグ情報を格納するリスト.
     */
    private List<Map<String, Object>> mList = new ArrayList<>();

    /**
     * タグIDを取得します.
     *
     * @return タグID
     */
    public String getTagId() {
        return mTagId;
    }

    /**
     * タグIDを設定します.
     *
     * @param tagId タグID
     */
    public void setTagId(final String tagId) {
        mTagId = tagId;
    }

    /**
     * タグ情報のリストを取得します.
     *
     * @return タグ情報のリスト
     */
    public List<Map<String, Object>> getList() {
        return mList;
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                "mTagId='" + mTagId + '\'' +
                ", mList=" + mList +
                '}';
    }
}
