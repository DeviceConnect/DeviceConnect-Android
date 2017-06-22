/*
 HostFileProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * File Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostFileProfile extends FileProfile {

    /** Debug Tag. */
    private static final String TAG = "HOST";

    /**
     * 属性: {@value} .
     */
    private String ATTRIBUTE_DIRECTORY = "directory";

    /** FileManager. */
    private FileManager mFileManager;

    /** SimpleDataFormat. */
    private SimpleDateFormat mDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private ExecutorService mImageService = Executors.newSingleThreadExecutor();

    private final DConnectApi mGetReceiveApi = new GetApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String path = getPath(request);

            File mFile = null;
            String filePath = "";

            // パス名の先頭に"/"が含まれている場合
            if (path.indexOf("/") == 0) {
                mFile = new File(getFileManager().getBasePath() + path);
                filePath = getFileManager().getContentUri() + path;
            } else {
                mFile = new File(getFileManager().getBasePath() + "/" + path);
                filePath = getFileManager().getContentUri() + "/" + path;
            }

            if (mFile.isFile()) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
                response.putExtra(FileProfile.PARAM_MIME_TYPE, getMIMEType(path));
                response.putExtra(FileProfile.PARAM_URI, filePath);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "not found:" + path);
            }
            return true;
        }
    };

    private final DConnectApi mPutMoveApi = new PutApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String oldPath = request.getStringExtra("oldPath");
            final String newPath = request.getStringExtra("newPath");

            File oldFile, newFile;
            final String[] oldFilePath = new String[1];
            final String[] newFilePath = new String[1];

            if (oldPath.equalsIgnoreCase(newPath)) {
                MessageUtils.setInvalidRequestParameterError(response, "Same file:" + oldPath);
                return true;
            }
            // パス名の先頭に"/"が含まれている場合
            if (oldPath.indexOf("/") == 0) {
                oldFile = new File(getFileManager().getBasePath() + oldPath);
                oldFilePath[0] = getFileManager().getContentUri() + "/" + oldFile.getName();
            } else {
                oldFile = new File(getFileManager().getBasePath() + "/" + oldPath);
                oldFilePath[0] = getFileManager().getContentUri() + "/" + oldFile.getName();
            }
            if (newPath.indexOf("/") == 0) {
                newFile = new File(getFileManager().getBasePath() + newPath);
            } else {
                newFile = new File(getFileManager().getBasePath() + "/" + newPath);
            }
            File newDirectory = new File(newFile.getParent());
            if (!newDirectory.exists()) {
                MessageUtils.setInvalidRequestParameterError(response, "not found:" + newDirectory.getPath());
                return true;
            }
            if (getMIMEType(newFile.getPath()) == null) {
                newFilePath[0] = newPath + "/" + oldFile.getName();
            } else {
                newFilePath[0] = newPath;
            }

            if (oldFile.isFile()) {
                final boolean forceOverwrite = isForce(request, "forceOverwrite");
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = getContentData(oldFilePath[0]);
                        if (data != null) {
                            saveFile(response, newFilePath[0], getMIMEType(newPath), data, forceOverwrite, new OnSavedListener() {

                                @Override
                                public void onSavedListener() {
                                    removeFile(response, oldPath);
                                }
                            });
                            return;
                        } else {
                            MessageUtils.setInvalidRequestParameterError(response, "not found:" + oldPath);
                        }
                        sendResponse(response);
                    }
                });
                return false;
            } else if (getMIMEType(oldFile.getPath()) == null) {
                MessageUtils.setInvalidRequestParameterError(response, oldPath + " is directory.");
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "not found:" + oldPath);
            }

            return true;
        }
    };


    private final DConnectApi mGetListApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_LIST;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getFileList(request, response);
                }
            }).start();
            return false;
        }
    };
    private final DConnectApi mGetDirectoryApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DIRECTORY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getFileList(request, response);
                }
            }).start();
            return false;
        }
    };

    private final DConnectApi mPostSendApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String path = getPath(request);
            final String uri = getURI(request);
            final String mimeType = getMIMEType(request);
            final byte[] data = getData(request);
            final boolean forceOverwrite = isForce(request, "forceOverwrite");
            if (data == null) {
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        byte[] result;
                        try {
                           result = getData(uri);
                        } catch (OutOfMemoryError e) {
                            MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                            sendResponse(response);
                            return;
                        }
                        if (result == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "could not get image from uri.");
                            sendResponse(response);
                            return;
                        }
                        saveFile(response, path, mimeType, result, forceOverwrite, new OnSavedListener() {
                            @Override
                            public void onSavedListener() {
                                sendResponse(response);
                            }
                        });
                    }
                });
                return false;
            }

            saveFile(response, path, mimeType, data, forceOverwrite, new OnSavedListener() {
                @Override
                public void onSavedListener() {
                    sendResponse(response);
                }
            });
            return false;
        }
    };
    private DConnectApi mDeleteRemoveApi = new DeleteApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String path = getPath(request);
            removeFile(response, path);
            return false;
        }
    };



    private final DConnectApi mPostMkdirApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DIRECTORY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String path = getPath(request);
            getFileManager().checkWritePermission(new FileManager.CheckPermissionCallback() {
                @Override
                public void onSuccess() {
                    File mBaseDir = mFileManager.getBasePath();
                    File mMakeDir = new File(mBaseDir, path);

                    if (mMakeDir.isDirectory()) {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                        MessageUtils.setInvalidRequestParameterError(response,
                            "can not make dir, \"" + mMakeDir + "\" already exist.");
                    } else {
                        boolean isMakeDir = mMakeDir.mkdirs();
                        if (isMakeDir) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                            MessageUtils.setInvalidRequestParameterError(response, "can not make dir :" + mMakeDir);
                        }
                    }
                    sendResponse(response);
                }

                @Override
                public void onFail() {
                    MessageUtils.setIllegalServerStateError(response,
                        "Permission WRITE_EXTERNAL_STORAGE not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutMvDirApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DIRECTORY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getFileManager().checkWritePermission(new FileManager.CheckPermissionCallback() {
                @Override
                public void onSuccess() {
                    String oldPath = request.getStringExtra("oldPath");
                    String newPath = request.getStringExtra("newPath");
                    File baseDir = mFileManager.getBasePath();
                    File oldDir = new File(baseDir, oldPath);
                    File newDir = new File(baseDir, newPath);
                    File tempNewDir = new File(baseDir, newPath + "/" + oldDir.getName());

                    if (!oldDir.exists()) {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                        MessageUtils.setInvalidRequestParameterError(response,
                                "Not exists, \"" + oldDir + "\"");
                    } else if (!newDir.exists()) {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                            MessageUtils.setInvalidRequestParameterError(response,
                                    "Not exists, \"" + newDir + "\"");
                    } else if (tempNewDir.exists()) {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                        MessageUtils.setInvalidRequestParameterError(response,
                                "Already exists, \"" + tempNewDir + "\"");
                    } else if (!oldDir.isDirectory()) {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                            MessageUtils.setInvalidRequestParameterError(response,
                                    "Not directory, \"" + oldDir + "\"");
                    } else if (!newDir.isDirectory()) {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                        MessageUtils.setInvalidRequestParameterError(response,
                                "Not directory, \"" + newDir + "\"");
                    } else {
                        if (oldPath.equalsIgnoreCase(newPath)) {
                            MessageUtils.setInvalidRequestParameterError(response, "Same directory:" + oldPath);
                            sendResponse(response);
                            return;
                        }
                        boolean isMoveDir = oldDir.renameTo(tempNewDir);
                        if (isMoveDir) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                            MessageUtils.setInvalidRequestParameterError(response, "can not move dir :" + oldDir + " and " + newDir);
                        }
                    }
                    sendResponse(response);
                }

                @Override
                public void onFail() {
                    MessageUtils.setIllegalServerStateError(response,
                            "Permission WRITE_EXTERNAL_STORAGE not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mDeleteRmdirApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DIRECTORY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String path = getPath(request);
            final boolean forceRemove = isForce(request, "forceRemove");
            getFileManager().checkWritePermission(new FileManager.CheckPermissionCallback() {
                @Override
                public void onSuccess() {
                    File mBaseDir = mFileManager.getBasePath();
                    File mDeleteDir = new File(mBaseDir, path);

                    if (mDeleteDir.isFile()) {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                        MessageUtils.setInvalidRequestParameterError(response, mDeleteDir + "is file");
                    } else {
                        if (!forceRemove && existFileInDirectory(mDeleteDir)) {
                            setResult(response, DConnectMessage.RESULT_ERROR);
                            MessageUtils.setIllegalDeviceStateError(response, "Exist file in directory:" + mDeleteDir);
                        } else {
                            boolean isDelete = deleteDirectory(mDeleteDir);
                            if (isDelete) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                setResult(response, DConnectMessage.RESULT_ERROR);
                                MessageUtils.setIllegalDeviceStateError(response, "can not delete dir :" + mDeleteDir);
                            }
                        }
                    }
                    sendResponse(response);
                }

                @Override
                public void onFail() {
                    MessageUtils.setIllegalServerStateError(response,
                        "Permission WRITE_EXTERNAL_STORAGE not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    /**
     * ファイルの一覧を返す.
     * @param request Request Message.
     * @param response Response Message.
     */
    private void getFileList(final Intent request, final Intent response) {
        final String path = getPath(request);
        final String order = getOrder(request);
        final Integer limit = getLimit(request);
        final Integer offset = getOffset(request);

        File tmpDir = null;
        String mPath = null;
        Boolean currentTop = false;
        if (path == null) {
            // nullの時はTopに指定
            tmpDir = getFileManager().getBasePath();
            currentTop = true;
        } else if (path.equals("/")) {
            // /の場合はTopに指定
            tmpDir = getFileManager().getBasePath();
            currentTop = true;
        } else if (path.contains("..")) {
            // ..の場合は、1つ上のフォルダを指定
            String[] mDirs = path.split("/", 0);
            mPath = "/";
            int mCount = 0;
            if (mDirs[0].equals("")) {
                mCount = 1;
            }
            for (int i = mCount; i < mDirs.length - 2; i++) {
                mPath += mDirs[i] + "/";
            }
            if (mDirs.length == 1 || mPath.equals("/")) {
                currentTop = true;
            }
            tmpDir = new File(getFileManager().getBasePath(), mPath);
        } else {
            // それ以外は、そのフォルダを指定
            tmpDir = new File(getFileManager().getBasePath() + "/" + path);
            currentTop = false;
        }

        final File finalTmpDir = tmpDir;
        final Boolean finalCurrentTop = currentTop;
        final String finalMPath = mPath;
        getFileManager().checkReadPermission(new FileManager.CheckPermissionCallback() {
            @Override
            public void onSuccess() {
                File[] respFileList = finalTmpDir.listFiles();
                if (respFileList == null) {
                    setResult(response, DConnectMessage.RESULT_ERROR);
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Dir is not exist:" + finalTmpDir);
                    sendResponse(response);
                } else if (order != null && !order.endsWith("desc") && !order.endsWith("asc")) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    sendResponse(response);
                } else {
                    // Set arraylist from respFileList
                    ArrayList<FileAttribute> filelist = new ArrayList<FileAttribute>();
                    filelist = setArrayList(respFileList, filelist);

                    // Sort
                    filelist = sortFilelist(order, filelist);

                    List<Bundle> resp = new ArrayList<Bundle>();
                    Bundle respParam = new Bundle();

                    // ..のフォルダを追加(常時)
                    if (!finalCurrentTop) {
                        String tmpPath = path;
                        if (finalMPath != null) {
                            tmpPath = finalMPath;
                        }
                        File parentDir = new File(tmpPath + "/..");
                        String path = parentDir.getPath().replaceAll("" + mFileManager.getBasePath(), "");
                        String name = parentDir.getName();
                        Long size = parentDir.length();
                        String mineType = "folder/dir";
                        int filetype = 1;
                        String date = mDataFormat.format(parentDir.lastModified());
                        FileAttribute fa = new FileAttribute(path, name, mineType, filetype, size, date);
                        respParam = addResponseParamToArray(fa, respParam);
                        resp.add((Bundle) respParam.clone());
                    }

                    ArrayList<FileAttribute> tmpfilelist = new ArrayList<FileAttribute>();
                    if (order != null && order.endsWith("desc")) {
                        int last = filelist.size();
                        for (int i = last - 1; i >= 0; i--) {
                            tmpfilelist.add(filelist.get(i));
                        }
                        filelist = tmpfilelist;
                    }

                    int counter = 0;
                    int tmpLimit = 0;
                    int tmpOffset = 0;
                    if (limit != null) {
                        if (limit >= 0) {
                            tmpLimit = limit;
                        } else {
                            MessageUtils.setInvalidRequestParameterError(response);
                            sendResponse(response);
                            return;
                        }
                    } else {
                        if (request.getStringExtra(PARAM_LIMIT) != null) {
                            MessageUtils.setInvalidRequestParameterError(response);
                            sendResponse(response);
                            return;
                        }
                    }
                    if (offset != null) {
                        if (offset >= 0) {
                            tmpOffset = offset;
                        } else {
                            MessageUtils.setInvalidRequestParameterError(response);
                            sendResponse(response);
                            return;
                        }
                    } else {
                        if (request.getStringExtra(PARAM_OFFSET) != null) {
                            MessageUtils.setInvalidRequestParameterError(response);
                            sendResponse(response);
                            return;
                        }
                    }
                    if (tmpOffset > filelist.size()) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);
                        return;
                    }
                    int limitCounter = tmpLimit + tmpOffset;

                    for (FileAttribute fa : filelist) {
                        if (limit == null || (limit != null && limitCounter > counter)) {
                            respParam = addResponseParamToArray(fa, respParam);
                            if (offset == null || (offset != null && counter >= offset)) {
                                resp.add((Bundle) respParam.clone());
                            }
                        }
                        counter++;
                    }

                    // 結果を非同期で返信
                    setResult(response, IntentDConnectMessage.RESULT_OK);
                    response.putExtra(PARAM_COUNT, filelist.size());
                    response.putExtra(PARAM_FILES, resp.toArray(new Bundle[resp.size()]));
                    sendResponse(response);
                }
            }

            @Override
            public void onFail() {
                MessageUtils.setIllegalServerStateError(response,
                        "Permission READ_EXTERNAL_STORAGE not granted.");
                sendResponse(response);
            }
        });
    }
    // ファイルを保存する.
    private void saveFile(final Intent response, final String path, final String mimeType, final byte[] data, final boolean forceOverwrite,
                          final OnSavedListener l) {
        getFileManager().saveFile(path, data, forceOverwrite, new FileManager.SaveFileCallback() {
            @Override
            public void onSuccess(@NonNull final String uri) {
                String mMineType = mimeType;
                if (mMineType == null) {
                    mMineType = getMIMEType(getFileManager().getBasePath() + "/" + path);
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "mMineType:" + mMineType);
                }

                // MimeTypeが不明の場合はエラーを返す
                if (mMineType == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not support format");
                    setResult(response, DConnectMessage.RESULT_ERROR);
                    sendResponse(response);
                    return;
                }
                // 音楽データに関してはContents Providerに登録
                if (mMineType.endsWith("audio/mpeg") || mMineType.endsWith("audio/x-wav")
                        || mMineType.endsWith("audio/mp4") || mMineType.endsWith("audio/ogg")
                        || mMineType.endsWith("audio/mp3") || mMineType.endsWith("audio/x-ms-wma")) {

                    MediaMetadataRetriever mMediaMeta = new MediaMetadataRetriever();
                    mMediaMeta.setDataSource(getFileManager().getBasePath() + "/" + path);
                    String mTitle = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String mComposer = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
                    String mArtist = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String mDuration = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    ContentResolver mContentResolver = getContext().getApplicationContext().getContentResolver();
                    ContentValues mValues = new ContentValues();

                    if (mTitle == null) {
                        String[] array = path.split("/");
                        mTitle = array[array.length - 1];
                    }
                    mValues.put(MediaStore.Audio.Media.TITLE, mTitle);
                    mValues.put(MediaStore.Audio.Media.DISPLAY_NAME, mTitle);
                    mValues.put(MediaStore.Audio.Media.COMPOSER, mComposer);
                    mValues.put(MediaStore.Audio.Media.ARTIST, mArtist);
                    mValues.put(MediaStore.Audio.Media.DURATION, mDuration);
                    mValues.put(MediaStore.Audio.Media.MIME_TYPE, mMineType);
                    mValues.put(MediaStore.Audio.Media.DATA, getFileManager().getBasePath() + "/" + path);
                    mContentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mValues);
                } else if (mMineType.endsWith("video/mp4") || mMineType.endsWith("video/3gpp")
                        || mMineType.endsWith("video/3gpp2") || mMineType.endsWith("video/mpeg")
                        || mMineType.endsWith("video/m4v")) {
                    MediaMetadataRetriever mMediaMeta = new MediaMetadataRetriever();
                    mMediaMeta.setDataSource(getFileManager().getBasePath() + "/" + path);
                    String mTitle = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String mArtist = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String mDuration = mMediaMeta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    ContentResolver mContentResolver = getContext().getApplicationContext().getContentResolver();
                    ContentValues mValues = new ContentValues();

                    mValues.put(MediaStore.Video.Media.TITLE, mTitle);
                    mValues.put(MediaStore.Video.Media.DISPLAY_NAME, mTitle);
                    mValues.put(MediaStore.Video.Media.ARTIST, mArtist);
                    mValues.put(MediaStore.Video.Media.DURATION, mDuration);
                    mValues.put(MediaStore.Video.Media.MIME_TYPE, mMineType);
                    mValues.put(MediaStore.Video.Media.DATA, getFileManager().getBasePath() + "/" + path);
                    mContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mValues);
                }
                setResult(response, DConnectMessage.RESULT_OK);
                if (l != null) {
                    l.onSavedListener();
                }
            }

            @Override
            public void onFail(@NonNull final Throwable throwable) {
                setResult(response, DConnectMessage.RESULT_ERROR);
                MessageUtils.setInvalidRequestParameterError(response, throwable.getMessage());
                sendResponse(response);
            }
        });
    }

    // ファイルの削除を行う.
    private void removeFile(final Intent response, final String path) {
        getFileManager().removeFile(path, new FileManager.RemoveFileCallback() {
            @Override
            public void onSuccess() {
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }

            @Override
            public void onFail(@NonNull final Throwable throwable) {
                MessageUtils.setInvalidRequestParameterError(response, "Failed to remove file: " + path);
                sendResponse(response);
            }
        });
    }

    // ディレクトリ内部にファイルがあっても削除する.
    private boolean deleteDirectory(File f) {
        if (!f.exists()) {
            return false;
        }
        if (f.isFile()) {
            f.delete();
        } else if(f.isDirectory()){
            File[] files = f.listFiles();
            for(int i = 0; i < files.length; i++) {
                deleteDirectory(files[i]);
            }
            f.delete();
        }
        return true;
    }
    // ディレクトリ内に１つでもファイルが存在するか。true:存在する false:存在しない
    private boolean existFileInDirectory(final File directory) {
        return directory.listFiles() != null && directory.listFiles().length > 0;
    }

    // 強制フラグを取得する.
    private boolean isForce(final Intent request, final String key) {
        Boolean force = parseBoolean(request, key);
        if (force == null) {
            force = Boolean.FALSE;
        }
        return force;
    }

    /**
     * コンストラクタ.
     * 
     * @param fileMgr ファイル管理クラス.
     */
    public HostFileProfile(final FileManager fileMgr) {
        super(fileMgr);
        mFileManager = fileMgr;
        addApi(mGetReceiveApi);
        addApi(mPutMoveApi);
        addApi(mGetListApi);
        addApi(mGetDirectoryApi);
        addApi(mPostSendApi);
        addApi(mDeleteRemoveApi);
        addApi(mPostMkdirApi);
        addApi(mPutMvDirApi);
        addApi(mDeleteRmdirApi);
    }

    /**
     * Sort File list.
     * 
     * @param order Sort order.
     * @param filelist Sort filelist.
     * @return Sorted filelist.
     */
    protected ArrayList<FileAttribute> sortFilelist(final String order, final ArrayList<FileAttribute> filelist) {
        if (order != null) {
            if (order.startsWith(PARAM_PATH)) {
                Collections.sort(filelist, new Comparator<FileAttribute>() {
                    public int compare(final FileAttribute fa1, final FileAttribute fa2) {
                        return fa1.getPath().compareTo(fa2.getPath());
                    }
                });
            } else if (order.startsWith(PARAM_FILE_NAME)) {
                Collections.sort(filelist, new Comparator<FileAttribute>() {
                    public int compare(final FileAttribute fa1, final FileAttribute fa2) {
                        return fa1.getName().compareTo(fa2.getName());
                    }
                });
            } else if (order.startsWith(PARAM_MIME_TYPE)) {
                Collections.sort(filelist, new Comparator<FileAttribute>() {
                    public int compare(final FileAttribute fa1, final FileAttribute fa2) {
                        return fa1.getMimeType().compareTo(fa2.getMimeType());
                    }
                });
            } else if (order.startsWith(PARAM_FILE_TYPE)) {
                Collections.sort(filelist, new Comparator<FileAttribute>() {
                    public int compare(final FileAttribute fa1, final FileAttribute fa2) {
                        return fa1.getFileType() - fa2.getFileType();
                    }
                });
            } else if (order.startsWith(PARAM_FILE_SIZE)) {
                Collections.sort(filelist, new Comparator<FileAttribute>() {
                    public int compare(final FileAttribute fa1, final FileAttribute fa2) {
                        return (int) (fa1.getFileSize() - fa2.getFileSize());
                    }
                });
            } else if (order.startsWith(PARAM_UPDATE_DATE)) {
                Collections.sort(filelist, new Comparator<FileAttribute>() {
                    public int compare(final FileAttribute fa1, final FileAttribute fa2) {
                        return fa1.getUpdateDate().compareTo(fa2.getUpdateDate());
                    }
                });
            }
        }
        return filelist;
    }

    /**
     * Set Arraylist.
     * 
     * @param respFileList File list information.
     * @param filelist FileAttribute list.
     * @return FileAttribute list.
     */
    protected ArrayList<FileAttribute> setArrayList(final File[] respFileList, final ArrayList<FileAttribute> filelist) {
        for (File file : respFileList) {
            String path = file.getPath().replaceAll("" + mFileManager.getBasePath(), "");
            if (path == null) {
                path = "unknown";
            }
            String name = file.getName();
            if (name == null) {
                name = "unknown";
            }
            Long size = file.length();
            String date = mDataFormat.format(file.lastModified());
            int filetype = 0;
            String mimetype = null;
            if (file.isFile()) {
                filetype = 0;
                mimetype = getMIMEType(file.getPath() + file.getName());
                if (mimetype == null) {
                    mimetype = "unknown";
                }
            } else {
                filetype = 1;
                mimetype = "dir/folder";
            }
            FileAttribute fileAttr = new FileAttribute(path, name, mimetype, filetype, size, date);
            filelist.add(fileAttr);
        }
        return filelist;
    }

    /**
     * ファイルパラメータ格納用メソッド.
     * 
     * @param fa FileAttributeデータ.
     * @param respParam ファイルパラメータ格納用Bundle.
     * @return ファイルパラメータ格納済みBundle
     */
    protected Bundle addResponseParamToArray(final FileAttribute fa, final Bundle respParam) {
        respParam.putString(PARAM_PATH, fa.getPath());
        respParam.putString(PARAM_FILE_NAME, fa.getName());
        respParam.putString(PARAM_MIME_TYPE, fa.getMimeType());
        respParam.putString(PARAM_FILE_TYPE, String.valueOf(fa.getFileType()));
        respParam.putLong(PARAM_FILE_SIZE, fa.getFileSize());
        respParam.putString(PARAM_UPDATE_DATE, fa.getUpdateDate());
        return respParam;
    }

    /**
     * ファイル名からMIMEタイプ取得.
     * 
     * @param path パス
     * @return MIMEタイプ
     */
    public String getMIMEType(final String path) {

        // 拡張子を取得
        File file = new File(path);
        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        String ext = (pos >= 0) ? fileName.substring(pos + 1) : null;
        if (ext != null) {
            // 小文字に変換
            ext = ext.toLowerCase(Locale.getDefault());
            // MIME Typeを返す
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        } else {
            return null;
        }
    }

    /**
     * 保存後の処理を行う.
     */
    private interface OnSavedListener {
        void onSavedListener();
    }

    /**
     * File Attribute Class.
     * 
     */
    public class FileAttribute {
        /** File Path. */
        private String mPath;

        /** File Name. */
        private String mName;

        /** MIME Type. */
        private String mMimeType;

        /** File Type. */
        private int mFileType;

        /** File Size. */
        private long mSize;

        /** Update Date. */
        private String mUpdateDate;

        /**
         * Constructor.
         * 
         * @param path File Path.
         * @param name File Name.
         * @param mimetype MIME Type.
         * @param filetype File Type.
         * @param size File Size.
         * @param date Update Date.
         */
        public FileAttribute(final String path, final String name, final String mimetype, final int filetype,
                final long size, final String date) {
            this.mPath = path;
            this.mName = name;
            this.mMimeType = mimetype;
            this.mFileType = filetype;
            this.mSize = size;
            this.mUpdateDate = date;
        }

        /**
         * Get path.
         * 
         * @return path
         */
        public String getPath() {
            return this.mPath;
        }

        /**
         * Get name.
         * 
         * @return File Name
         */
        public String getName() {
            return this.mName;
        }

        /**
         * Get MIME Type.
         * 
         * @return MIME Type
         */
        public String getMimeType() {
            return this.mMimeType;
        }

        /**
         * Get File Type.
         * 
         * @return File Type
         */
        public int getFileType() {
            return this.mFileType;
        }

        /**
         * Get File Size.
         * 
         * @return File Size
         */
        public long getFileSize() {
            return this.mSize;
        }

        /**
         * Get Update Date.
         * 
         * @return Update Date
         */
        public String getUpdateDate() {
            return this.mUpdateDate;
        }
    }

}
