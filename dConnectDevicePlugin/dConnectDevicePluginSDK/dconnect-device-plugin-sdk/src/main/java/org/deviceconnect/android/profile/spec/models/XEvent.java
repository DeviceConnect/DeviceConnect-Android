/*
 XEvent.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.Map;

/**
 * イベント定義.
 *
 * <p>
 * Device Connect で拡張した定義。<br>
 * x-event
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class XEvent extends AbstractSpec {
    /**
     * イベント定義の詳細.
     */
    private String mDescription;

    /**
     * イベント定義の構造体.
     */
    private Schema mSchema;

    /**
     * イベント例.
     */
    private Map<String, Example> mExamples;

    /**
     * イベント定義の詳細を取得します.
     *
     * @return イベントの詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * イベント定義の詳細を設定します.
     *
     * @param description イベントの詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * イベント定義の構造体を取得します.
     *
     * @return イベント定義の構造体
     */
    public Schema getSchema() {
        return mSchema;
    }

    /**
     * イベント定義の構造体を設定します.
     *
     * @param schema イベント定義の構造体
     */
    public void setSchema(Schema schema) {
        mSchema = schema;
    }

    /**
     * イベント例を取得します.
     *
     * @return イベント例のマップ。(キーは MIME Type)
     */
    public Map<String, Example> getExamples() {
        return mExamples;
    }

    /**
     * イベント例を設定します.
     *
     * @param examples イベント例のマップ。(キーは MIME Type)
     */
    public void setExamples(Map<String, Example> examples) {
        mExamples = examples;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mSchema != null) {
            bundle.putParcelable("schema", mSchema.toBundle());
        }

        if (mExamples != null && !mExamples.isEmpty()) {
            Bundle examples = new Bundle();
            for (Map.Entry<String, Example> entry : mExamples.entrySet()) {
                examples.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("examples", examples);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
