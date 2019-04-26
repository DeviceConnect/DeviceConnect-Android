/*
 Swagger.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 定義.
 */
public class Swagger extends AbstractSpec {
    /**
     * 使用されている Swagger Specification のバージョン.
     * <p>
     * Required.
     * </p>
     */
    private String mSwagger;

    /**
     * API を提供しているホスト（名前またはIP）.
     */
    private String mHost;

    /**
     * API が提供されるベースパス.
     */
    private String mBasePath;

    /**
     * API に関するメタデータ.
     * <p>
     * Required.
     * </p>
     */
    private Info mInfo;

    /**
     * 仕様で使用されているタグと追加のメタデータのリスト.
     */
    private List<Tag> mTags;

    /**
     * API のプロトコル.
     */
    private List<String> mSchemes;

    /**
     * API が使用できる MIME Type のリスト.
     */
    private List<String> mConsumes;

    /**
     * API が返却する MIME Type のリスト.
     */
    private List<String> mProduces;

    /**
     * API で利用可能なパスのマップ.
     */
    private Paths mPaths;

    /**
     * データ型を保持するためのオブジェクト.
     */
    private Map<String, Definition> mDefinitions;

    /**
     * API 全体で使用できるパラメータを保持するオブジェクト.
     */
    private Map<String, Parameter> mParameters;

    /**
     * API 全体で使用できるレスポンスを保持するオブジェクト.
     */
    private Map<String, Response> mResponses;

    /**
     * API 全体で使用できるセキュリティ方式の定義.
     */
    private Map<String, SecurityScheme> mSecurityDefinitions;

    /**
     * API 全体に適用されるセキュリティスキーム.
     */
    private Map<String, String> mSecurityRequirement;

    /**
     * 追加の外部文書.
     */
    private ExternalDocs mExternalDocs;

    /**
     * 使用されている Swagger Specification のバージョンを取得します.
     *
     * @return Swagger Specification のバージョン
     */
    public String getSwagger() {
        return mSwagger;
    }

    /**
     * 使用されている Swagger Specification のバージョンを設定します.
     *
     * @param swagger Swagger Specification のバージョン
     */
    public void setSwagger(String swagger) {
        mSwagger = swagger;
    }

    /**
     * API を提供しているホスト（名前またはIP）を取得します.
     *
     * @return API を提供しているホスト（名前またはIP）
     */
    public String getHost() {
        return mHost;
    }

    /**
     * API を提供しているホスト（名前またはIP）を設定します.
     *
     * @param host API を提供しているホスト（名前またはIP）
     */
    public void setHost(String host) {
        mHost = host;
    }

    /**
     * API が提供されるベースパスを取得します.
     * <p>
     * ホストからの相対パスになります。
     * </p>
     * @return API が提供されるベースパス
     */
    public String getBasePath() {
        return mBasePath;
    }

    /**
     * API が提供されるベースパスを設定します.
     *
     * @param basePath API が提供されるベースパス
     */
    public void setBasePath(String basePath) {
        mBasePath = basePath;
    }

    /**
     * API に関するメタデータを取得します.
     *
     * @return API に関するメタデータ
     */
    public Info getInfo() {
        return mInfo;
    }

    /**
     * API に関するメタデータを設定します.
     *
     * @param info API に関するメタデータ
     */
    public void setInfo(Info info) {
        mInfo = info;
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
     * API が使用できる MIME Type をリストに追加します．
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
     * 指定されたmime typeを削除します.
     *
     * @param consume 削除するmime type
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeConsume(String consume) {
        if (mConsumes != null) {
            return mConsumes.remove(consume);
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
     * API のプロトコルをリストに追加します.
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
     * API のプロトコルをリストから削除します.
     *
     * @param scheme API のプロトコル
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeScheme(String scheme) {
        if (mSchemes != null) {
            return mSchemes.remove(scheme);
        }
        return false;
    }

    /**
     * API が返却する MIME Type のリストを取得します.
     *
     * @return  API が返却する MIME Type のリスト
     */
    public List<String> getProduces() {
        return mProduces;
    }

    /**
     * API が返却する MIME Type のリストを設定します.
     *
     * @param produces  API が返却する MIME Type のリスト
     */
    public void setProduces(List<String> produces) {
        mProduces = produces;
    }

    /**
     * API が返却する MIME Type をリストに追加します.
     *
     * @param produce API が返却する MIME Type
     */
    public void addProduces(String produce) {
        if (mProduces == null) {
            mProduces = new ArrayList<>();
        }
        mProduces.add(produce);
    }

    /**
     * API が返却する MIME Type をリストから削除します.
     *
     * @param produce 削除するMIME Type
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeProduces(String produce) {
        if (mProduces != null) {
            return mProduces.remove(produce);
        }
        return false;
    }

    /**
     * API で利用可能なパスを取得します.
     *
     * @return API で利用可能なパス
     */
    public Paths getPaths() {
        return mPaths;
    }

    /**
     * API で利用可能なパスを設定します.
     *
     * @param paths API で利用可能なパス
     */
    public void setPaths(Paths paths) {
        mPaths = paths;
    }

    /**
     * 仕様で使用されているタグと追加のメタデータのリストを取得します.
     *
     * @return 仕様で使用されているタグと追加のメタデータのリスト.
     */
    public List<Tag> getTags() {
        return mTags;
    }

    /**
     * 仕様で使用されているタグと追加のメタデータのリストを設定します.
     *
     * @param tags 仕様で使用されているタグと追加のメタデータのリスト
     */
    public void setTags(List<Tag> tags) {
        mTags = tags;
    }

    /**
     * 仕様で使用されているタグと追加のメタデータをリストに追加します.
     *
     * @param tag 仕様で使用されているタグと追加のメタデータ
     */
    public void addTag(Tag tag) {
        if (mTags == null) {
            mTags = new ArrayList<>();
        }
        mTags.add(tag);
    }

    /**
     * 仕様で使用されているタグと追加のメタデータをリストから削除します.
     *
     * @param tag 削除するメタデータ
     * @return 削除に成功した場合はtrue、それ以外はfalse
     */
    public boolean removeTag(Tag tag) {
        if (mTags != null) {
            return mTags.remove(tag);
        }
        return false;
    }

    /**
     * データ型を保持するためのオブジェクトを取得します.
     *
     * @return データ型を保持するためのオブジェクト.
     */
    public Map<String, Definition> getDefinitions() {
        return mDefinitions;
    }

    /**
     * データ型を保持するためのオブジェクトを設定します.
     *
     * @param definitions データ型を保持するためのオブジェクト
     */
    public void setDefinitions(Map<String, Definition> definitions) {
        mDefinitions = definitions;
    }

    /**
     * API 全体で使用できるパラメータ仕様を保持するオブジェクトを取得します.
     *
     * @return API 全体で使用できるパラメータを保持するオブジェクト
     */
    public Map<String, Parameter> getParameters() {
        return mParameters;
    }

    /**
     * API 全体で使用できるパラメータ仕様を保持するオブジェクトを設定します.
     *
     * @param parameters API 全体で使用できるパラメータを保持するオブジェクト
     */
    public void setParameters(Map<String, Parameter> parameters) {
        mParameters = parameters;
    }

    /**
     * API 全体で使用できるパラメータ仕様を追加します.
     *
     * @param key キー
     * @param parameter パラメータ仕様
     */
    public void addParameter(String key, Parameter parameter) {
        if (mParameters == null) {
            mParameters = new HashMap<>();
        }
        mParameters.put(key, parameter);
    }

    /**
     * API 全体で使用できるレスポンスを保持するオブジェクトを取得します.
     *
     * @return API 全体で使用できるレスポンスを保持するオブジェクト
     */
    public Map<String, Response> getResponses() {
        return mResponses;
    }

    /**
     * API 全体で使用できるレスポンスを保持するオブジェクトを設定します.
     *
     * @param responses API 全体で使用できるレスポンスを保持するオブジェクト
     */
    public void setResponses(Map<String, Response> responses) {
        mResponses = responses;
    }

    /**
     * 追加の外部文書を取得します.
     *
     * @return 追加の外部文書
     */
    public ExternalDocs getExternalDocs() {
        return mExternalDocs;
    }

    /**
     * 追加の外部文書を設定します.
     *
     * @param externalDocs 追加の外部文書
     */
    public void setExternalDocs(ExternalDocs externalDocs) {
        mExternalDocs = externalDocs;
    }

    /**
     * API 全体で使用できるセキュリティ方式の定義を取得します.
     *
     * @return API 全体で使用できるセキュリティ方式の定義
     */
    public Map<String, SecurityScheme> getSecurityDefinitions() {
        return mSecurityDefinitions;
    }

    /**
     * API 全体で使用できるセキュリティ方式の定義を設定します.
     *
     * @param securityDefinitions API 全体で使用できるセキュリティ方式の定義
     */
    public void setSecurityDefinitions(Map<String, SecurityScheme> securityDefinitions) {
        mSecurityDefinitions = securityDefinitions;
    }

    /**
     * API 全体で使用できるセキュリティ方式の定義をマップに追加します.
     *
     * @param key キー
     * @param securityScheme API 全体で使用できるセキュリティ方式の定義
     */
    public void addSecurityDefinition(String key, SecurityScheme securityScheme) {
        if (mSecurityDefinitions == null) {
            mSecurityDefinitions = new HashMap<>();
        }
        mSecurityDefinitions.put(key, securityScheme);
    }

    /**
     * API 全体に適用されるセキュリティスキームを取得します.
     *
     * @return API 全体に適用されるセキュリティスキーム
     */
    public Map<String, String> getSecurityRequirement() {
        return mSecurityRequirement;
    }

    /**
     * API 全体に適用されるセキュリティスキームを設定します.
     *
     * @param securityRequirement API 全体に適用されるセキュリティスキーム
     */
    public void setSecurityRequirement(Map<String, String> securityRequirement) {
        mSecurityRequirement = securityRequirement;
    }

    /**
     * API 全体に適用されるセキュリティスキームをマップに追加します.
     *
     * @param key キー
     * @param value API 全体に適用されるセキュリティスキーム.
     */
    public void addSecurityRequirement(String key, String value) {
        if (mSecurityRequirement == null) {
            mSecurityRequirement = new HashMap<>();
        }
        mSecurityRequirement.put(key, value);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mSwagger != null) {
            bundle.putString("swagger", mSwagger);
        }

        if (mBasePath != null) {
            bundle.putString("basePath", mBasePath);
        }

        if (mHost != null) {
            bundle.putString("host", mHost);
        }

        if (mInfo != null) {
            bundle.putParcelable("info", mInfo.toBundle());
        }

        if (mConsumes != null) {
            bundle.putStringArray("consumes", mConsumes.toArray(new String[0]));
        }

        if (mProduces != null) {
            bundle.putStringArray("produces", mProduces.toArray(new String[0]));
        }

        if (mSchemes != null) {
            bundle.putStringArray("schemes", mSchemes.toArray(new String[0]));
        }

        if (mPaths != null) {
            bundle.putParcelable("paths", mPaths.toBundle());
        }

        if (mDefinitions != null && !mDefinitions.isEmpty()) {
            Bundle definitions = new Bundle();
            for (Map.Entry<String, Definition> entry : mDefinitions.entrySet()) {
                definitions.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("definitions", definitions);
        }

        if (mParameters != null && !mParameters.isEmpty()) {
            Bundle parameters = new Bundle();
            for (Map.Entry<String, Parameter> entry : mParameters.entrySet()) {
                parameters.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("parameters", parameters);
        }

        if (mResponses != null && !mResponses.isEmpty()) {
            Bundle responses = new Bundle();
            for (Map.Entry<String, Response> entry : mResponses.entrySet()) {
                responses.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("responses", responses);
        }

        if (mTags != null && !mTags.isEmpty()) {
            List<Bundle> tags = new ArrayList<>();
            for (Tag tag : mTags) {
                tags.add(tag.toBundle());
            }
            bundle.putParcelableArray("tags", tags.toArray(new Bundle[0]));
        }

        if (mSecurityDefinitions != null && !mSecurityDefinitions.isEmpty()) {
            Bundle securityDefinitions = new Bundle();
            for (Map.Entry<String, SecurityScheme> entry : mSecurityDefinitions.entrySet()) {
                securityDefinitions.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("securityDefinitions", securityDefinitions);
        }

        if (mSecurityRequirement != null && !mSecurityRequirement.isEmpty()) {
            Bundle security = new Bundle();
            for (Map.Entry<String, String> entry : mSecurityRequirement.entrySet()) {
                security.putString(entry.getKey(), entry.getValue());
            }
            bundle.putParcelable("security", security);
        }

        if (mExternalDocs != null) {
            bundle.putParcelable("externalDocs", mExternalDocs.toBundle());
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
