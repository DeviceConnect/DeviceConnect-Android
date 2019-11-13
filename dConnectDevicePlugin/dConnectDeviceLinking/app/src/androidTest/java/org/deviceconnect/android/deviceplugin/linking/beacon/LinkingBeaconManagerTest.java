package org.deviceconnect.android.deviceplugin.linking.beacon;

import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.BatteryData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.GattData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.HumidityData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.RawData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.TemperatureData;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LinkingBeaconManagerTest {

    private static Context mContext;

    private static final int LINKING_EXTRA_ID = 12345;
    private static final int LINKING_VENDOR_ID = 1;
    private static final int LINKING_VERSION = 1;
    private static final long LINKING_TIME_STAMP = System.currentTimeMillis();
    private static final int LINKING_RSSI = -67;
    private static final int LINKING_TX_POWER = -59;
    private static final int LINKING_DISTANCE = 1;
    private static final float LINKING_ATMOSPHERIC_PRESSURE = 1008.0f;
    private static final boolean LINKING_LOW_BATTERY = false;
    private static final float LINKING_BATTERY_LEVEL = 68.0f;
    private static final float LINKING_HUMIDITY = 43.0f;
    private static final float LINKING_TEMPERATURE = 23.0f;
    private static final int LINKING_RAW_DATA = 1;
    private static final int LINKING_BUTTON_ID = 4;

    @BeforeClass
    public static void beforeClass() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void createLinkingBeaconManager() throws Exception {
        LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));
        mgr.destroy();
    }

    @Test
    public void getLinkingBeacons() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LinkingBeacon[] test = new LinkingBeacon[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                test[0] = beacon;
                latch.countDown();
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));

        List<LinkingBeacon> beacons = mgr.getLinkingBeacons();

        assertThat(beacons, is(notNullValue()));
        assertThat(beacons.get(0).getExtraId(), is(LINKING_EXTRA_ID));
        assertThat(beacons.get(0).getVendorId(), is(LINKING_VENDOR_ID));
        assertThat(beacons.get(0).getVersion(), is(LINKING_VERSION));
        assertThat(beacons.get(0).getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(beacons.get(0).getGattData().getRssi(), is(LINKING_RSSI));
        assertThat(beacons.get(0).getGattData().getTxPower(), is(LINKING_TX_POWER));
        assertThat(beacons.get(0).getGattData().getDistance(), is(LINKING_DISTANCE));
        assertThat(beacons.get(0).getAtmosphericPressureData().getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));
        assertThat(beacons.get(0).getBatteryData().isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(beacons.get(0).getBatteryData().getLevel(), is(LINKING_BATTERY_LEVEL));
        assertThat(beacons.get(0).getHumidityData().getValue(), is(LINKING_HUMIDITY));
        assertThat(beacons.get(0).getTemperatureData().getValue(), is(LINKING_TEMPERATURE));
        assertThat(beacons.get(0).getRawData().getValue(), is(LINKING_RAW_DATA));

        mgr.destroy();
    }

    @Test
    public void addOnBeaconConnectListener_connect() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LinkingBeacon[] test = new LinkingBeacon[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                test[0] = beacon;
                latch.countDown();
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0], notNullValue());
        assertThat(test[0].getExtraId(), is(LINKING_EXTRA_ID));
        assertThat(test[0].getVendorId(), is(LINKING_VENDOR_ID));
        assertThat(test[0].getVersion(), is(LINKING_VERSION));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test[0].getGattData().getRssi(), is(LINKING_RSSI));
        assertThat(test[0].getGattData().getTxPower(), is(LINKING_TX_POWER));
        assertThat(test[0].getGattData().getDistance(), is(LINKING_DISTANCE));
        assertThat(test[0].getAtmosphericPressureData().getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));
        assertThat(test[0].getBatteryData().isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(test[0].getBatteryData().getLevel(), is(LINKING_BATTERY_LEVEL));
        assertThat(test[0].getHumidityData().getValue(), is(LINKING_HUMIDITY));
        assertThat(test[0].getTemperatureData().getValue(), is(LINKING_TEMPERATURE));
        assertThat(test[0].getRawData().getValue(), is(LINKING_RAW_DATA));

        mgr.destroy();
    }

    @Test
    public void addOnBeaconConnectListener_disconnect() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LinkingBeacon[] test = new LinkingBeacon[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                test[0] = beacon;
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(40, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0], notNullValue());
        assertThat(test[0].isOnline(), is(false));
        assertThat(test[0].getExtraId(), is(LINKING_EXTRA_ID));
        assertThat(test[0].getVendorId(), is(LINKING_VENDOR_ID));
        assertThat(test[0].getVersion(), is(LINKING_VERSION));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test[0].getGattData().getRssi(), is(LINKING_RSSI));
        assertThat(test[0].getGattData().getTxPower(), is(LINKING_TX_POWER));
        assertThat(test[0].getGattData().getDistance(), is(LINKING_DISTANCE));
        assertThat(test[0].getAtmosphericPressureData().getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));
        assertThat(test[0].getBatteryData().isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(test[0].getBatteryData().getLevel(), is(LINKING_BATTERY_LEVEL));
        assertThat(test[0].getHumidityData().getValue(), is(LINKING_HUMIDITY));
        assertThat(test[0].getTemperatureData().getValue(), is(LINKING_TEMPERATURE));
        assertThat(test[0].getRawData().getValue(), is(LINKING_RAW_DATA));

        mgr.destroy();
    }

    @Test
    public void all_listener() throws Exception {
        final CountDownLatch latch = new CountDownLatch(8);
        final LinkingBeacon[] test1 = new LinkingBeacon[1];
        final GattData[] test2 = new GattData[1];
        final BatteryData[] test3 = new BatteryData[1];
        final int[] test4 = new int[1];
        final long[] test5 = new long[1];
        final AtmosphericPressureData[] test6 = new AtmosphericPressureData[1];
        final HumidityData[] test7 = new HumidityData[1];
        final TemperatureData[] test8 = new TemperatureData[1];
        final RawData[] test9 = new RawData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                test1[0] = beacon;
                latch.countDown();
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
            }
        });

        mgr.addOnBeaconProximityEventListener(new LinkingBeaconManager.OnBeaconProximityEventListener() {
            @Override
            public void onProximity(LinkingBeacon beacon, GattData gatt) {
                test2[0] = gatt;
                latch.countDown();
            }
        });

        mgr.addOnBeaconAtmosphericPressureEventListener(new LinkingBeaconManager.OnBeaconAtmosphericPressureEventListener() {
            @Override
            public void onAtmosphericPressure(LinkingBeacon beacon, AtmosphericPressureData atmosphericPressure) {
                test6[0] = atmosphericPressure;
                latch.countDown();
            }
        });

        mgr.addOnBeaconHumidityEventListener(new LinkingBeaconManager.OnBeaconHumidityEventListener() {
            @Override
            public void onHumidity(LinkingBeacon beacon, HumidityData humidity) {
                test7[0] = humidity;
                latch.countDown();
            }
        });

        mgr.addOnBeaconTemperatureEventListener(new LinkingBeaconManager.OnBeaconTemperatureEventListener() {
            @Override
            public void onTemperature(LinkingBeacon beacon, TemperatureData temperature) {
                test8[0] = temperature;
                latch.countDown();
            }
        });

        mgr.addOnBeaconBatteryEventListener(new LinkingBeaconManager.OnBeaconBatteryEventListener() {
            @Override
            public void onBattery(LinkingBeacon beacon, BatteryData battery) {
                test3[0] = battery;
                latch.countDown();
            }
        });

        mgr.addOnBeaconButtonEventListener(new LinkingBeaconManager.OnBeaconButtonEventListener() {
            @Override
            public void onClickButton(LinkingBeacon beacon, int keyCode, long timeStamp) {
                test4[0] = keyCode;
                test5[0] = timeStamp;
                latch.countDown();
            }
        });

        mgr.addOnBeaconRawDataEventListener(new LinkingBeaconManager.OnBeaconRawDataEventListener() {
            @Override
            public void onRawData(LinkingBeacon beacon, RawData rawData) {
                test9[0] = rawData;
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test1[0], notNullValue());
        assertThat(test1[0].getExtraId(), is(LINKING_EXTRA_ID));
        assertThat(test1[0].getVendorId(), is(LINKING_VENDOR_ID));
        assertThat(test1[0].getVersion(), is(LINKING_VERSION));
        assertThat(test1[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test2[0].getRssi(), is(LINKING_RSSI));
        assertThat(test2[0].getTxPower(), is(LINKING_TX_POWER));
        assertThat(test2[0].getDistance(), is(LINKING_DISTANCE));
        assertThat(test6[0].getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));
        assertThat(test3[0].isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(test3[0].getLevel(), is(LINKING_BATTERY_LEVEL));
        assertThat(test7[0].getValue(), is(LINKING_HUMIDITY));
        assertThat(test8[0].getValue(), is(LINKING_TEMPERATURE));
        assertThat(test9[0].getValue(), is(LINKING_RAW_DATA));
        assertThat(test4[0], is(LINKING_BUTTON_ID));
        assertThat(test5[0], is(LINKING_TIME_STAMP));

        mgr.destroy();
    }

    @Test
    public void atmosphericPressure() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtmosphericPressureData[] test = new AtmosphericPressureData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconAtmosphericPressureEventListener(new LinkingBeaconManager.OnBeaconAtmosphericPressureEventListener() {
            @Override
            public void onAtmosphericPressure(LinkingBeacon beacon, AtmosphericPressureData atmosphericPressure) {
                test[0] = atmosphericPressure;
                latch.countDown();
            }
        });

        sendDelay(mgr, createAtmosphericPressureIntent());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test[0].getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));

        mgr.destroy();
    }

    @Test
    public void temperature() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final TemperatureData[] test = new TemperatureData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconTemperatureEventListener(new LinkingBeaconManager.OnBeaconTemperatureEventListener() {
            @Override
            public void onTemperature(LinkingBeacon beacon, TemperatureData temperature) {
                test[0] = temperature;
                latch.countDown();
            }
        });

        sendDelay(mgr, createTemperatureIntent());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test[0].getValue(), is(LINKING_TEMPERATURE));

        mgr.destroy();
    }

    @Test
    public void humidity() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final HumidityData[] test = new HumidityData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconHumidityEventListener(new LinkingBeaconManager.OnBeaconHumidityEventListener() {
            @Override
            public void onHumidity(LinkingBeacon beacon, HumidityData humidity) {
                test[0] = humidity;
                latch.countDown();
            }
        });

        sendDelay(mgr, createHumidityIntent());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test[0].getValue(), is(LINKING_HUMIDITY));

        mgr.destroy();
    }

    @Test
    public void rawData() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final RawData[] test = new RawData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconRawDataEventListener(new LinkingBeaconManager.OnBeaconRawDataEventListener() {
            @Override
            public void onRawData(LinkingBeacon beacon, RawData rawData) {
                test[0] = rawData;
                latch.countDown();
            }
        });

        sendDelay(mgr, createRawDataIntent());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(test[0].getValue(), is(LINKING_RAW_DATA));

        mgr.destroy();
    }

    @Test
    public void proximity() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final GattData[] test = new GattData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconProximityEventListener(new LinkingBeaconManager.OnBeaconProximityEventListener() {
            @Override
            public void onProximity(LinkingBeacon beacon, GattData gatt) {
                test[0] = gatt;
                latch.countDown();
            }
        });

        sendDelay(mgr, createGatt());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test[0].getRssi(), is(LINKING_RSSI));
        assertThat(test[0].getTxPower(), is(LINKING_TX_POWER));
        assertThat(test[0].getDistance(), is(LINKING_DISTANCE));
        assertThat(test[0].getTimeStamp(), is(LINKING_TIME_STAMP));

        mgr.destroy();
    }

    @Test
    public void battery() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final BatteryData[] test1 = new BatteryData[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconBatteryEventListener(new LinkingBeaconManager.OnBeaconBatteryEventListener() {
            @Override
            public void onBattery(LinkingBeacon beacon, BatteryData battery) {
                test1[0] = battery;
                latch.countDown();
            }
        });

        sendDelay(mgr, createBatteryIntent());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test1[0].isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(test1[0].getLevel(), is(LINKING_BATTERY_LEVEL));

        mgr.destroy();
    }

    @Test
    public void buttonId() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] test1 = new int[1];
        final long[] test2 = new long[1];
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconButtonEventListener(new LinkingBeaconManager.OnBeaconButtonEventListener() {
            @Override
            public void onClickButton(LinkingBeacon beacon, int keyCode, long timeStamp) {
                test1[0] = keyCode;
                test2[0] = timeStamp;
                latch.countDown();
            }
        });

        sendDelay(mgr, createButtonIntent());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));
        assertThat(test1[0], is(LINKING_BUTTON_ID));
        assertThat(test2[0], is(LINKING_TIME_STAMP));

        mgr.destroy();
    }

    @Test
    public void findBeacon() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                latch.countDown();
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));

        LinkingBeacon beacon = mgr.findBeacon(LINKING_EXTRA_ID, LINKING_VENDOR_ID);

        assertThat(beacon, notNullValue());
        assertThat(beacon.getExtraId(), is(LINKING_EXTRA_ID));
        assertThat(beacon.getVendorId(), is(LINKING_VENDOR_ID));
        assertThat(beacon.getVersion(), is(LINKING_VERSION));
        assertThat(beacon.getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(beacon.getGattData().getRssi(), is(LINKING_RSSI));
        assertThat(beacon.getGattData().getTxPower(), is(LINKING_TX_POWER));
        assertThat(beacon.getGattData().getDistance(), is(LINKING_DISTANCE));
        assertThat(beacon.getAtmosphericPressureData().getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));
        assertThat(beacon.getBatteryData().isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(beacon.getBatteryData().getLevel(), is(LINKING_BATTERY_LEVEL));
        assertThat(beacon.getHumidityData().getValue(), is(LINKING_HUMIDITY));
        assertThat(beacon.getTemperatureData().getValue(), is(LINKING_TEMPERATURE));
        assertThat(beacon.getRawData().getValue(), is(LINKING_RAW_DATA));

        mgr.destroy();
    }

    @Test
    public void findBeacon_NotFound() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                latch.countDown();
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));

        LinkingBeacon beacon = mgr.findBeacon(-1, -1);

        assertThat(beacon, is(nullValue()));

        mgr.destroy();
    }

    @Test
    public void findBeacon_NotFound2() throws Exception {
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        LinkingBeacon beacon = mgr.findBeacon(-1, -1);
        assertThat(beacon, is(nullValue()));

        mgr.destroy();
    }

    @Test
    public void removeBeacon() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                latch.countDown();
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                latch.countDown();
            }
        });

        sendDelay(mgr, createAll());

        boolean wait = latch.await(10, TimeUnit.SECONDS);
        assertThat(wait, is(true));

        LinkingBeacon beacon = mgr.findBeacon(LINKING_EXTRA_ID, LINKING_VENDOR_ID);

        assertThat(beacon, notNullValue());
        assertThat(beacon.getExtraId(), is(LINKING_EXTRA_ID));
        assertThat(beacon.getVendorId(), is(LINKING_VENDOR_ID));
        assertThat(beacon.getVersion(), is(LINKING_VERSION));
        assertThat(beacon.getTimeStamp(), is(LINKING_TIME_STAMP));
        assertThat(beacon.getGattData().getRssi(), is(LINKING_RSSI));
        assertThat(beacon.getGattData().getTxPower(), is(LINKING_TX_POWER));
        assertThat(beacon.getGattData().getDistance(), is(LINKING_DISTANCE));
        assertThat(beacon.getAtmosphericPressureData().getValue(), is(LINKING_ATMOSPHERIC_PRESSURE));
        assertThat(beacon.getBatteryData().isLowBatteryFlag(), is(LINKING_LOW_BATTERY));
        assertThat(beacon.getBatteryData().getLevel(), is(LINKING_BATTERY_LEVEL));
        assertThat(beacon.getHumidityData().getValue(), is(LINKING_HUMIDITY));
        assertThat(beacon.getTemperatureData().getValue(), is(LINKING_TEMPERATURE));
        assertThat(beacon.getRawData().getValue(), is(LINKING_RAW_DATA));

        mgr.removeBeacon(beacon);

        beacon = mgr.findBeacon(LINKING_EXTRA_ID, LINKING_VENDOR_ID);

        assertThat(beacon, is(nullValue()));

        mgr.destroy();
    }

    @Test
    public void removeBeacon_null() throws Exception {
        final LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));

        try {
            mgr.removeBeacon(null);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }

        mgr.destroy();
    }
    private void sendDelay(final LinkingBeaconManager mgr, final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mgr.onReceivedBeacon(intent);
            }
        }).start();
    }

    private Intent createAll() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.ATMOSPHERIC_PRESSURE, LINKING_ATMOSPHERIC_PRESSURE);
        intent.putExtra(LinkingBeaconUtil.LOW_BATTERY, LINKING_LOW_BATTERY);
        intent.putExtra(LinkingBeaconUtil.BATTERY_LEVEL, LINKING_BATTERY_LEVEL);
        intent.putExtra(LinkingBeaconUtil.HUMIDITY, LINKING_HUMIDITY);
        intent.putExtra(LinkingBeaconUtil.TEMPERATURE, LINKING_TEMPERATURE);
        intent.putExtra(LinkingBeaconUtil.RAW_DATA, LINKING_RAW_DATA);
        intent.putExtra(LinkingBeaconUtil.BUTTON_ID, LINKING_BUTTON_ID);
        return intent;
    }

    private Intent createGatt() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        return intent;
    }

    private Intent createBatteryIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.BATTERY_LEVEL, LINKING_BATTERY_LEVEL);
        intent.putExtra(LinkingBeaconUtil.LOW_BATTERY, LINKING_LOW_BATTERY);
        return intent;
    }

    private Intent createAtmosphericPressureIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.ATMOSPHERIC_PRESSURE, LINKING_ATMOSPHERIC_PRESSURE);
        return intent;
    }


    private Intent createTemperatureIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.TEMPERATURE, LINKING_TEMPERATURE);
        return intent;
    }

    private Intent createHumidityIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.HUMIDITY, LINKING_HUMIDITY);
        return intent;
    }

    private Intent createRawDataIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.RAW_DATA, LINKING_RAW_DATA);
        return intent;
    }


    private Intent createButtonIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDevicePluginService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        intent.putExtra(LinkingBeaconUtil.BUTTON_ID, LINKING_BUTTON_ID);
        return intent;
    }
}
