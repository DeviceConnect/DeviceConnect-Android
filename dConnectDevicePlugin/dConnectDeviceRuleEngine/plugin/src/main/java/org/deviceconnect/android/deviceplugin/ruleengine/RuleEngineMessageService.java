/*
 RuleEngineMessageService.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.ruleengine.params.AndRule;
import org.deviceconnect.android.deviceplugin.ruleengine.params.ErrorStatus;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Operation;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Rule;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Trigger;
import org.deviceconnect.android.deviceplugin.ruleengine.profiles.RuleEngineRuleProfile;
import org.deviceconnect.android.deviceplugin.ruleengine.profiles.RuleEngineRuleserviceProfile;
import org.deviceconnect.android.deviceplugin.ruleengine.profiles.RuleEngineSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * RuleEngineMessageServiceクラス.
 * @author NTT DOCOMO, INC.
 */
public class RuleEngineMessageService extends DConnectMessageService {
    /** サービスID. */
    public static final String SERVICE_ID = "rule_engine_service_id";
    /** Rule name prefix. */
    private static final String RULE_NAME_PREFIX = "rule-";
    /** ルールサービスプロファイル. */
    private RuleEngineRuleserviceProfile mRuleServiceProfile;
    /** DBHelper. */
    private RuleEngineDBHelper mRuleEngineDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mRuleEngineDBHelper = new RuleEngineDBHelper(getContext());

        DConnectService service = new DConnectService(SERVICE_ID);
        service.setName("RuleEnginePlugin Service");
        service.setOnline(true);
        service.setNetworkType(NetworkType.UNKNOWN);
        mRuleServiceProfile = new RuleEngineRuleserviceProfile();
        service.addProfile(mRuleServiceProfile);
        getServiceProvider().addService(service);

        loadRuleData();
    }

    /**
     * DBからルール情報読み込み.
     */
    private void loadRuleData() {
        List<Rule> rules = mRuleEngineDBHelper.getRules();
        if (!(rules.isEmpty())) {
            // 保存ルール情報展開.
            for (Rule rule : rules) {
                addRuleService(rule);
                mRuleServiceProfile.addRuleServiceList(rule.getRuleServiceId());
            }
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new RuleEngineSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
    }

    @Override
    protected void onManagerTerminated() {
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
    }

    @Override
    protected void onDevicePluginReset() {
    }

    /**
     * ルール用サービス生成.
     * @param type Rule type ("RULE" or "AND").
     * @return Rule service ID.
     */
    public String createRuleService(final String type) {
        String hash = md5(getPackageName() + getClass().getName());
        String idBase = "." + hash + ".localhost.deviceconnect.org";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmssSSS", Locale.getDefault());
        String dateText = sdf.format(Calendar.getInstance().getTime());
        String ruleServiceId = RULE_NAME_PREFIX + dateText;
        DConnectService service = getServiceProvider().getService(ruleServiceId);
        if (service == null) {
            service = new DConnectService(ruleServiceId);
            String name = "rule(" + dateText + ")";
            service.setName(name);
            service.setOnline(true);
            service.setNetworkType(ServiceDiscoveryProfileConstants.NetworkType.UNKNOWN);
            RuleEngineRuleProfile profile = new RuleEngineRuleProfile(type);
            profile.getRule().setRuleServiceId(ruleServiceId + idBase);
            service.addProfile(profile);
            mRuleEngineDBHelper.addRuleData(profile.getRule());
            getServiceProvider().addService(service);
            return ruleServiceId + idBase;
        } else {
            return service.getId() + idBase;
        }
    }

    /**
     * ルール用サービス削除.
     * @return true / false.
     */
    public boolean removeRuleService(final String ruleServiceId) {
        DConnectService service = getServiceProvider().getService(separateRuleServiceId(ruleServiceId));
        if (service == null) {
            return false;
        } else {
            mRuleEngineDBHelper.removeRuleDataByRuleServiceId(ruleServiceId);
            service.setOnline(false);
            getServiceProvider().removeService(service);
            return true;
        }
    }

    /**
     * ルール用サービス追加.
     * @param rule ルール構造体.
     */
    public void addRuleService(final Rule rule) {
        String ruleServiceId = separateRuleServiceId(rule.getRuleServiceId());
        DConnectService service = getServiceProvider().getService(ruleServiceId);
        if (service == null) {
            service = new DConnectService(ruleServiceId);
            String dateText = ruleServiceId.replace(RULE_NAME_PREFIX,"");
            service.setName("rule(" + dateText + ")");
            service.setOnline(true);
            service.setNetworkType(NetworkType.UNKNOWN);
            RuleEngineRuleProfile profile = new RuleEngineRuleProfile(rule.getRuleServiceType());
            profile.getRule().setRuleServiceId(rule.getRuleServiceId());
            profile.getRule().setRuleEnable(rule.isRuleEnable());
            String errorStatus = rule.getErrorStarus();
            if (errorStatus != null) {
                profile.getRule().setErrorStarus(errorStatus);
            }
            String errorTimestamp = rule.getErrorTimestamp();
            if (errorTimestamp != null) {
                profile.getRule().setErrorTimestamp(errorTimestamp);
            }
            String description = rule.getRuleDescription();
            if (description != null) {
                profile.getRule().setRuleDescription(description);
            }
            Trigger trigger = rule.getTrigger();
            if (trigger != null) {
                profile.getRule().setTrigger(trigger);
            }
            List<Operation> operations = rule.getOperations();
            if (operations != null && !operations.isEmpty()) {
                profile.getRule().setOperations(operations);
            }
            AndRule andRule = rule.getAndRule();
            if (andRule != null) {
                profile.getRule().setAndRule(andRule);
            }
            service.addProfile(profile);
            getServiceProvider().addService(service);
        }
    }

    /**
     * 指定ルール構造体の情報でDBを更新.
     * @param rule ルール構造体.
     * @return 更新インデックス.
     */
    public long updateRuleData(final Rule rule) {
        return mRuleEngineDBHelper.updateRuleData(rule);
    }

    /**
     * エラーステータス取得.
     * @param ruleServiceId Rule service ID.
     * @return エラーステータス構造体 / null(Error).
     */
    public ErrorStatus getErrorStatus(final String ruleServiceId) {
        DConnectService service = getServiceProvider().getService(separateRuleServiceId(ruleServiceId));
        if (service != null) {
            return ((RuleEngineRuleProfile)(service.getProfile(RuleEngineRuleProfile.PROFILE_NAME))).getErrorStatus();
        } else {
            return null;
        }
    }

    /**
     * 指定IDのルール情報取得.
     * @param ruleServiceId Rule service ID.
     * @return ルール情報.
     */
    public Rule getRuleInformation(final String ruleServiceId) {
        DConnectService service = getServiceProvider().getService(separateRuleServiceId(ruleServiceId));
        if (service != null) {
            return ((RuleEngineRuleProfile)(service.getProfile(RuleEngineRuleProfile.PROFILE_NAME))).getRule();
        } else {
            return null;
        }
    }

    /**
     * エラー発生通知処理.
     * @param ruleServiceId 発生元ruleServiceId.
     * @param errorStatus エラーステータス.
     * @param errorTimestamp エラー発生時刻.
     */
    public void sendEventErrorStatus(final String ruleServiceId, final String errorStatus, final String errorTimestamp) {
        mRuleServiceProfile.sendEventErrorStatus(separateRuleServiceId(ruleServiceId), errorStatus, errorTimestamp);
    }

    /**
     * 指定された文字列をMD5の文字列に変換する.
     * MD5への変換に失敗した場合にはnullを返却する。
     * @param s MD5にする文字列
     * @return MD5にされた文字列
     */
    private String md5(final String s) {
        try {
            return toMD5(s);
        } catch (UnsupportedEncodingException e) {
            Log.w("RuleEnginePlugin", "Not support Charset.");
        } catch (NoSuchAlgorithmException e) {
            Log.w("RuleEnginePlugin", "Not support MD5.");
        }
        return null;
    }

    /**
     * 指定された文字列をMD5の文字列に変換する.
     * <p>
     * MD5への変換に失敗した場合には{@code null}を返却する。
     * </p>
     * @param s MD5にする文字列
     * @return MD5にされた文字列
     * @throws UnsupportedEncodingException 文字列の解析に失敗した場合
     * @throws NoSuchAlgorithmException MD5がサポートされていない場合
     */
    private static String toMD5(final String s)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(s.getBytes("ASCII"));
        return hexToString(digest.digest());
    }

    /**
     * バイト配列を16進数の文字列に変換する.
     * @param buf 文字列に変換するバイト
     * @return 文字列
     */
    private static String hexToString(final byte[] buf) {
        StringBuilder hexString = new StringBuilder();
        for (byte aBuf : buf) {
            hexString.append(Integer.toHexString(0xFF & aBuf));
        }
        return hexString.toString();
    }

    /**
     * ルールサービスID切り出し.
     * @param id ルールサービスIDを含んだserviceId.
     * @return ルールサービスID.
     */
    private String separateRuleServiceId(final String id) {
        String[] ruleServiceId = id.split(Pattern.quote("."), 0);
        return ruleServiceId[0];
    }
}