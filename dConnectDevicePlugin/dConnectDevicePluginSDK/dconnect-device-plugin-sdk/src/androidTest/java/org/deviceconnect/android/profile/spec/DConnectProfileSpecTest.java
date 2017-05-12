package org.deviceconnect.android.profile.spec;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DConnectProfileSpecTest extends InstrumentationTestCase {

    @Test
    public void testDeepCopy() throws Exception {
        Bundle src = new Bundle();
        src.putInt("int", 1);
        src.putLong("long", 1L);
        src.putDouble("double", 1d);
        src.putBoolean("boolean", true);
        src.putString("string", "");
        src.putIntArray("intArray", new int[] {1, 1, 1});
        src.putLongArray("longArray", new long[] {1, 1, 1});
        src.putDoubleArray("doubleArray", new double[] {1, 1, 1});
        src.putBooleanArray("booleanArray", new boolean[] {true, true, true});
        src.putStringArray("stringArray", new String[] {"", "", ""});
        Bundle srcObj = new Bundle();
        srcObj.putInt("int", 1);
        srcObj.putLong("long", 1L);
        srcObj.putDouble("double", 1d);
        srcObj.putBoolean("boolean", true);
        srcObj.putString("string", "");
        srcObj.putIntArray("intArray", new int[] {1, 1, 1});
        srcObj.putLongArray("longArray", new long[] {1, 1, 1});
        srcObj.putDoubleArray("doubleArray", new double[] {1, 1, 1});
        srcObj.putBooleanArray("booleanArray", new boolean[] {true, true, true});
        srcObj.putStringArray("stringArray", new String[] {"", "", ""});
        src.putBundle("object", srcObj);

        Bundle dst = new Bundle();
        DConnectProfileSpec.deepCopy(src, dst);

        assertThat(dst.getInt("int"), is(1));
        assertThat(dst.getLong("long"), is(1L));
        assertThat(dst.getDouble("double"), is(1d));
        assertThat(dst.getBoolean("boolean"), is(true));
        assertThat(dst.getString("string"), is(""));
        assertThat(dst.getIntArray("intArray"), is(new int[] {1, 1, 1}));
        assertThat(dst.getLongArray("longArray"), is(new long[] {1, 1, 1}));
        assertThat(dst.getDoubleArray("doubleArray"), is(new double[] {1, 1, 1}));
        assertThat(dst.getBooleanArray("booleanArray"), is(new boolean[] {true, true, true}));
        assertThat(dst.getStringArray("stringArray"), is(new String[] {"", "", ""}));
        Bundle dstObj = dst.getBundle("object");
        assertThat(dstObj.getInt("int"), is(1));
        assertThat(dstObj.getLong("long"), is(1L));
        assertThat(dstObj.getDouble("double"), is(1d));
        assertThat(dstObj.getBoolean("boolean"), is(true));
        assertThat(dstObj.getString("string"), is(""));
        assertThat(dstObj.getIntArray("intArray"), is(new int[] {1, 1, 1}));
        assertThat(dstObj.getLongArray("longArray"), is(new long[] {1, 1, 1}));
        assertThat(dstObj.getDoubleArray("doubleArray"), is(new double[] {1, 1, 1}));
        assertThat(dstObj.getBooleanArray("booleanArray"), is(new boolean[] {true, true, true}));
        assertThat(dstObj.getStringArray("stringArray"), is(new String[] {"", "", ""}));
    }
}
