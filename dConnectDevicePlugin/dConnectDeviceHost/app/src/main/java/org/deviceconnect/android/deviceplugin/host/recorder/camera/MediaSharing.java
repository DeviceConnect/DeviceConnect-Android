package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.deviceconnect.android.deviceplugin.host.BuildConfig.DEBUG;

/**
 * メディア共有ロジック.
 */
abstract class MediaSharing {

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * 指定された静止画ファイルを端末内の他アプリと共有する.
     *
     * @param context コンテキスト
     * @param photoFile 静止画ファイル
     * @return 発行されたURI
     */
    public abstract Uri sharePhoto(final @NonNull Context context,
                                   final @NonNull File photoFile);

    /**
     * 指定された動画ファイルを端末内の他アプリと共有する.
     *
     * @param context コンテキスト
     * @param videoFile 動画ファイル
     * @param fileManager ファイルマネージャ
     * @return 発行されたURI
     */
    public abstract Uri shareVideo(final @NonNull Context context,
                                   final @NonNull File videoFile,
                                   final @NonNull FileManager fileManager);

    /**
     * 動作環境に合わせたメディア共有ロジックを取得する.
     * @return メディア共有ロジックのインスタンス
     */
    public static MediaSharing getInstance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new MediaSharingForApi29();
        } else {
            return new LegacyMediaSharing();
        }
    }

    private static abstract class BaseMediaSharing extends MediaSharing {

        boolean checkMediaFile(final @NonNull File file) {
            return file.exists() && file.length() > 0;
        }
    }

    @SuppressWarnings("deprecation")
    private static class LegacyMediaSharing extends BaseMediaSharing {

        @Override
        public Uri sharePhoto(final @NonNull Context context, final @NonNull File photoFile) {
            if (checkMediaFile(photoFile)) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, photoFile.getName());
                values.put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.getName());
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DATA, photoFile.toString());
                return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
            return null;
        }

        @Override
        public Uri shareVideo(final @NonNull Context context,
                              final @NonNull File videoFile,
                              final @NonNull FileManager fileManager) {
            if (checkMediaFile(videoFile)) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.TITLE, videoFile.getName());
                values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.getName());
                values.put(MediaStore.Video.Media.ARTIST, "DeviceConnect");
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/avc");
                values.put(MediaStore.Video.Media.DATA, videoFile.toString());
                Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

                // 動画IDをサムネイルDBに挿入.
                try {
                    if (uri != null) {
                        String id = uri.getLastPathSegment();
                        if (id != null) {
                            long videoId = Long.parseLong(id);
                            long thumbnailId = registerVideoThumbnail(context, videoFile, videoId, fileManager);
                            boolean updated = updateThumbnailInfo(context, thumbnailId, videoId);
                            if (updated) {
                                if (DEBUG) {
                                    Log.d(TAG, "Updated videoID on thumbnail info: videoId="
                                            + videoId + ", thumbnailId=" + thumbnailId);
                                }
                            } else {
                                Log.w(TAG, "Failed to update videoID on thumbnail info: videoId="
                                        + videoId + ", thumbnailId=" + thumbnailId);
                            }

                        }
                        return uri;
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Failed to parse videoID as long type: video URI=" + uri, e);
                }
            }
            return null;
        }

        long registerVideoThumbnail(final @NonNull Context context,
                                    final @NonNull File videoFile,
                                    final long videoId,
                                    final @NonNull FileManager fileManager) {
            String videoFilePath = videoFile.getAbsolutePath();
            final int kind = MediaStore.Images.Thumbnails.MINI_KIND;
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath, kind);
            if (thumbnail == null) {
                return -1;
            }

            ByteArrayOutputStream data = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, data);
            String fileName = videoFile.getName() + ".jpg";

            try {
                String thumbnailFilePath = fileManager.saveFile(fileName, data.toByteArray());
                if (DEBUG) {
                    Log.d(TAG, "Stored thumbnail file: path=" + thumbnailFilePath);
                }
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Thumbnails.DATA, videoFilePath);
                values.put(MediaStore.Video.Thumbnails.WIDTH, thumbnail.getWidth());
                values.put(MediaStore.Video.Thumbnails.HEIGHT, thumbnail.getHeight());
                values.put(MediaStore.Video.Thumbnails.KIND, kind);
                values.put(MediaStore.Video.Media.MIME_TYPE, "image/jpeg");

                values.put(MediaStore.Video.Thumbnails.VIDEO_ID, videoId);
                ContentResolver resolver = context.getApplicationContext().getContentResolver();
                Uri uri = resolver.insert(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to register video thumbnail on content provider: videoFilePath=" + videoFilePath);
                    }
                    return -1;
                }
                if (DEBUG) {
                    Log.d(TAG, "Registered video thumbnail: uri=" + uri.toString());
                }
                String id = uri.getLastPathSegment();
                if (id == null) {
                    if (DEBUG) {
                        Log.e(TAG, "Thumbnail ID is not found in URI: " + uri);
                    }
                    return -1;
                }
                return Long.parseLong(id);
            } catch (IOException | NumberFormatException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to parse thumbnail ID as long type: videoFilePath=" + videoFilePath);
                }
                return -1;
            } finally {
                thumbnail.recycle();
            }
        }

        boolean updateThumbnailInfo(final @NonNull Context context,
                                    final long thumbnailId,
                                    final long videoId) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Thumbnails.VIDEO_ID, videoId);
            return updateThumbnailInfo(context, thumbnailId, values);
        }

        boolean updateThumbnailInfo(final @NonNull Context context,
                                    final long thumbnailId,
                                    final ContentValues values) {
            ContentResolver resolver = context.getApplicationContext().getContentResolver();
            Uri uri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
            String where = MediaStore.Video.Thumbnails._ID + " =?";
            String[] args = { Long.toString(thumbnailId) };
            int result = resolver.update(uri, values, where, args);
            return result == 1;
        }
    }

    @TargetApi(29)
    private static class MediaSharingForApi29 extends BaseMediaSharing {

        @Override
        public Uri sharePhoto(final @NonNull Context context,
                              final @NonNull File photoFile) {
            if (!checkMediaFile(photoFile)) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, photoFile.getName());
                values.put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.getName());
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    return null;
                }

                try (InputStream in = new FileInputStream(photoFile);
                     OutputStream out = resolver.openOutputStream(uri))
                {
                    if (out == null) {
                        return null;
                    }
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException(e);
                } catch (IOException e) {
                    return null;
                }

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
                return uri;
            }
            return null;
        }

        @Override
        public Uri shareVideo(final @NonNull Context context,
                              final @NonNull File videoFile,
                              final @NonNull FileManager fileManager) {
            // TODO 動画ファイルの共有処理
            return null;
        }
    }
}
