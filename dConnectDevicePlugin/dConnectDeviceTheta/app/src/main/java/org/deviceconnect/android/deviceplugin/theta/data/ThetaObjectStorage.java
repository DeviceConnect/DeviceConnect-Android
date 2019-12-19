/*
 ThetaObjectStorage
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.BaseColumns;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.MediaSharing;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Save the data of Theta in Storage of Android.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaObjectStorage {
    /** THETA Device Plug-in apk limit size. */
    private static final int LIMIT_APK_SIZE = 100;

    /**
     * DB Name.
     */
    private static final String DB_NAME = "theta_object_storage.db";

    /**
     * DB Version.
     */
    private static final int DB_VERSION = 1;

    /**
     * Gallery Table.
     */
    private static final String THETA_GALLERY_TBL_NAME = "theta_gallery_tbl";

    /** File Name. */
    private static final String THETA_FILE_NAME = "file_name";

    /** Date Time. */
    private static final String THETA_DATE_TIME = "date_time";

    /** width. */
    private static final String THETA_WIDTH = "width";

    /** height. */
    private static final String THETA_HEIGHT = "height";

    /** MimeType. */
    private static final String THETA_MIME_TYPE = "mime_type";

    /** Thumbnail URL. */
    private static final String THETA_THUMB_URL = "thumb_url";

    /** Main Data URL. */
    private static final String THETA_MAIN_URL = "main_url";

    /** Theta's Image mimeType. */
    private static final String MIMETYPE_IMAGE = "image/jpeg";

    /** Theta's Movie mimeType. */
    private static final String MIMETYPE_VIDEO = "video/mpeg";

    /**
     * DB Helper.
     */
    private ThetaDBHelper mThetaDBHelper;

    /** Context. */
    private Context mContext;

    /** Listener. */
    private Listener mListener;

    /** File Manager. */
    private final FileManager mFileManager;

    /** Media Sharing Logic. */
    private final MediaSharing mMediaSharing = MediaSharing.getInstance();

    /**
     * Logger.
     */
    private static final Logger sLogger = Logger.getLogger("theta.sampleapp");

    /** Storage's Listener. */
    public interface Listener {
        void onCompleted(final DBMode mode, final long result);
    }

    /** DBMode. */
    public enum DBMode {
        /** Add. */
        Add,
        /** Update. */
        Update,
        /** Delete. */
        Delete
    }

    /**
     * Constructor.
     */
    public ThetaObjectStorage(final Context context) {
        mContext = context;
        mFileManager = new FileManager(context);
        mThetaDBHelper = new ThetaDBHelper(context);
    }


    /**
     * Set Theta Object Storage Listener.
     * @param l Listener
     */
    public void setListener(final Listener l) {
        mListener = l;
    }


    /**
     * Add Theta Object's Cache.
     * @param object Theta Object
     */
    public synchronized void addThetaObjectCache(final ThetaObject object) {
        ContentValues values;
        try {
            values = makeContentValue(object);
        } catch (IOException e) {
            e.printStackTrace();
            sLogger.severe("Failed to store theta object: name = " + object.getFileName() + ", error = " + e.getMessage());
            if (mListener != null) {
                mListener.onCompleted(DBMode.Add, -1);
            }
            return;
        }

        if (!object.isFetched(ThetaObject.DataType.MAIN)
                || !object.isFetched(ThetaObject.DataType.THUMBNAIL)) {
            sLogger.severe("theta object not fetched: name = " + object.getFileName());
            if (mListener != null) {
                mListener.onCompleted(DBMode.Add, -1);
            }
            return;
        }
        SQLiteDatabase db = mThetaDBHelper.getWritableDatabase();
        long result = -1;
        try {
            result = db.insert(THETA_GALLERY_TBL_NAME, null, values);
        } finally {
            db.close();
            if (mListener != null) {
                mListener.onCompleted(DBMode.Add, result);
            }
        }
    }

    /**
     * Update Theta Object Cache.
     * @param object Theta Object
     */
    public  synchronized void updateThetaObjectCache(final ThetaObject object) {
        ContentValues values;
        try {
            values = makeContentValue(object);
        } catch (IOException e) {
            if (mListener != null) {
                mListener.onCompleted(DBMode.Update, -1);
            }
            return;
        }

        String whereClause = THETA_FILE_NAME + "=?";
        String[] whereArgs = {
                object.getFileName()
        };

        SQLiteDatabase db = mThetaDBHelper.getWritableDatabase();
        long result = -1;
        try {
            result = db.update(THETA_GALLERY_TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
            if (mListener != null) {
                mListener.onCompleted(DBMode.Update, result);
            }
        }
    }


    /**
     * Remove Theta Object Cache.
     * @param object Theta Object
     * @return Success or failure
     */
    public  synchronized void removeThetaObjectCache(final ThetaObject object) {
        List<ThetaObject> removeObj = geThetaObjectCaches(object.getFileName());
        ThetaObjectDB obj = (ThetaObjectDB) removeObj.get(0);
        if (obj.getThumbnailURL() != null) {
            File thumbFile = new File(obj.getThumbnailURL());
            thumbFile.delete();
        }
        if (obj.getMainURL() != null) {
            File dataFile = new File(obj.getMainURL());
            dataFile.delete();
        }
        String whereClause = THETA_FILE_NAME + "=?";
        String[] whereArgs = {
                object.getFileName()
        };
        SQLiteDatabase db = mThetaDBHelper.getWritableDatabase();
        int isDeleteCache = -1;
        try {
            isDeleteCache = db.delete(THETA_GALLERY_TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
            if (mListener != null) {
                mListener.onCompleted(DBMode.Delete, isDeleteCache);
            }
        }
    }
    /**
     * Get ThetaObject Caches.
     * @return ThetaObjectList
     */
    public synchronized List<ThetaObject> geThetaObjectCaches(final String name) {
        String sql = "SELECT * FROM " + THETA_GALLERY_TBL_NAME;
        if (name != null) {
            sql += " WHERE " + THETA_FILE_NAME + "='" + name + "' ";
        }

        sql += " ORDER BY " + THETA_DATE_TIME + " DESC;";

        String[] selectionArgs = {};
        SQLiteDatabase db = null;
        List<ThetaObject> objects = new ArrayList<ThetaObject>();
        try {
            db = mThetaDBHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql, selectionArgs);

            boolean next = cursor.moveToFirst();
            while (next) {
                String fileName = cursor.getString(cursor.getColumnIndex(THETA_FILE_NAME));
                String dateTime = cursor.getString(cursor.getColumnIndex(THETA_DATE_TIME));
                int width = cursor.getInt(cursor.getColumnIndex(THETA_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(THETA_HEIGHT));
                String mimeType = cursor.getString(cursor.getColumnIndex(THETA_MIME_TYPE));
                String thumbURL = cursor.getString(cursor.getColumnIndex(THETA_THUMB_URL));
                String mainURL = cursor.getString(cursor.getColumnIndex(THETA_MAIN_URL));
                ThetaObject object = new ThetaObjectDB(fileName, dateTime, width, height,
                        mimeType, thumbURL, mainURL);
                objects.add(object);
                next = cursor.moveToNext();
            }
        } finally {
            db.close();
        }
        return objects;
    }

    /**
     * THETA Data's index.
     * @param name search data name
     * @return index
     */
    public synchronized int getThetaObjectCachesIndex(final String name) {
        List<ThetaObject> objects = geThetaObjectCaches(name);
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getFileName().equals(name)) {
                return i;
            }
        }
        return -1;
    }
    /** Make Content Value. */
    private ContentValues makeContentValue(final ThetaObject object) throws IOException {
        ContentValues values = new ContentValues();
        values.put(THETA_FILE_NAME, object.getFileName());
        values.put(THETA_DATE_TIME, object.getCreationTime());
        values.put(THETA_WIDTH, object.getWidth());
        values.put(THETA_HEIGHT, object.getHeight());
        values.put(THETA_MIME_TYPE, object.getMimeType());
        if (object.isImage()) {
            // check thumb url
            byte[] thumbImage = object.getThumbnailData();
            if (thumbImage == null) {
                try {
                    object.fetch(ThetaObject.DataType.THUMBNAIL);
                    thumbImage = object.getThumbnailData();
                } catch (ThetaDeviceException e) {
                    e.printStackTrace();
                }
            }
            if (thumbImage != null) {
                values.put(THETA_THUMB_URL, saveThetaImage("thumbnails",
                        object.getFileName(),
                        thumbImage));
            }

            // check main data url
            byte[] dataImage = object.getMainData();
            if (dataImage == null) {
                try {
                    object.fetch(ThetaObject.DataType.MAIN);
                    dataImage = object.getMainData();
                } catch (ThetaDeviceException e) {
                    e.printStackTrace();
                }
            }
            if (dataImage != null) {
                String imagePath = saveThetaImage("images",
                        object.getFileName(),
                        dataImage);
                values.put(THETA_MAIN_URL, imagePath);

                mMediaSharing.sharePhoto(mContext, new File(imagePath));
            }
        }
        return values;
    }


    // save theta's image
    @SuppressWarnings("deprecation")
    private String saveThetaImage(final String cacheDirName,
                                  final String originalFileName,
                                  final byte[] thetaImage) throws IOException {
        File cacheDir = new File(mFileManager.getBasePath(), cacheDirName);
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new IOException("Failed to create directory: path = " + cacheDir.getAbsolutePath());
            }
        }

        Date date = new Date();
        SimpleDateFormat fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final String fileName =  originalFileName + "." + fileDate.format(date);

        mFileManager.saveFile(cacheDirName + "/" + fileName, thetaImage);
        return new File(cacheDir, fileName).getAbsolutePath();
    }

    // load theta's image data.
    private  byte[] loadThetaImage(final String filePath) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        ContentResolver contentResolver = mContext.getContentResolver();
        InputStream in = null;
        int read;
        try {
            in = contentResolver.openInputStream(Uri.parse("file://" + filePath));
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new IOException("Failed to load a file." + filePath);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return out.toByteArray();
        }
    }


    /** Theta Object Storage Model. */
    private class ThetaObjectDB implements ThetaObject {

        private final String mFilename;

        private final String mDateTime;

        private final int mWidth;

        private final int mHeight;

        private String mMimeType;

        private byte[] mThumbnail;

        private byte[] mMain;

        private String mThumbURL;

        private String mMainURL;


        ThetaObjectDB(final String filename,
                             final String date,
                             final int width,
                             final int height,
                             final String mimeType,
                             final String thumbURL,
                             final String mainURL) {
            mFilename = filename;
            mWidth = width;
            mHeight = height;
            mMimeType = mimeType;
            mDateTime = date;
            mThumbURL = thumbURL;
            mMainURL = mainURL;
        }


        @Override
        public void fetch(final DataType type) {
            switch (type) {
                case THUMBNAIL:
                    mThumbnail = loadThetaImage(mThumbURL);
                    break;
                case MAIN:
                    mMain = loadThetaImage(mMainURL);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

        }

        @Override
        public boolean isFetched(final DataType type) {
            switch (type) {
                case THUMBNAIL:
                    return mThumbnail != null;
                case MAIN:
                    return mMain != null;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public void remove() {
            removeThetaObjectCache(this);
        }

        @Override
        public void clear(final DataType type) {
            switch (type) {
                case THUMBNAIL:
                    mThumbnail = null;
                    break;
                case MAIN:
                    mMain = null;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public String getMimeType() {
            return mMimeType;
        }

        @Override
        public Boolean isImage() {
            return (mMimeType.equals(MIMETYPE_IMAGE));
        }

        @Override
        public String getCreationTime() {
            return mDateTime;
        }

        @Override
        public long getCreationTimeWithUnixTime() {
            return -1;  // Unsupported method.
        }

        @Override
        public String getFileName() {
            return mFilename;
        }

        @Override
        public Integer getWidth() {
            return mWidth;
        }

        @Override
        public Integer getHeight() {
            return mHeight;
        }

        @Override
        public byte[] getThumbnailData() {
            return mThumbnail;
        }

        @Override
        public byte[] getMainData() {
            return mMain;
        }

        public String getThumbnailURL() {
            return mThumbURL;
        }

        public String getMainURL() {
            return mMainURL;
        }
    }
    /**
     * DB Helper to store the Theta storage
     */
    private class ThetaDBHelper extends SQLiteOpenHelper {

        /**
         * Constructor.
         * param context application context
         */
        public ThetaDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + THETA_GALLERY_TBL_NAME);
            createDB(db);
        }

        /**
         * DB to store the Theta storage
         * param db DB
         */
        private void createDB(final SQLiteDatabase db) {
            String thetaStorageSQL = "CREATE TABLE " + THETA_GALLERY_TBL_NAME + " ("
                                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + THETA_FILE_NAME + " TEXT NOT NULL,"
                                + THETA_DATE_TIME + " TEXT NOT NULL,"
                                + THETA_WIDTH + " INTEGER,"
                                + THETA_HEIGHT +  " INTEGER,"
                                + THETA_MIME_TYPE + " TEXT NOT NULL,"
                                + THETA_THUMB_URL + " TEXT,"
                                + THETA_MAIN_URL + " TEXT"
                    + ");";
            db.execSQL(thetaStorageSQL);
        }
    }

    /**
     * Check Android Storage size.
     * @return Return a false if true, otherwise there is a minimum required value or more free
     */
    public static boolean hasEnoughStorageSize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        float total = 1.0f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            total = stat.getTotalBytes();
        } else {
            total = (float) stat.getBlockSize() * stat.getAvailableBlocks();
        }
        int v = (int) (total / (1024.f * 1024.f));
        if(BuildConfig.DEBUG) {
            if(v < LIMIT_APK_SIZE) {
                sLogger.warning("hasEnoughStorageSize is less than " + LIMIT_APK_SIZE + ", rest size =" + v);
            }
        }
        return v >= LIMIT_APK_SIZE;
    }
}
