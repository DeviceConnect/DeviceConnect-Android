package org.deviceconnect.android.deviceplugin.heartrate.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.InstrumentationRegistry.getContext;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BleDeviceDetectorTest {
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBleDeviceDetector() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        assertNotNull(detector);
    }

    @Test
    public void testBleDeviceDetector_ContextIsNull() {
        try {
            new BleDeviceDetector(null);
            fail("not occurred exception");
        } catch (NullPointerException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Illegal exception");
        }
    }

    @Test
    public void testInitialize() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        detector.initialize();
        assertTrue(true);
    }

    @Test
    public void testSetListener() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        detector.setListener(new BleDeviceDetector.BleDeviceDiscoveryListener() {
            @Override
            public void onDiscovery(List<BluetoothDevice> devices) {
            }
        });
        assertTrue(true);
    }

    @Test
    public void testSetListener_ListenerIsNull() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        detector.setListener(null);
        assertTrue(true);
    }

    @Test
    public void testIsScanning() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        assertEquals(false, detector.isScanning());
    }

    @Test
    public void testIsScanning_StartScan() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        try {
            detector.startScan();
            assertEquals(true, detector.isScanning());
        } finally {
            detector.stopScan();
        }
    }

    @Test
    public void testIsEnabled() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext());
        assertEquals(true, detector.isEnabled());
    }

    @Test
    public void testStartScan() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        BleDeviceDetector detector = new BleDeviceDetector(getContext(),
                new BleDeviceAdapterFactory() {
                    @Override
                    public BleDeviceAdapter createAdapter(Context context) {
                        BleDeviceAdapter adapter = new BleDeviceAdapter() {
                            @Override
                            public void startScan(BleDeviceScanCallback callback) {
                                // TODO make a mock.
                            }
                            @Override
                            public void stopScan(BleDeviceScanCallback callback) {
                            }
                            @Override
                            public BluetoothDevice getDevice(String address) {
                                return null;
                            }
                            @Override
                            public Set<BluetoothDevice> getBondedDevices() {
                                return null;
                            }
                            @Override
                            public boolean isEnabled() {
                                return true;
                            }
                            @Override
                            public boolean checkBluetoothAddress(String address) {
                                return false;
                            }
                        };
                        return adapter;
                    }
                });
        detector.setListener(new BleDeviceDetector.BleDeviceDiscoveryListener() {
            @Override
            public void onDiscovery(List<BluetoothDevice> devices) {
                countDownLatch.countDown();
            }
        });

        try {
            detector.startScan();
            assertEquals(true, countDownLatch.await(20, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("no discovery");
        } finally {
            detector.stopScan();
        }
    }

    @Test
    public void testStartScan_ScanAlreadyStarted() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        BleDeviceDetector detector = new BleDeviceDetector(getContext(),
                new BleDeviceAdapterFactory() {
                    @Override
                    public BleDeviceAdapter createAdapter(Context context) {
                        BleDeviceAdapter adapter = new BleDeviceAdapter() {
                            @Override
                            public void startScan(BleDeviceScanCallback callback) {
                                // TODO make a mock.
                            }
                            @Override
                            public void stopScan(BleDeviceScanCallback callback) {
                            }
                            @Override
                            public BluetoothDevice getDevice(String address) {
                                return null;
                            }
                            @Override
                            public Set<BluetoothDevice> getBondedDevices() {
                                return null;
                            }
                            @Override
                            public boolean isEnabled() {
                                return true;
                            }
                            @Override
                            public boolean checkBluetoothAddress(String address) {
                                return false;
                            }
                        };
                        return adapter;
                    }
                });
        detector.setListener(new BleDeviceDetector.BleDeviceDiscoveryListener() {
            @Override
            public void onDiscovery(List<BluetoothDevice> devices) {
                countDownLatch.countDown();
            }
        });

        try {
            detector.startScan();
            detector.startScan();
            assertEquals(true, countDownLatch.await(20, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("no discovery");
        } finally {
            detector.stopScan();
        }
    }

    @Test
    public void testStopScan() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        BleDeviceDetector detector = new BleDeviceDetector(getContext(),
                new BleDeviceAdapterFactory() {
                    @Override
                    public BleDeviceAdapter createAdapter(Context context) {
                        BleDeviceAdapter adapter = new BleDeviceAdapter() {
                            @Override
                            public void startScan(BleDeviceScanCallback callback) {
                                // TODO make a mock.
                            }
                            @Override
                            public void stopScan(BleDeviceScanCallback callback) {
                            }
                            @Override
                            public BluetoothDevice getDevice(String address) {
                                return null;
                            }
                            @Override
                            public Set<BluetoothDevice> getBondedDevices() {
                                return null;
                            }
                            @Override
                            public boolean isEnabled() {
                                return true;
                            }
                            @Override
                            public boolean checkBluetoothAddress(String address) {
                                return false;
                            }
                        };
                        return adapter;
                    }
                });
        detector.setListener(new BleDeviceDetector.BleDeviceDiscoveryListener() {
            @Override
            public void onDiscovery(List<BluetoothDevice> devices) {
                countDownLatch.countDown();
            }
        });

        try {
            detector.startScan();
            detector.stopScan();
            assertEquals(false, countDownLatch.await(20, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("no discovery");
        } finally {
        }
    }

    @Test
    public void testStopScan_NoStart() {
        BleDeviceDetector detector = new BleDeviceDetector(getContext(),
                new BleDeviceAdapterFactory() {
                    @Override
                    public BleDeviceAdapter createAdapter(Context context) {
                        BleDeviceAdapter adapter = new BleDeviceAdapter() {
                            @Override
                            public void startScan(BleDeviceScanCallback callback) {
                                // TODO make a mock.
                            }
                            @Override
                            public void stopScan(BleDeviceScanCallback callback) {
                            }
                            @Override
                            public BluetoothDevice getDevice(String address) {
                                return null;
                            }
                            @Override
                            public Set<BluetoothDevice> getBondedDevices() {
                                return null;
                            }
                            @Override
                            public boolean isEnabled() {
                                return true;
                            }
                            @Override
                            public boolean checkBluetoothAddress(String address) {
                                return false;
                            }
                        };
                        return adapter;
                    }
                });
        detector.setListener(new BleDeviceDetector.BleDeviceDiscoveryListener() {
            @Override
            public void onDiscovery(List<BluetoothDevice> devices) {
            }
        });

        try {
            detector.stopScan();
            assertTrue(true);
        } catch (Exception e) {
            fail("no discovery");
        }
    }
}