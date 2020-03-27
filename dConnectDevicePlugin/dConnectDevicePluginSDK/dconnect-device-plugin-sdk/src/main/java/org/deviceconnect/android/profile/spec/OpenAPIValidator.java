/*
 OpenAPIValidator.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Property;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.parameters.BodyParameter;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API 定義されたにリクエストが妥当なパラメータか確認するためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class OpenAPIValidator {

    /**
     * RGB のパターン解析用の正規表現.
     */
    private static final Pattern RGB_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");

    /**
     * trueの文字列.
     */
    private static final String TRUE = "true";

    /**
     * falseの文字列.
     */
    private static final String FALSE = "false";

    private OpenAPIValidator() {
    }

    /**
     * リクエストを妥当性を確認します.
     *
     * <p>
     * 送られてきたリクエストのパラメータに仕様にないパラメータが存在した場合には特にチェックは行わずに妥当とします。
     * </p>
     *
     * @param swagger 操作API
     * @param request リクエスト
     * @return 妥当なリクエストの場合はtrue、それ以外はfalse
     */
    public static boolean validate(Swagger swagger, Intent request) {
        Bundle extras = request.getExtras();
        if (extras == null) {
            return true;
        }

        List<Parameter> parameters = findParameters(swagger, request);
        for (Parameter parameter : parameters) {
            Object value = extras.get(parameter.getName());
            if (!validate(parameter, value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 仕様定義されているパラメータの仕様のリストを取得します.
     *
     * <p>
     * Operation > Path > Root の順に parameters のリストに追加します。<br>
     * 同じ名前のパラメータが存在する場合はリストには追加しません。
     * </p>
     *
     * @param swagger 仕様定義
     * @param request リクエスト
     * @return パラメータの仕様リスト
     */
    public static List<Parameter> findParameters(Swagger swagger, Intent request) {
        List<Parameter> parameters = new ArrayList<>();

        Operation operation = DConnectServiceSpec.findOperationSpec(swagger, request);
        if (operation != null && operation.getParameters() != null) {
            parameters.addAll(operation.getParameters());
        }

        Path path = DConnectServiceSpec.findPathSpec(swagger, request);
        if (path != null && path.getParameters() != null) {
            for (Parameter parameter : path.getParameters()) {
                if (!parameters.contains(parameter)) {
                    parameters.add(parameter);
                }
            }
        }

        if (swagger.getParameters() != null) {
            for (Parameter parameter : swagger.getParameters().values()) {
                if (!parameters.contains(parameter)) {
                    parameters.add(parameter);
                }
            }
        }

        return parameters;
    }

    /**
     * リクエストされたパラメータが妥当か確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return パラメータが妥当な場合はtrue、それ以外はfalse
     */
    public static boolean validate(Parameter parameter, Object value) {
        if (value == null) {
            // パラメータが必須の場合は不正
            return !parameter.isRequired();
        }

        if (parameter instanceof BodyParameter) {
            return validateProperty(((BodyParameter) parameter).getSchema(), value);
        } else {
            return validateProperty((Property) parameter, value);
        }
    }

    /**
     * リクエストされたパラメータが妥当か確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return パラメータが妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateProperty(Property property, Object value) {
        if (property == null || property.getType() == null) {
            // TODO 定義ファイルのフォーマットエラー
            return true;
        }

        switch (property.getType()) {
            case INTEGER:
                return validateInteger(property, value);
            case NUMBER:
                return validateNumber(property, value);
            case STRING:
                return validateString(property, value);
            case ARRAY:
                return validateArray(property, value);
            case BOOLEAN:
                return validateBoolean(property, value);
            case FILE:
                return validateFile(property, value);
            default:
                // TODO 定義ファイルのフォーマットエラー
                return true;
        }
    }

    /**
     * Integer のパラメータの妥当性を確認します.
     *
     * <p>
     * 整数値の妥当性を確認しますが、リクエストされたパラメータ値が
     * 整数値の文字列の場合も妥当とみなします。
     * </p>
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInteger(Property property, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            // 文字列を数値に変換できるか確認
            try {
                value = formatInteger(property.getFormat(), (String) value);
            } catch (Exception e) {
                return false;
            }
        }

        if (!(value instanceof Integer || value instanceof Long)) {
            return false;
        }

        // format が省略された場合は、int で処理を行う
        if (property.getFormat() == null) {
            return validateInt32(property, ((Number) value).intValue());
        }

        switch (property.getFormat()) {
            case INT32:
                return validateInt32(property, ((Number) value).intValue());
            case INT64:
                return validateInt64(property,  ((Number) value).longValue());
            default:
                // TODO 定義ファイルのフォーマットエラー
                return true;
        }
    }

    /**
     * int のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInt32(Property property, int value) {
        if (property.getEnum() != null) {
            return validateEnum(property.getEnum(), value);
        }

        if (property.getMaximum() != null) {
            int maximum = property.getMaximum().intValue();
            if (!(property.isExclusiveMaximum() ? (maximum > value) : (maximum >= value))) {
                return false;
            }
        }

        if (property.getMinimum() != null) {
            int minimum = property.getMinimum().intValue();
            if (!(property.isExclusiveMinimum() ? (minimum < value) : (minimum <= value))) {
                return false;
            }
        }

        if (property.getMultipleOf() != null) {
            int multipleOf = property.getMultipleOf().intValue();
            if (multipleOf != 0 && value % multipleOf != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * long のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInt64(Property property, long value) {
        if (property.getEnum() != null) {
            return validateEnum(property.getEnum(), value);
        }

        if (property.getMaximum() != null) {
            long maximum = property.getMaximum().longValue();
            if (!(property.isExclusiveMaximum() ? (maximum > value) : (maximum >= value))) {
                return false;
            }
        }

        if (property.getMinimum() != null) {
            long minimum = property.getMinimum().longValue();
            if (!(property.isExclusiveMinimum() ? (minimum < value) : (minimum <= value))) {
                return false;
            }
        }

        if (property.getMultipleOf() != null) {
            long multipleOf = property.getMultipleOf().longValue();
            if (multipleOf != 0 && value % multipleOf != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Number のパラメータの妥当性を確認します.
     *
     * <p>
     * 実数値の妥当性を確認しますが、リクエストされたパラメータ値が
     * 実数値の文字列の場合も妥当とみなします。
     * </p>
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateNumber(Property property, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            // 文字列を数値に変換できるか確認
            try {
                value = formatNumber(property.getFormat(), (String) value);
            } catch (Exception e) {
                return false;
            }
        }

        if (!(value instanceof Number)) {
            return false;
        }

        if (property.getFormat() == null) {
            return validateFloat(property, ((Number) value).floatValue());
        }

        switch (property.getFormat()) {
            case FLOAT:
                return validateFloat(property, ((Number) value).floatValue());
            case DOUBLE:
                return validateDouble(property, ((Number) value).doubleValue());
            default:
                // TODO 定義ファイルのフォーマットエラー
                return true;
        }
    }

    /**
     * float のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateFloat(Property property, float value) {
        if (property.getEnum() != null) {
            return validateEnum(property.getEnum(), value);
        }

        if (property.getMaximum() != null) {
            float maximum = property.getMaximum().floatValue();
            if (!(property.isExclusiveMaximum() ? (maximum > value) : (maximum >= value))) {
                return false;
            }
        }

        if (property.getMinimum() != null) {
            float minimum = property.getMinimum().floatValue();
            if (!(property.isExclusiveMinimum() ? (minimum < value) : (minimum <= value))) {
                return false;
            }
        }

        return true;
    }

    /**
     * double のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateDouble(Property property, double value) {
        if (property.getEnum() != null) {
            return validateEnum(property.getEnum(), value);
        }

        if (property.getMaximum() != null) {
            double maximum = property.getMaximum().doubleValue();
            if (!(property.isExclusiveMaximum() ? (maximum > value) : (maximum >= value))) {
                return false;
            }
        }
        if (property.getMinimum() != null) {
            double minimum = property.getMinimum().doubleValue();
            if (!(property.isExclusiveMinimum() ? (minimum < value) : (minimum <= value))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 文字列のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateString(Property property, Object value) {
        if (value == null) {
            return true;
        }

        if (!(value instanceof String)) {
            return false;
        }

        if (property.getEnum() != null) {
            return validateEnum(property.getEnum(), value);
        }

        if (property.getPattern() != null) {
            Pattern p = Pattern.compile(property.getPattern());
            Matcher m = p.matcher((String) value);
            if (!m.find()) {
                return false;
            }
        }

        if (property.getFormat() == null) {
            return validateLength(property, (String) value);
        }

        switch (property.getFormat()) {
            case TEXT:
                return validateLength(property, (String) value);
            case BYTE:
            case BINARY:
                return validateBinary((String) value);
            case DATE:
                return validateDateTime((String) value);
            case DATE_TIME:
                return validateDateTime((String) value);
            case RGB:
                return validateRGB((String) value);
            default:
                // TODO 定義ファイルのフォーマットエラー
                return true;
        }
    }

    /**
     * 文字列の長さの妥当性を確認します.
     *
     * <p>
     * TODO 最大値、最小値を含むのか、含まないのか仕様がなかったので、ここでは含まないようにしています。
     * </p>
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateLength(Property property, String value) {
        Integer maxLength = property.getMaxLength();
        Integer minLength = property.getMinLength();
        int stringLength = value.length();
        return (maxLength == null || stringLength < maxLength) &&
                (minLength == null || stringLength > minLength);
    }

    /**
     * バイナリーの妥当性を確認します.
     *
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateBinary(final String value) {
        // TODO 未実装
        // バイナリのサイズ確認 (現状、プラグインにはURL形式で通知される)
        return true;
    }

    /**
     * 日付の妥当性を確認します.
     *
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateDateTime(final String value) {
        // TODO 未実装
        // RFC3339 形式であることの確認を実装すること。
        return true;
    }

    /**
     * RGB の妥当性を確認します.
     *
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateRGB(String value) {
        return RGB_PATTERN.matcher(value).matches();
    }

    /**
     * 配列の妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateArray(Property property, Object value) {
        if (value == null) {
            return true;
        }

        // TODO 文字列以外の数値も文字列に変換して使用しているが問題ないかを確認すること。

        String arrayValue = value.toString();
        if (arrayValue.equals("")) {
            // 空の配列が許可されているか
            return property.isAllowEmptyValue();
        }

        String[] array;
        switch (property.getCollectionFormat()) {
            default:
            case "csv":
                array = splitString(arrayValue,",", 0);
                break;
            case "ssv":
                array = splitString(arrayValue," ", 0);
                break;
            case "tsv":
                array = splitString(arrayValue,"\t", 0);
                break;
            case "pipes":
                array = splitString(arrayValue,"|", 0);
                break;
            case "multi":
                // Device Connect では同じパラメータ名があった場合には後勝ちになるので使用できない。
                return true;
        }

        if (property.getMaxItems() != null) {
            if (array.length >= property.getMaxItems()) {
                return false;
            }
        }

        if (property.getMinItems() != null) {
            if (array.length < property.getMinItems()) {
                return false;
            }
        }

        if (property.isUniqueItems()) {
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (array[i].equals(array[j])) {
                        return false;
                    }
                }
            }
        }

        for (String v : array) {
            if (!validateProperty(property.getItems(), v)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 文字列を分割して配列に変換します.
     *
     * <p>
     * {@link String#split(String)} で分割しようとしたが、pipe が正規表現の
     * 文字のために使用できなかったので、分割するメソッドを自作しました。
     * </p>
     *
     * @param str 分割する文字列
     * @param delimiter 区切り文字
     * @param limit 分割する文字数
     * @return 分割された文字列
     */
    private static String[] splitString(String str, String delimiter, int limit) {
        List<String> strings = new ArrayList<>();
        int delimiterLen = delimiter.length();
        if (limit <= 0) {
            limit = str.length();
        }

        if (limit > 0) {
            int start = 0;
            int end;
            for (int i = 1; i < limit; i++) {
                end = str.indexOf(delimiter, start);
                if (end < 0) {
                    break;
                }
                strings.add(str.substring(start, end));
                start = end + delimiterLen;
            }
            strings.add(str.substring(start));
        } else {
            int start;
            int end = str.length();
            for (int i = -1; i > limit; i--) {
                start = str.lastIndexOf(delimiter, end - 1);
                if (start < 0) {
                    break;
                }
                strings.add(str.substring(start + delimiterLen, end));
                end = start;
            }
            strings.add(str.substring(0, end));
        }
        return strings.toArray(new String[0]);
    }

    /**
     * boolean のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateBoolean(Property property, Object value) {
        if (value == null) {
            return true;
        }

        if (property.getEnum() != null) {
            return validateEnum(property.getEnum(), value);
        }

        if (value instanceof String) {
            return TRUE.equalsIgnoreCase((String) value) || FALSE.equalsIgnoreCase((String) value);
        }
        return (value instanceof Boolean);
    }

    /**
     * File のパラメータの妥当性を確認します.
     *
     * @param property パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateFile(Property property, Object value) {
        // TODO 未実装
        return true;
    }

    /**
     * 指定された値が enum の中に存在するか確認します.
     *
     * @param enums enumに定義されたリスト
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateEnum(List<Object> enums, Object value) {
        for (Object e : enums) {
            if (e != null && e.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定された文字列を Integer もしくは Long に変換します.
     *
     * <p>
     * 変換できない場合には、そのまま返却します。
     * </p>
     *
     * @param format データフォーマット
     * @param value 文字列
     * @return 変換されたオブジェクト
     */
    private static Object formatInteger(DataFormat format, String value) {
        if (format == null) {
            return Integer.parseInt(value);
        } else {
            switch (format) {
                case INT32:
                    return Integer.parseInt(value);
                case INT64:
                    return Long.parseLong(value);
                default:
                    // TODO 定義ファイルのフォーマットエラー
                    break;
            }
        }
        return value;
    }

    /**
     * 指定された文字列を Integer もしくは Long に変換します.
     *
     * <p>
     * 変換できない場合には、そのまま返却します。
     * </p>
     *
     * @param format データフォーマット
     * @param value 文字列
     * @return 変換されたオブジェクト
     */
    private static Object formatNumber(DataFormat format, String value) {
        if (format == null) {
            return Float.parseFloat(value);
        } else {
            switch (format) {
                case FLOAT:
                    return Float.parseFloat(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                default:
                    // TODO 定義ファイルのフォーマットエラー
                    break;
            }
        }
        return value;
    }
}
