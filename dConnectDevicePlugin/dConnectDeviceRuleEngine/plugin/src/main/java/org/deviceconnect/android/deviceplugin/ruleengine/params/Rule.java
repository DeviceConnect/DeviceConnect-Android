/*
 Rule.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.params;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Ruleクラス.
 * @author NTT DOCOMO, INC.
 */
public class Rule {
    /** ルールサービスID. */
    private String mRuleServiceId = null;
    /** ルールサービスタイプ. */
    private String mRuleServiceType = null;
    /** ルール稼働状態. */
    private boolean mRuleEnable = false;
    /** エラーステータス. */
    private String mErrorStatus = ErrorStatus.NO_DATA;
    /** エラー発生時刻. */
    private String mErrorTimestamp = ErrorStatus.NO_DATA;
    /** ルール説明. */
    private String mRuleDescription;
    /** トリガー構造体. */
    private Trigger mTrigger = null;
    /** オペレーション構造体リスト. */
    private List<Operation> mOperations = new ArrayList<>();
    /** ANDルール構造体. */
    private AndRule mAndRule = null;

    /**
     * コンストラクター.
     * @param type ルールサービスタイプ.
     */
    public Rule(final String type) {
        mRuleServiceType = type;
        if (mRuleServiceType.contains(RuleType.AND)) {
            mAndRule = new AndRule();
        }
    }

    /**
     * ルールサービスID取得.
     * @return ルールサービスID.
     */
    public String getRuleServiceId() {
        return mRuleServiceId;
    }

    /**
     * ルールサービスタイプ取得.
     * @return ルールサービスタイプ.
     */
    public String getRuleServiceType() {
        return mRuleServiceType;
    }

    /**
     * ルール稼働状態取得.
     * @return true / false .
     */
    public boolean isRuleEnable() {
        return mRuleEnable;
    }

    /**
     * エラーステータス取得.
     * @return エラーステータス.
     */
    public String getErrorStarus() {
        return mErrorStatus;
    }

    /**
     * エラー発生時刻取得.
     * @return エラー発生時刻.
     */
    public String getErrorTimestamp() {
        return mErrorTimestamp;
    }

    /**
     * ルール説明取得.
     * @return ルール説明.
     */
    public String getRuleDescription() {
        return mRuleDescription;
    }

    /**
     * トリガー構造体取得.
     * @return トリガー構造体.
     */
    public Trigger getTrigger() {
        if (mTrigger == null) {
            return null;
        }
        return mTrigger;
    }

    /**
     * オペレーション構造体リスト取得.
     * @return オペレーション構造体リスト.
     */
    public List<Operation> getOperations() {
        return mOperations;
    }

    /**
     * 指定オペレーション構造体取得.
     * @param index インデックス.
     * @return オペレーション構造体.
     */
    public Operation getOperation(final long index) {
        if (mOperations.size() == 0) {
            return null;
        }
        for (Operation operation : mOperations) {
            if (operation.getIndex() == index) {
                return operation;
            }
        }
        // Not found.
        return null;
    }

    /**
     * ANDルール構造体取得.
     * @return ANDルール構造体.
     */
    public AndRule getAndRule() {
        return mAndRule;
    }

    /**
     * ルールサービスID設定.
     * @param ruleServiceId ルールサービスID.
     */
    public void setRuleServiceId(final String ruleServiceId) {
        mRuleServiceId = ruleServiceId;
    }

    /**
     * ルールサービスタイプ設定.
     * @param ruleServiceType ルールサービスタイプ.
     */
    public void setRuleServiceType(final String ruleServiceType) {
        mRuleServiceType = ruleServiceType;
    }

    /**
     * エラーステータス設定.
     * @param errorStarus エラーステータス.
     */
    public void setErrorStarus(final String errorStarus) {
        mErrorStatus = errorStarus;
    }

    /**
     *  エラー発生時刻設定.
     * @param errorTimestamp エラー発生時刻.
     */
    public void setErrorTimestamp(final String errorTimestamp) {
        mErrorTimestamp = errorTimestamp;
    }

    /**
     * ルール稼働状態設定.
     * @param ruleEnable true / false .
     */
    public void setRuleEnable(final boolean ruleEnable) {
        mRuleEnable = ruleEnable;
    }

    /**
     * ルール説明設定.
     * @param ruleDescription ルール説明.
     */
    public void setRuleDescription(String ruleDescription) {
        mRuleDescription = ruleDescription;
    }

    /**
     * トリガー構造体設定.
     * @param trigger トリガー構造体.
     */
    public void setTrigger(final Trigger trigger) {
        mTrigger = trigger;
    }

    /**
     * オペレーション構造体リスト設定.
     * @param operations オペレーション構造体リスト.
     */
    public void setOperations(final List<Operation> operations) {
        mOperations = operations;
    }

    /**
     * オペレーション構造体追加.
     * @param operation オペレーション構造体リスト.
     * @return インデックス.
     */
    public long addOperation(final Operation operation) {
        CharSequence dateText  = android.text.format.DateFormat.format("yyyyMMddkkmmss", Calendar.getInstance());
        long index = Long.parseLong(dateText.toString());
        operation.setIndex(index);
        mOperations.add(operation);
        return index;
    }

    /**
     * オペレーション構造体設定.
     * @param operation オペレーション構造体.
     * @return インデックス(正常終了) or -1(エラー).
     */
    public long setOperation(final Operation operation) {
        if (mOperations.size() == 0 && operation.getIndex() <= 0) {
            return -1;
        }

        int listIndex = mOperations.indexOf(operation);
        mOperations.set(listIndex, operation);
        return operation.getIndex();
    }

    /**
     * ANDルール構造体設定.
     * @param andRule ANDルール構造体.
     */
    public void setAndRule(final AndRule andRule) {
        mAndRule = andRule;
    }

    /**
     * 指定オペレーション構造体削除.
     * @param index 削除オペレーション構造体のリストインデックス.
     * @return true(正常終了) / false(失敗).
     */
    public boolean deleteOperation(final long index) {
        Operation op = new Operation();
        op.setIndex(index);

        int listIndex = mOperations.indexOf(op);
        if (listIndex != -1){
            mOperations.remove(listIndex);
            return true;
        } else {
            return false;
        }
    }
}
