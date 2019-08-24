/*
 OpenAPIParser.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.parser;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.Contact;
import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.DataType;
import org.deviceconnect.android.profile.spec.models.Definition;
import org.deviceconnect.android.profile.spec.models.Example;
import org.deviceconnect.android.profile.spec.models.ExternalDocs;
import org.deviceconnect.android.profile.spec.models.Header;
import org.deviceconnect.android.profile.spec.models.In;
import org.deviceconnect.android.profile.spec.models.Info;
import org.deviceconnect.android.profile.spec.models.Items;
import org.deviceconnect.android.profile.spec.models.License;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Paths;
import org.deviceconnect.android.profile.spec.models.Response;
import org.deviceconnect.android.profile.spec.models.Responses;
import org.deviceconnect.android.profile.spec.models.Schema;
import org.deviceconnect.android.profile.spec.models.SecurityScheme;
import org.deviceconnect.android.profile.spec.models.SecurityScopes;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.Tag;
import org.deviceconnect.android.profile.spec.models.XEvent;
import org.deviceconnect.android.profile.spec.models.XType;
import org.deviceconnect.android.profile.spec.models.parameters.AbstractParameter;
import org.deviceconnect.android.profile.spec.models.parameters.BodyParameter;
import org.deviceconnect.android.profile.spec.models.parameters.FormParameter;
import org.deviceconnect.android.profile.spec.models.parameters.HeaderParameter;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;
import org.deviceconnect.android.profile.spec.models.parameters.PathParameter;
import org.deviceconnect.android.profile.spec.models.parameters.QueryParameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAPI Specification を解析するクラス.
 *
 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md
 */
public final class OpenAPIParser {

    private OpenAPIParser() {
    }

    // TODO ツールを使用してプラグインを作成することを前提としているので、定義ファイルのフォーマットチェックは行なっていない。
    // 必要に応じて、フォーマットチェックを行うこと。

    /**
     * JSON で定義された OpenAPI Specification を解析して、Swagger オブジェクトに変換します.
     *
     * @param jsonString 定義ファイル
     * @return Swagger オブジェクト
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    public static Swagger parse(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        Swagger swagger = new Swagger();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("swagger".equalsIgnoreCase(key)) {
                    swagger.setSwagger((String) object);
                } else if ("basePath".equalsIgnoreCase(key)) {
                    swagger.setBasePath((String) object);
                } else if ("host".equalsIgnoreCase(key)) {
                    swagger.setHost((String) object);
                } else if ("info".equalsIgnoreCase(key)) {
                    swagger.setInfo(parseInfo((JSONObject) object));
                } else if ("schemes".equalsIgnoreCase(key)) {
                    swagger.setSchemes(parseStringArray((JSONArray) object));
                } else if ("consumes".equalsIgnoreCase(key)) {
                    swagger.setConsumes(parseStringArray((JSONArray) object));
                } else if ("produces".equalsIgnoreCase(key)) {
                    swagger.setProduces(parseStringArray((JSONArray) object));
                } else if ("paths".equalsIgnoreCase(key)) {
                    swagger.setPaths(parsePaths((JSONObject) object));
                } else if ("definitions".equalsIgnoreCase(key)) {
                    swagger.setDefinitions(parseDefinitions((JSONObject) object));
                } else if ("parameters".equalsIgnoreCase(key)) {
                    swagger.setParameters(parseParameters((JSONObject) object));
                } else if ("responses".equalsIgnoreCase(key)) {
                    swagger.setResponses(parseResponsesWithRoot((JSONObject) object));
                } else if ("securityDefinitions".equalsIgnoreCase(key)) {
                    swagger.setSecurityDefinitions(parseSecurityDefinitions((JSONObject) object));
                } else if ("security".equalsIgnoreCase(key)) {
                    swagger.setSecurityRequirement(parseSecurity((JSONObject) object));
                } else if ("tags".equalsIgnoreCase(key)) {
                    swagger.setTags(parseTags((JSONArray) object));
                } else if ("externalDocs".equalsIgnoreCase(key)) {
                    swagger.setExternalDocs(parseExternalDocs((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    swagger.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }

        if (swagger.getSwagger() == null) {
            throw new IllegalArgumentException("swagger does not exist.");
        }

        if (!swagger.getSwagger().equalsIgnoreCase("2.0")) {
            throw new IllegalArgumentException("swagger is invalid.");
        }

        if (swagger.getInfo() == null) {
            throw new IllegalArgumentException("info does not exist.");
        }

        if (swagger.getPaths() == null) {
            throw new IllegalArgumentException("paths does not exist.");
        }

        return swagger;
    }

    /**
     * API 全体に適用されるセキュリティスキームを解析します.
     *
     * @param jsonObject API 全体に適用されるセキュリティスキームを格納したJSONオブジェクト
     * @return API 全体に適用されるセキュリティスキーム
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Map<String, String> parseSecurity(JSONObject jsonObject) throws JSONException {
        Map<String, String> securityScheme = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof String) {
                securityScheme.put(key, (String) object);
            }
        }
        return securityScheme;
    }

    /**
     * API 全体で使用できるセキュリティの定義を解析します.
     *
     * @param jsonObject API 全体で使用できるセキュリティの定義を格納したオブジェクト
     * @return API 全体で使用できるセキュリティの定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Map<String, SecurityScheme> parseSecurityDefinitions(JSONObject jsonObject) throws JSONException {
        Map<String, SecurityScheme> securityScheme = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                securityScheme.put(key, parseSecurityScheme((JSONObject) object));
            }
        }
        return securityScheme;
    }

    /**
     * 個別の API 全体で使用できるセキュリティの定義を解析します.
     *
     * @param jsonObject 個別の API 全体で使用できるセキュリティの定義を格納したJSONオブジェクト
     * @return 個別の API 全体で使用できるセキュリティの定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static SecurityScheme parseSecurityScheme(JSONObject jsonObject) throws JSONException {
        SecurityScheme securityScheme = new SecurityScheme();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("type".equalsIgnoreCase(key)) {
                    securityScheme.setType((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    securityScheme.setDescription((String) object);
                } else if ("name".equalsIgnoreCase(key)) {
                    securityScheme.setName((String) object);
                } else if ("in".equalsIgnoreCase(key)) {
                    securityScheme.setIn((String) object);
                } else if ("flow".equalsIgnoreCase(key)) {
                    securityScheme.setAuthorizationUrl((String) object);
                } else if ("tokenUrl".equalsIgnoreCase(key)) {
                    securityScheme.setTokenUrl((String) object);
                } else if ("scopes".equalsIgnoreCase(key)) {
                    securityScheme.setSecurityScopes(parseScopes((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    securityScheme.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return securityScheme;
    }

    /**
     * OAuth2 で使用されるセキュリティスコープの解析を行います.
     *
     * @param jsonObject OAuth2 で使用されるセキュリティスコープが格納されたJSONオブジェクト
     * @return OAuth2 で使用されるセキュリティスコープ
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static SecurityScopes parseScopes(JSONObject jsonObject) throws JSONException {
        SecurityScopes securityScopes = new SecurityScopes();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if (!key.startsWith("x-")) {
                    securityScopes.addScope(key, (String) object);
                } else {
                    securityScopes.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return securityScopes;
    }

    /**
     * 提供する API に関するメタデータを解析します.
     *
     * @param jsonObject メタデータが格納されているJSONオブジェクト
     * @return Infoオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Info parseInfo(JSONObject jsonObject) throws JSONException {
        Info info = new Info();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("title".equalsIgnoreCase(key)) {
                    info.setTitle((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    info.setDescription((String) object);
                } else if ("version".equalsIgnoreCase(key)) {
                    info.setVersion((String) object);
                } else if ("termsOfService".equalsIgnoreCase(key)) {
                    info.setTermsOfService((String) object);
                } else if ("contact".equalsIgnoreCase(key)) {
                    info.setContact(parseContact((JSONObject) object));
                } else if ("license".equalsIgnoreCase(key)) {
                    info.setLicense(parseLicense((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    info.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return info;
    }

    /**
     * API 提供者の連絡先情報を解析します.
     *
     * @param jsonObject 連絡先情報が格納されているJSONオブジェクト
     * @return Contactオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Contact parseContact(JSONObject jsonObject) throws JSONException {
        Contact contact = new Contact();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("name".equalsIgnoreCase(key)) {
                    contact.setName((String) object);
                } else if ("url".equalsIgnoreCase(key)) {
                    contact.setUrl((String) object);
                } else if ("email".equalsIgnoreCase(key)) {
                    contact.setEMail((String) object);
                } else if (key.startsWith("x-")) {
                    contact.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return contact;
    }

    /**
     * 提供する API のライセンス情報を解析します.
     *
     * @param jsonObject ライセンス情報が格納されているJSONオブジェクト
     * @return Licenseオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static License parseLicense(JSONObject jsonObject) throws JSONException {
        License license = new License();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("name".equalsIgnoreCase(key)) {
                    license.setName((String) object);
                } else if ("url".equalsIgnoreCase(key)) {
                    license.setUrl((String) object);
                } else if (key.startsWith("x-")) {
                    license.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return license;
    }

    /**
     * API 操作で使用されるデータ型を解析します.
     *
     * @param jsonObject API 操作で使用されるデータ型が格納されているJSONオブジェクト
     * @return API 操作で使用されるデータ型のマップ
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Map<String, Definition> parseDefinitions(JSONObject jsonObject) throws JSONException {
        Map<String, Definition> definitions = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                definitions.put(key, parseDefinition((JSONObject) object));
            }
        }
        return definitions;
    }

    /**
     * パラメータ情報のリストを解析します.
     *
     * @param jsonObject パラメータ情報のリストが格納されているJSONオブジェクト
     * @return Parameterのリスト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Map<String, Parameter> parseParameters(JSONObject jsonObject) throws JSONException {
        Map<String, Parameter> parameters = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                parameters.put(key, parseParameter((JSONObject) object));
            }
        }
        return parameters;
    }

    /**
     * API 全体で使用できるレスポンス定義を解析します.
     *
     * @param jsonObject レスポンス定義が格納されているJSONオブジェクト
     * @return Responseのマップ
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Map<String, Response> parseResponsesWithRoot(JSONObject jsonObject) throws JSONException {
        Map<String, Response> responses = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                responses.put(key, parseResponse((JSONObject) object));
            }
        }
        return responses;
    }

    /**
     * タグのリストを解析します.
     *
     * @param jsonArray タグのリストが格納されたJSON配列
     * @return Tagのリスト
     * @throws JSONException JSON配列の読み込みに失敗した場合に発生
     */
    private static List<Tag> parseTags(JSONArray jsonArray) throws JSONException {
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tags.add(parseTag(jsonArray.getJSONObject(i)));
        }
        return tags;
    }

    /**
     * タグ情報の解析を行います.
     *
     * @param jsonObject タグ情報が格納されているJSONオブジェクト
     * @return Tagオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Tag parseTag(JSONObject jsonObject) throws JSONException {
        Tag tag = new Tag();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("name".equalsIgnoreCase(key)) {
                    tag.setName((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    tag.setDescription((String) object);
                } else if ("externalDocs".equalsIgnoreCase(key)) {
                    tag.setExternalDocs(parseExternalDocs((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    tag.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return tag;
    }

    /**
     * 相対パスのエンドポイントのリストを解析します.
     *
     * @param jsonObject エンドポイントが格納されているJSONオブジェクト
     * @return Pathsオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Paths parsePaths(JSONObject jsonObject) throws JSONException {
        Paths paths = new Paths();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if (key.startsWith("/") && object instanceof JSONObject) {
                    paths.addPath(key, parsePath((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    paths.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return paths;
    }

    /**
     * 相対パスのエンドポイントを解析します.
     *
     * @param jsonObject エンドポイントが格納されているJSONオブジェクト
     * @return Pathオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Path parsePath(JSONObject jsonObject) throws JSONException {
        Path path = new Path();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("get".equalsIgnoreCase(key)) {
                    path.setGet(parseOperation((JSONObject) object));
                } else if ("put".equalsIgnoreCase(key)) {
                    path.setPut(parseOperation((JSONObject) object));
                } else if ("post".equalsIgnoreCase(key)) {
                    path.setPost(parseOperation((JSONObject) object));
                } else if ("delete".equalsIgnoreCase(key)) {
                    path.setDelete(parseOperation((JSONObject) object));
                } else if ("head".equalsIgnoreCase(key)) {
                    path.setHead(parseOperation((JSONObject) object));
                } else if ("options".equalsIgnoreCase(key)) {
                    path.setOptions(parseOperation((JSONObject) object));
                } else if ("patch".equalsIgnoreCase(key)) {
                    path.setPatch(parseOperation((JSONObject) object));
                } else if ("parameters".equalsIgnoreCase(key)) {
                    path.setParameters(parseParameters((JSONArray) object));
                } else if (key.startsWith("x-")) {
                    path.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return path;
    }

    /**
     * パスに対する単一の API 操作を解析します.
     *
     * @param jsonObject パスに対する単一の API 操作情報が格納されたJSONオブジェクト
     * @return Operationオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Operation parseOperation(JSONObject jsonObject) throws JSONException {
        Operation operation = new Operation();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("tags".equalsIgnoreCase(key)) {
                    operation.setTags(parseStringArray((JSONArray) object));
                } else if ("summary".equalsIgnoreCase(key)) {
                    operation.setSummary((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    operation.setDescription((String) object);
                } else if ("externalDocs".equalsIgnoreCase(key)) {
                    operation.setExternalDocs(parseExternalDocs((JSONObject) object));
                } else if ("operationId".equalsIgnoreCase(key)) {
                    operation.setOperationId((String) object);
                } else if ("consumes".equalsIgnoreCase(key)) {
                    operation.setConsumes(parseStringArray((JSONArray) object));
                } else if ("produces".equalsIgnoreCase(key)) {
                    operation.setProduces(parseStringArray((JSONArray) object));
                } else if ("parameters".equalsIgnoreCase(key)) {
                    operation.setParameters(parseParameters((JSONArray) object));
                } else if ("responses".equalsIgnoreCase(key)) {
                    operation.setResponses(parseResponses((JSONObject) object));
                } else if ("schemes".equalsIgnoreCase(key)) {
                    operation.setSchemes(parseStringArray((JSONArray) object));
                } else if ("deprecated".equalsIgnoreCase(key)) {
                    operation.setDeprecated((Boolean) object);
                } else if ("security".equalsIgnoreCase(key)) {
                    operation.setSecurityRequirement(parseSecurity((JSONObject) object));
                } else if ("x-event".equalsIgnoreCase(key)) {
                    operation.setXEvent(parseXEvent((JSONObject) object));
                } else if ("x-type".equalsIgnoreCase(key)) {
                    operation.setXType(XType.parse((String) object));
                } else if (key.startsWith("x-")) {
                    operation.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return operation;
    }

    /**
     * Device Connect 拡張 x-event の解析を行います.
     *
     * @param jsonObject x-event が格納されたJSONオブジェクト
     * @return XEvent のインスタンス
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static XEvent parseXEvent(JSONObject jsonObject) throws JSONException {
        XEvent event = new XEvent();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("description".equalsIgnoreCase(key)) {
                    event.setDescription((String) object);
                } else if ("schema".equalsIgnoreCase(key)) {
                    event.setSchema(parseSchema((JSONObject) object));
                } else if ("examples".equalsIgnoreCase(key)) {
                    event.setExamples(parseExamples((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    event.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return event;
    }

    /**
     * パラメータ情報のリストの解析を行います.
     *
     * @param jsonArray パラメータ情報のリストが格納されているJSON配列
     * @return パラメータ情報のリスト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static List<Parameter> parseParameters(JSONArray jsonArray) throws JSONException {
        List<Parameter> parameters = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            parameters.add(parseParameter(jsonArray.getJSONObject(i)));
        }
        return parameters;
    }

    /**
     * パラメータ情報の解析を行います.
     *
     * @param jsonObject パラメータ情報が格納されているJSON配列
     * @return パラメータ情報
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Parameter parseParameter(JSONObject jsonObject) throws JSONException {
        AbstractParameter parameter;

        String in = jsonObject.getString("in");
        if (in == null) {
            throw new IllegalArgumentException("in does not exist in parameter.");
        }

        In inType = In.parse(in);
        if (inType == null) {
            throw new IllegalArgumentException("in does not exist. in parameter");
        }

        switch (inType) {
            case BODY:
                return parseBodyParameter(jsonObject);
            case QUERY:
                parameter = new QueryParameter();
                break;
            case HEADER:
                parameter = new HeaderParameter();
                break;
            case FORM:
                parameter = new FormParameter();
                break;
            case PATH:
                parameter = new PathParameter();
                break;
            default:
                throw new IllegalArgumentException("in is an unknown type. in=" + in);
        }

        return parseOtherParameter(parameter, jsonObject);
    }

    /**
     * Body のパラメータ情報の解析を行います.
     *
     * @param jsonObject Body用パラメータ情報が格納されているJSON配列
     * @return パラメータ情報
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Parameter parseBodyParameter(JSONObject jsonObject) throws JSONException {
        BodyParameter parameter = new BodyParameter();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("name".equalsIgnoreCase(key)) {
                    parameter.setName((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    parameter.setDescription((String) object);
                } else if ("in".equalsIgnoreCase(key)) {
                    parameter.setIn(In.parse((String) object));
                } else if ("required".equalsIgnoreCase(key)) {
                    parameter.setRequired((Boolean) object);
                } else if ("schema".equalsIgnoreCase(key)) {
                    parameter.setSchema(parseSchema((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    parameter.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return parameter;
    }

    /**
     * Body 以外のパラメータ情報の解析を行います.
     *
     * @param parameter パラメータ情報を格納するオブジェクト
     * @param jsonObject パラメータ情報が格納されているJSON配列
     * @return パラメータ情報
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Parameter parseOtherParameter(AbstractParameter parameter, JSONObject jsonObject) throws JSONException {
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("name".equalsIgnoreCase(key)) {
                    parameter.setName((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    parameter.setDescription((String) object);
                } else if ("in".equalsIgnoreCase(key)) {
                    parameter.setIn(In.parse((String) object));
                } else if ("required".equalsIgnoreCase(key)) {
                    parameter.setRequired((Boolean) object);
                } else if ("type".equalsIgnoreCase(key)) {
                    parameter.setType(DataType.fromName((String) object));
                } else if ("format".equalsIgnoreCase(key)) {
                    parameter.setFormat(DataFormat.fromName((String) object));
                } else if ("allowEmptyValue".equalsIgnoreCase(key)) {
                    parameter.setAllowEmptyValue((Boolean) object);
                } else if ("items".equalsIgnoreCase(key)) {
                    parameter.setItems(parseItems((JSONObject) object));
                } else if ("collectionFormat".equalsIgnoreCase(key)) {
                    parameter.setCollectionFormat((String) object);
                } else if ("default".equalsIgnoreCase(key)) {
                    parameter.setDefault(parseDefault(object));
                } else if ("maximum".equalsIgnoreCase(key)) {
                    parameter.setMaximum((Number) object);
                } else if ("exclusiveMaximum".equalsIgnoreCase(key)) {
                    parameter.setExclusiveMaximum((Boolean) object);
                } else if ("minimum".equalsIgnoreCase(key)) {
                    parameter.setMinimum((Number) object);
                } else if ("exclusiveMinimum".equalsIgnoreCase(key)) {
                    parameter.setExclusiveMinimum((Boolean) object);
                } else if ("maxLength".equalsIgnoreCase(key)) {
                    parameter.setMaxLength((Integer) object);
                } else if ("minLength".equalsIgnoreCase(key)) {
                    parameter.setMinLength((Integer) object);
                } else if ("pattern".equalsIgnoreCase(key)) {
                    parameter.setPattern((String) object);
                } else if ("maxItems".equalsIgnoreCase(key)) {
                    parameter.setMaxItems((Integer) object);
                } else if ("minItems".equalsIgnoreCase(key)) {
                    parameter.setMinItems((Integer) object);
                } else if ("uniqueItems".equalsIgnoreCase(key)) {
                    parameter.setUniqueItems((Boolean) object);
                } else if ("enum".equalsIgnoreCase(key)) {
                    parameter.setEnum(parseEnum((JSONArray) object));
                } else if ("multipleOf".equalsIgnoreCase(key)) {
                    parameter.setMultipleOf((Number) object);
                } else if (key.startsWith("x-")) {
                    parameter.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return parameter;
    }

    /**
     * 文字列の数値判定を行います.
     *
     * @param num 判定を行う文字列
     * @return 数値の場合はtrue、それ以外の場合はfalse
     */
    private static boolean isNumber(String num) {
        String regex = "^\\-?[0-9]*\\.?[0-9]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(num);
        return m.find();
    }

    /**
     * 応答用定義の解析を行います.
     *
     * @param jsonObject 応答用定義が格納されたJSONオブジェクト
     * @return 応答用定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Responses parseResponses(JSONObject jsonObject) throws JSONException {
        Responses responses = new Responses();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("default".equalsIgnoreCase(key)) {
                    responses.addResponse(key, parseResponse((JSONObject) object));
                } else if (isNumber(key)) {
                    responses.addResponse(key, parseResponse((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    responses.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return responses;
    }

    /**
     * 応答用定義の解析を行います.
     *
     * @param jsonObject 応答用定義が格納されたJSONオブジェクト
     * @return 応答用定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Response parseResponse(JSONObject jsonObject) throws JSONException {
        Response response = new Response();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("description".equalsIgnoreCase(key)) {
                    response.setDescription((String) object);
                } else if ("schema".equalsIgnoreCase(key)) {
                    response.setSchema(parseSchema((JSONObject) object));
                } else if ("headers".equalsIgnoreCase(key)) {
                    response.setHeaders(parseHeaders((JSONObject) object));
                } else if ("examples".equalsIgnoreCase(key)) {
                    response.setExamples(parseExamples((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    response.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return response;
    }

    /**
     * API 操作によって使用されるデータ型、作成されるデータ型を定義するオブジェクトを解析します.
     *
     * @param jsonObject データ定義が格納されたJSONオブジェクト
     * @return データ定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Definition parseDefinition(JSONObject jsonObject) throws JSONException {
        Definition definition = new Definition();
        parseSchema(jsonObject, definition);
        return definition;
    }

    /**
     * 配列などの要素のタイプを定義するオブジェクトを解析します.
     *
     * @param jsonObject 要素定義が格納されたJSONオブジェクト
     * @return 要素定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Items parseItems(JSONObject jsonObject) throws JSONException {
        Items items = new Items();
        parseSchema(jsonObject, items);
        return items;
    }

    /**
     * JSON スキーマ定義を解析します.
     *
     * @param jsonObject JSON スキーマ定義が格納されたJSONオブジェクト
     * @return JSON スキーマ定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Schema parseSchema(JSONObject jsonObject) throws JSONException {
        Schema schema = new Schema();
        parseSchema(jsonObject, schema);
        return schema;
    }

    /**
     * JSON スキーマ定義を解析し、指定されたオブジェクトに格納します.
     *
     * @param jsonObject JSON スキーマ定義が格納されたJSONオブジェクト
     * @param schema 格納先のオブジェクト
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static void parseSchema(JSONObject jsonObject, Schema schema) throws JSONException {
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("$ref".equalsIgnoreCase(key)) {
                    schema.setReference((String) object);
                } else if ("type".equalsIgnoreCase(key)) {
                    schema.setType(DataType.fromName((String) object));
                } else if ("format".equalsIgnoreCase(key)) {
                    schema.setFormat(DataFormat.fromName((String) object));
                } else if ("title".equalsIgnoreCase(key)) {
                    schema.setTitle((String) object);
                } else if ("description".equalsIgnoreCase(key)) {
                    schema.setDescription((String) object);
                } else if ("default".equalsIgnoreCase(key)) {
                    schema.setDefault(parseDefault(object));
                } else if ("multipleOf".equalsIgnoreCase(key)) {
                    schema.setMultipleOf((Number) object);
                } else if ("maximum".equalsIgnoreCase(key)) {
                    schema.setMaximum((Number) object);
                } else if ("exclusiveMaximum".equalsIgnoreCase(key)) {
                    schema.setExclusiveMaximum((Boolean) object);
                } else if ("minimum".equalsIgnoreCase(key)) {
                    schema.setMinimum((Number) object);
                } else if ("exclusiveMinimum".equalsIgnoreCase(key)) {
                    schema.setExclusiveMinimum((Boolean) object);
                } else if ("maxLength".equalsIgnoreCase(key)) {
                    schema.setMaxLength((Integer) object);
                } else if ("minLength".equalsIgnoreCase(key)) {
                    schema.setMinLength((Integer) object);
                } else if ("pattern".equalsIgnoreCase(key)) {
                    schema.setPattern((String) object);
                } else if ("maxItems".equalsIgnoreCase(key)) {
                    schema.setMaxItems((Integer) object);
                } else if ("minItems".equalsIgnoreCase(key)) {
                    schema.setMinItems((Integer) object);
                } else if ("uniqueItems".equalsIgnoreCase(key)) {
                    schema.setUniqueItems((Boolean) object);
                } else if ("maxProperties".equalsIgnoreCase(key)) {
                    schema.setMaxProperties((Integer) object);
                } else if ("minProperties".equalsIgnoreCase(key)) {
                    schema.setMinProperties((Integer) object);
                } else if ("required".equalsIgnoreCase(key)) {
                    schema.setRequired(parseStringArray((JSONArray) object));
                } else if ("enum".equalsIgnoreCase(key)) {
                    schema.setEnum(parseEnum((JSONArray) object));
                } else if ("collectionFormat".equalsIgnoreCase(key)) {
                    schema.setCollectionFormat((String) object);
                } else if ("items".equalsIgnoreCase(key)) {
                    schema.setItems(parseItems((JSONObject) object));
                } else if ("allOf".equalsIgnoreCase(key)) {
                    schema.setAllOf(parseAllOf((JSONArray) object));
                } else if ("properties".equalsIgnoreCase(key)) {
                    schema.setProperties(parseProperties((JSONObject) object));
                } else if ("additionalProperties".equalsIgnoreCase(key)) {
                    schema.setAdditionalProperties(parseSchema((JSONObject) object));
                } else if (key.startsWith("x-")) {
                    schema.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
    }

    /**
     * デフォルト値の解析を行います.
     *
     * @param object デフォルト値
     * @return デフォルト値
     */
    private static Object parseDefault(Object object) {
        return object;
    }

    /**
     * 列挙値の解析を行います.
     *
     * @param jsonArray 列挙値を格納しているJSON配列
     * @return 列挙値
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static List<Object> parseEnum(JSONArray jsonArray) throws JSONException {
        List<Object> enums = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            enums.add(jsonArray.get(i));
        }
        return enums;
    }

    /**
     * allOf の解析を行います.
     *
     * @param jsonArray スキーマを格納しているJSON配列
     * @return JSONスキーマの配列
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static List<Schema> parseAllOf(JSONArray jsonArray) throws JSONException {
        List<Schema> allOf = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            allOf.add(parseSchema(jsonArray.getJSONObject(i)));
        }
        return allOf;
    }

    /**
     * properties の解析を行います.
     *
     * @param jsonObject プロパティが格納されたJSONオブジェクト
     * @return プロパティのマップ
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Map<String, Schema> parseProperties(JSONObject jsonObject) throws JSONException {
        Map<String, Schema> properties = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                properties.put(key, parseSchema((JSONObject) object));
            }
        }
        return properties;
    }

    /**
     * headers の解析を行います.
     *
     * @param jsonObject ヘッダーが格納されたJSONオブジェクト
     * @return ヘッダーのマップ
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Map<String, Header> parseHeaders(JSONObject jsonObject) throws JSONException {
        Map<String, Header> headers = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                headers.put(key, parseHeader((JSONObject) object));
            }
        }
        return headers;
    }

    /**
     * レスポンス用のヘッダー定義を解析します.
     *
     * @param jsonObject ヘッダー定義が格納されたJSONオブジェクト
     * @return ヘッダー定義
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Header parseHeader(JSONObject jsonObject) throws JSONException {
        Header header = new Header();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("description".equalsIgnoreCase(key)) {
                    header.setDescription((String) object);
                } else if ("type".equalsIgnoreCase(key)) {
                    header.setType(DataType.fromName((String) object));
                } else if ("format".equalsIgnoreCase(key)) {
                    header.setFormat(DataFormat.fromName((String) object));
                } else if ("items".equalsIgnoreCase(key)) {
                    header.setItems(parseItems((JSONObject) object));
                } else if ("collectionFormat".equalsIgnoreCase(key)) {
                    header.setCollectionFormat((String) object);
                } else if ("default".equalsIgnoreCase(key)) {
                    header.setDefault(parseDefault(object));
                } else if ("maximum".equalsIgnoreCase(key)) {
                    header.setMaximum((Number) object);
                } else if ("exclusiveMaximum".equalsIgnoreCase(key)) {
                    header.setExclusiveMaximum((Boolean) object);
                } else if ("minimum".equalsIgnoreCase(key)) {
                    header.setMinimum((Number) object);
                } else if ("exclusiveMinimum".equalsIgnoreCase(key)) {
                    header.setExclusiveMinimum((Boolean) object);
                } else if ("maxLength".equalsIgnoreCase(key)) {
                    header.setMaxLength((Integer) object);
                } else if ("minLength".equalsIgnoreCase(key)) {
                    header.setMinLength((Integer) object);
                } else if ("pattern".equalsIgnoreCase(key)) {
                    header.setPattern((String) object);
                } else if ("maxItems".equalsIgnoreCase(key)) {
                    header.setMaxItems((Integer) object);
                } else if ("minItems".equalsIgnoreCase(key)) {
                    header.setMinItems((Integer) object);
                } else if ("uniqueItems".equalsIgnoreCase(key)) {
                    header.setUniqueItems((Boolean) object);
                } else if ("enum".equalsIgnoreCase(key)) {
                    header.setEnum(parseEnum((JSONArray) object));
                } else if ("multipleOf".equalsIgnoreCase(key)) {
                    header.setMultipleOf((Number) object);
                } else if (key.startsWith("x-")) {
                    header.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return header;
    }

    /**
     * レスポンスの例の定義のマップを解析します.
     *
     * @param jsonObject レスポンスの例が格納されたJSONオブジェクト
     * @return レスポンスの例のマップ
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private static Map<String, Example> parseExamples(JSONObject jsonObject) throws JSONException {
        Map<String, Example> examples = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object instanceof JSONObject) {
                examples.put(key, parseExample((JSONObject) object));
            }
        }
        return examples;
    }

    /**
     * レスポンスの例の定義を解析します.
     *
     * @param jsonObject レスポンスの例が格納されたJSONオブジェクト
     * @return レスポンスの例
     */
    private static Example parseExample(JSONObject jsonObject) {
        Example example = new Example();
        example.setExample(jsonObject);
        return example;
    }

    /**
     * 外部文書情報を解析します.
     *
     * @param jsonObject 外部文書情報が格納されているJSONオブジェクト
     * @return ExternalDocsオブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static ExternalDocs parseExternalDocs(JSONObject jsonObject) throws JSONException {
        ExternalDocs externalDocs = new ExternalDocs();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if ("description".equalsIgnoreCase(key)) {
                    externalDocs.setDescription((String) object);
                } else if ("url".equalsIgnoreCase(key)) {
                    externalDocs.setUrl((String) object);
                } else if (key.startsWith("x-")) {
                    externalDocs.addVendorExtension(key, parseVendorExtension(object));
                }
            }
        }
        return externalDocs;
    }

    /**
     * ベンダー拡張情報を解析します.
     *
     * @param object ベンダー拡張情報が格納されているオブジェクト
     * @return オブジェクト
     * @throws JSONException JSONオブジェクトの読み込みに失敗した場合に発生
     */
    private static Object parseVendorExtension(Object object) throws JSONException {
        if (object instanceof JSONObject) {
            return parseJSONObject((JSONObject) object);
        } else if (object instanceof JSONArray) {
            return parseJSONArray((JSONArray) object);
        } else {
            return object;
        }
    }

    /**
     * 文字列の配列を解析します.
     *
     * @param jsonArray 文字列の配列が格納されているJSON配列
     * @return Stringのリスト
     * @throws JSONException JSON配列の読み込みに失敗した場合に発生
     */
    private static List<String> parseStringArray(JSONArray jsonArray) throws JSONException {
        List<String> stringArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            stringArrayList.add(jsonArray.getString(i));
        }
        return stringArrayList;
    }

    @SuppressWarnings("unchecked")
    public static Bundle parseJSONObject(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object object = jsonObject.get(key);
            if (object != null) {
                if (object instanceof Integer) {
                    bundle.putInt(key, (Integer) object);
                } else if (object instanceof int[]) {
                    bundle.putIntArray(key, (int[]) object);
                } else if (object instanceof Long) {
                    bundle.putLong(key, (Long) object);
                } else if (object instanceof long[]) {
                    bundle.putLongArray(key, (long[]) object);
                } else if (object instanceof Short) {
                    bundle.putShort(key, (Short) object);
                } else if (object instanceof short[]) {
                    bundle.putShortArray(key, (short[]) object);
                } else if (object instanceof Byte) {
                    bundle.putByte(key, (Byte) object);
                } else if (object instanceof byte[]) {
                    bundle.putByteArray(key, (byte[]) object);
                } else if (object instanceof Boolean) {
                    bundle.putBoolean(key, (Boolean) object);
                } else if (object instanceof String) {
                    bundle.putString(key, (String) object);
                } else if (object instanceof String[]) {
                    bundle.putStringArray(key, (String[]) object);
                } else if (object instanceof JSONObject) {
                    bundle.putParcelable(key, parseJSONObject((JSONObject) object));
                } else if (object instanceof JSONArray) {
                    bundle.putParcelableArrayList(key, parseJSONArray((JSONArray) object));
                }
            }
        }
        return bundle;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList parseJSONArray(JSONArray jsonArray) throws JSONException {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object object = jsonArray.get(i);
            if (object != null) {
                if (object instanceof Integer) {
                    arrayList.add(object);
                } else if (object instanceof int[]) {
                    arrayList.add(object);
                } else if (object instanceof Long) {
                    arrayList.add(object);
                } else if (object instanceof long[]) {
                    arrayList.add(object);
                } else if (object instanceof Short) {
                    arrayList.add(object);
                } else if (object instanceof short[]) {
                    arrayList.add(object);
                } else if (object instanceof Byte) {
                    arrayList.add(object);
                } else if (object instanceof byte[]) {
                    arrayList.add(object);
                } else if (object instanceof Boolean) {
                    arrayList.add(object);
                } else if (object instanceof String) {
                    arrayList.add(object);
                } else if (object instanceof String[]) {
                    arrayList.add(object);
                } else if (object instanceof JSONObject) {
                    arrayList.add(parseJSONObject((JSONObject) object));
                } else if (object instanceof JSONArray) {
                    arrayList.add(parseJSONArray((JSONArray) object));
                }
            }
        }
        return arrayList;
    }
}
