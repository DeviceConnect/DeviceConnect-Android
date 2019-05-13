package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class BooleanDataSpecTest {

    @Test
    public void testValidate_Default() {
        BooleanDataSpec.Builder builder = new BooleanDataSpec.Builder();
        BooleanDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(true), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Defined() {
        BooleanDataSpec.Builder builder = new BooleanDataSpec.Builder();
        builder.setEnum(new Boolean[] {true});
        BooleanDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(true), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Undefined() {
        BooleanDataSpec.Builder builder = new BooleanDataSpec.Builder();
        builder.setEnum(new Boolean[] {true});
        BooleanDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(false), is(equalTo(false)));
    }
}
