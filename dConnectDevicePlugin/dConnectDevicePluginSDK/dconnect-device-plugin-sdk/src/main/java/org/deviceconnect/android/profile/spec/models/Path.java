/*
 Path.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * API で利用可能なパス情報を格納するオブジェクト.
 *
 * @author NTT DOCOMO, INC.
 */
public class Path extends AbstractSpec {
    /**
     * GET メソッド用のパス情報.
     */
    private Operation mGet;

    /**
     * PUT メソッド用のパス情報.
     */
    private Operation mPut;

    /**
     * POST メソッド用のパス情報.
     */
    private Operation mPost;

    /**
     * DELETE メソッド用のパス情報.
     */
    private Operation mDelete;

    /**
     * HEAD メソッド用のパス情報.
     */
    private Operation mHead;

    /**
     * PATCH メソッド用のパス情報.
     */
    private Operation mPatch;

    /**
     * OPTIONS メソッド用のパス情報.
     */
    private Operation mOptions;

    /**
     * パスで共通に使用するパラメータ.
     */
    private List<Parameter> mParameters;

    /**
     * Method に合わせて各パス情報に設定します.
     *
     * @param method HTTPメソッド
     * @param operation パス情報
     */
    public void setOperation(Method method, Operation operation) {
        switch (method) {
            case GET:
                mGet = operation;
                break;
            case PUT:
                mPut = operation;
                break;
            case POST:
                mPost = operation;
                break;
            case DELETE:
                mDelete = operation;
                break;
            case OPTIONS:
                mOptions = operation;
                break;
            case PATCH:
                mPatch = operation;
                break;
            case HEAD:
                mHead = operation;
                break;
            default:
                break;
        }
    }

    /**
     * Method に合わせて各パス情報を取得します.
     *
     * @param method HTTPメソッド
     * @return パス情報
     */
    public Operation getOperation(Method method) {
        switch (method) {
            case GET:
                return mGet;
            case PUT:
                return mPut;
            case POST:
                return mPost;
            case DELETE:
                return mDelete;
            case OPTIONS:
                return mOptions;
            case PATCH:
                return mPatch;
            case HEAD:
                return mHead;
            default:
                return null;
        }
    }

    /**
     * GET メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return GET メソッドのパス情報
     */
    public Operation getGet() {
        return mGet;
    }

    /**
     * GET メソッドのパス情報を設定します.
     *
     * @param get GET メソッドのパス情報
     */
    public void setGet(Operation get) {
        mGet = get;
    }

    /**
     * PUT メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return PUT メソッドのパス情報
     */
    public Operation getPut() {
        return mPut;
    }

    /**
     * PUT メソッドのパス情報を設定します.
     *
     * @param put PUT メソッドのパス情報
     */
    public void setPut(Operation put) {
        mPut = put;
    }

    /**
     * POST メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return POST メソッドのパス情報
     */
    public Operation getPost() {
        return mPost;
    }

    /**
     * POST メソッドのパス情報を設定します.
     *
     * @param post POST メソッドのパス情報
     */
    public void setPost(Operation post) {
        mPost = post;
    }

    /**
     * DELETE メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return DELETE メソッドのパス情報
     */
    public Operation getDelete() {
        return mDelete;
    }

    /**
     * DELETE メソッドのパス情報を設定します.
     *
     * @param delete DELETE メソッドのパス情報
     */
    public void setDelete(Operation delete) {
        mDelete = delete;
    }

    /**
     * HEAD メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return  HEAD メソッドのパス情報
     */
    public Operation getHead() {
        return mHead;
    }

    /**
     * HEAD メソッドのパス情報を設定します.
     *
     * @param head HEAD メソッドのパス情報
     */
    public void setHead(Operation head) {
        mHead = head;
    }

    /**
     * PATCH メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return PATCH メソッドのパス情報
     */
    public Operation getPatch() {
        return mPatch;
    }

    /**
     * PATCH メソッドのパス情報を設定します.
     *
     * @param patch PATCH メソッドのパス情報
     */
    public void setPatch(Operation patch) {
        mPatch = patch;
    }

    /**
     * OPTIONS メソッドのパス情報を取得します.
     *
     * <p>
     * 定義されていない場合は null を返却します。
     * </p>
     *
     * @return OPTIONS メソッドのパス情報
     */
    public Operation getOptions() {
        return mOptions;
    }

    /**
     * OPTIONS メソッドのパス情報を設定します.
     *
     * @param options OPTIONS メソッドのパス情報
     */
    public void setOptions(Operation options) {
        mOptions = options;
    }

    /**
     * このパス全体で使用できるパラメータを取得します.
     *
     * @return このパス全体で使用できるパラメータ
     */
    public List<Parameter> getParameters() {
        return mParameters;
    }

    /**
     * このパス全体で使用できるパラメータを設定します.
     *
     * @param parameters このパス全体で使用できるパラメータ
     */
    public void setParameters(List<Parameter> parameters) {
        mParameters = parameters;
    }

    /**
     * このパス全体で使用できるパラメータを追加します.
     *
     * @param parameter 追加するパラメータ
     */
    public void addParameter(Parameter parameter) {
        if (mParameters == null) {
            mParameters = new ArrayList<>();
        }
        mParameters.add(parameter);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mGet != null) {
            bundle.putParcelable("get", mGet.toBundle());
        }

        if (mPut != null) {
            bundle.putParcelable("put", mPut.toBundle());
        }

        if (mPost != null) {
            bundle.putParcelable("post", mPost.toBundle());
        }

        if (mDelete != null) {
            bundle.putParcelable("delete", mDelete.toBundle());
        }

        if (mHead != null) {
            bundle.putParcelable("head", mHead.toBundle());
        }

        if (mPatch != null) {
            bundle.putParcelable("patch", mPatch.toBundle());
        }

        if (mOptions != null) {
            bundle.putParcelable("options", mOptions.toBundle());
        }

        if (mParameters != null && !mParameters.isEmpty()) {
            List<Bundle> parameters = new ArrayList<>();
            for (Parameter p : mParameters) {
                parameters.add(p.toBundle());
            }
            bundle.putParcelableArray("parameters", parameters.toArray(new Bundle[0]));
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
