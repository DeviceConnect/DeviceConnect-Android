/*
 IlluminationData.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.data;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class Setting {
    // ID
    byte mId;
    // 子項目数
    int mChildCount;
    // デフォルト設定ID
    byte mDefaultSettingId;
    // 項目名言語数
    int mNameLangCount;
    Name[] mNames;
    Setting[] mChildren;

    public byte getId() {
        return mId;
    }

    public int getChildCount() {
        return mChildCount;
    }

    public byte getDefaultSettingId() {
        return mDefaultSettingId;
    }

    public int getNameLangCount() {
        return mNameLangCount;
    }

    public Name getName(int index) {
        return mNames[index];
    }

    public Name[] getNames() {
        return mNames;
    }

    public Setting getChild(int index) {
        return mChildren[index];
    }

    public Setting[] getChildren() {
        return mChildren;
    }

    @Override
    public String toString() {
        return Setting.class.getSimpleName() + ":{mId:" + ByteUtil.byteToHex(mId) + ", mChildCount:" + mChildCount + ", mDefaultSettingId:" + ByteUtil.byteToHex(mDefaultSettingId) + "," +
                " mNameLangCount:" + mNameLangCount + ", mNames:" + toString(mNames) + ", mChildren:" + toString(mChildren) + "}";
    }

    private String toString(Object[] list) {
        if (list == null) {
            return "";
        }
        String s = "[";
        for (Object obj : list) {
            s += obj.toString() + " ";
        }
        s += "]";
        return s;
    }
}
