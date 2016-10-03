package org.deviceconnect.android.deviceplugin.theta.core;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThetaDeviceClient {

    private final ThetaDeviceManager mDeviceMgr;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public ThetaDeviceClient(final ThetaDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
    }

    public boolean hasDevice(final String id) {
        return mDeviceMgr.getConnectedDeviceById(id) != null;
    }

    public void fetchAllObjectList(final String id, final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    listener.onObjectList(device.fetchAllObjectList());
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void takePicture(final String deviceId, final String target,
                            final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(deviceId);
                    ThetaDevice.Recorder recorder = device.getRecorder();
                    if (recorder == null) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_RECORDER, "THETA has no recorder");
                    }
                    if (target != null && !recorder.getId().equals(target)) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_RECORDER, "Invalid recorder ID.");
                    }
                    if (!recorder.supportsPhoto()) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_SUPPORTED_FEATURE,
                            recorder.getName() + " does not support to take a photo.");
                    }
                    ThetaObject obj = device.takePicture();
                    listener.onTakenPicture(obj);
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void startVideoRecording(final String id, final String target, final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    ThetaDevice.Recorder recorder = device.getRecorder();
                    if (recorder == null) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_RECORDER, "THETA has no recorder");
                    }
                    if (target != null && !recorder.getId().equals(target)) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_RECORDER, "Invalid recorder ID.");
                    }
                    if (!recorder.supportsVideoRecording()) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_SUPPORTED_FEATURE,
                            recorder.getName() + " does not support video recording.");
                    }
                    ThetaDevice.RecorderState state = recorder.getState();
                    boolean hadStarted = false;
                    switch (state) {
                        case INACTIVE:
                            device.startVideoRecording();
                            break;
                        case RECORDING:
                            hadStarted = true;
                            break;
                        default:
                            break;
                    }
                    listener.onStartedVideoRecording(recorder, hadStarted);
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void stopVideoRecording(final String id, final String target, final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    ThetaDevice.Recorder recorder = device.getRecorder();
                    if (recorder == null) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_RECORDER, "THETA has no recorder");
                    }
                    if (target != null && !recorder.getId().equals(target)) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_RECORDER, "Invalid recorder ID.");
                    }
                    if (!recorder.supportsVideoRecording()) {
                        throw new ThetaDeviceException(ThetaDeviceException.NOT_SUPPORTED_FEATURE,
                            recorder.getName() + " does not support video recording.");
                    }
                    ThetaDevice.RecorderState state = recorder.getState();
                    boolean hadStopped = false;
                    switch (state) {
                        case INACTIVE:
                            hadStopped = true;
                            break;
                        case RECORDING:
                            device.stopVideoRecording();
                            break;
                        default:
                            break;
                    }
                    listener.onStoppedVideoRecording(recorder, hadStopped);
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void getBatteryLevel(final String id, final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    listener.onBatteryLevel(device.getBatteryLevel());
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void getShootingMode(final String id, final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    listener.onShootingMode(device.getShootingMode());
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void changeShootingMode(final String id, final ThetaDevice.ShootingMode mode,
                                   final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    device.changeShootingMode(mode);
                    listener.onShootingModeChanged();
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void fetchRecorder(final String id, final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    listener.onRecorder(device.getRecorder());
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void fetchObject(final String id, final String fileName,
                            final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    List<ThetaObject> list = device.fetchAllObjectList();
                    for (ThetaObject obj : list) {
                        if (obj.getFileName().equals(fileName)) {
                            obj.fetch(ThetaObject.DataType.MAIN);
                            byte[] data = obj.getMainData();
                            obj.clear(ThetaObject.DataType.MAIN);
                            listener.onObjectFetched(data, obj.getMimeType());
                            return;
                        }
                    }
                    throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_OBJECT);
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public void removeObject(final String id, final String fileName,
                             final ResponseListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThetaDevice device = getConnectedDevice(id);
                    List<ThetaObject> list = device.fetchAllObjectList();
                    for (ThetaObject obj : list) {
                        if (obj.getFileName().equals(fileName)) {
                            obj.remove();
                            listener.onObjectRemoved();
                            return;
                        }
                    }
                    throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_OBJECT);
                } catch (ThetaDeviceException e) {
                    listener.onFailed(e);
                }
            }
        });
    }

    public ThetaDevice getConnectedDevice(final String id) throws ThetaDeviceException {
        ThetaDevice device = mDeviceMgr.getConnectedDeviceById(id);
        if (device == null) {
            throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_THETA);
        }
        return device;
    }

    public ThetaDevice getCurrentConnectDevice() {
        return mDeviceMgr.getConnectedDevice();
    }

    public void execute(final Runnable r) {
        mExecutor.execute(r);
    }

    public interface ResponseListener {

        void onModel(ThetaDeviceModel model);

        void onObjectList(List<ThetaObject> list);

        void onTakenPicture(ThetaObject picture);

        void onStartedVideoRecording(ThetaDevice.Recorder recorder, boolean hadStarted);

        void onStoppedVideoRecording(ThetaDevice.Recorder recorder, boolean hadStopped);

        void onBatteryLevel(double level);

        void onRecorder(ThetaDevice.Recorder recorder);

        void onShootingMode(ThetaDevice.ShootingMode mode);

        void onShootingModeChanged();

        void onObjectFetched(byte[] data, String mimeType);

        void onObjectRemoved();

        void onLivePreview(LiveCamera camera);

        void onFailed(ThetaDeviceException cause);

    }

    public static class DefaultListener implements ResponseListener {

        @Override
        public void onModel(final ThetaDeviceModel model) {
        }

        @Override
        public void onObjectList(final List<ThetaObject> list) {
        }

        @Override
        public void onTakenPicture(final ThetaObject picture) {
        }

        @Override
        public void onStartedVideoRecording(final ThetaDevice.Recorder recorder, final boolean hadStarted) {
        }

        @Override
        public void onStoppedVideoRecording(final ThetaDevice.Recorder recorder, final boolean hadStopped) {
        }

        @Override
        public void onBatteryLevel(final double level) {
        }

        @Override
        public void onRecorder(final ThetaDevice.Recorder recorder) {
        }

        @Override
        public void onShootingMode(final ThetaDevice.ShootingMode mode) {
        }

        @Override
        public void onShootingModeChanged() {
        }

        @Override
        public void onObjectFetched(final byte[] data, final String mimeType) {
        }

        @Override
        public void onObjectRemoved() {
        }

        @Override
        public void onLivePreview(final LiveCamera camera) {
        }

        @Override
        public void onFailed(final ThetaDeviceException cause) {
        }

    }

}
