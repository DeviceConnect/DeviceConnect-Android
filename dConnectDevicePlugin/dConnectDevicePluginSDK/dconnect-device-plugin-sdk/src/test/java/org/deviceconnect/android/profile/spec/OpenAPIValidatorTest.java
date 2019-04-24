package org.deviceconnect.android.profile.spec;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.models.DataType;
import org.deviceconnect.android.profile.spec.models.Items;
import org.deviceconnect.android.profile.spec.models.parameters.AbstractParameter;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class OpenAPIValidatorTest {
    @RunWith(PluginSDKTestRunner.class)
    public static class IntegerTest {
        /**
         * 整数を指定してエラーが発生しないこと。
         */
        @Test
        public void test() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "11");
            assertThat(result, is(true));
        }

        /**
         * 実数を指定された時にエラーになること。
         */
        @Test
        public void testNumber() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "5.5f");
            assertThat(result, is(false));
        }

        /**
         * 文字列を指定された時にエラーになること。
         */
        @Test
        public void testString() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 指定された最大値を超えるパラメータを渡してエラーになること。
         */
        @Test
        public void testMaximum() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setMaximum(10);

            result = OpenAPIValidator.validate(p, 5);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 100);
            assertThat(result, is(false));
        }

        /**
         * exclusiveMaximum を true に指定した時に最大値が含まれないこと。
         */
        @Test
        public void testExclusiveMaximumWithTrue() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setMaximum(10);
            p.setExclusiveMaximum(true);

            boolean result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(false));
        }

        /**
         * exclusiveMaximum を false に指定した時に最大値が含まれること。
         */
        @Test
        public void testExclusiveMaximumWithFalse() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setMaximum(10);
            p.setExclusiveMaximum(false);

            boolean result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));
        }

        /**
         * 指定された最小値を超えるパラメータを渡してエラーになること。
         */
        @Test
        public void testMinimum() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setMinimum(10);

            result = OpenAPIValidator.validate(p, 5);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 100);
            assertThat(result, is(true));
        }

        /**
         * exclusiveMinimum を true に指定した時に最小値が含まれないこと。
         */
        @Test
        public void testExclusiveMinimumWithTrue() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setMinimum(10);
            p.setExclusiveMinimum(true);

            boolean result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(false));
        }

        /**
         * exclusiveMinimum を false に指定した時に最小値が含まれること。
         */
        @Test
        public void testExclusiveMinimumWithFalse() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setMinimum(10);
            p.setExclusiveMinimum(false);

            boolean result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));
        }

        /**
         * enum に定義されている値が指定されること
         */
        @Test
        public void testEnum() {
            boolean result;

            List<Object> enums = new ArrayList<>();
            enums.add(1);
            enums.add(2);
            enums.add(3);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.INTEGER);
            p.setEnum(enums);

            result = OpenAPIValidator.validate(p, 2);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(false));
        }
    }

    @RunWith(PluginSDKTestRunner.class)
    public static class NumberTest {
        /**
         * 実数を指定してエラーが発生しないこと。
         */
        @Test
        public void test() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "5.5f");
            assertThat(result, is(true));
        }

        /**
         * 整数を指定された時にエラーになること。
         */
        @Test
        public void testNumber() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10");
            assertThat(result, is(true));
        }

        /**
         * 文字列を指定された時にエラーになること。
         */
        @Test
        public void testString() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 指定された最大値を超えるパラメータを渡してエラーになること。
         */
        @Test
        public void testMaximum() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setMaximum(5.5f);

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 100);
            assertThat(result, is(false));
        }

        /**
         * exclusiveMaximum を true に指定した時に最大値が含まれないこと。
         */
        @Test
        public void testExclusiveMaximumWithTrue() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setMaximum(5.5f);
            p.setExclusiveMaximum(true);

            boolean result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(false));
        }

        /**
         * exclusiveMaximum を false に指定した時に最大値が含まれること。
         */
        @Test
        public void testExclusiveMaximumWithFalse() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setMaximum(5.5f);
            p.setExclusiveMaximum(false);

            boolean result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));
        }

        /**
         * 指定された最小値を超えるパラメータを渡してエラーになること。
         */
        @Test
        public void testMinimum() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setMinimum(5.5f);

            result = OpenAPIValidator.validate(p, 5.0f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 100.0f);
            assertThat(result, is(true));
        }

        /**
         * exclusiveMinimum を true に指定した時に最小値が含まれないこと。
         */
        @Test
        public void testExclusiveMinimumWithTrue() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setMinimum(5.5f);
            p.setExclusiveMinimum(true);

            boolean result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(false));
        }

        /**
         * exclusiveMinimum を false に指定した時に最小値が含まれること。
         */
        @Test
        public void testExclusiveMinimumWithFalse() {
            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setMinimum(5.5f);
            p.setExclusiveMinimum(false);

            boolean result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));
        }

        /**
         * enum に定義されている値が指定されること
         */
        @Test
        public void testEnum() {
            boolean result;

            List<Object> enums = new ArrayList<>();
            enums.add(1.5f);
            enums.add(2.5f);
            enums.add(3.5f);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.NUMBER);
            p.setEnum(enums);

            result = OpenAPIValidator.validate(p, 1.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(false));
        }
    }

    @RunWith(PluginSDKTestRunner.class)
    public static class StringTest {
        /**
         * 文字列を指定してエラーが発生しないこと。
         */
        @Test
        public void test() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.STRING);

            result = OpenAPIValidator.validate(p, "text");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "5.5f");
            assertThat(result, is(true));
        }

        /**
         * 文字数の最大数を設定してエラーが発生しないこと。
         */
        @Test
        public void testMaxLength() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.STRING);
            p.setMaxLength(10);

            result = OpenAPIValidator.validate(p, "012345678");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "0123456789");
            assertThat(result, is(false));
        }

        /**
         * 文字数の最小数を設定してエラーが発生しないこと。
         */
        @Test
        public void testMinLength() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.STRING);
            p.setMinLength(10);

            result = OpenAPIValidator.validate(p, "0123456789");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "01234567890");
            assertThat(result, is(true));
        }

        /**
         * 文字数のパターンを設定してエラーが発生しないこと。
         */
        @Test
        public void testPattern() {
            boolean result;

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.STRING);
            p.setPattern("[a-z]+");

            result = OpenAPIValidator.validate(p, "abcdefg");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "01234567890");
            assertThat(result, is(false));
        }

        /**
         * enum に定義されている値が指定されること
         */
        @Test
        public void testEnum() {
            boolean result;

            List<Object> enums = new ArrayList<>();
            enums.add("sample");
            enums.add("example");
            enums.add("test");

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.STRING);
            p.setEnum(enums);

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "no-define");
            assertThat(result, is(false));
        }
    }

    @RunWith(PluginSDKTestRunner.class)
    public static class ArrayTest {
        /**
         * 配列を指定してエラーが発生しないこと。
         */
        @Test
        public void test() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10,123,1");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "text");
            assertThat(result, is(false));
        }

        /**
         * allowEmptyValue を true にして、空配列を渡してエラーが発生しないこと。
         */
        @Test
        public void testAllowEmptyValueWithTrue() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setAllowEmptyValue(true);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(true));
        }

        /**
         * allowEmptyValue を false にして、空配列を渡してエラーが発生しないこと。
         */
        @Test
        public void testAllowEmptyValueWithFalse() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setAllowEmptyValue(false);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(false));
        }

        /**
         * 配列の最大数を設定してエラーが発生しないこと。
         */
        @Test
        public void testMaxItems() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setMaxItems(5);

            result = OpenAPIValidator.validate(p, "10,123,1,12");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10,123,1,12,32");
            assertThat(result, is(false));
        }

        /**
         * 配列の最小数を設定してエラーが発生しないこと。
         */
        @Test
        public void testMinItems() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setMinItems(5);

            result = OpenAPIValidator.validate(p, "10,123,1,12");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "10,123,1,12,32");
            assertThat(result, is(true));
        }

        /**
         * 数値の配列に文字列を渡してエラーが発生しないこと。
         */
        @Test
        public void testArrayString() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 配列の要素のユニーク設定 uniqueItems を true に設定してエラーが発生しないこと。
         */
        @Test
        public void testUniqueItemsWithTrue() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setUniqueItems(true);

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,1,3");
            assertThat(result, is(false));
        }

        /**
         * collectionFormat に csv を設定してエラーが発生しないこと。
         */
        @Test
        public void testCollectionFormatWithCSV() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("csv");

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1\t2\t3");
            assertThat(result, is(false));
        }

        /**
         * collectionFormat に ssv を設定してエラーが発生しないこと。
         */
        @Test
        public void testCollectionFormatWithSSV() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("ssv");

            result = OpenAPIValidator.validate(p, "1 2 3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(false));
        }

        /**
         * collectionFormat に tsv を設定してエラーが発生しないこと。
         */
        @Test
        public void testCollectionFormatWithTSV() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("tsv");

            result = OpenAPIValidator.validate(p, "1\t2\t3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(false));
        }

        /**
         * collectionFormat に pipes を設定してエラーが発生しないこと。
         */
        @Test
        public void testCollectionFormatWithPIPES() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            AbstractParameter p = new AbstractParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("pipes");

            result = OpenAPIValidator.validate(p, "1|2|3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(false));
        }
    }
}
