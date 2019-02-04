/*
 NFCTagProfile.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.nfc.profiles;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.tag.TagMessageService;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.nfc.NFCService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NFCのタグ用のプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class NFCTagProfile extends DConnectProfile {

    /**
     * NFC 読み込み結果を受け取るコールバック.
     */
    private final NFCService.ReaderCallback mReaderCallback = (result, tagInfo) -> {
        List<Event> events = EventManager.INSTANCE.getEventList(getService().getId(),
                getProfileName(), null, "onRead");
        for (Event event : events) {
            Intent message = EventManager.createEventMessage(event);
            if (tagInfo != null) {
                setTagId(message, tagInfo.getTagId());
                setTags(message, createTagList(tagInfo.getList()));
            }
            sendEvent(message, event.getAccessToken());
        }
    };

    public NFCTagProfile() {

        // GET /gotapi/tag
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                getNFCService().readNFCOnce((result, tagInfo) -> {
                    switch (result) {
                        case TagConstants.RESULT_SUCCESS:
                            setResult(response, DConnectMessage.RESULT_OK);
                            setTagId(response, tagInfo.getTagId());
                            setTags(response, createTagList(tagInfo.getList()));
                            break;
                        case TagConstants.RESULT_NOT_SUPPORT:
                            MessageUtils.setNotSupportProfileError(response, "This device does not support NFC.");
                            break;
                        case TagConstants.RESULT_NO_PERMISSION:
                            MessageUtils.setIllegalDeviceStateError(response, "Plugin has no permission.");
                            break;
                        case TagConstants.RESULT_DISABLED:
                            MessageUtils.setIllegalDeviceStateError(response, "NFC is disabled.");
                            break;
                        default:
                            MessageUtils.setUnknownError(response, "Failed to read a NFC.");
                            break;
                    }
                    sendResponse(response);
                });

                return false;
            }
        });

        // POST /gotapi/tag
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                String text = (String) request.getExtras().get("text");
                String uri = (String) request.getExtras().get("uri");
                String languageCode = (String) request.getExtras().get("languageCode");

                Map<String, String> data = new HashMap<>();
                if (text != null) {
                    data.put(TagConstants.EXTRA_TAG_TYPE, TagConstants.TYPE_TEXT);
                    data.put(TagConstants.EXTRA_TAG_DATA, text);
                    if (languageCode != null) {
                        data.put(TagConstants.EXTRA_LANGUAGE_CODE, languageCode);
                    }
                } else if (uri != null) {
                    data.put(TagConstants.EXTRA_TAG_TYPE, TagConstants.TYPE_URI);
                    data.put(TagConstants.EXTRA_TAG_DATA, uri);
                } else {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return true;
                }

                getNFCService().writeNFC(data, (result) -> {
                    switch (result) {
                        case TagConstants.RESULT_SUCCESS:
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        case TagConstants.RESULT_NOT_SUPPORT:
                            MessageUtils.setNotSupportProfileError(response, "This device does not support NFC.");
                            break;
                        case TagConstants.RESULT_NO_PERMISSION:
                            MessageUtils.setIllegalDeviceStateError(response, "Plugin has no permission.");
                            break;
                        case TagConstants.RESULT_DISABLED:
                            MessageUtils.setIllegalDeviceStateError(response, "NFC is disabled.");
                            break;
                        case TagConstants.RESULT_INVALID_FORMAT:
                            MessageUtils.setIllegalDeviceStateError(response, "Failed to write to NFC because NFC is unknown format.");
                            break;
                        case TagConstants.RESULT_NOT_WRIATEBLE:
                            MessageUtils.setIllegalDeviceStateError(response, "Failed to write to NFC because NFC is not writable.");
                            break;
                        default:
                            MessageUtils.setUnknownError(response, "Failed to write to NFC.");
                            break;
                    }
                    sendResponse(response);
                });
                return false;
            }
        });

        // PUT /gotapi/tag/onRead
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onRead";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                Long interval = parseLong(request, "interval", 1000L);

                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);
                        getNFCService().readNFC(mReaderCallback);
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

        // DELETE /gotapi/tag/onRead
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onRead";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
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
    }

    @Override
    public String getProfileName() {
        return "tag";
    }

    /**
     * レスポンスにタグ情報を格納します.
     *
     * @param response レスポンス
     * @param tags タグ情報
     */
    private void setTags(final Intent response, final Bundle[] tags) {
        if (tags != null) {
            response.putExtra("tags", tags);
        }
    }

    /**
     * レスポンスにタグIDを格納します.
     *
     * @param response レスポンス
     * @param tagId タグID
     */
    private void setTagId(final Intent response, final String tagId) {
        if (tagId != null) {
            response.putExtra("tagId", tagId);
        }
    }

    /**
     * タグ情報のリストをBundleの配列に変換します.
     *
     * @param tagList タグ情報のリスト
     * @return タグ情報のBundleの配列
     */
    private Bundle[] createTagList(final List<Map<String, Object>> tagList) {
        ArrayList<Bundle> tags = new ArrayList<>();
        for (Map<String, Object> tag : tagList) {
            tags.add(createTag(tag));
        }
        return  tags.toArray(new Bundle[0]);
    }

    /**
     * QR のタグ情報を格納するオブジェクトを作成します.
     *
     * @param tag タグ情報
     * @return Bundleオブジェクト
     */
    private Bundle createTag(final Map<String, Object> tag) {
        Bundle bundle = new Bundle();
        for (String key : tag.keySet()) {
            bundle.putString(key, (String) tag.get(key));
        }
        return bundle;
    }

    /**
     * NFC読み込み用クラスを取得します.
     *
     * @return NFC読み込み用クラス
     */
    private NFCService getNFCService() {
        TagMessageService service = (TagMessageService) getContext();
        return service.getNFCService();
    }

    /**
     * リクエストから指定のキーの値を取得します.
     * <p>
     *     キーに対応する値が存在しない場合には、defaultValueの値を返却します.
     * </p>
     * @param request リクエスト
     * @param key キー
     * @param defaultValue デフォルトの値
     * @return キーに対応する値
     */
    private static Long parseLong(final Intent request, final String key, final Long defaultValue) {
        Long value = parseLong(request, key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}