package org.deviceconnect.android.deviceplugin.test.profile;

import java.util.ArrayList;
import java.util.List;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

/**
 * JUnit用テストデバイスプラグイン、Lightプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestLightProfile extends LightProfile {

    // テストデータ.
    /**
     * テスト用のライトID.
     */
    private static final String LIGHT_ID = "test_light_id";

    /**
     * テスト用のライト名前.
     */
    private static final String LIGHT_NAME = "test_light_name";

    /**
     * テスト用のライト状態.
     */
    private static final boolean LIGHT_ON = true;

    /**
     * テスト用のライトコンフィグ.
     */
    private static final String LIGHT_CONFIG = "";

    /**
     * テスト用の色情報.
     */
    private static final Integer LIGHT_COLOR = Color.argb(0, 255, 0, 0);

    /**
     * テスト用の明るさ.
     */
    private static final Double LIGHT_BRIGHTNESS = 0.5;

    /**
     * テスト用の点滅情報.
     */
    private static final long[] LIGHT_FLASHING = {1000, 1001, 1002};

    /**
     * テスト用の新規ライト名.
     */
    private static final String LIGHT_NEW_NAME = "test_new_light_name";

    /**
     * テスト用のライトグループID.
     */
    private static final String LIGHT_GROUP_ID = "test_group_id";

    /**
     * テスト用のグループ名.
     */
    private static final String LIGHT_GROUP_NAME = "test_group_name";

    /**
     * テスト用のライトIDリスト.
     */
    private static final String[] LIGHT_IDS = {
        "test_light_id1", "test_light_id2", "test_light_id3"
    };

    /**
     * テスト用の新規グループID.
     */
    private static final String LIGHT_NEW_GROUP_ID = "test_new_group_id";

    /**
     * テスト用の新規グループ名.
     */
    private static final String LIGHT_NEW_GROUP_NAME = "test_new_group_name";

    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            List<Bundle> lightList = new ArrayList<Bundle>();

            Bundle light = new Bundle();
            setLightId(light, LIGHT_ID);
            setName(light, LIGHT_NAME);
            setOn(light, LIGHT_ON);
            setConfig(light, LIGHT_CONFIG);
            lightList.add(light);

            setLights(response, lightList);
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
            final String lightId, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkLightId(lightId)) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
        } else if (!checkColor(color)) {
            MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
        } else if (!checkBrightness(brightness)) {
            MessageUtils.setInvalidRequestParameterError(response, "brightness is invalid.");
        } else if (!checkFlashing(flashing)) {
            MessageUtils.setInvalidRequestParameterError(response, "flashing is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
            final String lightId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkLightId(lightId)) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutLight(final Intent request, final Intent response, final String serviceId,
            final String lightId, final String name, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkLightId(lightId)) {
            MessageUtils.setInvalidRequestParameterError(response, "lightId is invalid.");
        } else if (!checkName(name)) {
            MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
        } else if (!checkColor(color)) {
            MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
        } else if (!checkBrightness(brightness)) {
            MessageUtils.setInvalidRequestParameterError(response, "brightness is invalid.");
        } else if (!checkFlashing(flashing)) {
            MessageUtils.setInvalidRequestParameterError(response, "flashing is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetLightGroup(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle light = new Bundle();
            setLightId(light, LIGHT_ID);
            setName(light, LIGHT_NAME);
            setOn(light, LIGHT_ON);
            setConfig(light, LIGHT_CONFIG);

            List<Bundle> lightList = new ArrayList<Bundle>();
            lightList.add(light);

            Bundle group = new Bundle();
            setGroupId(group, LIGHT_GROUP_ID);
            setGroupName(group, LIGHT_GROUP_NAME);
            setLights(group, lightList);

            List<Bundle> groupList = new ArrayList<Bundle>();
            groupList.add(group);

            setLightGroups(response, groupList);
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPostLightGroup(final Intent request, final Intent response, final String serviceId,
            final String groupId, final Integer color, final Double brightness, final long[] flashing) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkGroupId(groupId)) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is invalid.");
        } else if (!checkColor(color)) {
            MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
        } else if (!checkBrightness(brightness)) {
            MessageUtils.setInvalidRequestParameterError(response, "brightness is invalid.");
        } else if (!checkFlashing(flashing)) {
            MessageUtils.setInvalidRequestParameterError(response, "flashing is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteLightGroup(final Intent request, final Intent response, final String serviceId, final String groupId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkGroupId(groupId)) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutLightGroup(Intent request, Intent response, String serviceId, String groupId, String name,
            Integer color, Double brightness, long[] flashing) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkGroupId(groupId)) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is invalid.");
        } else if (!checkGroupName(name)) {
            MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
        } else if (!checkColor(color)) {
            MessageUtils.setInvalidRequestParameterError(response, "color is invalid.");
        } else if (!checkBrightness(brightness)) {
            MessageUtils.setInvalidRequestParameterError(response, "brightness is invalid.");
        } else if (!checkFlashing(flashing)) {
            MessageUtils.setInvalidRequestParameterError(response, "flashing is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPostLightGroupCreate(final Intent request, final Intent response, final String serviceId,
            final String[] lightIds, final String name) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkLightIds(lightIds)) {
            MessageUtils.setInvalidRequestParameterError(response, "lightIds is invalid.");
        } else if (!checkGroupName(name)) {
            MessageUtils.setInvalidRequestParameterError(response, "groupName is invalid.");
        } else {
            setGroupId(response, LIGHT_NEW_GROUP_ID);
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onDeleteLightGroupClear(Intent request, Intent response, String serviceId, String groupId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (!checkGroupId(groupId)) {
            MessageUtils.setInvalidRequestParameterError(response, "groupId is invalid.");
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * ライトIDをチェックする.
     * @param lightId ライトID
     * @return lightIdがテスト用ライトIDに等しい場合はtrue、それ以外はfalse
     */
    private boolean checkLightId(final String lightId) {
        return LIGHT_ID.equals(lightId);
    }

    /**
     * ライトグループIDをチェックする.
     * @param groupId ライトグループID
     * @return groupIdがテスト用ライトIDに等しい場合はtrue、それ以外はfalse
     */
    private boolean checkGroupId(final String groupId) {
        return LIGHT_GROUP_ID.equals(groupId);
    }

    /**
     * 色情報をチェックする.
     * nullが指定された場合には省略されたので、trueを返却する。
     * @param color 色情報
     * @return テスト用の色情報に等しい場合はtrue、それ以外はfalse
     */
    private boolean checkColor(final Integer color) {
        if (color == null) {
            return true;
        }
        return Color.red(color) == Color.red(LIGHT_COLOR) 
                && Color.green(color) == Color.green(LIGHT_COLOR)
                && Color.blue(color) == Color.blue(LIGHT_COLOR);
    }

    /**
     * 明るさをチェックする.
     * nullが指定された場合には省略されたので、trueを返却する。
     * @param brightness 明るさ
     * @return テスト用の明るさに等しい場合はtrue、それ以外はfalse
     */
    private boolean checkBrightness(final Double brightness) {
        if (brightness == null) {
            return true;
        }
        return LIGHT_BRIGHTNESS.doubleValue() == brightness;
    }

    /**
     * 点滅情報をチェックする.
     * nullが指定された場合には省略されたので、trueを返却する。
     * @param flashing 点滅情報
     * @return テスト用の点滅情報に等しい場合はtrue、それ以外はfalse
     */
    private boolean checkFlashing(final long[] flashing) {
        if (flashing == null) {
            return true;
        }
        if (flashing.length != LIGHT_FLASHING.length) {
            return false;
        }
        for (int i = 0; i < LIGHT_FLASHING.length; i++) {
            if (flashing[i] != LIGHT_FLASHING[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 名前をチェックする.
     * @param name 名前
     * @return テスト用の名前に等しい場合はtrue、それ以外はfalse
     */
    private boolean checkName(final String name) {
        return LIGHT_NEW_NAME.equals(name);
    }

    /**
     * ライトIDリストをチェックする.
     * @param ids ライトIDリスト
     * @return テスト用のライトIDリストに等しい場合はtrue、それ以外はfalse
     */
    private boolean checkLightIds(final String[] ids) {
        if (LIGHT_IDS.length != ids.length) {
            return false;
        }
        for (int i = 0; i < LIGHT_IDS.length; i++) {
            if (!LIGHT_IDS[i].equals(ids[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * ライトグループ名をチェックする.
     * @param name ライトグループ名
     * @return テスト用のライトグループ名に等しい場合はtrue、それ以外はfalse
     */
    private boolean checkGroupName(final String name) {
        return LIGHT_NEW_GROUP_NAME.equals(name);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response, "Service ID is empty.");
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response, "Service is not found.");
    }
}
