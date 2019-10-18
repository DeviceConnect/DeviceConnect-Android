package org.deviceconnect.android.deviceplugin.theta.core;


import com.burgstaller.okhttp.digest.Credentials;

import org.deviceconnect.android.deviceplugin.theta.core.osc.OscClient;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscCommand;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscEntry;
import org.deviceconnect.android.deviceplugin.theta.core.osc.OscState;
import org.deviceconnect.utils.RFC3339DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.SocketFactory;

public class ThetaV extends AbstractThetaDevice {

    private static final String ID_PREFIX = "theta-v-";

    private static final String PARAM_RESULTS = "results";

    private static final String PARAM_ENTRIES = "entries";

    private static final String PARAM_ID = "id";

    private static final String PARAM_FILE_URI = "fileUrl";

    private static final String PARAM_EXIF = "exif";

    private static final String PARAM_OPTIONS = "options";

    private static final String OPTION_CAPTURE_MODE = "captureMode";

    private static final String OPTION_FILE_FORMAT = "fileFormat";

    private static final String OPTION_DATE_TIME_ZONE = "dateTimeZone";

    private static final String CAPTURE_MODE_IMAGE = "image";

    private static final String CAPTURE_MODE_VIDEO = "video";

    private static final String CAPTURE_MODE_LIVE_STREAMING = "_liveStreaming";

    private static final SimpleDateFormat BEFORE_FORMAT_WITH_TIMEZONE = new SimpleDateFormat("yyyy:MM:dd HH:mm:ssZ");

    private static final SimpleDateFormat BEFORE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    private static final SimpleDateFormat AFTER_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private OscClient mOscClient;

    public ThetaV(final String ssId, final String host, final Credentials credentials, final SocketFactory socketFactory) {
        super(ssId);
        mOscClient = new OscClient(host, credentials, socketFactory);
    }

    @Override
    public String getId() {
        return ID_PREFIX + mSSID;
    }

    @Override
    public ThetaDeviceModel getModel() {
        return ThetaDeviceModel.THETA_V;
    }

    @Override
    public List<ThetaObject> fetchAllObjectList() throws ThetaDeviceException {
        return fetchObjectList(0, 10000);
    }

    @Override
    public List<ThetaObject> fetchObjectList(final int offset, final int maxLength) throws ThetaDeviceException {
        try {
            OscCommand.Result result = mOscClient.listFiles(offset, maxLength);
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
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
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
        return new ThetaObjectV(entry.getUri(), entry.getName(), entry.getDateTime(),
                                entry.getWidth(), entry.getHeight());
    }

    private boolean checkExtension(final String filename) {
        return filename.toLowerCase().endsWith(".mp4") || filename.toLowerCase().endsWith(".jpg");
    }

    @Override
    public ThetaObject takePicture() throws ThetaDeviceException {
        try {
            OscCommand.Result result = mOscClient.takePicture();
            throwExceptionIfError(result);
            JSONObject json = result.getJSON();
            String id = json.getString(PARAM_ID);

            result = mOscClient.waitForDone(id);
            json = result.getJSON();
            JSONObject results = json.getJSONObject(PARAM_RESULTS);
            String fileUri = results.getString(PARAM_FILE_URI);

            //画像の情報取得
            JSONArray optionNames = new JSONArray();
            optionNames.put(OPTION_FILE_FORMAT);
            optionNames.put(OPTION_DATE_TIME_ZONE);

            OscCommand.Result resultOption = mOscClient.getOptions(optionNames);
            throwExceptionIfError(resultOption);
            json = resultOption.getJSON();

            results = json.getJSONObject(PARAM_RESULTS);
            JSONObject options = results.getJSONObject(PARAM_OPTIONS);
            JSONObject format = options.getJSONObject(OPTION_FILE_FORMAT);
            String timeZone = options.getString(OPTION_DATE_TIME_ZONE);
            int width = format.getInt("width");
            int height = format.getInt("height");

            return new ThetaObjectV(fileUri, parseFileName(fileUri), timeZone,
                                    width, height);
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
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
        return result[result.length - 1];
    }

    @Override
    public synchronized void startVideoRecording() throws ThetaDeviceException {
        try {
            OscCommand.Result result = mOscClient.startCapture();
            throwExceptionIfError(result);
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public synchronized void stopVideoRecording() throws ThetaDeviceException {
        try {
            OscCommand.Result result = mOscClient.stopCapture();
            throwExceptionIfError(result);
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
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
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public ShootingMode getShootingMode() throws ThetaDeviceException {
        try {
            JSONArray optionNames = new JSONArray();
            optionNames.put("captureMode");

            OscCommand.Result result = mOscClient.getOptions(optionNames);
            throwExceptionIfError(result);
            JSONObject json = result.getJSON();
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

            return mode;
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
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

            JSONObject options = new JSONObject();
            options.put(OPTION_CAPTURE_MODE, captureMode);

            OscCommand.Result result = mOscClient.setOptions(options);
            throwExceptionIfError(result);
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public Recorder getRecorder() throws ThetaDeviceException {
        try {
            JSONArray optionNames = new JSONArray();
            optionNames.put("fileFormat");

            OscCommand.Result result = mOscClient.getOptions(optionNames);
            throwExceptionIfError(result);
            JSONObject json = result.getJSON();
            JSONObject results = json.getJSONObject(PARAM_RESULTS);
            JSONObject options = results.getJSONObject(PARAM_OPTIONS);
            JSONObject format = options.getJSONObject(OPTION_FILE_FORMAT);
            String type = format.getString("type");
            int width = format.getInt("width");
            int height = format.getInt("height");
            if ("jpeg".equals(type)) {
                return new ThetaImageRecorderV("0", width, height);
            } else if ("mp4".equals(type)) {
                return new ThetaVideoRecorderV("1", width, height);
            } else {
                return null;
            }
        } catch (SocketTimeoutException e) {
            throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
    }

    @Override
    public InputStream getLiveStream() throws IOException {
        try {
            return mOscClient.getLivePreview();
        } catch (JSONException e) {
            throw new IOException(e);
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

    private class ThetaObjectV implements ThetaObject {

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

        public ThetaObjectV(final String fileUri, final String fileName, final String dateTime,
                            final int width, final int height) {
            String uri = fileUri;
            //ローカルホスト上のURLが指定された場合は、外部IPに置き換える
            mFileUri = uri.replace("127.0.0.1", mOscClient.getHost());

            mFileName = fileName;
            mWidth = width;
            mHeight = height;

            Date date = parseDate(dateTime);
            if (date != null) {
                mDateTime = RFC3339DateUtils.toString(date);
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
                            result = mOscClient.getImageFromFileURL(mFileUri, true);
                        } else {
                            result = mOscClient.getVideoFromFileURL(mFileUri, true);
                        }
                        throwExceptionIfError(result);
                        mThumbnail = result.getBytes();
                    }    break;
                    case MAIN: {
                        OscCommand.Result result;
                        if (isImage()) {
                            result = mOscClient.getImageFromFileURL(mFileUri, false);
                        } else {
                            result = mOscClient.getVideoFromFileURL(mFileUri, false);
                        }
                        throwExceptionIfError(result);
                        mMain = result.getBytes();
                    }    break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (SocketTimeoutException e) {
                throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
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
                mOscClient.delete(Arrays.asList(mFileUri));
            } catch (SocketTimeoutException e) {
                throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
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

    private class ThetaImageRecorderV extends ThetaRecorderV {

        private static final String NAME = "THETA V - photo";
        private static final String MIME_TYPE = "image/jpeg";
        private static final int PREVIEW_WIDTH = 640;
        private static final int PREVIEW_HEIGHT = 320;
        private static final double PREVIEW_MAX_FRAME_RATE = 10.0d;

        public ThetaImageRecorderV(final String id, final int imageWidth, final int imageHeight) {
            super(id, NAME, MIME_TYPE, imageWidth, imageHeight);
        }

        @Override
        public int getPreviewWidth() {
            return PREVIEW_WIDTH;
        }

        @Override
        public int getPreviewHeight() {
            return PREVIEW_HEIGHT;
        }

        @Override
        public double getPreviewMaxFrameRate() {
            return PREVIEW_MAX_FRAME_RATE;
        }

        @Override
        public boolean supportsPreview() {
            return true;
        }

        @Override
        public boolean supportsVideoRecording() {
            return false;
        }

        @Override
        public boolean supportsPhoto() {
            return true;
        }

        @Override
        public RecorderState getState() throws ThetaDeviceException {
            return RecorderState.INACTIVE;
        }
    }

    private class ThetaVideoRecorderV extends ThetaRecorderV {

        private static final String NAME = "THETA V - video";
        private static final String MIME_TYPE = "video/mp4";

        public ThetaVideoRecorderV(final String id, final int imageWidth, final int imageHeight) {
            super(id, NAME, MIME_TYPE, imageWidth, imageHeight);
        }

        @Override
        public int getPreviewWidth() {
            return 0;
        }

        @Override
        public int getPreviewHeight() {
            return 0;
        }

        @Override
        public double getPreviewMaxFrameRate() {
            return 0;
        }

        @Override
        public boolean supportsPreview() {
            return false;
        }

        @Override
        public boolean supportsVideoRecording() {
            return true;
        }

        @Override
        public boolean supportsPhoto() {
            return false;
        }

        @Override
        public RecorderState getState() throws ThetaDeviceException {
            try {
                OscState state = mOscClient.state();
                String captureStatus = state.getCaptureStatus();
                if ("shooting".equals(captureStatus)) {
                    return RecorderState.RECORDING;
                } else if ("idle".equals(captureStatus)) {
                    return RecorderState.INACTIVE;
                } else {
                    return RecorderState.UNKNOWN;
                }
            } catch (SocketTimeoutException e) {
                throw new ThetaDeviceException(ThetaDeviceException.TIMEOUT, e);
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
            } catch (JSONException e) {
                throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
            }
        }
    }

    private abstract class ThetaRecorderV implements Recorder {

        private final String mId;
        private final String mName;
        private final String mMimeType;
        private final int mImageWidth;
        private final int mImageHeight;

        public ThetaRecorderV(final String id, final String name, final String mimeType,
                              final int imageWidth, final int imageHeight) {
            mId = id;
            mName = name;
            mMimeType = mimeType;
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;
        }

        @Override
        public String getId() {
            return mId;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getMimeType() {
            return mMimeType;
        }

        @Override
        public int getImageWidth() {
            return mImageWidth;
        }

        @Override
        public int getImageHeight() {
            return mImageHeight;
        }

    }


}
