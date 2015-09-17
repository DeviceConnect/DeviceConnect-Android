/*
 HostMediaPlayerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    /** Sort flag. */
    enum SortOrder {
        /** Title (asc). */
        TITLE_ASC,
        /** Title (desc). */
        TITLE_DESC,
        /** Duration (asc). */
        DURATION_ASC,
        /** Duration (desc). */
        DURATION_DESC,
        /** Artist (asc). */
        ARTIST_ASC,
        /** Artist (desc). */
        ARTIST_DESC,
        /** Mime (asc). */
        MIME_ASC,
        /** Mime (desc). */
        MIME_DESC,
        /** Id (asc). */
        ID_ASC,
        /** Id (desc). */
        ID_DESC,
        /** Composer (asc). */
        COMPOSER_ASC,
        /** Composer (desc). */
        COMPOSER_DESC,
        /** Language (asc). */
        LANGUAGE_ASC,
        /** Language (desc). */
        LANGUAGE_DESC
    }

    /**
     * AudioのContentProviderのキー一覧を定義する.
     */
    private static final String[] AUDIO_TABLE_KEYS = { MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.COMPOSER, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DISPLAY_NAME };

    /**
     * VideoのContentProviderのキー一覧を定義する.
     */
    private static final String[] VIDEO_TABLE_KEYS = { MediaStore.Video.Media.ALBUM, MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.LANGUAGE, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media._ID, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DISPLAY_NAME };

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
            ((HostDeviceService) getContext()).stopMedia(response);
            return false;
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
                PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                FileManager mFileManager = new FileManager(getContext());

                                long newMediaId = mediaIdFromPath(getContext(), mFileManager.getBasePath() + mediaId);
                                if (newMediaId == -1) {
                                    MessageUtils.setInvalidRequestParameterError(response);
                                    getContext().sendBroadcast(response);
                                    return;
                                }
                                ((HostDeviceService) getContext()).putMediaId(response, "" + newMediaId);
                                getContext().sendBroadcast(response);
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                MessageUtils.setIllegalServerStateError(response,
                                        "Permission READ_EXTERNAL_STORAGE not granted.");
                                getContext().sendBroadcast(response);
                            }
                        });

                return false;
            }
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
            PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            onGetMediaInternal(request, response, serviceId, mediaId);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalServerStateError(response,
                                    "Permission READ_EXTERNAL_STORAGE not granted.");
                            getContext().sendBroadcast(response);
                        }
                    });
            return false;
        }
        return true;
    }

    private void onGetMediaInternal(final Intent request, final Intent response, final String serviceId,
            final String mediaId) {
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
                getContext().sendBroadcast(response);
                return;
            }
            param = AUDIO_TABLE_KEYS;
            uriType = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            param = VIDEO_TABLE_KEYS;
            uriType = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        ContentResolver cresolver = getContext().getApplicationContext().getContentResolver();
        try {
            cursor = cresolver.query(uriType, param, filter, new String[] { fileName }, null);
            if (cursor.moveToFirst()) {
                loadMediaData(uriType, cursor, response);
            }
            setResult(response, DConnectMessage.RESULT_OK);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        getContext().sendBroadcast(response);
    }

    /**
     * cursorからMediaDataを読み込みBundleに格納して返却する.
     *
     * @param uriType メディアタイプ
     * @param cursor データが格納されているCursor
     * @param response response.
     */
    private void loadMediaData(final Uri uriType, final Cursor cursor, final Intent response) {
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

            setType(response, "Music");

            // Make creator
            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creator = new Bundle();
            setCreator(creator, mArtist);
            setRole(creator, "Artist");
            dataList.add((Bundle) creator.clone());
            setCreator(creator, mComp);
            setRole(creator, "Composer");
            dataList.add((Bundle) creator.clone());

            setCreators(response, dataList.toArray(new Bundle[dataList.size()]));
        } else {
            mId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
            mType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
            mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
            mDuration = (cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))) / UNIT_SEC;
            mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
            String mLang = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.LANGUAGE));
            setLanguage(response, mLang);

            setType(response, "Video");

            // Make creator
            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creatorVideo = new Bundle();
            setCreator(creatorVideo, mArtist);
            setRole(creatorVideo, "Artist");
            dataList.add((Bundle) creatorVideo.clone());
            setCreators(response, dataList.toArray(new Bundle[dataList.size()]));
        }
        setMediaId(response, mId);
        setMIMEType(response, mType);
        setTitle(response, mTitle);
        setDuration(response, mDuration);
        return;
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
            ContentResolver mContentResolver = getContext().getApplicationContext().getContentResolver();
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
    protected boolean onGetMediaList(final Intent request, final Intent response, final String serviceId,
            final String query, final String mimeType, final String[] orders, final Integer offset,
            final Integer limit) {
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
            return getMediaList(response, query, mimeType, orders, offset, limit);
        }
        return true;
    }

    /**
     * Get Media List.
     *
     * @param response Response
     * @param query Query
     * @param mimeType MIME Type
     * @param orders Order
     * @param offset Offset
     * @param limit Limit
     */
    private boolean getMediaList(final Intent response, final String query, final String mimeType,
            final String[] orders, final Integer offset, final Integer limit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            getMediaListInternal(response, query, mimeType, orders, offset, limit);
                            getContext().sendBroadcast(response);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalServerStateError(response,
                                    "READ_EXTERNAL_STORAGE permission not granted.");
                            getContext().sendBroadcast(response);
                        }
                    });
            return false;
        }
        getMediaListInternal(response, query, mimeType, orders, offset, limit);
        return true;
    }

    private void getMediaListInternal(final Intent response, final String query, final String mimeType,
            final String[] orders, final Integer offset, final Integer limit) {
        try {
            SortOrder mSort = SortOrder.TITLE_ASC;
            int counter = 0;
            if (limit != null) {
                if (limit < 0) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return;
                }
            }
            if (offset != null) {
                if (offset < 0) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return;
                }
            }

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
            if (orders != null) {
                if (orders.length == 2) {
                    mOrderBy = orders[0] + " " + orders[1];
                    mSort = getSortOrder(orders[0], orders[1]);
                } else {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return;
                }
            } else {
                mOrderBy = "title asc";
                mSort = SortOrder.TITLE_ASC;
            }

            // 音楽用のテーブルキー設定.
            mMusicParam = new String[] { MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.COMPOSER, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DATE_ADDED };
            mMusicUriType = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            // 動画用のテーブルキー設定.
            mVideoParam = new String[] { MediaStore.Video.Media.ALBUM, MediaStore.Video.Media.ARTIST,
                    MediaStore.Video.Media.LANGUAGE, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media._ID, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED };

            mVideoUriType = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            ContentResolver mContentResolver = this.getContext().getApplicationContext().getContentResolver();

            try {
                cursorMusic = mContentResolver.query(mMusicUriType, mMusicParam, mMusicFilter, null, mOrderBy);
                cursorMusic.moveToFirst();
            } catch (Exception e) {
                MessageUtils.setInvalidRequestParameterError(response);
                if (cursorMusic != null) {
                    cursorMusic.close();
                }
                return;
            }

            ArrayList<MediaList> mList = new ArrayList<MediaList>();
            if (cursorMusic.getCount() > 0) {
                counter = getMusicList(cursorMusic, mList);
            }

            try {
                cursorVideo = mContentResolver.query(mVideoUriType, mVideoParam, mVideoFilter, null, mOrderBy);
                cursorVideo.moveToFirst();
            } catch (Exception e) {
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
                counter = getVideoList(cursorVideo, mList);
            }

            List<Bundle> list = new ArrayList<Bundle>();
            counter = getMediaDataList(mList, list, offset, limit, mSort);
            setCount(response, counter);
            setMedia(response, list.toArray(new Bundle[list.size()]));
            setResult(response, DConnectMessage.RESULT_OK);
            cursorMusic.close();
            cursorVideo.close();
        } catch (Throwable throwable) {
            MessageUtils.setUnknownError(response, "Failed to get a media list.");
        }
    }

    /**
     * Get Music List.
     *
     * @param cursorMusic Cursor Music
     * @param list List
     * @return counter Music data count.
     */
    private int getMusicList(final Cursor cursorMusic, final ArrayList<MediaList> list) {
        int counter = 0;
        do {
            String mId = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media._ID));
            String mType = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
            String mTitle = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.TITLE));
            int mDuration = (cursorMusic.getInt(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                    / UNIT_SEC;
            String mArtist = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String mComp = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.COMPOSER));

            list.add(new MediaList(mId, mType, mTitle, mArtist, mDuration, mComp, null, false));
            counter++;
        } while (cursorMusic.moveToNext());
        return counter;
    }

    /**
     * Get Video List.
     *
     * @param cursorVideo Cursor Video
     * @param list List
     * @return counter Video data count.
     */
    private int getVideoList(final Cursor cursorVideo, final ArrayList<MediaList> list) {
        int counter = 0;
        do {
            String mLang = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.LANGUAGE));
            String mId = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media._ID));
            String mType = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
            String mTitle = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.TITLE));
            int mDuration = (cursorVideo.getInt(cursorVideo.getColumnIndex(MediaStore.Video.Media.DURATION)))
                    / UNIT_SEC;
            String mArtist = cursorVideo.getString(cursorVideo.getColumnIndex(MediaStore.Video.Media.ARTIST));

            list.add(new MediaList(mId, mType, mTitle, mArtist, mDuration, null, mLang, true));
            counter++;
        } while (cursorVideo.moveToNext());
        return counter;
    }

    /**
     * Get Media List.
     *
     * @param orglist original list.
     * @param medialist Media list.
     * @param offset Offset.
     * @param limit Limit.
     * @param sortflag Sort flag.
     * @return counter Video data count.
     */
    private int getMediaDataList(final ArrayList<MediaList> orglist, final List<Bundle> medialist, final Integer offset,
            final Integer limit, final SortOrder sortflag) {

        switch (sortflag) {
        case DURATION_ASC:
        case DURATION_DESC:
            Collections.sort(orglist, new MediaListDurationComparator());
            break;
        case ARTIST_ASC:
        case ARTIST_DESC:
            Collections.sort(orglist, new MediaListArtistComparator());
            break;
        case MIME_ASC:
        case MIME_DESC:
            Collections.sort(orglist, new MediaListTypeComparator());
            break;
        case ID_ASC:
        case ID_DESC:
            Collections.sort(orglist, new MediaListIdComparator());
            break;
        case COMPOSER_ASC:
        case COMPOSER_DESC:
            Collections.sort(orglist, new MediaListComposerComparator());
            break;
        case LANGUAGE_ASC:
        case LANGUAGE_DESC:
            Collections.sort(orglist, new MediaListLanguageComparator());
            break;
        case TITLE_ASC:
        case TITLE_DESC:
        default:
            Collections.sort(orglist, new MediaListTitleComparator());
            break;
        }

        switch (sortflag) {
        case TITLE_DESC:
        case DURATION_DESC:
        case ARTIST_DESC:
        case MIME_DESC:
        case ID_DESC:
        case COMPOSER_DESC:
        case LANGUAGE_DESC:
            Collections.reverse(orglist);
            break;
        default:
            break;
        }

        int mOffset = 0;
        if (offset == null) {
            mOffset = 0;
        } else {
            mOffset = offset;
        }

        int mLimit = 0;
        if (limit == null) {
            mLimit = orglist.size();
        } else {
            mLimit = limit + mOffset;
        }

        for (int i = mOffset; i < mLimit; i++) {
            Bundle medium = new Bundle();
            String mComp = null;

            String mId = orglist.get(i).getId();
            String mType = orglist.get(i).getType();
            String mTitle = orglist.get(i).getTitle();
            String mArtist = orglist.get(i).getArtist();
            int mDuration = orglist.get(i).getDuration();

            setMediaId(medium, mId);
            setMIMEType(medium, mType);
            setTitle(medium, mTitle);
            setDuration(medium, mDuration);

            if (orglist.get(i).isVideo()) {
                String mLang = orglist.get(i).getLanguage();
                setType(medium, "Video");
                setLanguage(medium, mLang);
            } else {
                mComp = orglist.get(i).getComposer();
                setType(medium, "Music");
            }

            List<Bundle> dataList = new ArrayList<Bundle>();
            Bundle creator = new Bundle();
            setCreator(creator, mArtist);
            setRole(creator, "Artist");
            dataList.add((Bundle) creator.clone());

            if (!(orglist.get(i).isVideo())) {
                setCreator(creator, mComp);
                setRole(creator, "Composer");
                dataList.add((Bundle) creator.clone());
            }
            setCreators(medium, dataList.toArray(new Bundle[dataList.size()]));
            medialist.add(medium);
        }

        return orglist.size();
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
    protected boolean onPutSeek(final Intent request, final Intent response, final String serviceId,
            final Integer pos) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (pos == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        } else if (0 > pos) {
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
        String[] mParam = { BaseColumns._ID };
        String[] mArgs = new String[] { path };
        Cursor mAudioCursor = null;

        // Audio
        Uri mAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String mFilter = MediaStore.Audio.AudioColumns.DATA + " LIKE ?";

        // Search Contents Provider
        ContentResolver mAudioContentsProvider = context.getContentResolver();
        try {
            mAudioCursor = mAudioContentsProvider.query(mAudioUri, mParam, mFilter, mArgs, null);
            if (mAudioCursor.moveToFirst()) {
                int mIdField = mAudioCursor.getColumnIndex(mParam[0]);
                id = mAudioCursor.getLong(mIdField);
            }
            mAudioCursor.close();

        } catch (NullPointerException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            if (mAudioCursor != null) {
                mAudioCursor.close();
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            if (mAudioCursor != null) {
                mAudioCursor.close();
            }
            return -1;
        }

        // Search video
        if (id == 0) {
            Cursor mVideoCursor = null;

            Uri mViodeUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            mFilter = MediaStore.Video.VideoColumns.DATA + " LIKE ?";

            // Search Contents Provider
            ContentResolver mVideoContentsProvider = context.getContentResolver();
            try {
                mVideoCursor = mVideoContentsProvider.query(mViodeUri, mParam, mFilter, mArgs, null);

                if (mVideoCursor.moveToFirst()) {
                    int mIdField = mVideoCursor.getColumnIndex(mParam[0]);
                    id = mVideoCursor.getLong(mIdField);
                } else {
                    id = -1;
                }
                mVideoCursor.close();

            } catch (NullPointerException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                if (mVideoCursor != null) {
                    mVideoCursor.close();
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                if (mVideoCursor != null) {
                    mVideoCursor.close();
                }
                return -1;
            }

        }

        return id;
    }

    /**
     * Media list class.
     */
    public class MediaList {
        /** ID. */
        private String mId;
        /** Mime Type. */
        private String mType;
        /** Title. */
        private String mTitle;
        /** Artist. */
        private String mArtist;
        /** Duration. */
        private int mDuration;
        /** Composer(Audio only). */
        private String mComposer;
        /** Language(Video only). */
        private String mLanguage;
        /** Video flag. */
        private boolean mIsVideo;

        /**
         * Constructor.
         *
         * @param id Id.
         * @param type Mime type.
         * @param title Title.
         * @param artist Artist.
         * @param duration Duration.
         * @param composer Composer(Audio only).
         * @param language Language(Video only).
         * @param isvideo Video flag.
         */
        public MediaList(final String id, final String type, final String title, final String artist,
                final int duration, final String composer, final String language, final boolean isvideo) {
            this.setId(id);
            this.setType(type);
            this.setTitle(title);
            this.setArtist(artist);
            this.setDuration(duration);
            this.setComposer(composer);
            this.setLanguage(language);
            this.setVideo(isvideo);
        }

        /**
         * Get Id.
         *
         * @return Id.
         */
        public String getId() {
            return mId;
        }

        /**
         * Set Id.
         *
         * @param id Id.
         */
        public void setId(final String id) {
            this.mId = id;
        }

        /**
         * Get mime type.
         *
         * @return Mime type.
         */
        public String getType() {
            return mType;
        }

        /**
         * Set mime type.
         *
         * @param type mime type.
         */
        public void setType(final String type) {
            this.mType = type;
        }

        /**
         * Get title.
         *
         * @return Title.
         */
        public String getTitle() {
            return mTitle;
        }

        /**
         * Set title.
         *
         * @param title Title.
         */
        public void setTitle(final String title) {
            this.mTitle = title;
        }

        /**
         * Get artist.
         *
         * @return Artist.
         */
        public String getArtist() {
            return mArtist;
        }

        /**
         * Set artist.
         *
         * @param artist Artist.
         */
        public void setArtist(final String artist) {
            this.mArtist = artist;
        }

        /**
         * Get duration.
         *
         * @return Duration.
         */
        public int getDuration() {
            return mDuration;
        }

        /**
         * Set duration.
         *
         * @param duration Duration.
         */
        public void setDuration(final int duration) {
            this.mDuration = duration;
        }

        /**
         * Get composer.
         *
         * @return Composer.
         */
        public String getComposer() {
            return mComposer;
        }

        /**
         * Set composer.
         *
         * @param composer Composer.
         */
        public void setComposer(final String composer) {
            this.mComposer = composer;
        }

        /**
         * Get language.
         *
         * @return Language.
         */
        public String getLanguage() {
            return mLanguage;
        }

        /**
         * Set language.
         *
         * @param language Language.
         */
        public void setLanguage(final String language) {
            this.mLanguage = language;
        }

        /**
         * Get Video flag.
         *
         * @return the mIsVideo.
         */
        public boolean isVideo() {
            return mIsVideo;
        }

        /**
         * Set video flag.
         *
         * @param isvideo the isVideo to set.
         */
        public void setVideo(final boolean isvideo) {
            this.mIsVideo = isvideo;
        }
    }

    /**
     * Duration sorting comparator.
     */
    public class MediaListDurationComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            int mData1 = lhs.getDuration();
            int mData2 = rhs.getDuration();

            if (mData1 > mData2) {
                return 1;
            } else if (mData1 == mData2) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * Title sorting comparator.
     */
    public class MediaListTitleComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            return compareData(lhs.getTitle(), rhs.getTitle());
        }
    }

    /**
     * Artist sorting comparator.
     */
    public class MediaListArtistComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            return compareData(lhs.getArtist(), rhs.getArtist());
        }
    }

    /**
     * Composer sorting comparator.
     */
    public class MediaListComposerComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            return compareData(lhs.getComposer(), rhs.getComposer());
        }
    }

    /**
     * Language sorting comparator.
     */
    public class MediaListLanguageComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            return compareData(lhs.getLanguage(), rhs.getLanguage());
        }
    }

    /**
     * ID sorting comparator.
     */
    public class MediaListIdComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            return compareData(lhs.getId(), rhs.getId());
        }
    }

    /**
     * Type sorting comparator.
     */
    public class MediaListTypeComparator implements Comparator<MediaList> {

        @Override
        public int compare(final MediaList lhs, final MediaList rhs) {
            return compareData(lhs.getType(), rhs.getType());
        }
    }

    /**
     * Data compare.
     *
     * @param data1 Data1.
     * @param data2 Data2.
     * @return result.
     */
    public int compareData(final String data1, final String data2) {
        if (data1 == null && data2 == null) {
            return 0;
        } else if (data1 != null && data2 == null) {
            return 1;
        } else if (data1 == null && data2 != null) {
            return -1;
        }

        if (data1.compareTo(data2) > 0) {
            return 1;
        } else if (data1.compareTo(data2) == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Get sort order.
     *
     * @param order1 sort column.
     * @param order2 asc / desc.
     * @return SortOrder flag.
     */
    public SortOrder getSortOrder(final String order1, final String order2) {
        if (order1.compareToIgnoreCase("id") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.ID_DESC;
        } else if (order1.compareToIgnoreCase("id") == 0 && order2.compareToIgnoreCase("asc") == 0) {
            return SortOrder.ID_ASC;
        } else if (order1.compareToIgnoreCase("duration") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.DURATION_DESC;
        } else if (order1.compareToIgnoreCase("duration") == 0 && order2.compareToIgnoreCase("asc") == 0) {
            return SortOrder.DURATION_ASC;
        } else if (order1.compareToIgnoreCase("artist") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.ARTIST_DESC;
        } else if (order1.compareToIgnoreCase("artist") == 0 && order2.compareToIgnoreCase("asc") == 0) {
            return SortOrder.ARTIST_ASC;
        } else if (order1.compareToIgnoreCase("composer") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.COMPOSER_DESC;
        } else if (order1.compareToIgnoreCase("composer") == 0 && order2.compareToIgnoreCase("asc") == 0) {
            return SortOrder.COMPOSER_ASC;
        } else if (order1.compareToIgnoreCase("language") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.LANGUAGE_DESC;
        } else if (order1.compareToIgnoreCase("language") == 0 && order2.compareToIgnoreCase("asc") == 0) {
            return SortOrder.LANGUAGE_ASC;
        } else if (order1.compareToIgnoreCase("mime") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.MIME_DESC;
        } else if (order1.compareToIgnoreCase("mime") == 0 && order2.compareToIgnoreCase("asc") == 0) {
            return SortOrder.MIME_ASC;
        } else if (order1.compareToIgnoreCase("title") == 0 && order2.compareToIgnoreCase("desc") == 0) {
            return SortOrder.TITLE_DESC;
        } else {
            return SortOrder.TITLE_ASC;
        }
    }
}
