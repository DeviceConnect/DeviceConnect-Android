/*
 NoteNameTableTest.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link NoteNameTable} の単体テスト.
 */
public class NoteNameTableTest {

    @Test
    public void testNumberToNameSuccess() {
        for (int i = 0; i < 128; i++) {
            String name = NoteNameTable.numberToName(i);
            Assert.assertNotNull(name);
            System.out.println(i + " -> " + name);
        }
    }

    @Test
    public void testNumberToNameErrorTooSmallNumber() {
        String name = NoteNameTable.numberToName(-1);
        Assert.assertNull(name);
    }

    @Test
    public void testNumberToNameErrorTooBigNumber() {
        String name = NoteNameTable.numberToName(129);
        Assert.assertNull(name);
    }

    @Test
    public void testNameToNumberSuccess() {
        for (int i = 0; i < 128; i++) {
            String name = NoteNameTable.numberToName(i);
            Integer number = NoteNameTable.nameToNumber(name);
            Assert.assertNotNull(number);
            Assert.assertEquals(i, number.intValue());
            System.out.println(name + " -> " + number);
        }
    }

    @Test
    public void testNumberToNameErrorUnknownName() {
        Integer number = NoteNameTable.nameToNumber("");
        Assert.assertNull(number);
    }
}
