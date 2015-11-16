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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ThetaS extends AbstractThetaDevice {

    private static final String PARAM_RESULTS = "results";

    private static final String PARAM_ENTRIES = "entries";

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
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        }
    }

    @Override
    public double getBatteryLevel() throws ThetaDeviceException {
        try {
            OscState state = mOscClient.state();
            return state.getBatteryLevel();
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (JSONException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        }
    }

    @Override
    public void changeShootingMode(final ShootingMode mode) throws ThetaDeviceException {
        // TODO Implement.
    }

    private class ThetaObjectS implements ThetaObject {

        private static final String MIMETYPE_IMAGE = "image/jpeg";

        private static final String MIMETYPE_VIDEO = "video/mp4";

        private final OscEntry mEntry;

        private byte[] mThumbnail;

        private byte[] mMain;

        public ThetaObjectS(final OscEntry entry) {
            mEntry = entry;
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
                        mThumbnail = result.getBytes();
                    }    break;
                    case MAIN: {
                        OscCommand.Result result;
                        if (isImage()) {
                            result = mOscClient.getImage(mEntry.getUri(), false);
                        } else {
                            result = mOscClient.getVideo(mEntry.getUri(), false);
                        }
                        mMain = result.getBytes();
                    }    break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            } catch (JSONException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            } catch (JSONException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
            return mEntry.getDateTime();
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
