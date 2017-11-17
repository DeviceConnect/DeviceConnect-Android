package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class IntegerParameterSpecTest {

    @Test
    public void testValidate_Mandatory_Default() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Optional_Default() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(false);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Defined() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Long[] {1L});
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Undefined() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Long[] {1L});
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Enum_Null() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(false);
        builder.setEnum(new Long[] {1L});
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10L);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_TooLarge() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10L);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10L);
        builder.setExclusiveMaximum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_TooLarge() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10L);
        builder.setExclusiveMaximum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Min_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Min_TooSmall() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Min_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        builder.setExclusiveMinimum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Min_TooSmall() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        builder.setExclusiveMinimum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_Inclusive_Min_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_Exclusive_Min_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        builder.setExclusiveMinimum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_Inclusive_Min_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        builder.setExclusiveMaximum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_Exclusive_Min_OK() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        builder.setExclusiveMaximum(true);
        builder.setExclusiveMinimum(true);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Optional_Range_Null() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(false);
        builder.setMinimum(10L);
        builder.setMinimum(1L);
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
