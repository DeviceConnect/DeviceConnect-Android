package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

import static org.deviceconnect.android.deviceplugin.host.BuildConfig.DEBUG;

@TargetApi(Build.VERSION_CODES.Q)
class MediaSharingForApi29 extends MediaSharing {
    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    @Override
    public Uri sharePhoto(final @NonNull Context context, final @NonNull File photoFile) {
        if (checkMediaFile(photoFile)) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, photoFile.getName());
            values.put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.getName());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Log.e(TAG, "Failed to share photo: not inserted to media store: path = " + photoFile.getAbsolutePath());
                return null;
            }

            try (InputStream in = new FileInputStream(photoFile);
                 OutputStream out = resolver.openOutputStream(uri))
            {
                if (out == null) {
                    Log.e(TAG, "Failed to share photo: no output stream: path = " + photoFile.getAbsolutePath());
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
                Log.e(TAG, "Failed to share photo: I/O error: path = " + photoFile.getAbsolutePath(), e);
                return null;
            }

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return uri;
        } else {
            Log.e(TAG, "Failed to share photo: file not found: path = " + photoFile.getAbsolutePath());
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
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
            Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return null;
            }

            try (InputStream in = new FileInputStream(videoFile);
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
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);

            try {
                Size size = new Size(100, 100);
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFile, size, new CancellationSignal());

                ByteArrayOutputStream data = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, data);
                String fileName = videoFile.getName() + ".jpg";
                String thumbnailFilePath = fileManager.saveFile(fileName, data.toByteArray());
                if (DEBUG) {
                    Log.d(TAG, "Stored thumbnail file: path=" + thumbnailFilePath);
                }
                return uri;
            } catch (IOException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to save video thumbnail: videoFilePath=" + videoFile.getAbsolutePath());
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Uri shareAudio(@NonNull Context context, @NonNull File audioFile) {
        if (checkMediaFile(audioFile)) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.TITLE, audioFile.getName());
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, audioFile.getName());
            values.put(MediaStore.Audio.Media.ARTIST, "DeviceConnect");
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac");
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
            Uri collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            Uri uri = resolver.insert(collection, values);
            if (uri == null) {
                Log.e(TAG, "Failed to share audio: not inserted to media store: path = " + audioFile.getAbsolutePath());
                return null;
            }

            try (InputStream in = new FileInputStream(audioFile);
                 OutputStream out = resolver.openOutputStream(uri))
            {
                if (out == null) {
                    Log.e(TAG, "Failed to share audio: no output stream: path = " + audioFile.getAbsolutePath());
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
                Log.e(TAG, "Failed to share audio: I/O error: path = " + audioFile.getAbsolutePath(), e);
                return null;
            }

            values.clear();
            values.put(MediaStore.Audio.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return uri;
        } else {
            Log.e(TAG, "Failed to share audio: file not found: path = " + audioFile.getAbsolutePath());
        }
        return null;
    }
}
