package org.deviceconnect.android.profile.spec;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Schema;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.parameters.AbstractParameter;
import org.deviceconnect.android.profile.spec.models.parameters.BodyParameter;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;

import java.util.List;
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
        // TODO 定義ファイルが見つからない場合
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
        // TODO 定義ファイルが見つからない場合
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
            Object paramValue = extras.get(parameter.getName());
            if (!validate(parameter, paramValue)) {
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
            return validateSchema(((BodyParameter) parameter).getSchema(), value);
        } else {
            AbstractParameter ap = (AbstractParameter) parameter;
            if (ap.getType() == null) {
                // TODO フォーマットエラー
                return true;
            }

            switch (ap.getType()) {
                case INTEGER:
                    return validateInteger(ap, value);
                case NUMBER:
                    return validateNumber(ap, value);
                case STRING:
                    return validateString(ap, value);
                case ARRAY:
                    return validateArray(ap, value);
                case BOOLEAN:
                    return validateBoolean(ap, value);
                case FILE:
                    return validateFile(ap, value);
                default:
                    return false;
            }
        }
    }

    /**
     * Integer のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInteger(AbstractParameter parameter, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            // 文字列を数値に変換できるか確認
            value = formatInteger(parameter.getFormat(), (String) value);
        }

        if (!(value instanceof Number)) {
            return false;
        }

        // format が省略された場合は、int で処理を行う
        if (parameter.getFormat() == null) {
            return validateInt32(parameter, ((Number) value).intValue());
        }

        switch (parameter.getFormat()) {
            case INT32:
                return validateInt32(parameter, ((Number) value).intValue());
            case INT64:
                return validateInt64(parameter,  ((Number) value).longValue());
            default:
                // TODO フォーマットエラー
                return true;
        }
    }

    /**
     * int のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInt32(AbstractParameter parameter, int value) {
        if (parameter.getEnum() != null) {
            return validateEnum(parameter.getEnum(), value);
        }

        boolean isValid = true;
        if (parameter.getMaximum() != null) {
            int maximum = parameter.getMaximum().intValue();
            isValid = parameter.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (parameter.getMinimum() != null) {
            int minimum = parameter.getMinimum().intValue();
            isValid &= parameter.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * long のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInt64(AbstractParameter parameter, long value) {
        if (parameter.getEnum() != null) {
            return validateEnum(parameter.getEnum(), value);
        }

        boolean isValid = true;
        if (parameter.getMaximum() != null) {
            long maximum = parameter.getMaximum().longValue();
            isValid = parameter.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (parameter.getMinimum() != null) {
            long minimum = parameter.getMinimum().longValue();
            isValid &= parameter.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * Number のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateNumber(AbstractParameter parameter, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            // 文字列を数値に変換できるか確認
            value = formatNumber(parameter.getFormat(), (String) value);
        }

        if (!(value instanceof Number)) {
            return false;
        }

        if (parameter.getFormat() == null) {
            return validateFloat(parameter, ((Number) value).floatValue());
        }

        switch (parameter.getFormat()) {
            case FLOAT:
                return validateFloat(parameter, ((Number) value).floatValue());
            case DOUBLE:
                return validateDouble(parameter, ((Number) value).doubleValue());
            default:
                // TODO フォーマットエラー
                return true;
        }
    }

    /**
     * float のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateFloat(AbstractParameter parameter, float value) {
        if (parameter.getEnum() != null) {
            return validateEnum(parameter.getEnum(), value);
        }

        boolean isValid = true;
        if (parameter.getMaximum() != null) {
            float maximum = parameter.getMaximum().floatValue();
            isValid = parameter.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (parameter.getMinimum() != null) {
            float minimum = parameter.getMinimum().floatValue();
            isValid &= parameter.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * double のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateDouble(AbstractParameter parameter, double value) {
        if (parameter.getEnum() != null) {
            return validateEnum(parameter.getEnum(), value);
        }

        boolean isValid = true;
        if (parameter.getMaximum() != null) {
            double maximum = parameter.getMaximum().doubleValue();
            isValid = parameter.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (parameter.getMinimum() != null) {
            double minimum = parameter.getMinimum().doubleValue();
            isValid &= parameter.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * 文字列のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateString(AbstractParameter parameter, Object value) {
        if (value == null) {
            return true;
        }

        if (!(value instanceof String)) {
            return false;
        }

        if (parameter.getEnum() != null) {
            return validateEnum(parameter.getEnum(), value);
        }

        if (parameter.getPattern() != null) {
            // TODO 未実装
            // 正規表現のマッチングを実装すること
        }

        if (parameter.getFormat() == null) {
            return validateLength(parameter, (String) value);
        }

        switch (parameter.getFormat()) {
            case TEXT:
                return validateLength(parameter, (String) value);
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
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateLength(AbstractParameter parameter, String value) {
        Integer maxLength = parameter.getMaxLength();
        Integer minLength = parameter.getMinLength();
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
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateArray(AbstractParameter parameter, Object value) {
        if (value == null) {
            return true;
        }

        String arrayParam = value.toString();
        if (arrayParam.equals("")) {
            return parameter.isAllowEmptyValue();
        }

        String[] array;
        switch (parameter.getCollectionFormat()) {
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

        for (String v : array) {
            if (!validateSchema(parameter.getItems(), v)) {
                return false;
            }
        }

        return true;
    }

    /**
     * boolean のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateBoolean(AbstractParameter parameter, Object value) {
        if (value == null) {
            return true;
        }

        if (parameter.getEnum() != null) {
            return validateEnum(parameter.getEnum(), value);
        }

        if (value instanceof String) {
            return TRUE.equalsIgnoreCase((String) value) || FALSE.equalsIgnoreCase((String) value);
        }
        return (value instanceof Boolean);
    }

    /**
     * File のパラメータの妥当性を確認します.
     *
     * @param parameter パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateFile(AbstractParameter parameter, Object value) {
        // TODO 未実装
        return true;
    }

    /**
     * Enum の妥当性を確認します.
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

    private static boolean validateSchema(Schema schema, Object value) {
        if (value == null) {
            return true;
        }

        if (schema.getType() == null) {
            // TODO フォーマットエラー
            return false;
        }

        switch (schema.getType()) {
            case INTEGER:
                return validateInteger(schema, value);
            case NUMBER:
                return validateNumber(schema, value);
            case STRING:
                return validateString(schema, value);
            case ARRAY:
                // TODO 未実装
//                return validateArray(schema, value);
                return true;
            case BOOLEAN:
                return validateBoolean(schema, value);
            case FILE:
                return validateFile(schema, value);
            default:
                return false;
        }
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
            }
        }
        return value;
    }

    /**
     * Integer のパラメータの妥当性を確認します.
     *
     * @param schema パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInteger(Schema schema, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            // 文字列を数値に変換できるか確認
            value = formatInteger(schema.getFormat(), (String) value);
        }

        if (!(value instanceof Number)) {
            return false;
        }

        // format が省略された場合は、int で処理を行う
        if (schema.getFormat() == null) {
            return validateInt32(schema, ((Number) value).intValue());
        }

        switch (schema.getFormat()) {
            case INT32:
                return validateInt32(schema, ((Number) value).intValue());
            case INT64:
                return validateInt64(schema,  ((Number) value).longValue());
            default:
                // TODO フォーマットエラー
                return true;
        }
    }

    /**
     * int のパラメータの妥当性を確認します.
     *
     * @param schema パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInt32(Schema schema, int value) {
        if (schema.getEnum() != null) {
            return validateEnum(schema.getEnum(), value);
        }

        boolean isValid = true;
        if (schema.getMaximum() != null) {
            int maximum = schema.getMaximum().intValue();
            isValid = schema.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (schema.getMinimum() != null) {
            int minimum = schema.getMinimum().intValue();
            isValid &= schema.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * long のパラメータの妥当性を確認します.
     *
     * @param schema パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateInt64(Schema schema, long value) {
        if (schema.getEnum() != null) {
            return validateEnum(schema.getEnum(), value);
        }

        boolean isValid = true;
        if (schema.getMaximum() != null) {
            long maximum = schema.getMaximum().longValue();
            isValid = schema.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (schema.getMinimum() != null) {
            long minimum = schema.getMinimum().longValue();
            isValid &= schema.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * Number のパラメータの妥当性を確認します.
     *
     * @param schema パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateNumber(Schema schema, Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            // 文字列を数値に変換できるか確認
            value = formatNumber(schema.getFormat(), (String) value);
        }

        if (!(value instanceof Number)) {
            return false;
        }

        if (schema.getFormat() == null) {
            return validateFloat(schema, ((Number) value).floatValue());
        }

        switch (schema.getFormat()) {
            case FLOAT:
                return validateFloat(schema, ((Number) value).floatValue());
            case DOUBLE:
                return validateDouble(schema, ((Number) value).doubleValue());
            default:
                // TODO フォーマットエラー
                return true;
        }
    }

    /**
     * float のパラメータの妥当性を確認します.
     *
     * @param schema パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateFloat(Schema schema, float value) {
        if (schema.getEnum() != null) {
            return validateEnum(schema.getEnum(), value);
        }

        boolean isValid = true;
        if (schema.getMaximum() != null) {
            float maximum = schema.getMaximum().floatValue();
            isValid = schema.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (schema.getMinimum() != null) {
            float minimum = schema.getMinimum().floatValue();
            isValid &= schema.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    /**
     * double のパラメータの妥当性を確認します.
     *
     * @param schema パラメータの仕様
     * @param value リクエストされたパラメータの値
     * @return 値が妥当な場合はtrue、それ以外はfalse
     */
    private static boolean validateDouble(Schema schema, double value) {
        if (schema.getEnum() != null) {
            return validateEnum(schema.getEnum(), value);
        }

        boolean isValid = true;
        if (schema.getMaximum() != null) {
            double maximum = schema.getMaximum().doubleValue();
            isValid = schema.isExclusiveMaximum() ? (maximum > value) : (maximum >= value);
        }
        if (schema.getMinimum() != null) {
            double minimum = schema.getMinimum().doubleValue();
            isValid &= schema.isExclusiveMinimum() ? (minimum < value) : (minimum <= value);
        }
        return isValid;
    }

    private static boolean validateString(Schema schema, Object value) {
        if (value == null) {
            return true;
        }

        if (!(value instanceof String)) {
            return false;
        }

        if (schema.getEnum() != null) {
            return validateEnum(schema.getEnum(), value);
        }

        if (schema.getFormat() == null) {
            return validateLength(schema, (String) value);
        }

        switch (schema.getFormat()) {
            case TEXT:
                return validateLength(schema, (String) value);
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
                // TODO フォーマットエラー
                throw new IllegalStateException();
        }
    }

    private static boolean validateLength(Schema schema, String value) {
        Integer maxLength = schema.getMaxLength();
        Integer minLength = schema.getMinLength();
        int stringLength = value.length();
        return (maxLength == null || stringLength < maxLength) &&
                (minLength == null || stringLength > minLength);
    }


    private static boolean validateBoolean(Schema schema, Object value) {
        if (value == null) {
            return true;
        }

        if (schema.getEnum() != null) {
            return validateEnum(schema.getEnum(), value);
        }

        if (value instanceof String) {
            return TRUE.equalsIgnoreCase((String) value) || FALSE.equalsIgnoreCase((String) value);
        }
        return (value instanceof Boolean);
    }

    private static boolean validateFile(Schema parameter, Object value) {
        return true;
    }
}
