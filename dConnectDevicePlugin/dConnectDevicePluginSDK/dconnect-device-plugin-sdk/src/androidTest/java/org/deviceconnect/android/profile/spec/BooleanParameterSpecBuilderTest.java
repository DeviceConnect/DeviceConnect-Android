package org.deviceconnect.android.profile.spec;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class BooleanParameterSpecBuilderTest implements DConnectSpecConstants {

    @Test
    public void testBuild() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(true);
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.getEnum(), is(nullValue()));
    }

    @Test
    public void testBuild_WithEnum() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Boolean[] {true});
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.getEnum(), is(notNullValue()));
    }
}
