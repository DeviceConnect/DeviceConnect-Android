/*
 OpenAPIValidator.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Property;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.parameters.BodyParameter;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;

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

        Operation operation = findOperationSpec(swagger, request);
        if (operation != null) {
            return validate(operation, request);
        }
        // TODO API 定義が見つからない場合は true で良いか？
        return true;
    }

    /**
     * リクエストで指定された API 定義から Path を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Path が存在しない場合には null を返却します。
     * </p>
     *
     * @param swagger API 定義
     * @param request リクエスト
     * @return Path
     */
    public static Path findPathSpec(Swagger swagger, Intent request) {
        for (String key : swagger.getPaths().getKeySet()) {
            String path1 = createPath(swagger, key);
            String path2 = createPath(request);
            if (path1.equalsIgnoreCase(path2)) {
                return swagger.getPaths().getPath(key);
            }
        }
        return null;
    }

    /**
     * リクエストで指定された API 定義から Operation を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param swagger API 定義
     * @param request リクエスト
     * @return Operation
     */
    public static Operation findOperationSpec(Swagger swagger, Intent request) {
        for (String key : swagger.getPaths().getKeySet()) {
            String path1 = createPath(swagger, key);
            String path2 = createPath(request);
            if (path1.equalsIgnoreCase(path2)) {
                Method method = Method.fromAction(request.getAction());
                if (method != null) {
                    return swagger.getPaths().getPath(key).getOperation(method);
                }
            }
        }
        return null;
    }

    /**
     * 定義ファイルからパスを作成します.
     *
     * @param swagger 定義ファイル
     * @param path パス
     * @return パス
     */
    private static String createPath(Swagger swagger, String path) {
        if (path != null && path.endsWith("/")) {
            // 最後に / が付いている場合は削除
            path = path.substring(0, path.length() - 1);
        }

        String basePath = swagger.getBasePath();
        if (basePath != null) {
            return basePath + path;
        } else {
            return path;
        }
    }

    /**
     * リクエストからパスを作成します.
     *
     * @param request リクエスト
     * @return パス
     */
    private static String createPath(Intent request) {
        String apiName = DConnectProfile.getApi(request);
        String profileName = DConnectProfile.getProfile(request);
        String interfaceName = DConnectProfile.getInterface(request);
        String attributeName = DConnectProfile.getAttribute(request);

        StringBuilder path = new StringBuilder();

        if (apiName != null) {
            path.append("/").append(apiName);
        }

        if (profileName != null) {
            path.append("/").append(profileName);
        }

        if (interfaceName != null) {
            path.append("/").append(interfaceName);
        }

        if (attributeName != null) {
            path.append("/").append(attributeName);
        }

        return path.toString();
    }

    /**
     * リクエストを妥当性を確認します.
     *
     * <p>
     * 送られてきたリクエストのパラメータに仕様にないパラメータが存在した場合には特にチェックは行わずに妥当とします。
     * </p>
     *
     * @param operation 操作API
     * @param request リクエスト
     * @return 妥当なリクエストの場合はtrue、それ以外はfalse
     */
    public static boolean validate(Operation operation, Intent request) {
        Bundle extras = request.getExtras();
        if (extras == null) {
            return true;
        }

        for (Parameter parameter : operation.getParameters()) {
            Object value = extras.get(parameter.getName());
            if (!validate(parameter, value)) {
                return false;
            }
        }
        return true;
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
        if (property.getType() == null) {
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
                return false;
        }
    }

    /**
     * Integer のパラメータの妥当性を確認します.
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
                throw new IllegalStateException();
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

        String arrayParam = value.toString();
        if (arrayParam.equals("")) {
            // 空の配列が許可されているか
            return property.isAllowEmptyValue();
        }

        String[] array;
        switch (property.getCollectionFormat()) {
            default:
            case "csv":
                array = arrayParam.split(",");
                break;
            case "ssv":
                array = arrayParam.split(" ");
                break;
            case "tsv":
                array = arrayParam.split("\t");
                break;
            case "pipes":
                array = arrayParam.split("|");
                break;
            case "multi":
                // TODO 未実装
                return false;
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
