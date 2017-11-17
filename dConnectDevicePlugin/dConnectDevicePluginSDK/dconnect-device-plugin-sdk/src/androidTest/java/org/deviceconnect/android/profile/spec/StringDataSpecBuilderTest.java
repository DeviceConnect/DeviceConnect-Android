package org.deviceconnect.android.profile.spec;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StringDataSpecBuilderTest implements DConnectSpecConstants {

    @Test
    public void testBuild_Enum() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setEnum(new String[] {"a"});
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        assertThat(dataSpec.getEnum(), is(notNullValue()));
        assertThat(dataSpec.getMaxLength(), is(nullValue()));
        assertThat(dataSpec.getMinLength(), is(nullValue()));
    }

    @Test
    public void testBuild_Length() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMaxLength(1);
        builder.setMinLength(0);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        assertThat(dataSpec.getEnum(), is(nullValue()));
        assertThat(dataSpec.getMaxLength(), is(notNullValue()));
        assertThat(dataSpec.getMinLength(), is(notNullValue()));
    }

    @Test
    public void testBuild_Enum_Length() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setEnum(new String[] {"a"});
        builder.setMaxLength(1);
        builder.setMinLength(0);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        assertThat(dataSpec.getEnum(), is(notNullValue()));
        assertThat(dataSpec.getMaxLength(), is(nullValue()));
        assertThat(dataSpec.getMinLength(), is(nullValue()));
    }
}
