/*
 RuleEngineRuleServiceProfile.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.profiles;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.ruleengine.RuleEngineMessageService;
import org.deviceconnect.android.deviceplugin.ruleengine.params.AndRule;
import org.deviceconnect.android.deviceplugin.ruleengine.params.ErrorStatus;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Operation;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Rule;
import org.deviceconnect.android.deviceplugin.ruleengine.params.RuleType;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Trigger;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * RuleServiceProfileクラス.
 * @author NTT DOCOMO, INC.
 */
public class RuleEngineRuleserviceProfile extends DConnectProfile {

    /** ルール管理テーブル. */
    private List<String> mRuleServiceList = new ArrayList<>();

    /** エラー監視要求管理テーブル. */
    private List<Intent> mErrorStatusRequestList = new ArrayList<>();

    public RuleEngineRuleserviceProfile() {

        // POST /ruleService
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String ruleServiceType = (String) request.getExtras().get("ruleServiceType");
                String ruleType;
                // ルールタイプ設定.
                if (ruleServiceType != null && ruleServiceType.toUpperCase().equals(RuleType.AND)) {
                    ruleType = RuleType.AND;
                } else {
                    ruleType = RuleType.RULE;
                }
                // ルールベース作成.
                String ruleServiceId = ((RuleEngineMessageService) getContext()).createRuleService(ruleType);

                // ルール管理テーブル登録.
                mRuleServiceList.add(ruleServiceId);

                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                root.putString("ruleServiceId", ruleServiceId);
                response.putExtras(root);
                return true;
            }
        });

        // DELETE /ruleService
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String ruleServiceId = (String) request.getExtras().get("ruleServiceId");

                if (((RuleEngineMessageService) getContext()).removeRuleService(ruleServiceId)) {
                    // ルール管理テーブル削除.
                    java.util.Iterator<String> iter = mRuleServiceList.iterator();
                    while (iter.hasNext()) {
                        if (iter.next().equals(ruleServiceId)) {
                            iter.remove();
                            break;
                        }
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response, "Failure to delete rule service.");
                }
                return true;
            }
        });

        // GET /ruleService
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String ruleServiceId = (String) request.getExtras().get("ruleServiceId");

                if (mRuleServiceList.size() == 0) {
                    // ルール登録無し.
                    MessageUtils.setIllegalDeviceStateError(response, "Not register rule.");
                    return true;
                }

                Bundle root = response.getExtras();
                List<Bundle> ruleList = new ArrayList<>();
                Bundle rule = new Bundle();
                Bundle trigger = new Bundle();
                Bundle operation = new Bundle();

                if (ruleServiceId == null) {
                    // 全ルール情報取得.
                    for (String id : mRuleServiceList) {
                        Rule ruleInfo = ((RuleEngineMessageService) getContext()).getRuleInformation(id);
                        if (ruleInfo != null) {
                            List<Bundle> triggerList = new ArrayList<>();
                            Trigger triggers = ruleInfo.getTrigger();
                            if (triggers != null) {
                                trigger.putLong("triggerInterval", triggers.getInterval());
                                trigger.putString("triggerIntervalUnit", triggers.getIntervalUnit());
                                trigger.putString("triggerIntervalReferenceTime", triggers.getReferenceTime());
                                trigger.putString("trigger", triggers.getRest());
                                trigger.putString("triggerAction", triggers.getAction());
                                trigger.putString("triggerParameter", triggers.getParameter());
                                trigger.putString("comparisonLeft", triggers.getComparisionLeft());
                                trigger.putString("comparisonLeftDataType", triggers.getComparisionLeftDataType());
                                trigger.putString("comparison", triggers.getComparision());
                                trigger.putString("comparisonRight", triggers.getComparisionRight());
                                trigger.putString("comparisonRightDataType", triggers.getComparisionRightDataType());
                            } else {
                                trigger.putString("triggerInterval", "");
                                trigger.putString("triggerIntervalUnit", "");
                                trigger.putString("triggerIntervalReferenceTime", "");
                                trigger.putString("trigger", "");
                                trigger.putString("triggerAction", "");
                                trigger.putString("triggerParameter", "");
                                trigger.putString("comparisonLeft", "");
                                trigger.putString("comparisonLeftDataType", "");
                                trigger.putString("comparison", "");
                                trigger.putString("comparisonRight", "");
                                trigger.putString("comparisonRightDataType", "");
                            }
                            AndRule andRule = ruleInfo.getAndRule();
                            if (andRule != null) {
                                StringBuilder sb = new StringBuilder();
                                List<String> andRuleServiceIds = andRule.getAndRuleServiceId();
                                int index = andRuleServiceIds.size();
                                for (String andRuleServiceId : andRuleServiceIds) {
                                    sb.append(andRuleServiceId);
                                    index--;
                                    if (index > 0) {
                                        sb.append(", ");
                                    }
                                }
                                trigger.putString("andRuleServiceId", sb.toString());
                                trigger.putLong("judgementTime", andRule.getJudgementTime());
                                trigger.putString("judgementTimeUnit", andRule.getJudgementTimeUnit());
                            } else {
                                trigger.putString("andRuleServiceId", "");
                                trigger.putString("judgementTime", "");
                                trigger.putString("judgementTimeUnit", "");
                            }
                            triggerList.add((Bundle) trigger.clone());

                            List<Bundle> operationList = new ArrayList<>();
                            List<Operation> operations = ruleInfo.getOperations();
                            if (operations != null) {
                                for (Operation op : operations) {
                                    operation.putLong("operationIndex", op.getIndex());
                                    operation.putString("operation", op.getRest());
                                    operation.putString("operationAction", op.getAction());
                                    operation.putString("operationParameter", op.getParameter());
                                    operation.putString("delayOccurrence", op.getDelayOccurrence());
                                    operationList.add((Bundle) operation.clone());
                                }
                            } else {
                                operation.putString("operationIndex", "");
                                operation.putString("operation", "");
                                operation.putString("operationAction", "");
                                operation.putString("operationParameter", "");
                                operation.putString("delayOccurrence", "");
                                operationList.add((Bundle) operation.clone());
                            }

                            rule.putString("ruleServiceId", ruleInfo.getRuleServiceId());
                            rule.putString("ruleServiceType", ruleInfo.getRuleServiceType());
                            rule.putBoolean("ruleEnable", ruleInfo.isRuleEnable());
                            rule.putString("errorStatus", ruleInfo.getErrorStarus());
                            rule.putString("errorTimestamp", ruleInfo.getErrorTimestamp());
                            rule.putString("description", ruleInfo.getRuleDescription());
                            rule.putParcelableArray("triggers", triggerList.toArray(new Bundle[triggerList.size()]));
                            rule.putParcelableArray("operations", operationList.toArray(new Bundle[operationList.size()]));

                            ruleList.add((Bundle) rule.clone());
                        }
                    }
                    root.putParcelableArray("rules", ruleList.toArray(new Bundle[ruleList.size()]));
                    response.putExtras(root);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    // 特定IDルール情報取得.
                    Rule ruleInfo = ((RuleEngineMessageService) getContext()).getRuleInformation(ruleServiceId);
                    if (ruleInfo != null) {
                        List<Bundle> triggerList = new ArrayList<>();
                        Trigger triggers = ruleInfo.getTrigger();

                        if (triggers != null) {
                            trigger.putLong("triggerInterval", triggers.getInterval());
                            trigger.putString("triggerIntervalUnit", triggers.getIntervalUnit());
                            trigger.putString("triggerIntervalReferenceTime", triggers.getReferenceTime());
                            trigger.putString("trigger", triggers.getRest());
                            trigger.putString("triggerAction", triggers.getAction());
                            trigger.putString("triggerParameter", triggers.getParameter());
                            trigger.putString("comparisonLeft", triggers.getComparisionLeft());
                            trigger.putString("comparisonLeftDataType", triggers.getComparisionLeftDataType());
                            trigger.putString("comparison", triggers.getComparision());
                            trigger.putString("comparisonRight", triggers.getComparisionRight());
                            trigger.putString("comparisonRightDataType", triggers.getComparisionRightDataType());
                        } else {
                            trigger.putString("triggerInterval", "");
                            trigger.putString("triggerIntervalUnit", "");
                            trigger.putString("triggerIntervalReferenceTime", "");
                            trigger.putString("trigger", "");
                            trigger.putString("triggerAction", "");
                            trigger.putString("triggerParameter", "");
                            trigger.putString("comparisonLeft", "");
                            trigger.putString("comparisonLeftDataType", "");
                            trigger.putString("comparison", "");
                            trigger.putString("comparisonRight", "");
                            trigger.putString("comparisonRightDataType", "");
                        }
                        AndRule andRule = ruleInfo.getAndRule();
                        if (andRule != null) {
                            StringBuilder sb = new StringBuilder();
                            List<String> andRuleServiceIds = andRule.getAndRuleServiceId();
                            int index = andRuleServiceIds.size();
                            for (String andRuleServiceId : andRuleServiceIds) {
                                sb.append(andRuleServiceId);
                                index--;
                                if (index > 0) {
                                    sb.append(", ");
                                }
                            }
                            trigger.putString("andRuleServiceId", sb.toString());
                            trigger.putLong("judgementTime", andRule.getJudgementTime());
                            trigger.putString("judgementTimeUnit", andRule.getJudgementTimeUnit());
                        } else {
                            trigger.putString("andRuleServiceId", "");
                            trigger.putString("judgementTime", "");
                            trigger.putString("judgementTimeUnit", "");
                        }
                        triggerList.add((Bundle) trigger.clone());

                        List<Bundle> operationList = new ArrayList<>();
                        List<Operation> operations = ruleInfo.getOperations();
                        if (operations != null) {
                            for (Operation op : operations) {
                                operation.putLong("operationIndex", op.getIndex());
                                operation.putString("operation", op.getRest());
                                operation.putString("operationAction", op.getAction());
                                operation.putString("operationParameter", op.getParameter());
                                operation.putString("delayOccurrence", op.getDelayOccurrence());
                                operationList.add((Bundle) operation.clone());
                            }
                        } else {
                            operation.putString("operationIndex", "");
                            operation.putString("operation", "");
                            operation.putString("operationAction", "");
                            operation.putString("operationParameter", "");
                            operation.putString("delayOccurrence", "");
                            operationList.add((Bundle) operation.clone());
                        }

                        rule.putString("ruleServiceId", ruleInfo.getRuleServiceId());
                        rule.putString("ruleServiceType", ruleInfo.getRuleServiceType());
                        rule.putBoolean("ruleEnable", ruleInfo.isRuleEnable());
                        rule.putString("errorStatus", ruleInfo.getErrorStarus());
                        rule.putString("errorTimestamp", ruleInfo.getErrorTimestamp());
                        rule.putString("description", ruleInfo.getRuleDescription());
                        rule.putParcelableArray("triggers", triggerList.toArray(new Bundle[triggerList.size()]));
                        rule.putParcelableArray("operations", operationList.toArray(new Bundle[operationList.size()]));

                        ruleList.add((Bundle) rule.clone());
                        root.putParcelableArray("rules", ruleList.toArray(new Bundle[ruleList.size()]));
                        response.putExtras(root);
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        // ルール登録無し
                        MessageUtils.setIllegalDeviceStateError(response, "Not register rule.");
                    }
                }
                return true;
            }
        });

        // PUT /ruleService/errorStatus
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "errorStatus";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                // イベント登録.
                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        // リクエスト保存.
                        mErrorStatusRequestList.add(request);
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /ruleService/errorStatus
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "errorStatus";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                // イベント削除.
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        // リクエスト削除.
                        java.util.Iterator<Intent> iter = mErrorStatusRequestList.iterator();
                        while (iter.hasNext()) {
                            if (iter.next().equals(request)) {
                                iter.remove();
                                break;
                            }
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event is not registered.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // GET /ruleService/errorStatus
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "errorStatus";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String ruleServiceId = (String) request.getExtras().get("ruleServiceId");

                if (mRuleServiceList.size() == 0) {
                    // ルール登録無し
                    MessageUtils.setIllegalDeviceStateError(response, "Not register rule.");
                    return true;
                }

                if (ruleServiceId != null) {
                    // 特定ID取得
                    for (String executeRuleServiceId : mRuleServiceList) {
                        if (executeRuleServiceId.contains(ruleServiceId)) {
                            ErrorStatus status = ((RuleEngineMessageService) getContext()).getErrorStatus(ruleServiceId);
                            if (status != null) {
                                setResult(response, DConnectMessage.RESULT_OK);
                                Bundle root = response.getExtras();
                                Bundle errors = new Bundle();
                                List<Bundle> statusList = new ArrayList<>();
                                errors.putString("ruleServiceId", ruleServiceId);
                                if (status.getStatus() == null) {
                                    errors.putString("errorStatus", ErrorStatus.NO_DATA);
                                } else {
                                    errors.putString("errorStatus", status.getStatus());
                                }
                                if (status.getTimestamp() == null) {
                                    errors.putString("errorTimestamp", ErrorStatus.NO_DATA);
                                } else {
                                    errors.putString("errorTimestamp", status.getTimestamp());
                                }
                                statusList.add((Bundle) errors.clone());
                                root.putParcelableArray("errors", statusList.toArray(new Bundle[statusList.size()]));
                                response.putExtras(root);
                                return true;
                            }
                        }
                    }
                    // 指定IDのルールサービス無し。
                    MessageUtils.setInvalidRequestParameterError(response);
                } else {
                    Bundle root = response.getExtras();
                    Bundle errors = new Bundle();
                    List<Bundle> statusList = new ArrayList<>();
                    // 全件取得
                    for (String executeRuleServiceId : mRuleServiceList) {
                        ErrorStatus status = ((RuleEngineMessageService) getContext()).getErrorStatus(executeRuleServiceId);
                        if (status != null) {
                            errors.putString("ruleServiceId", executeRuleServiceId);
                            if (status.getStatus() == null) {
                                errors.putString("errorStatus", ErrorStatus.NO_DATA);
                            } else {
                                errors.putString("errorStatus", status.getStatus());
                            }
                            if (status.getTimestamp() == null) {
                                errors.putString("errorTimestamp", ErrorStatus.NO_DATA);
                            } else {
                                errors.putString("errorTimestamp", status.getTimestamp());
                            }
                            statusList.add((Bundle) errors.clone());
                        }
                    }
                    root.putParcelableArray("errors", statusList.toArray(new Bundle[statusList.size()]));
                    response.putExtras(root);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });
    }

    /**
     * エラーステータスイベント送信処理.
     * @param ruleServiceId エラーが発生した　Rule service ID.
     * @param errorStatus エラーステータス.
     * @param errorTimestamp エラー発生時刻.
     */
    public void sendEventErrorStatus(final String ruleServiceId, final String errorStatus, final String errorTimestamp) {
        // エラー監視要求分ループ処理.
        for (Intent request : mErrorStatusRequestList) {
            // 該当リクエストのイベント取得.
            List<Event> lists = EventManager.INSTANCE.getEventList(request);
            // イベント分ループ.
            for (int i = 0; i < lists.size(); i++) {
                Event event = lists.get(i);
                String serviceId = (String) request.getExtras().get("ruleServiceId");
                // ID未指定または指定IDと合致した場合、イベント送信.
                if (serviceId == null || serviceId.contains(ruleServiceId)) {
                    Intent message = EventManager.createEventMessage(event);
                    Bundle root = message.getExtras();
                    Bundle errors = new Bundle();
                    List<Bundle> statusList = new ArrayList<>();
                    errors.putString("ruleServiceId", ruleServiceId);
                    errors.putString("errorStatus", errorStatus);
                    errors.putString("errorTimestamp", errorTimestamp);
                    statusList.add((Bundle) errors.clone());
                    root.putParcelableArray("errors", statusList.toArray(new Bundle[statusList.size()]));
                    message.putExtras(root);
                    sendEvent(message, event.getAccessToken());
                }
            }
        }
    }

    @Override
    public String getProfileName() {
        return "ruleService";
    }

    /**
     * ルールサービス管理テーブルにIDを追加.
     * @param ruleServiceId ルールサービスID.
     */
    public void addRuleServiceList(final String ruleServiceId) {
        if (mRuleServiceList != null) {
            mRuleServiceList.add(ruleServiceId);
        }
    }
}