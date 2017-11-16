package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class NumberParameterSpecTest {

    @Test
    public void testValidate_Mandatory_Default() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Optional_Default() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(false);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Defined() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Double[] {0.5d});
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Undefined() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Double[] {0.5d});
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(-0.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Enum_Null() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(false);
        builder.setEnum(new Double[] {0.5d});
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10.5d);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_TooLarge() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10.5d);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10.5d);
        builder.setExclusiveMaximum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_TooLarge() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaximum(10.5d);
        builder.setExclusiveMaximum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Min_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Min_TooSmall() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Min_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        builder.setExclusiveMinimum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Min_TooSmall() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        builder.setExclusiveMinimum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_Inclusive_Min_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Inclusive_Max_Exclusive_Min_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        builder.setExclusiveMinimum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_Inclusive_Min_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        builder.setExclusiveMaximum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Range_Exclusive_Max_Exclusive_Min_OK() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        builder.setExclusiveMaximum(true);
        builder.setExclusiveMinimum(true);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Optional_Range_Null() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(false);
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
