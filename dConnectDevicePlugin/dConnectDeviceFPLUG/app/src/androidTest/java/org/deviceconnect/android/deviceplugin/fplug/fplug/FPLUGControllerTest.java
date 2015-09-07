/*
 FPLUGControllerTest.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothDevice;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test class for FPLUGController.
 * <p>
 * NOTICE: This test is enable only when Bluetooth is available.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class FPLUGControllerTest {

    private final static String TAG = FPLUGControllerTest.class.getSimpleName();

    @Before
    public void waitForDisconnect() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFPLUGController() {
        BluetoothDevice fplug = getSampleFPLUG();
        FPLUGController controller = new FPLUGController(fplug);
        assertNotNull("unknown error", controller);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFPLUGController_DeviceIsNull() {
        new FPLUGController(null);
    }

    @Test
    public void testFPLUGController_Connect() {
        FPLUGController controller = getConnectedController();
        controller.disconnect();
    }

    //    @Test (Danger. If you want to do this test, you should manual run.)
    public void testFPLUGController_RequestInitPlug() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestInitPlug(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    //    @Test (Danger. If you want to do this test, you should manual run.)
    public void testFPLUGController_RequestCancelPairing() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestCancelPairing(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestWattHour() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestWattHour(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                List<WattHour> wattHourList = response.getWattHourList();
                assertNotNull(wattHourList);
                assertTrue(wattHourList.size() == 24);
                for (WattHour wh : wattHourList) {
                    int watt = wh.getWatt();
                    int hoursAgo = wh.getHoursAgo();
                    assertTrue("watt under limit", watt > -3276);
                    assertTrue("watt over limit", watt < 3276);
                    assertTrue("hours age over range", hoursAgo > 0 && hoursAgo < 25);
                    Log.d(TAG, "hour:" + hoursAgo + " watt:" + watt + " reliable:" + wh.isReliable());
                }
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestTemperature() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestTemperature(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                double temperature = response.getTemperature();
                assertTrue("temperature under limit", temperature > -273.3);
                assertTrue("temperature over limit", temperature < 3276.7);
                Log.d(TAG, "temperature:" + temperature);
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestHumidity() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestHumidity(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                int humidity = response.getHumidity();
                assertTrue("humidity under limit", humidity > -1);
                assertTrue("humidity over limit", humidity < 101);
                Log.d(TAG, "humidity:" + humidity);
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestIlluminance() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestIlluminance(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                int illuminance = response.getIlluminance();
                assertTrue("illuminance under limit", illuminance > -1);
                assertTrue("illuminance over limit", illuminance < 65534);
                Log.d(TAG, "illuminance:" + illuminance);
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestRealtimeWatt() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestRealtimeWatt(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                double watt = response.getRealtimeWatt();
                assertTrue("watt under limit", watt > -3276.8);
                assertTrue("watt over limit", watt < 3276.7);
                Log.d(TAG, "watt:" + watt);
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestPastWattHour() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestPastWattHour(Calendar.getInstance(), new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                List<WattHour> wattHourList = response.getWattHourList();
                assertNotNull(wattHourList);
                assertTrue(wattHourList.size() == 24);
                for (WattHour wh : wattHourList) {
                    assertTrue("watt under limit", wh.getWatt() > -3276);
                    assertTrue("watt over limit", wh.getWatt() < 3277);
                    assertTrue("hours age out of range", wh.getHoursAgo() > 0 && wh.getHoursAgo() < 25);
                    Log.d(TAG, "hour:" + wh.getHoursAgo() + " watt:" + wh.getWatt() + " reliable:" + wh.isReliable());
                }
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestPastValues() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestPastValues(Calendar.getInstance(), new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                List<PastValues> pastValuesList = response.getPastValuesList();
                assertNotNull(pastValuesList);
                assertTrue(pastValuesList.size() == 24);
                for (PastValues pv : pastValuesList) {
                    double temperature = pv.getTemperature();
                    int humidity = pv.getHumidity();
                    int illuminance = pv.getIlluminance();
                    int hoursAgo = pv.getHoursAgo();
                    assertTrue("temperature out of range", temperature > -273.3 && temperature < 3276.7);
                    assertTrue("humidity out of range", humidity > -1 && humidity < 65534);
                    assertTrue("illuminance out of limit", illuminance > -1 && illuminance < 65534);
                    assertTrue("hours age out of range", hoursAgo > 0 && hoursAgo < 25);
                    Log.d(TAG, "hour:" + hoursAgo + " temperature:" + temperature + " humidity:" + humidity + " illuminance:" + illuminance);
                }
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestSetDate() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestSetDate(Calendar.getInstance(), new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestLEDControlON() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestLEDControl(true, new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_RequestLEDControlOFF() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestLEDControl(false, new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                latch.countDown();
            }
        });

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_ManyRequest() {
        final int REQUEST_COUNT = 10;

        final CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);

        final FPLUGController controller = getConnectedController();
        FPLUGRequestCallback callback = new FPLUGRequestCallback() {
            @Override
            public void onSuccess(FPLUGResponse response) {
                double watt = response.getRealtimeWatt();
                Log.d(TAG, "watt:" + watt);
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError:" + message);
                latch.countDown();
            }

            @Override
            public void onTimeout() {
                controller.disconnect();
                fail();
            }
        };

        for (int i = 0; i < REQUEST_COUNT; i++) {
            controller.requestRealtimeWatt(callback);
        }

        waitAndClose(latch, controller);
    }

    @Test
    public void testFPLUGController_CalculateDiscomfortIndex() {
        final CountDownLatch latch = new CountDownLatch(1);

        final FPLUGController controller = getConnectedController();
        controller.requestTemperature(new RequestCallback(controller) {
            @Override
            public void onSuccess(FPLUGResponse response) {
                final double temperature = response.getTemperature();
                controller.requestHumidity(new RequestCallback(controller) {
                    @Override
                    public void onSuccess(FPLUGResponse response) {
                        super.onSuccess(response);

                        int humidity = response.getHumidity();

                        double discomfortIndex = (0.81 * temperature) + (0.01 * humidity) * ((0.99 * temperature) - 14.3) + 46.3;
                        Log.d(TAG, "discomfortIndex:" + discomfortIndex + " temperature:" + temperature + " humidity:" + humidity);
                        latch.countDown();

                        //discomfort index
                        //〜55   cold
                        //55〜60 little cold
                        //60〜65 no feel
                        //65〜70 comfort
                        //70〜75 not hot
                        //75〜80 little hot
                        //80〜85 hot
                        //85〜   very hot
                    }
                });
            }
        });

        waitAndClose(latch, controller);
    }

    private BluetoothDevice getSampleFPLUG() {
        List<BluetoothDevice> devices = FPLUGDiscover.getAll();
        assertNotNull("Bluetooth OFF or BLE not supported", devices);
        assertTrue("FPLUG not found", devices.size() > 0);
        return devices.get(0);
    }

    private FPLUGController getConnectedController() {
        final CountDownLatch waitForConnect = new CountDownLatch(1);
        BluetoothDevice fplug = getSampleFPLUG();
        FPLUGController controller = new FPLUGController(fplug);
        controller.connect(new FPLUGController.FPLUGConnectionListener() {
            @Override
            public void onConnected(String address) {
                waitForConnect.countDown();
            }

            @Override
            public void onDisconnected(String address) {
            }

            @Override
            public void onConnectionError(String address, String message) {
                fail();
            }
        });

        boolean isPassed = false;
        try {
            isPassed = waitForConnect.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("InterruptedException occurred");
        }
        if (!isPassed) {
            fail("Timeout occurred");
        }
        return controller;
    }

    private void waitAndClose(CountDownLatch latch, FPLUGController controller) {
        boolean isPassed = false;
        try {
            isPassed = latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            controller.disconnect();
            fail("InterruptedException occurred");
        }
        if (!isPassed) {
            controller.disconnect();
            fail("Timeout occurred");
        }
        controller.disconnect();
    }

    private class RequestCallback implements FPLUGRequestCallback {

        FPLUGController mController;

        public RequestCallback(FPLUGController controller) {
            mController = controller;
        }


        @Override
        public void onSuccess(FPLUGResponse response) {
            assertNotNull(response);
        }

        @Override
        public void onError(String message) {
            mController.disconnect();
            fail();
        }

        @Override
        public void onTimeout() {
            mController.disconnect();
            fail();
        }
    }


}
