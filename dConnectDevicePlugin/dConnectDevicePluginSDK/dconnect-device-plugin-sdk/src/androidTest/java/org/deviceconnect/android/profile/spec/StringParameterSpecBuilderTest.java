package org.deviceconnect.android.profile.spec;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class StringParameterSpecBuilderTest implements DConnectSpecConstants {

    @Test
    public void testBuild_Enum() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new String[] {"a"});
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        assertThat(dataSpec.getEnum(), is(notNullValue()));
        assertThat(dataSpec.getMaxLength(), is(nullValue()));
        assertThat(dataSpec.getMinLength(), is(nullValue()));
    }

    @Test
    public void testBuild_Length() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaxLength(1);
        builder.setMinLength(0);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        assertThat(dataSpec.getEnum(), is(nullValue()));
        assertThat(dataSpec.getMaxLength(), is(notNullValue()));
        assertThat(dataSpec.getMinLength(), is(notNullValue()));
    }

    @Test
    public void testBuild_Enum_Length() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new String[] {"a"});
        builder.setMaxLength(1);
        builder.setMinLength(0);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        assertThat(dataSpec.getEnum(), is(notNullValue()));
        assertThat(dataSpec.getMaxLength(), is(nullValue()));
        assertThat(dataSpec.getMinLength(), is(nullValue()));
    }
}
