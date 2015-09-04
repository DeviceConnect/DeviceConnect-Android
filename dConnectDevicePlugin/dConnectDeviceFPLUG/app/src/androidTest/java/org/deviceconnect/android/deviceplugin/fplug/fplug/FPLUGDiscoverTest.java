/*
 FPLUGDiscoverTest.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothDevice;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for FPLUGDiscover.
 * <p>
 * NOTICE: This test is enable only when Bluetooth is available.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class FPLUGDiscoverTest {

    @Test
    public void testFPLUGDiscover() {
        List<BluetoothDevice> devices = FPLUGDiscover.getAll();
        assertNotNull("Bluetooth OFF or BLE not supported", devices);
        assertTrue("F-PLUG not found", devices.size() > 0);
    }

}
