/*
 IlluminationData.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.data;

public class Name {
    // 言語。"ja"固定。
    String mNameLang;
    // 名前のサイズ。
    int mSize;
    // 項目名(UTF-8)。
    String mName;

    public String getNameLang() {
        return mNameLang;
    }

    public int getSize() {
        return mSize;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return Name.class.getSimpleName() + ":{mNameLang:" + mNameLang + ", mSize:" + mSize + ", mName:" + mName + "}";
    }
}
