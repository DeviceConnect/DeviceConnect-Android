package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class IntegerDataSpecTest {

    @Test
    public void testValidate_Enum_Defined() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setEnum(new Long[] {1L});
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Undefined() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setEnum(new Long[] {1L});
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0L), is(equalTo(false)));
    }
}
