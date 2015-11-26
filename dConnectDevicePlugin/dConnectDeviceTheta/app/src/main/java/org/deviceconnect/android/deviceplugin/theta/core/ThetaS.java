package org.deviceconnect.android.deviceplugin.theta.core;


import org.deviceconnect.android.deviceplugin.theta.core.osc.OscClient;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscCommand;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscEntry;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscSession;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

class ThetaS extends AbstractThetaDevice {

    private static final String PARAM_RESULTS = "results";

    private static final String PARAM_ENTRIES = "entries";

    private static final String PARAM_ID = "id";

    private static final String PARAM_FILE_URI = "fileUri";

    private static final String PARAM_EXIF = "exif";

    private static final String PARAM_OPTIONS = "options";

    private static final String OPTION_CAPTURE_MODE = "captureMode";

    private static final String CAPTURE_MODE_IMAGE = "image";

    private static final String CAPTURE_MODE_VIDEO = "_video";

    private static final String CAPTURE_MODE_LIVE_STREAMING = "_liveStreaming";

    private static final SimpleDateFormat BEFORE_FORMAT_WITH_TIMEZONE = new SimpleDateFormat("yyyy:MM:dd HH:mm:ssZ");

    private static final SimpleDateFormat BEFORE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    private static final SimpleDateFormat AFTER_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private OscClient mOscClient = new OscClient();

    private String mSessionId;

    ThetaS(final String name) {
        super(name);
    }

    @Override
    public ThetaDeviceModel getModel() {
        return ThetaDeviceModel.THETA_S;
    }

    @Override
    public List<ThetaObject> fetchAllObjectList() throws ThetaDeviceException {
        return fetchObjectList(0, 10000);
    }

    @Override
    public List<ThetaObject> fetchObjectList(final int offset, final int maxLength) throws ThetaDeviceException {
        try {
            OscCommand.Result result = mOscClient.listAll(offset, maxLength);
            throwExceptionIfError(result);
            JSONObject json = result.getJSON();
            JSONObject results = json.getJSONObject(PARAM_RESULTS);
            JSONArray entries = results.getJSONArray(PARAM_ENTRIES);
            List<OscEntry> list = OscEntry.parseList(entries, true);

            List<ThetaObject> objects = new ArrayList<ThetaObject>();
            for (Iterator<OscEntry> it = list.iterator(); it.hasNext(); ) {
                ThetaObject object = createThetaObject(it.next());
                if (object != null) {
                    objects.add(object);
                }
            }
            return objects;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    private ThetaObject createThetaObject(final OscEntry entry) {
        if (!checkExtension(entry.getName())) {
            return null;
        }
        return new ThetaObjectS(entry.getUri(), entry.getName(), entry.getDateTime(),
                                entry.getWidth(), entry.getHeight());
    }

    private boolean checkExtension(final String filename) {
        return filename.toLowerCase().endsWith(".mp4") || filename.toLowerCase().endsWith(".jpg");
    }

    @Override
    public ThetaObject takePicture() throws ThetaDeviceException {
        try {
            OscSession session = mOscClient.startSession();
            OscCommand.Result result = mOscClient.takePicture(session.getId());
            throwExceptionIfError(result);
            JSONObject json = result.getJSON();
            String id = json.getString(PARAM_ID);

            result = mOscClient.waitForDone(id);
            json = result.getJSON();
            JSONObject results = json.getJSONObject(PARAM_RESULTS);
            String fileUri = results.getString(PARAM_FILE_URI);

            result = mOscClient.getMetaData(fileUri);
            throwExceptionIfError(result);
            json = result.getJSON();
            results = json.getJSONObject(PARAM_RESULTS);

            mOscClient.closeSession(session.getId());

            Exif exif = new Exif(results.getJSONObject(PARAM_EXIF));
            return new ThetaObjectS(fileUri, parseFileName(fileUri), exif.mDateTime,
                                    exif.mImageWidth, exif.mImageLength);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        } catch (InterruptedException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        }
    }

    private String parseFileName(final String fileUri) {
        String[] result = fileUri.split("/");
        if (result.length < 2) {
            return fileUri;
        }
        return result[1];
    }

    @Override
    public synchronized void startVideoRecording() throws ThetaDeviceException {
        try {
            if (mSessionId != null) {
                return;
            }
            OscSession session = mOscClient.startSession();
            mSessionId = session.getId();
            mOscClient.startCapture(mSessionId);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public synchronized void stopVideoRecording() throws ThetaDeviceException {
        try {
            if (mSessionId == null) {
                return;
            }
            mOscClient.stopCapture(mSessionId);
            mSessionId = null;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public long getMaxVideoLength() {
        return 25 * 60 * 1000;
    }

    @Override
    public double getBatteryLevel() throws ThetaDeviceException {
        try {
            OscState state = mOscClient.state();
            return state.getBatteryLevel();
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public ShootingMode getShootingMode() throws ThetaDeviceException {
        try {
            OscSession session = mOscClient.startSession();
            String sessionId = session.getId();
            JSONArray optionNames = new JSONArray();
            optionNames.put("captureMode");

            OscCommand.Result result = mOscClient.getOptions(sessionId, optionNames);
            JSONObject json = result.getJSON();
            throwExceptionIfError(result);
            JSONObject results = json.getJSONObject(PARAM_RESULTS);
            JSONObject options = results.getJSONObject(PARAM_OPTIONS);
            String captureMode = options.getString(OPTION_CAPTURE_MODE);

            ShootingMode mode;
            if (CAPTURE_MODE_IMAGE.equals(captureMode)) {
                mode = ShootingMode.IMAGE;
            } else if (CAPTURE_MODE_VIDEO.equals(captureMode)) {
                mode = ShootingMode.VIDEO;
            } else if (CAPTURE_MODE_LIVE_STREAMING.equals(captureMode)) {
                mode = ShootingMode.LIVE_STREAMING;
            } else {
                mode = ShootingMode.UNKNOWN;
            }

            mOscClient.closeSession(sessionId);

            return mode;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public void changeShootingMode(final ShootingMode mode) throws ThetaDeviceException {
        try {
            String captureMode;
            switch (mode) {
                case IMAGE:
                    captureMode = CAPTURE_MODE_IMAGE;
                    break;
                case VIDEO:
                    captureMode = CAPTURE_MODE_VIDEO;
                    break;
                default:
                    throw new IllegalArgumentException("mode must be IMAGE or VIDEO.");
            }

            OscSession session = mOscClient.startSession();
            String sessionId = session.getId();
            JSONObject options = new JSONObject();
            options.put(OPTION_CAPTURE_MODE, captureMode);

            OscCommand.Result result = mOscClient.setOptions(sessionId, options);
            throwExceptionIfError(result);

            mOscClient.closeSession(sessionId);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public InputStream getLivePreview() throws ThetaDeviceException {
        try {
            OscSession session = mOscClient.startSession();
            return mOscClient.getLivePreview(session.getId());
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public void destroy() {
    }

    private void throwExceptionIfError(final OscCommand.Result result) throws ThetaDeviceException {
        if (result.isSuccess()) {
            return;
        }
        OscCommand.Error error = result.getError();
        String code = error.getCode();
        int status = result.getHttpStatusCode();
        switch (status) {
            case 400:
                throw new ThetaDeviceException(ThetaDeviceException.BAD_REQUEST, code);
            case 403:
                throw new ThetaDeviceException(ThetaDeviceException.FORBIDDEN, code);
            case 503:
                throw new ThetaDeviceException(ThetaDeviceException.UNAVAILABLE, code);
            default:
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        }
    }

    private class ThetaObjectS implements ThetaObject {

        private static final String MIMETYPE_IMAGE = "image/jpeg";

        private static final String MIMETYPE_VIDEO = "video/mp4";

        private final String mFileUri;

        private final String mFileName;

        private final int mWidth;

        private final int mHeight;

        private final String mDateTime;

        private final long mDateTimeUnix;

        private byte[] mThumbnail;

        private byte[] mMain;

        public ThetaObjectS(final String fileUri, final String fileName, final String dateTime,
                            final int width, final int height) {
            mFileUri = fileUri;
            mFileName = fileName;
            mWidth = width;
            mHeight = height;

            Date date = parseDate(dateTime);
            if (date != null) {
                mDateTime = AFTER_FORMAT.format(date);
                mDateTimeUnix = date.getTime();
            } else {
                mDateTime = "";
                mDateTimeUnix = 0;
            }
        }

        private Date parseDate(final String time) {
            Date date = null;
            try {
                date = BEFORE_FORMAT_WITH_TIMEZONE.parse(time);
            } catch (ParseException e) {
                // Nothing to do.
            }
            if (date == null) {
                try {
                    date = BEFORE_FORMAT.parse(time);
                } catch (ParseException e) {
                    // Nothing to do.
                }
            }
            return date;
        }

        @Override
        public void fetch(final DataType type) throws ThetaDeviceException {
            try {
                switch (type) {
                    case THUMBNAIL: {
                        OscCommand.Result result;
                        if (isImage()) {
                            result = mOscClient.getImage(mFileUri, true);
                        } else {
                            result = mOscClient.getVideo(mFileUri, true);
                        }
                        throwExceptionIfError(result);
                        mThumbnail = result.getBytes();
                    }    break;
                    case MAIN: {
                        OscCommand.Result result;
                        if (isImage()) {
                            result = mOscClient.getImage(mFileUri, false);
                        } else {
                            result = mOscClient.getVideo(mFileUri, false);
                        }
                        throwExceptionIfError(result);
                        mMain = result.getBytes();
                    }    break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
            } catch (JSONException e) {
                throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
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
        public void remove() throws ThetaDeviceException {
            try {
                mOscClient.delete(mFileUri);
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
            } catch (JSONException e) {
                throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
            }
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
            return isImage() ? MIMETYPE_IMAGE : MIMETYPE_VIDEO;
        }

        @Override
        public Boolean isImage() {
            return getFileName().toLowerCase().endsWith(".jpg");
        }

        @Override
        public String getCreationTime() {
            return mDateTime;
        }

        @Override
        public long getCreationTimeWithUnixTime() {
            return mDateTimeUnix;
        }

        @Override
        public String getFileName() {
            return mFileName;
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
    }

    private static class Exif {

        private static final String KEY_IMAGE_WIDTH = "ImageWidth";

        private static final String KEY_IMAGE_LENGTH = "ImageLength";

        private static final String KEY_DATE_TIME = "DateTime";

        private final int mImageWidth;

        private final int mImageLength;

        private final String mDateTime;

        public Exif(final JSONObject exif) throws JSONException {
            mImageWidth = exif.getInt(KEY_IMAGE_WIDTH);
            mImageLength = exif.getInt(KEY_IMAGE_LENGTH);
            mDateTime = exif.getString(KEY_DATE_TIME);
        }

    }
}
