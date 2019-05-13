package org.deviceconnect.android.profile.spec;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class BooleanDataSpecBuilderTest implements DConnectSpecConstants {

    @Test
    public void testBuild() {
        BooleanDataSpec.Builder builder = new BooleanDataSpec.Builder();
        BooleanDataSpec dataSpec = builder.build();

        assertThat(dataSpec.getEnum(), is(nullValue()));
    }

    @Test
    public void testBuild_WithEnum() {
        BooleanDataSpec.Builder builder = new BooleanDataSpec.Builder();
        builder.setEnum(new Boolean[] {true});
        BooleanDataSpec dataSpec = builder.build();

        assertThat(dataSpec.getEnum(), is(notNullValue()));
    }
}
