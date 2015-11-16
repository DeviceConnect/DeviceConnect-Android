package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;
import com.theta360.lib.ptpip.entity.ObjectHandles;
import com.theta360.lib.ptpip.entity.ObjectInfo;
import com.theta360.lib.ptpip.entity.PtpObject;
import com.theta360.lib.ptpip.eventlistener.PtpipEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.net.SocketFactory;

class ThetaM15 extends AbstractThetaDevice {

    private static final String HOST = "192.168.1.1";

    private static final String BRAND_SAMSUNG = "samsung";

    private static final String MANUFACTURER_SAMSUNG = "samsung";

    private final SocketFactory mSocketFactory;

    ThetaM15(final Context context, final String name) {
        super(name);
        mSocketFactory = getWifiSocketFactory(context);
    }

    private PtpipInitiator getInitiator() throws ThetaException, IOException {
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
            return result;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
                    }
                    lockObj.countDown();
                }
            });
            lockObj.await();

            if (photo[0] != null) {
                return photo[0];
            } else {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            }
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (InterruptedException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
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
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        }
    }

    @Override
    public double getBatteryLevel() throws ThetaDeviceException {
        try {
            return getInitiator().getBatteryLevel().getValue() / 100d;
        } catch (IOException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        } catch (ThetaException e) {
            throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
        }
    }

    @Override
    public void changeShootingMode(final ShootingMode mode) throws ThetaDeviceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getLivePreview() throws ThetaDeviceException {
        throw new UnsupportedOperationException();
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

        private final String mDate;

        private final int mWidth;

        private final int mHeight;

        private byte[] mThumbnail;

        private byte[] mMain;

        public ThetaObjectM15(final int handle, final String filename, final String date, final int width, final int height) {
            mObjectHandle = handle;
            mFilename = filename;
            mDate = date;
            mWidth = width;
            mHeight = height;
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
                        PtpipInitiator.close();
                        break;
                    case MAIN:
                        mMain = getInitiator().getObject(getHandle());
                        PtpipInitiator.close();
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            } catch (ThetaException e) {
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
                getInitiator().deleteObject(getHandle(), PtpipInitiator.PARAMETER_VALUE_DEFAULT);
                PtpipInitiator.close();
            } catch (IOException e) {
                throw new ThetaDeviceException(ThetaDeviceException.UNKNOWN);
            } catch (ThetaException e) {
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
        public String getCreationTime() {
            return mDate;
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
}
