/*
 Operation.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * パスに対する単一の API 操作を記述します。
 *
 * @author NTT DOCOMO, INC.
 */
public class Operation extends AbstractSpec {
    /**
     * API 管理用のタグのリスト.
     */
    private List<String> mTags;

    /**
     * API の概要.
     */
    private String mSummary;

    /**
     * API の詳細.
     */
    private String mDescription;

    /**
     * API に関する追加の外部資料.
     */
    private ExternalDocs mExternalDocs;

    /**
     * API を識別するための使用されるユニークな文字列.
     */
    private String mOperationId;

    /**
     * API が使用できる MIME Type のリスト.
     */
    private List<String> mConsumes;

    /**
     * API が返却する MIME Type のリスト.
     */
    private List<String> mProduces;

    /**
     * パラメータ.
     */
    private List<Parameter> mParameters;

    /**
     * レスポンス.
     */
    private Responses mResponses;

    /**
     * API のプロトコルのリスト.
     */
    private List<String> mSchemes;

    /**
     * API の非推奨の宣言.
     */
    private Boolean mDeprecated;

    /**
     * API に適用されるセキュリティスキーム.
     */
    private Map<String, String> mSecurityRequirement;

    /**
     * Device Connect 拡張のイベント定義.
     */
    private XEvent mXEvent;

    /**
     * Device Connect 拡張の API タイプ.
     */
    private XType mXType;

    /**
     * API 管理用のタグのリストを取得します.
     *
     * @return API 管理用のタグのリスト
     */
    public List<String> getTags() {
        return mTags;
    }

    /**
     * API 管理用のタグのリストを設定します.
     *
     * @param tags API 管理用のタグのリスト
     */
    public void setTags(List<String> tags) {
        mTags = tags;
    }

    /**
     * API の概要を取得します.
     *
     * @return API の概要
     */
    public String getSummary() {
        return mSummary;
    }

    /**
     * API の概要を設定します.
     *
     * @param summary API の概要.
     */
    public void setSummary(String summary) {
        mSummary = summary;
    }

    /**
     * API の詳細を取得します.
     *
     * @return API の詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * API の詳細を設定します.
     *
     * @param description API の詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * API に関する追加の外部資料を取得します.
     *
     * @return API に関する追加の外部資料
     */
    public ExternalDocs getExternalDocs() {
        return mExternalDocs;
    }

    /**
     * API に関する追加の外部資料を設定します.
     *
     * @param externalDocs API に関する追加の外部資料
     */
    public void setExternalDocs(ExternalDocs externalDocs) {
        mExternalDocs = externalDocs;
    }

    /**
     * API を識別するための使用されるユニークな文字列を取得します.
     *
     * @return API を識別するための使用されるユニークな文字列
     */
    public String getOperationId() {
        return mOperationId;
    }

    /**
     * API を識別するための使用されるユニークな文字列を設定します.
     *
     * @param operationId API を識別するための使用されるユニークな文字列
     */
    public void setOperationId(String operationId) {
        mOperationId = operationId;
    }

    /**
     * API が使用できる MIME Type のリストを取得します.
     *
     * @return API が使用できる MIME Type のリスト
     */
    public List<String> getConsumes() {
        return mConsumes;
    }

    /**
     * API が使用できる MIME Type のリストを設定します.
     *
     * @param consumes API が使用できる MIME Type のリスト
     */
    public void setConsumes(List<String> consumes) {
        mConsumes = consumes;
    }

    /**
     * API が使用できる MIME Type を追加します.
     *
     * @param consume API が使用できる MIME Type
     */
    public void addConsume(String consume) {
        if (mConsumes == null) {
            mConsumes = new ArrayList<>();
        }
        mConsumes.add(consume);
    }

    /**
     * API が使用できる MIME Type を削除します.
     *
     * @param consume 削除する MIME Type
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeConsume(String consume) {
        if (mConsumes != null) {
            return mConsumes.remove(consume);
        }
        return false;
    }

    /**
     * API が返却する MIME Type のリストを取得します.
     *
     * @return API が返却する MIME Type のリスト
     */
    public List<String> getProduces() {
        return mProduces;
    }

    /**
     * API が返却する MIME Type のリストを設定します.
     *
     * @param produces API が返却する MIME Type のリスト
     */
    public void setProduces(List<String> produces) {
        mProduces = produces;
    }

    /**
     * API が返却する MIME Type を追加します.
     *
     * @param produce API が返却する MIME Type
     */
    public void addProduce(String produce) {
        if (mProduces == null) {
            mProduces = new ArrayList<>();
        }
        mProduces.add(produce);
    }

    /**
     * API が返却する MIME Type を削除します.
     *
     * @param produce 削除する MIME Type
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeProduce(String produce) {
        if (mProduces != null) {
            return mProduces.remove(produce);
        }
        return false;
    }

    /**
     * API のプロトコルのリストを取得します.
     *
     * @return API のプロトコルのリスト
     */
    public List<String> getSchemes() {
        return mSchemes;
    }

    /**
     * API のプロトコルのリストを設定します.
     *
     * @param schemes API のプロトコルのリスト
     */
    public void setSchemes(List<String> schemes) {
        mSchemes = schemes;
    }

    /**
     * API のプロトコルを追加します.
     *
     * @param scheme API のプロトコル
     */
    public void addScheme(String scheme) {
        if (mSchemes == null) {
            mSchemes = new ArrayList<>();
        }
        mSchemes.add(scheme);
    }

    /**
     * API のプロトコルを削除します.
     *
     * @param scheme APIのプロトコル
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeScheme(String scheme) {
        if (mSchemes != null) {
            return mSchemes.remove(scheme);
        }
        return false;
    }

    /**
     * API の非推奨の宣言を取得します.
     * <p>
     * 未設定の場合も false を返却します。
     * </p>
     * @return 非推奨の場合はtrue、それ以外はfalse
     */
    public Boolean isDeprecated() {
        return mDeprecated != null ? mDeprecated : false;
    }

    /**
     * API の非推奨の宣言を設定します.
     *
     * @param deprecated 非推奨の場合はtrue、それ以外はfalse
     */
    public void setDeprecated(Boolean deprecated) {
        mDeprecated = deprecated;
    }

    /**
     * API で使用されるパラメータのリストを取得します.
     *
     * @return API で使用されるパラメータのリスト
     */
    public List<Parameter> getParameters() {
        return mParameters;
    }

    /**
     * API で使用されるパラメータのリストを設定します.
     *
     * @param parameters  API で使用されるパラメータのリスト
     */
    public void setParameters(List<Parameter> parameters) {
        mParameters = parameters;
    }

    /**
     * API で使用されるパラメータを追加します.
     *
     * @param parameter API で使用されるパラメータ
     */
    public void addParameter(Parameter parameter) {
        if (mParameters == null) {
            mParameters = new ArrayList<>();
        }
        mParameters.add(parameter);
    }

    /**
     * API で使用されるパラメータを削除します.
     *
     * @param parameter 削除するパラメータ
     * @return 削除に成功した場合は true、それ以外はfalse
     */
    public boolean removeParameter(Parameter parameter) {
        if (mParameters != null) {
            return mParameters.remove(parameter);
        }
        return false;
    }

    /**
     * API で返却されるパラメータのリストを取得します.
     *
     * @return API で返却されるパラメータのリスト
     */
    public Responses getResponses() {
        return mResponses;
    }

    /**
     * API で返却されるパラメータのリストを設定します.
     *
     * @param responses API で返却されるパラメータのリスト
     */
    public void setResponses(Responses responses) {
        mResponses = responses;
    }

    /**
     * API に適用されるセキュリティを取得します.
     *
     * @return API に適用されるセキュリティ
     */
    public Map<String, String> getSecurityRequirement() {
        return mSecurityRequirement;
    }

    /**
     * API に適用されるセキュリティを設定します.
     *
     * @param securityRequirement API に適用されるセキュリティ
     */
    public void setSecurityRequirement(Map<String, String> securityRequirement) {
        mSecurityRequirement = securityRequirement;
    }

    /**
     * イベント定義を取得します.
     *
     * @return イベント定義
     */
    public XEvent getXEvent() {
        return mXEvent;
    }

    /**
     * イベント定義を設定します.
     *
     * <p>
     * Device Connect 拡張のイベント定義。<br>
     * x-event
     * </p>
     *
     * @param XEvent イベント定義
     */
    public void setXEvent(XEvent XEvent) {
        mXEvent = XEvent;
    }

    /**
     * API タイプを取得します.
     *
     * @return API タイプ
     */
    public XType getXType() {
        return mXType;
    }

    /**
     * API タイプを設定します.
     *
     * <p>
     * Device Connect 拡張の API タイプ。<br>
     * x-type
     * </p>
     *
     * @param type APIタイプ
     */
    public void setXType(XType type) {
        mXType = type;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mTags != null) {
            bundle.putStringArray("tags", mTags.toArray(new String[0]));
        }

        if (mSummary != null) {
            bundle.putString("summary", mSummary);
        }

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mExternalDocs != null) {
            bundle.putParcelable("externalDocs", mExternalDocs.toBundle());
        }

        if (mOperationId != null) {
            bundle.putString("operationId", mOperationId);
        }

        if (mConsumes != null) {
            bundle.putStringArray("consumes", mConsumes.toArray(new String[0]));
        }

        if (mProduces != null) {
            bundle.putStringArray("produces", mProduces.toArray(new String[0]));
        }

        if (mParameters != null && !mParameters.isEmpty()) {
            List<Bundle> bundles = new ArrayList<>();
            for (Parameter parameter : mParameters) {
                bundles.add(parameter.toBundle());
            }
            bundle.putParcelableArray("parameters", bundles.toArray(new Bundle[0]));
        }

        if (mResponses != null) {
            bundle.putParcelable("responses", mResponses.toBundle());
        }

        if (mSchemes != null) {
            bundle.putStringArray("schemes", mSchemes.toArray(new String[0]));
        }

        if (mDeprecated != null) {
            bundle.putBoolean("deprecated", mDeprecated);
        }

        if (mSecurityRequirement != null && !mSecurityRequirement.isEmpty()) {
            Bundle security = new Bundle();
            for (Map.Entry<String, String> entry : mSecurityRequirement.entrySet()) {
                security.putString(entry.getKey(), entry.getValue());
            }
            bundle.putParcelable("security", security);
        }

        if (mXEvent != null) {
            bundle.putParcelable("x-event", mXEvent.toBundle());
        }

        if (mXType != null) {
            bundle.putString("x-type", mXType.getName());
        }

        copyVendorExtensions(bundle);

        return bundle;
    }


}
