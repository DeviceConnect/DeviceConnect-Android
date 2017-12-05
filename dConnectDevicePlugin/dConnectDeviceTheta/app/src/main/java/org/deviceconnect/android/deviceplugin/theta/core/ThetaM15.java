package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;
import com.theta360.lib.ptpip.entity.DeviceInfo;
import com.theta360.lib.ptpip.entity.ObjectHandles;
import com.theta360.lib.ptpip.entity.ObjectInfo;
import com.theta360.lib.ptpip.entity.PtpObject;
import com.theta360.lib.ptpip.eventlistener.PtpipEventListener;

import org.deviceconnect.utils.RFC3339DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.net.SocketFactory;

class ThetaM15 extends AbstractThetaDevice {

    private static final String ID_PREFIX = "theta-m15-";

    private static final String HOST = "192.168.1.1";

    private static final String BRAND_SAMSUNG = "samsung";

    private static final String MANUFACTURER_SAMSUNG = "samsung";

    private static final SimpleDateFormat BEFORE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    private static final SimpleDateFormat AFTER_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final VersionName VERSION_5_MIN_VIDEO = new VersionName("01.30");

    private final SocketFactory mSocketFactory;

    private final Comparator<ThetaObject> mComparator = new Comparator<ThetaObject>() {

        @Override
        public int compare(final ThetaObject o1, final ThetaObject o2) {
            Long t1 = new Long(o1.getCreationTimeWithUnixTime());
            Long t2 = new Long(o2.getCreationTimeWithUnixTime());
            return t2.compareTo(t1);
        }

    };

    private VersionName mDeviceVersion;

    private Recorder mRecorder;

    ThetaM15(final Context context, final String ssId) {
        super(ssId);
        mSocketFactory = getWifiSocketFactory(context);
    }

    public boolean initialize() {
        mDeviceVersion = getDeviceVersion();
        if (mDeviceVersion == null) {
            return false;
        }
        mRecorder = detectRecorder();
        if (mRecorder == null) {
            return false;
        }
        return true;
    }

    private PtpipInitiator getInitiator() throws ThetaException, IOException {
        // TODO Null check for mSocketFactory.
        return new PtpipInitiator(mSocketFactory, HOST);
    }

    private static SocketFactory getWifiSocketFactory(final Context context) {
        SocketFactory socketFactory = SocketFactory.getDefault();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isGalaxyDevice()) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] allNetwork = cm.getAllNetworks();
            for (Network network : allNetwork) {
                NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(network);
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    socketFactory = network.getSocketFactory();
                }
            }
        }
        return socketFactory;
    }

    private static boolean isGalaxyDevice() {
        if ((Build.BRAND != null) && (Build.BRAND.toLowerCase(Locale.ENGLISH).contains(BRAND_SAMSUNG))) {
            return true;
        }
        if ((Build.MANUFACTURER != null) && (Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains(MANUFACTURER_SAMSUNG))) {
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return ID_PREFIX + mSSID;
    }

    @Override
    public ThetaDeviceModel getModel() {
        return ThetaDeviceModel.THETA_M15;
    }

    @Override
    public List<ThetaObject> fetchAllObjectList() throws ThetaDeviceException {
        try {
            PtpipInitiator initiator = getInitiator();
            ObjectHandles objectHandles = initiator.getObjectHandles(
                PtpipInitiator.PARAMETER_VALUE_DEFAULT,
                PtpipInitiator.PARAMETER_VALUE_DEFAULT,
                PtpipInitiator.PARAMETER_VALUE_DEFAULT);

            List<ThetaObject> result = new ArrayList<ThetaObject>();
            for (int i = 0; i < objectHandles.size(); i++) {
                int objectHandle = objectHandles.getObjectHandle(i);
                ObjectInfo objectInfo = initiator.getObjectInfo(objectHandle);
                ThetaObject object = parseDevice(objectHandle, objectInfo);
                if (object != null) {
                    result.add(object);
                }
            }
            Collections.sort(result, mComparator);

            return result;
        } catch (IOException e) {
            try {
                PtpipInitiator.close();
            } catch (ThetaException e1) {
                e1.printStackTrace();
            }
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (ThetaException e) {
            try {
                PtpipInitiator.close();
            } catch (ThetaException e1) {
                e1.printStackTrace();
            }
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        }
    }

    private ThetaObject parseDevice(final int handle, final ObjectInfo info) {
        String name = info.getFilename();
        String date = info.getCaptureDate();
        int width = info.getImagePixWidth();
        int height = info.getImagePixHeight();
        int thumbnailFormat = info.getThumbFormat();
        int objectFormat = info.getObjectFormat();

        if (thumbnailFormat != 0) {
            if (objectFormat == ObjectInfo.OBJECT_FORMAT_CODE_EXIF_JPEG) {
                return new ThetaImageObject(handle, name, date, width, height);
            } else {
                return new ThetaVideoObject(handle, name, date, width, height);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<ThetaObject> fetchObjectList(int offset, int maxLength) throws ThetaDeviceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThetaObject takePicture() throws ThetaDeviceException {
        try {
            final CountDownLatch lockObj = new CountDownLatch(1);
            final PtpipInitiator initiator = getInitiator();
            final ThetaObject[] photo = new ThetaObject[1];
            if (initiator.getStillCaptureMode() == PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            }
            initiator.setStillCaptureMode(PtpipInitiator.DEVICE_PROP_VALUE_SINGLE_CAPTURE_MODE);
            initiator.initiateCapture(new PtpipEventListener() {
                @Override
                public void onObjectAdded(final int handle) {
                    try {
                        ObjectInfo info = initiator.getObjectInfo(handle);
                        photo[0] = parseDevice(handle, info);
                    } catch (ThetaException e) {
                        e.printStackTrace();
                        // Nothing to do.
                    } finally {
                        lockObj.countDown();
                    }
                }
            });
            lockObj.await();


            if (photo[0] != null) {
                return photo[0];
            } else {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            }
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        } catch (InterruptedException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        }
    }

    @Override
    public void startVideoRecording() throws ThetaDeviceException {
        try {
            PtpipInitiator initiator = getInitiator();
            final int mode = initiator.getStillCaptureMode();
            if (mode != PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            }
            final short status = initiator.getCaptureStatus();
            if (status == PtpipInitiator.DEVICE_PROP_VALUE_CAPTURE_STATUS_CONTINUOUS_SHOOTING_RUNNING) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            }
            initiator.initiateOpenCapture();
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        }
    }

    @Override
    public void stopVideoRecording() throws ThetaDeviceException {
        try {
            PtpipInitiator initiator = getInitiator();
            final short status = initiator.getCaptureStatus();
            if (status == PtpipInitiator.DEVICE_PROP_VALUE_CAPTURE_STATUS_WAIT) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            }
            initiator.terminateOpenCapture();
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        }
    }

    @Override
    public long getMaxVideoLength() {
        return is5minVideo() ? 5 * 60 * 1000 : 3 * 60 * 1000;
    }

    private boolean is5minVideo() {
        return mDeviceVersion.isSameOrLaterThan(VERSION_5_MIN_VIDEO);
    }

    @Override
    public double getBatteryLevel() throws ThetaDeviceException {
        try {
            return getInitiator().getBatteryLevel().getValue() / 100d;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        }
    }

    @Override
    public ShootingMode getShootingMode() throws ThetaDeviceException {
        try {
            PtpipInitiator ptpIp = getInitiator();
            short captureMode = ptpIp.getStillCaptureMode();
            ShootingMode mode;
            switch (captureMode) {
                case PtpipInitiator.DEVICE_PROP_VALUE_SINGLE_CAPTURE_MODE:
                    mode = ShootingMode.IMAGE;
                    break;
                case PtpipInitiator.DEVICE_PROP_VALUE_TIMELAPSE_CAPTURE_MODE:
                    mode = ShootingMode.IMAGE_INTERVAL;
                    break;
                case PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE:
                    mode = ShootingMode.VIDEO;
                    break;
                default:
                    mode = ShootingMode.UNKNOWN;
                    break;
            }
            return mode;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
        }
    }

    @Override
    public void changeShootingMode(final ShootingMode mode) throws ThetaDeviceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Recorder getRecorder() throws ThetaDeviceException {
        return mRecorder;
    }

    @Override
    public InputStream getLiveStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    private VersionName getDeviceVersion() {
        DeviceInfo deviceInfo;

        try {
            deviceInfo = getInitiator().getDeviceInfo();
        } catch (IOException e) {
            return null;
        } catch (ThetaException e) {
            return null;
        }

        return new VersionName(deviceInfo.getDeviceVersion());
    }

    private Recorder detectRecorder() {
        try {
            ShootingMode mode = getShootingMode();
            switch (mode) {
                case IMAGE:
                    return new ThetaImageRecorderM15("0");
                case VIDEO:
                    return new ThetaVideoRecorderM15("1");
                default:
                    return null;
            }
        } catch (ThetaDeviceException e) {
            return null;
        }
    }

    @Override
    public void destroy() {
        try {
            PtpipInitiator.close();
        } catch (ThetaException e) {
            // Nothing to do.
        }
    }

    private class ThetaImageObject extends ThetaObjectM15 {

        private static final String MIMETYPE_IMAGE = "image/jpeg";

        public ThetaImageObject(final int handle, final String filename, final String date, final int width, final int height) {
            super(handle, filename, date, width, height);
        }

        @Override
        public String getMimeType() {
            return MIMETYPE_IMAGE;
        }

        @Override
        public Boolean isImage() {
            return true;
        }

    }

    private class ThetaVideoObject extends ThetaObjectM15 {

        private static final String MIMETYPE_VIDEO = "video/mpeg";

        public ThetaVideoObject(final int handle, final String filename, final String date, final int width, final int height) {
            super(handle, filename, date, width, height);
        }

        @Override
        public String getMimeType() {
            return MIMETYPE_VIDEO;
        }

        @Override
        public Boolean isImage() {
            return false;
        }

    }

    private abstract class ThetaObjectM15 implements ThetaObject {

        private final int mObjectHandle;

        private final String mFilename;

        private final String mDateTime;

        private long mDateTimeUnix;

        private final int mWidth;

        private final int mHeight;

        private byte[] mThumbnail;

        private byte[] mMain;

        public ThetaObjectM15(final int handle, final String filename, final String date, final int width, final int height) {
            mObjectHandle = handle;
            mFilename = filename;
            mWidth = width;
            mHeight = height;

            String dateTime;
            try {
                dateTime = AFTER_FORMAT.format(BEFORE_FORMAT.parse(date));
                mDateTimeUnix = AFTER_FORMAT.parse(dateTime).getTime();
                dateTime = RFC3339DateUtils.toString(BEFORE_FORMAT.parse(date));
            } catch (ParseException e) {
                dateTime = "";
                mDateTimeUnix = 0;
            }
            mDateTime = dateTime;
        }

        protected int getHandle() {
            return mObjectHandle;
        }

        @Override
        public void fetch(final DataType type) throws ThetaDeviceException {
            try {
                switch (type) {
                    case THUMBNAIL:
                        PtpObject obj = getInitiator().getThumb(getHandle());
                        mThumbnail = obj.getDataObject();
                        break;
                    case MAIN:
                        mMain = getInitiator().getObject(getHandle());
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
            } catch (ThetaException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
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
                getInitiator().deleteObject(getHandle(), PtpipInitiator.PARAMETER_VALUE_DEFAULT);
                PtpipInitiator.close();
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.IO_ERROR, e);
            } catch (ThetaException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
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
        public String getCreationTime() {
            return mDateTime;
        }

        @Override
        public long getCreationTimeWithUnixTime() {
            return mDateTimeUnix;
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

    }

    private static class VersionName {

        private static final Comparator<VersionName> COMPARATOR = new Comparator<VersionName>() {
            @Override
            public int compare(final VersionName v1, final VersionName v2) {
                VersionName a = v1;
                VersionName b = v2;
                if (a.getLength() > b.getLength()) {
                    VersionName tmp = a;
                    a = b;
                    b = tmp;
                }
                for (int i = 0; i < b.getLength(); i++) {
                    int valueA = (i <= a.getLength() - 1) ? a.mVersions[i] : 0;
                    int valueB = b.mVersions[i];
                    if (valueA > valueB) {
                        return 1;
                    } else if (valueA < valueB) {
                        return -1;
                    }
                }
                return 0;
            }
        };

        private final int[] mVersions;

        public VersionName(final String expression) {
            mVersions = parse(expression);
        }

        private int getLength() {
            return mVersions.length;
        }

        private static int[] parse(final String expression) {
            String[] versions = expression.split("\\.");
            int[] result = new int[versions.length];
            for (int i = 0; i < versions.length; i++) {
                result[i] = Integer.parseInt(versions[i]);
            }
            return result;
        }

        public boolean isSameOrLaterThan(final VersionName otherVersion) {
            return COMPARATOR.compare(this, otherVersion) > 0;
        }
    }

    private class ThetaImageRecorderM15 extends ThetaRecorderM15 {

        private static final String NAME = "THETA m15 - photo";
        private static final String MIME_TYPE = "image/jpeg";
        private static final int IMAGE_WIDTH = 2048;
        private static final int IMAGE_HEIGHT = 1024;

        public ThetaImageRecorderM15(final String id) {
            super(id, NAME, MIME_TYPE, IMAGE_WIDTH, IMAGE_HEIGHT);
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

    private class ThetaVideoRecorderM15 extends ThetaRecorderM15 {

        private static final String NAME = "THETA m15 - video";
        private static final String MIME_TYPE = "video/mov";
        private static final int IMAGE_WIDTH = 1920;
        private static final int IMAGE_HEIGHT = 1080;

        public ThetaVideoRecorderM15(final String id) {
            super(id, NAME, MIME_TYPE, IMAGE_WIDTH, IMAGE_HEIGHT);
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
                short status = getInitiator().getCaptureStatus();
                switch (status) {
                    case 1:
                        return RecorderState.RECORDING;
                    case 0:
                        return RecorderState.INACTIVE;
                    default:
                        return RecorderState.UNKNOWN;
                }
            }  catch (ThetaException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN, e);
            }
        }
    }

    private abstract class ThetaRecorderM15 implements Recorder {

        private final String mId;
        private final String mName;
        private final String mMimeType;
        private final int mImageWidth;
        private final int mImageHeight;

        public ThetaRecorderM15(final String id, final String name, final String mimeType,
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
    }
}
