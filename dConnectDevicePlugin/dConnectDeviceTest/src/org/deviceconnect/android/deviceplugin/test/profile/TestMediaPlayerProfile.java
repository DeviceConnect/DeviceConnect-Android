/*
 TestMediaPlayerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * JUnit用テストデバイスプラグイン、MediaStreamsPlayプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestMediaPlayerProfile extends MediaPlayerProfile {
    
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
    
    

    @Override
    protected boolean onPutPlay(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onPutStop(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutPause(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutResume(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetPlayStatus(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setStatus(response, PlayStatus.PLAY);
        }
        
        return true;
    }

    @Override
    protected boolean onPutMedia(final Intent request, final Intent response,
                                    final String serviceId, final String mediaId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (TextUtils.isEmpty(mediaId)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetMedia(final Intent request, final Intent response,
                                        final String serviceId, final String mediaId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (TextUtils.isEmpty(mediaId)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setMIMEType(response, "audio/mp3");
            setTitle(response, "test title");
            setType(response, "test type");
            setLanguage(response, "ja");
            setDescription(response, "test description");
            setDuration(response, 60000);
            Bundle creator = new Bundle();
            setCreator(creator, "test creator");
            setRole(creator, "composer");
            setCreators(response, new Bundle[] {creator});
            setKeywords(response, new String[] {"keyword1", "keyword2"});
            setGenres(response, new String[] {"test1", "test2"});
        }
        
        return true;
    }

    @Override
    protected boolean onGetMediaList(final Intent request, final Intent response,
            final String serviceId, final String query, final String mimeType,
            final String[] orders, final Integer offset, final Integer limit) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setCount(response, 1);
            Bundle medium = new Bundle();
            setMediaId(medium, "media001");
            setMIMEType(medium, "audio/mp3");
            setTitle(medium, "test title");
            setType(medium, "test type");
            setLanguage(medium, "ja");
            setDescription(medium, "test description");
            setDuration(medium, 60000);
            Bundle creator = new Bundle();
            setCreator(creator, "test creator");
            setRole(creator, "composer");
            setCreators(medium, new Bundle[] {creator});
            setKeywords(medium, new String[] {"keyword1", "keyword2"});
            setGenres(medium, new String[] {"test1", "test2"});
            setMedia(response, new Bundle[] {medium});
        }
        
        return true;
    }

    @Override
    protected boolean onPutVolume(final Intent request, final Intent response,
                                    final String serviceId, final Double volume) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (volume == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (0.0 > volume || volume > 1.0) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetVolume(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setVolume(response, 0.5);
        }
        
        return true;
    }

    @Override
    protected boolean onPutSeek(final Intent request, final Intent response,
                                            final String serviceId, final Integer pos) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (pos == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (0 > pos) {
            // MEMO 本テストプラグインでは pos の最大値チェックは行わないが、実際には行うべき.
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onGetSeek(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setPos(response, 0);
        }
        
        return true;
    }

    @Override
    protected boolean onPutMute(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        
        return true;
    }

    @Override
    protected boolean onDeleteMute(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetMute(final Intent request, final Intent response, final String serviceId) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
            setMute(response, true);
        }
        
        return true;
    }

    @Override
    protected boolean onPutOnStatusChange(final Intent request, final Intent response,
                                                    final String serviceId, final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            // テスト用イベントメッセージを1秒後にブロードキャスト
            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_STATUS_CHANGE);
            Bundle mediaPlayer = new Bundle();
            setStatus(mediaPlayer, Status.PLAY);
            setMediaId(mediaPlayer, "test.mp4");
            setMIMEType(mediaPlayer, "video/mp4");
            setPos(mediaPlayer, 0);
            setVolume(mediaPlayer, 0.5);
            setMediaPlayer(message, mediaPlayer);
            Util.sendBroadcast(getContext(), message);
        }
        
        return true;
    }

    @Override
    protected boolean onDeleteOnStatusChange(final Intent request, final Intent response,
                                                final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

}
