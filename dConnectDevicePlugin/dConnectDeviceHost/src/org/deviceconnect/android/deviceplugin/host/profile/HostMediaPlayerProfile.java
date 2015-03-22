/*
 HostMediaPlayerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Media Player Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostMediaPlayerProfile extends MediaPlayerProfile {

    /** Debug Tag. */
    private static final String TAG = "HOST";

    /** Error. */
    private static final int ERROR_VALUE_IS_NULL = 100;

    /** ミリ秒 - 秒オーダー変換用. */
    private static final int UNIT_SEC = 1000;


    /**
     * AudioのContentProviderのキー一覧を定義する.
     */
    private static final String[] AUDIO_TABLE_KEYS = {
        MediaStore.Audio.Media.ALBUM, 
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.COMPOSER, 
        MediaStore.Audio.Media.TITLE, 
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media._ID, 
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.DATE_ADDED, 
        MediaStore.Audio.Media.DISPLAY_NAME
    };

    /**
     * VideoのContentProviderのキー一覧を定義する.
     */
    private static final String[] VIDEO_TABLE_KEYS = {
        MediaStore.Video.Media.ALBUM, 
        MediaStore.Video.Media.ARTIST,
        MediaStore.Video.Media.LANGUAGE, 
        MediaStore.Video.Media.TITLE, 
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media._ID, 
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.DATE_ADDED, 
        MediaStore.Video.Media.DISPLAY_NAME
    };

    /** Mute Status. */
    private static Boolean sIsMute = false;

    @Override
    protected boolean onPutPlay(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            ((HostDeviceService) getContext()).playMedia();
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
            ((HostDeviceService) getContext()).stopMedia();
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
            ((HostDeviceService) getContext()).pauseMedia();
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
            ((HostDeviceService) getContext()).resumeMedia();
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetPlayStatus(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        } else {
            ((HostDeviceService) getContext()).getPlayStatus(response);
            return false;
        }

    }

    @Override
    protected boolean onPutMedia(final Intent request, final Intent response, final String serviceId,
            final String mediaId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (TextUtils.isEmpty(mediaId)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            if (checkInteger(mediaId)) {
                ((HostDeviceService) getContext()).putMediaId(response, mediaId);
            } else {
                FileManager mFileManager = new FileManager(this.getContext());

                long newMediaId = mediaIdFromPath(this.getContext(), mFileManager.getBasePath() + mediaId);
                if (newMediaId == -1) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return true;
                }
                ((HostDeviceService) getContext()).putMediaId(response, "" + newMediaId);
            }

            return false;

        }

        return true;
    }

    @Override
    protected boolean onGetMedia(final Intent request, final Intent response, final String serviceId,
            final String mediaId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (TextUtils.isEmpty(mediaId)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            // Query table parameter.
            String[] param = null;
            // URI
            Uri uriType = null;
            // Query filter.
            String filter = "_display_name=?";
            // Query cursor.
            Cursor cursor = null;

            // Get media path.
            Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));
            String fileName = getDisplayNameFromUri(uri);
            if (fileName == null) {
                uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));
                fileName = getDisplayNameFromUri(uri);
                if (fileName == null) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return true;
                }
                param = AUDIO_TABLE_KEYS;
                uriType = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else {
                param = VIDEO_TABLE_KEYS;
                uriType = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }

            ContentResolver cresolver = getContext()
                    .getApplicationContext().getContentResolver();
            try {
                List<Bundle> list = new ArrayList<Bundle>();
                cursor = cresolver.query(uriType, param, filter, new String[] {fileName}, null);
                if (cursor.moveToFirst()) {
                    Bundle medium = loadMediaData(uriType, cursor);
                    list.add(medium);
                }
                setMedia(response, list.toArray(new Bundle[list.size()]));
                setResult(response, DConnectMessage.RESULT_OK);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return true;
    }

    /**
     * cursorからMediaDataを読み込みBundleに格納して返却する.
     * @param uriType メディアタイプ
     * @param cursor データが格納されているCursor
     * @return データを移し替えたBundle
     */
    private Bundle loadMediaData(final Uri uriType, final Cursor cursor) {
        Bundle medium = new Bundle();
        String mId = null;
        String mType = null;
        String mTitle = null;
        int mDuration = 0;
        String mArtist = null;
        String mComp = null;

        if (uriType == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
            mId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            mType = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
            mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            mDuration = (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))) / UNIT_SEC;
            mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            mComp = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER));

            setType(medium, "Music");

            // Make creator
            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creator = new Bundle();
            setCreator(creator, mArtist);
            setRole(creator, "Artist");
            dataList.add((Bundle) creator.clone());
            setCreator(creator, mComp);
            setRole(creator, "Composer");
            dataList.add((Bundle) creator.clone());

            setCreators(medium, dataList.toArray(new Bundle[dataList.size()]));
        } else {
            mId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
            mType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
            mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
            mDuration = (cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))) / UNIT_SEC;
            mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
            String mLang = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.LANGUAGE));
            setLanguage(medium, mLang);

            setType(medium, "Video");

            // Make creator
            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creatorVideo = new Bundle();
            setCreator(creatorVideo, mArtist);
            setRole(creatorVideo, "Artist");
            dataList.add((Bundle) creatorVideo.clone());
            setCreators(medium, dataList.toArray(new Bundle[dataList.size()]));
        }
        setMediaId(medium, mId);
        setMIMEType(medium, mType);
        setTitle(medium, mTitle);
        setDuration(medium, mDuration);
        return medium;
    }

    /**
     * Get display name from URI.
     * 
     * @param mUri URI
     * @return name display name.
     */
    private String getDisplayNameFromUri(final Uri mUri) {
        Cursor c = null;
        try {
            ContentResolver mContentResolver = getContext()
                    .getApplicationContext().getContentResolver();
            c = mContentResolver.query(mUri, null, null, null, null);
            if (c.moveToFirst()) {
                int index = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                if (index != -1) {
                    return c.getString(index);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    @Override
    protected boolean onGetMediaList(final Intent request, final Intent response,
            final String serviceId, final String query,
            final String mimeType, final String[] orders, final Integer offset, final Integer limit) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle b = request.getExtras();
            if (b.getString(PARAM_LIMIT) != null) {
                if (parseInteger(b.get(PARAM_LIMIT)) == null) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return true;
                }
            }
            if (b.getString(PARAM_OFFSET) != null) {
                if (parseInteger(b.get(PARAM_OFFSET)) == null) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return true;
                }
            }
            getMediaList(response, query, mimeType, orders, offset, limit);
        }
        return true;
    }

    /**
     * Get Media List.
     * @param response Response
     * @param query Query
     * @param mimeType MIME Type
     * @param orders Order
     * @param offset Offset
     * @param limit Limit
     */
    private void getMediaList(final Intent response, final String query,
            final String mimeType, final String[] orders, final Integer offset,
            final Integer limit) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onGetMediaList");
        }
        int counter = 0;
        int tmpLimit = 0;
        int tmpOffset = 0;
        if (limit != null) {
            if (limit >= 0) {
                tmpLimit = limit;
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
                return;
            }
        }
        if (limit != null && limit >= 0) {
            tmpLimit = limit;
        }
        if (offset != null) {
            if (offset >= 0) {
                tmpOffset = offset;
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
                return;
            }
        }
        int limitCounter = tmpLimit + tmpOffset;

        // 音楽用のテーブルの項目.
        String[] mMusicParam = null;
        String[] mVideoParam = null;

        // URI
        Uri mMusicUriType = null;
        Uri mVideoUriType = null;

        // 検索用 Filterを作成.
        String mVideoFilter = "";
        String mMusicFilter = "";

        // Orderの処理
        String mOrderBy = "";

        // 検索用Cursor.
        Cursor cursorMusic = null;
        Cursor cursorVideo = null;

        if (mimeType != null) {
            mVideoFilter = "" + MediaStore.Video.Media.MIME_TYPE + "='" + mimeType + "'";
            mMusicFilter = "" + MediaStore.Audio.Media.MIME_TYPE + "='" + mimeType + "'";
        }
        if (query != null) {
            if (!mVideoFilter.equals("")) {
                mVideoFilter += " AND ";
            }
            mVideoFilter += MediaStore.Video.Media.TITLE + " LIKE '%" + query + "%'";

            if (!mMusicFilter.equals("")) {
                mMusicFilter += " AND ";
            }
            mMusicFilter += "(" + MediaStore.Audio.Media.TITLE + " LIKE '%" + query + "%'";
            mMusicFilter += " OR " + MediaStore.Audio.Media.COMPOSER + " LIKE '%" + query + "%')";
        }
        if (BuildConfig.DEBUG) {
            if (orders != null) {
                for (int i = 0; i < orders.length; i++) {
                    Log.i(TAG, "orders[" + i + "]: " + orders[i]);
                }
            }
        }
        if (orders != null) {
            if (orders.length == 2) {
                mOrderBy = orders[0] + " " + orders[1];
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
                return;
            }
        } else {
            mOrderBy = "title asc";
        }

        // 音楽用のテーブルキー設定.
        mMusicParam = new String[] {MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.COMPOSER, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DATE_ADDED};
        mMusicUriType = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // 動画用のテーブルキー設定.
        mVideoParam = new String[] {MediaStore.Video.Media.ALBUM, MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.LANGUAGE, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media._ID, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED};

        mVideoUriType = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        ContentResolver mContentResolver = this.getContext().getApplicationContext().getContentResolver();

        try {
            cursorMusic = mContentResolver.query(mMusicUriType, mMusicParam, mMusicFilter, null, mOrderBy);
            cursorMusic.moveToFirst();
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            MessageUtils.setInvalidRequestParameterError(response);
            if (cursorMusic != null) {
                cursorMusic.close();
            }
            return;
        }

        List<Bundle> list = new ArrayList<Bundle>();

        if (cursorMusic.getCount() > 0) {
            counter = getMusicListData(offset, limit, counter, limitCounter,
                    cursorMusic, list);
        }

        try {
            cursorVideo = mContentResolver.query(mVideoUriType, mVideoParam, mVideoFilter, null, mOrderBy);
            cursorVideo.moveToFirst();
        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            MessageUtils.setInvalidRequestParameterError(response);
            if (cursorMusic != null) {
                cursorMusic.close();
            }
            if (cursorVideo != null) {
                cursorVideo.close();
            }
            return;
        }

        if (cursorVideo.getCount() > 0) {
            counter = getMovieListData(offset, limit, counter, limitCounter,
                    cursorVideo, list);
        }

        setCount(response, cursorMusic.getCount() + cursorVideo.getCount());
        setMedia(response, list.toArray(new Bundle[list.size()]));
        setResult(response, DConnectMessage.RESULT_OK);
        cursorMusic.close();
        cursorVideo.close();

        return;

    }

    /**
     * Get Music List Data.
     * @param offset Offset
     * @param limit Limit
     * @param c Counter
     * @param limitCounter Limit Counter
     * @param cursorMusic Cursor Music
     * @param list List
     * @return Music List
     */
    private int getMusicListData(final Integer offset, final Integer limit,
            final int c, final int limitCounter, final Cursor cursorMusic, final List<Bundle> list) {
        int counter = c;
        do {

            String mId = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media._ID));
            String mType = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
            String mTitle = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.TITLE));
            int mDuration = (cursorMusic.getInt(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                    / UNIT_SEC;
            String mArtist = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String mComp = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
            // 音楽のデータ作成
            Bundle medium = new Bundle();

            setType(medium, "Music");
            setMediaId(medium, mId);
            setMIMEType(medium, mType);
            setTitle(medium, mTitle);
            setDuration(medium, mDuration);

            // Creatorを作成
            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creator = new Bundle();
            setCreator(creator, mArtist);
            setRole(creator, "Artist");
            dataList.add((Bundle) creator.clone());
            setCreator(creator, mComp);
            setRole(creator, "Composer");
            dataList.add((Bundle) creator.clone());
            
            setCreators(medium, dataList.toArray(new Bundle[dataList.size()]));

            if (limit == null || (limit != null && limitCounter > counter)) {
                if (offset == null || (offset != null && counter >= offset)) {
                    list.add(medium);
                }
            }
            counter++;
        } while (cursorMusic.moveToNext());
        return counter;
    }

    /**
     * Get Movie List Data.
     * @param offset Offset
     * @param limit Limit
     * @param c Counter
     * @param limitCounter Limit Counter
     * @param cursorVideo Cursor Video
     * @param list List
     * @return Movie List
     */
    private int getMovieListData(final Integer offset, final Integer limit,
            final int c, final int limitCounter, final Cursor cursorVideo, final List<Bundle> list) {
        int counter = c;
        do {

            // 映像のリストデータ作成
            Bundle medium = new Bundle();

            String mLang = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.LANGUAGE));
            String mId = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media._ID));
            String mType = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
            String mTitle = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.TITLE));
            int mDuration = (cursorVideo.getInt(cursorVideo.getColumnIndex(MediaStore.Video.Media.DURATION)))
                    / UNIT_SEC;
            String mArtist = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.ARTIST));
            setType(medium, "Video");
            setLanguage(medium, mLang);
            setMediaId(medium, mId);
            setMIMEType(medium, mType);
            setTitle(medium, mTitle);
            setDuration(medium, mDuration);

            // Creatorを作成
            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creatorVideo = new Bundle();
            setCreator(creatorVideo, mArtist);
            setRole(creatorVideo, "Artist");
            dataList.add((Bundle) creatorVideo.clone());
            setCreators(medium, dataList.toArray(new Bundle[dataList.size()]));

            if (limit == null || (limit != null && limitCounter > counter)) {
                if (offset == null || (offset != null && counter >= offset)) {
                    list.add(medium);
                }
            }
            counter++;
        } while (cursorVideo.moveToNext());
        return counter;
    }

    @Override
    protected boolean onPutVolume(final Intent request, final Intent response, final String serviceId,
            final Double volume) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (volume == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (0.0 > volume || volume > 1.0) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            AudioManager manager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);

            double maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolume * volume), 1);
            setResult(response, DConnectMessage.RESULT_OK);

            ((HostDeviceService) getContext()).sendOnStatusChangeEvent("volume");
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
            AudioManager manager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);

            double maxVolume = 1;
            double mVolume = 0;

            mVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            setVolume(response, mVolume / maxVolume);

            setResult(response, DConnectMessage.RESULT_OK);
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
            return true;
        } else if (0 > pos) {
            // MEMO 本テストプラグインでは pos の最大値チェックは行わないが、実際には行うべき.
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        ((HostDeviceService) getContext()).setMediaPos(response, pos);
        return false;
    }

    @Override
    protected boolean onGetSeek(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            int pos = ((HostDeviceService) getContext()).getMediaPos();
            if (pos < 0) {
                setPos(response, 0);
                MessageUtils.setError(response, DConnectMessage.RESULT_ERROR, "Position acquisition failure.");
            } else if (pos == Integer.MAX_VALUE) {
                ((HostDeviceService) getContext()).setVideoMediaPosRes(response);
                return false;
            } else {
                setPos(response, pos);
                setResult(response, DConnectMessage.RESULT_OK);
            }
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
            AudioManager manager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);
            manager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            sIsMute = true;
            setResult(response, DConnectMessage.RESULT_OK);
            ((HostDeviceService) getContext()).sendOnStatusChangeEvent("mute");
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
            AudioManager manager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);
            manager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            sIsMute = false;
            setResult(response, DConnectMessage.RESULT_OK);
            ((HostDeviceService) getContext()).sendOnStatusChangeEvent("unmute");
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
            setMute(response, sIsMute);
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnStatusChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);

            return true;
        } else {
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HostDeviceService) getContext()).registerOnStatusChange(response, serviceId);
                return false;
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
                return true;
            }
        }
    }

    @Override
    protected boolean onDeleteOnStatusChange(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {

                ((HostDeviceService) getContext()).unregisterOnStatusChange(response);
                return false;

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
                return true;

            }
        }

    }

    /**
     * ファイル名からMIMEタイプ取得.
     * 
     * @param path パス
     * @return MIME-TYPE
     */
    public String getMIMEType(final String path) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, path);
        }
        // 拡張子を取得
        String ext = MimeTypeMap.getFileExtensionFromUrl(path);
        // 小文字に変換
        ext = ext.toLowerCase(Locale.getDefault());
        // MIME Typeを返す
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    /**
     * 数値かどうかをチェックする.
     * 
     * @param value チェックしたいID
     * @return 数値の場合はtrue、そうでない場合はfalse
     */
    private boolean checkInteger(final String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return HostServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        setResult(response, DConnectMessage.RESULT_ERROR);
    }

    /**
     * ファイルパスからメディアIDを取得する.
     * 
     * @param context コンテキスト
     * @param path パス
     * @return MediaID
     */
    public static long mediaIdFromPath(final Context context, final String path) {
        long id = 0;
        String[] mParam = {BaseColumns._ID};
        String[] mArgs = new String[] {path};

        // Audio
        Uri mAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String mFilter = MediaStore.Audio.AudioColumns.DATA + " LIKE ?";

        // Search Contents Provider
        ContentResolver mAudioContentsProvider = context.getContentResolver();
        try {
            Cursor mAudioCursor = mAudioContentsProvider.query(mAudioUri, mParam, mFilter, mArgs, null);
            mAudioCursor.moveToFirst();
            int mIdField = mAudioCursor.getColumnIndex(mParam[0]);
            id = mAudioCursor.getLong(mIdField);
            mAudioCursor.close();

        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return -1;
        }

        // Search video
        if (id == 0) {

            Uri mViodeUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            mFilter = MediaStore.Video.VideoColumns.DATA + " LIKE ?";

            // Search Contents Provider
            ContentResolver mVideoContentsProvider = context.getContentResolver();
            try {
                Cursor mVideoCursor = mVideoContentsProvider.query(mViodeUri, mParam, mFilter, mArgs, null);

                mVideoCursor.moveToFirst();
                int mIdField = mVideoCursor.getColumnIndex(mParam[0]);
                id = mVideoCursor.getLong(mIdField);
                mVideoCursor.close();

            } catch (NullPointerException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                return -1;
            }

        }

        return id;
    }
}
