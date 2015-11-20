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
        return new ThetaObjectS(entry);
    }

    private boolean checkExtension(final String filename) {
        return filename.toLowerCase().endsWith(".mp4") || filename.toLowerCase().endsWith(".jpg");
    }

    @Override
    public ThetaObject takePicture() throws ThetaDeviceException {
        try {
            OscSession session = mOscClient.startSession();
            String fileUri = mOscClient.takePicture(session.getId());
            mOscClient.closeSession(session.getId());
            return null; // TODO Implement.
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
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
    public void changeShootingMode(final ShootingMode mode) throws ThetaDeviceException {
        // TODO Implement.
    }

    @Override
    public InputStream getLivePreview() throws ThetaDeviceException {
        try {
            OscSession session = mOscClient.startSession();
            OscCommand.Result result = mOscClient.getLivePreview(session.getId());
            return result.getInputStream();
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.INVALID_RESPONSE, e);
        }
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

        private final OscEntry mEntry;

        private final String mDateTime;

        private final long mDateTimeUnix;

        private byte[] mThumbnail;

        private byte[] mMain;

        public ThetaObjectS(final OscEntry entry) {
            mEntry = entry;

            Date date = parseDate(entry.getDateTime());
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
                            result = mOscClient.getImage(mEntry.getUri(), true);
                        } else {
                            result = mOscClient.getVideo(mEntry.getUri(), true);
                        }
                        throwExceptionIfError(result);
                        mThumbnail = result.getBytes();
                    }    break;
                    case MAIN: {
                        OscCommand.Result result;
                        if (isImage()) {
                            result = mOscClient.getImage(mEntry.getUri(), false);
                        } else {
                            result = mOscClient.getVideo(mEntry.getUri(), false);
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
                mOscClient.delete(mEntry.getUri());
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
            return mEntry.getName();
        }

        @Override
        public Integer getWidth() {
            return mEntry.getWidth();
        }

        @Override
        public Integer getHeight() {
            return mEntry.getHeight();
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

}
