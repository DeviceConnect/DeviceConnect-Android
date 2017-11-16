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
    public void testValidate_Default() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1L), is(equalTo(true)));
    }

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

    @Test
    public void testValidate_Range_Inclusive_Max_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMaximum(10L);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_TooLarge() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMaximum(10L);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMaximum(10L);
        builder.setExclusiveMaximum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_TooLarge() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMaximum(10L);
        builder.setExclusiveMaximum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Inclusive_Min_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Inclusive_Min_TooSmall() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Exclusive_Min_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        builder.setExclusiveMinimum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Min_TooSmall() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        builder.setExclusiveMinimum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_Inclusive_Min_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_Exclusive_Min_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        builder.setExclusiveMinimum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_Inclusive_Min_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        builder.setExclusiveMaximum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_Exclusive_Min_OK() {
        IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        builder.setExclusiveMaximum(true);
        builder.setExclusiveMinimum(true);
        IntegerDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }
}
