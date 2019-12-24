/*
 QRTagProfile.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.qr.profiles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

import org.deviceconnect.android.deviceplugin.tag.TagMessageService;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;
import org.deviceconnect.android.deviceplugin.tag.services.qr.QRReader;
import org.deviceconnect.android.deviceplugin.tag.services.qr.QRService;
import org.deviceconnect.android.deviceplugin.tag.services.qr.QRWriter;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QRコードのタグ用のプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class QRTagProfile extends DConnectProfile {

    /**
     * QRコードの画像サイズ.
     */
    private static final int QR_CODE_SIZE = 500;

    /**
     * リソースのダウンロードを行うクラス.
     */
    private ResourceDownloader mResourceDownloader = new ResourceDownloader();

    public QRTagProfile() {

        // GET /gotapi/tag
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                String uri = (String) request.getExtras().get("uri");

                if (uri != null) {
                    mResourceDownloader.download(uri, (Bitmap bitmap) -> {
                        if (bitmap == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "Failed to download a resource.");
                        } else {
                            try {
                                TagInfo tagInfo = new QRReader().read(bitmap);
                                setResult(response, DConnectMessage.RESULT_OK);
                                setTags(response, createTagList(tagInfo.getList()));
                            } catch (NotFoundException e) {
                                MessageUtils.setInvalidRequestParameterError(response, "Resource of uri is not a QR code.");
                            }
                        }
                        sendResponse(response);
                    });
                } else {
                    getQRService().readQRCode((result, tagInfo) -> {
                        switch (result) {
                            case TagConstants.RESULT_SUCCESS:
                                setResult(response, DConnectMessage.RESULT_OK);
                                setTags(response, createTagList(tagInfo.getList()));
                                break;
                            case TagConstants.RESULT_NOT_SUPPORT:
                                MessageUtils.setNotSupportProfileError(response, "This device does not support camera.");
                                break;
                            case TagConstants.RESULT_NO_PERMISSION:
                                MessageUtils.setIllegalDeviceStateError(response, "Plugin has no permission.");
                                break;
                            case TagConstants.RESULT_DISABLED:
                                MessageUtils.setIllegalDeviceStateError(response, "Camera is disabled.");
                                break;
                            default:
                                MessageUtils.setUnknownError(response, "Failed to read a qr code.");
                                break;
                        }
                        sendResponse(response);
                    });
                }
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

                String data;
                if (text != null) {
                    data = text;
                } else if (uri != null) {
                    data = uri;
                } else {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return true;
                }

                writeQRCode(data, QR_CODE_SIZE, new FileManager.SaveFileCallback() {
                    @Override
                    public void onSuccess(@NonNull String uri) {
                        setResult(response, DConnectMessage.RESULT_OK);
                        setUri(response, uri);
                        sendResponse(response);
                    }

                    @Override
                    public void onFail(@NonNull Throwable throwable) {
                        MessageUtils.setUnknownError(response, throwable.getMessage());
                        sendResponse(response);
                    }
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

                        getQRService().startReadQRCode((result, tagInfo) -> {
                            switch (result) {
                                case TagConstants.RESULT_SUCCESS:
                                    List<Event> events = EventManager.INSTANCE.getEventList(getService().getId(),
                                            getProfileName(), null, "onRead");
                                    for (Event event : events) {
                                        Intent message = EventManager.createEventMessage(event);
                                        setTags(message, createTagList(tagInfo.getList()));
                                        sendEvent(message, event.getAccessToken());
                                    }
                                    break;
                            }
                        });
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
                        getQRService().stopReadQRCode();
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
     * レスポンスに URI 情報を格納します.
     *
     * @param response レスポンス
     * @param uri URI情報
     */
    private void setUri(final Intent response, final String uri) {
        response.putExtra("uri", uri);
    }

    /**
     * レスポンスにタグ情報を格納します.
     *
     * @param response レスポンス
     * @param tags タグ情報
     */
    private void setTags(final Intent response, final Bundle[] tags) {
        response.putExtra("tags", tags);
    }

    /**
     * QR のタグ情報のリストをBundleの配列に変換します.
     *
     * @param tagList タグ情報のリスト
     * @return タグ情報のBundleの配列
     */
    private Bundle[] createTagList(final List<Map<String, Object>> tagList) {
        ArrayList<Bundle> tags = new ArrayList<>();
        for (Map<String, Object> tag : tagList) {
            tags.add(createTag(tag));
        }
        return tags.toArray(new Bundle[0]);
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
     * QRコード読み込み用クラスを取得します.
     *
     * @return QRコード読み込み用クラス
     */
    private QRService getQRService() {
        TagMessageService service = (TagMessageService) getContext();
        return service.getQRService();
    }

    /**
     * ファイル管理クラスを取得します.
     *
     * @return ファイル管理クラス
     */
    private FileManager getFileManager() {
        TagMessageService service = (TagMessageService) getContext();
        return service.getFileManager();
    }

    /**
     * QR コードを作成して、ファイルに保存します.
     *
     * @param data QRコードに書き込むテキスト
     * @param size QRコードのサイズ
     * @param callback QRコードの生成結果を通知するコールバック
     */
    private void writeQRCode(final String data, final int size, final FileManager.SaveFileCallback callback) {
        try {
            Bitmap bitmap = new QRWriter().write(data, size, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] pngData = baos.toByteArray();
            getFileManager().saveFile("qr_code.png", pngData, true, callback);
        } catch (WriterException e) {
            callback.onFail(e);
        }
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