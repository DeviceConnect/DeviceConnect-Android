/*
 BodyParameter.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models.parameters;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.In;
import org.deviceconnect.android.profile.spec.models.Schema;

/**
 * API 操作で使用されるパラメータ情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class BodyParameter extends Parameter {
    /**
     * Body に格納される値の仕様.
     */
    private Schema mSchema;

    /**
     * コンストラクタ.
     */
    public BodyParameter() {
        setIn(In.BODY);
    }

    /**
     * Body に格納される値の仕様を取得します.
     *
     * @return Body に格納される値の仕様
     */
    public Schema getSchema() {
        return mSchema;
    }

    /**
     * Body に格納される値の仕様を設定します.
     *
     * @param schema Body に格納される値の仕様
     */
    public void setSchema(Schema schema) {
        mSchema = schema;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        copyParameter(bundle);

        if (mSchema != null) {
            bundle.putParcelable("schema", mSchema.toBundle());
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
