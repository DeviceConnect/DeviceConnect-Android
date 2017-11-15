package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class NumberDataSpecTest {

    @Test
    public void testValidate_Enum_Defined() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setEnum(new Double[] {0.5d});
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Undefined() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setEnum(new Double[] {0.5d});
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1.5d), is(equalTo(false)));
    }
}
