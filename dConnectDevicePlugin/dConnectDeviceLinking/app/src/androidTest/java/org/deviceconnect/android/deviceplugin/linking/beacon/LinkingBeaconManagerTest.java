package org.deviceconnect.android.deviceplugin.linking.beacon;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.GattData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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
        mContext = new RenamingDelegatingContext(
                InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
    }

    @Test
    public void createLinkingBeaconManager() throws Exception {
        LinkingBeaconManager mgr = new LinkingBeaconManager(mContext);
        assertThat(mgr, is(notNullValue()));
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
        intent.setClass(mContext, LinkingDeviceService.class);
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
        return intent;
    }

    private Intent createGatt() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDeviceService.class);
        intent.putExtra(LinkingBeaconUtil.EXTRA_ID, LINKING_EXTRA_ID);
        intent.putExtra(LinkingBeaconUtil.VENDOR_ID, LINKING_VENDOR_ID);
        intent.putExtra(LinkingBeaconUtil.VERSION, LINKING_VERSION);
        intent.putExtra(LinkingBeaconUtil.TIME_STAMP, LINKING_TIME_STAMP);
        intent.putExtra(LinkingBeaconUtil.RSSI, LINKING_RSSI);
        intent.putExtra(LinkingBeaconUtil.TX_POWER, LINKING_TX_POWER);
        intent.putExtra(LinkingBeaconUtil.DISTANCE, LINKING_DISTANCE);
        return intent;
    }

    private Intent createButtonIntent() {
        Intent intent = new Intent();
        intent.setAction(LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT);
        intent.setClass(mContext, LinkingDeviceService.class);
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
