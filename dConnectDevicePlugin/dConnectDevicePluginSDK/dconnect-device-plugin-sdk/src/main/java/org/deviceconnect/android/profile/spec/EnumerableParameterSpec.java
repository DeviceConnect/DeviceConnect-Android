/*
 EnumerableParameterSpec.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


abstract class EnumerableParameterSpec<TYPE, SPEC extends EnumerableDataSpec<TYPE>> extends DConnectParameterSpec<SPEC> {

    private final EnumerableDataSpec<TYPE> mEnumrableDataSpec;

    EnumerableParameterSpec(final SPEC dataSpec) {
        super(dataSpec);
        mEnumrableDataSpec = dataSpec;
    }

    /**
     * 定数一覧を取得する.
     * @return 定数の配列
     */
    public TYPE[] getEnum() {
        return mEnumrableDataSpec.getEnum();
    }

    /**
     * 定数一覧を設定する.
     * @param enumList 定数の配列
     */
    void setEnum(final TYPE[] enumList) {
        mEnumrableDataSpec.setEnum(enumList);
    }

    abstract static class Builder<TYPE, BUILDER extends Builder<TYPE, BUILDER>> extends BaseBuilder<BUILDER> {
        TYPE[] mEnum;

        public BUILDER setEnum(final TYPE[] enumList) {
            mEnum = enumList;
            return getThis();
        }
    }
}
