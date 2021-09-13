var util = (function(parent, global) {
    function initScope() {
        return  Array(
           'serviceDiscovery',
           'serviceInformation',
           'system',
           'battery',
           'connection',
           'deviceOrientation',
           'file',
           'mediaPlayer',
           'mediaStreamRecording',
           'notification',
           'phone',
           'proximity',
           'setting',
           'vibration',
           'light',
           'remoteController',
           'driveController',
           'mhealth',
           'sphero',
           'dice',
           'temperature',
           'camera',
           'canvas',
           'health',
           'touch',
           'humanDetection',
           'keyEvent',
           'omnidirectionalImage',
           'tv',
           'powerMeter',
           'humidity',
           'illuminance',
           'videoChat',
           'airConditioner',
           'atmosphericPressure',
           'ecg',
           'poseEstimation',
           'stressEstimation',
           'walkState',
           'gpio',
           'geolocation',
           'echonetLite');
    }

    var mAccessToken = null;
    var mSessionKey = "test-session-key";
    var mHost = "localhost";
    var mScopes = initScope();
    var mSSLEnabled = getQuery("ssl") === "on";
    var mPort = getQuery("port") == null ? 4035 : parseInt(getQuery("port"), 10);

    function init(callback) {
        loadScope();

        dConnect.setHost(mHost);
        dConnect.setPort(mPort);
        dConnect.setSSLEnabled(mSSLEnabled);
        dConnect.setExtendedOrigin("null");
        checkDeviceConnect(callback);
    }
    parent.init = init;

    function isSSL() {
        return mSSLEnabled;
    }
    parent.isSSL = isSSL;

    function loadScope() {
        var scopeStr = getCookie("scope");
        if (scopeStr) {
            mScopes = scopeStr.split(',');
            if (mScopes == undefined) {
                mScopes = initScope();
            }
        }
    }

    function saveScope() {
        var scopeStr = mScopes.join(',');
        setCookie("scope", scopeStr);
    }


    function startManager(onAvailable) {
        var errorCallback = function(errorCode, errorMessage) {
            switch (errorCode) {
            case dConnect.constants.ErrorCode.ACCESS_FAILED:
                alert('Device Connect Managerが起動していません。');
                break;
            case dConnect.constants.ErrorCode.INVALID_SERVER:
                alert('WARNING: Device Connect Manager may be spoofed.');
                break;
            case dConnect.constants.ErrorCode.INVALID_ORIGIN:
                alert('WARNING: Origin of this app is invalid. Maybe the origin is not registered in allowlist.');
                break;
            default:
                alert(errorMessage);
                break;
            }
        };
        dConnect.checkDeviceConnect(onAvailable, errorCallback);
    }

    function checkDeviceConnect(callback) {
        startManager(function(apiVersion) {
            console.log('Device Connect API version: ' + apiVersion);

            mAccessToken = getCookie('accessToken');

            serviceInformation(function(json) {
                openWebSocketIfNeeded();
                callback(null, json);
            });
        });
    }

    function authorization(callback, errorCallback) {
        dConnect.authorization(mScopes, 'デバイス確認画面',
            function(clientId, accessToken) {
                mAccessToken = accessToken;
                openWebSocketIfNeeded();
                setCookie('accessToken', mAccessToken);
                callback();
            },
            function(errorCode, errorMessage) {
                showAlert("認証に失敗しました", errorCode, errorMessage);
                if (errorCallback) {
                    errorCallback(errorCode, errorMessage);
                }
            });
    }

    function serviceDiscovery(callback) {
        dConnect.discoverDevices(mAccessToken, function(json) {
            callback(json.services);
        }, function(errorCode, errorMessage) {
            if (errorCode == 11 || errorCode == 12 || errorCode == 13 || errorCode == 15) {
                authorization(function() {
                    serviceDiscovery(callback);
                });
            } else {
                showAlert("デバイスの情報取得に失敗しました。", errorCode, errorMessage)
            }
        });
    }

    function serviceInformation(callback) {
        dConnect.getSystemDeviceInfo(getServiceId(), getAccessToken(), function(json) {
            callback(json);
        }, function(errorCode, errorMessage) {
            if (errorCode == 11 || errorCode == 12 || errorCode == 13 || errorCode == 15) {
                authorization(function() {
                    serviceInformation(callback);
                });
            } else {
                showAlert("デバイスの情報取得に失敗しました。", errorCode, errorMessage)
            }
        });
    }

    function openWebSocketIfNeeded() {
        try {
            if (!dConnect.isConnectedWebSocket()) {
                var accessToken = mAccessToken ? mAccessToken : mSessionKey;
                dConnect.connectWebSocket(accessToken, function(code, message) {
                    if (code > 0) {
                        alert('WebSocketが切れました。\n code=' + code + " message=" + message);
                    }
                    console.log("WebSocket: code=" + code + " message=" +message);
                });
            }
        } catch (e) {
            alert("この端末は、WebSocketをサポートしていません。");
        }
    }

    function setCookieInternal(key, value) {
        document.cookie = key + '=' + value;
    }

    function getCookieInternal(name) {
        var result = null;
        var cookieName = name + '=';
        var allCookies = document.cookie;
        var position = allCookies.indexOf(cookieName);
        if (position != -1) {
            var startIndex = position + cookieName.length;
            var endIndex = allCookies.indexOf(';', startIndex);
            if (endIndex == -1) {
                endIndex = allCookies.length;
            }
            result = decodeURIComponent(allCookies.substring(startIndex, endIndex));
        }
        return result;
    }

    function setCookie(key, value) {
        if (window.Android) {
            Android.setCookie(key, value);
        } else {
            setCookieInternal(key, value);
        }
    }

    function getCookie(name) {
        if (window.Android) {
            return Android.getCookie(name);
        } else {
            return getCookieInternal(name);
        }
    }

    function getQuery(name) {
        if (1 < document.location.search.length) {
            var query = document.location.search.substring(1);
            var parameters = query.split('&');
            for (var i = 0; i < parameters.length; i++) {
                var element = parameters[i].split('=');
                var paramName = decodeURIComponent(element[0]);
                var paramValue = decodeURIComponent(element[1]);
                if (paramName == name) {
                    return paramValue;
                }
            }
        }
        return null;
    }

    function containsScope(profile) {
        for (var i = 0; i < mScopes.length; i++) {
            if (mScopes[i] == profile) {
                return true;
            }
        }
        return false;
    }

    function appendScope(uri) {
        var elm = document.createElement('a');
        elm.href = uri;

        var p = elm.pathname.split('/');
        if (p.length < 3) {
            return;
        }
        if (!containsScope(p[2])) {
            mScopes.push(p[2]);
        }
        saveScope();
    }

    function rebuildUri(uri) {
        var elm = document.createElement('a');
        elm.href = uri;

        var u = elm.origin + elm.pathname + "?";

        var parameters = elm.search.substring(1).split('&');
        for (var i = 0; i < parameters.length; i++) {
            var element = parameters[i].split('=');
            var paramName = decodeURIComponent(element[0]);
            var paramValue = decodeURIComponent(element[1]);
            if (i > 0) {
                u += '&';
            }
            if (paramName == 'accessToken') {
                u += element[0] + "=" + mAccessToken;
            } else {
                u += element[0] + "=" + element[1];
            }
        }
        return u;
    }

    function createXMLHttpRequest() {
        try {
            return new XMLHttpRequest();
        } catch(e) {}
        try {
            return new ActiveXObject('MSXML2.XMLHTTP.6.0');
        } catch(e) {}
        try {
            return new ActiveXObject('MSXML2.XMLHTTP.3.0');
        } catch(e) {}
        try {
            return new ActiveXObject('MSXML2.XMLHTTP');
        } catch(e) {}
        return null;
    }

    function sendRequest(method, uri, body, callback) {
         var xhr = createXMLHttpRequest();
         xhr.onerror = function (e) {
             console.log("onerror:" + xhr.statusText);
         };

         xhr.onload = function (e) {
             console.log("onload:" + xhr.readyState);
         };

         xhr.onreadystatechange = function() {
             switch (xhr.readyState) {
             case 1: {
                 try {
                     xhr.setRequestHeader("X-GotAPI-Origin".toLowerCase(), "file://");
                 } catch (e) {
                     return;
                 }

                 if (method.toUpperCase() === 'DELETE' && (body === undefined || body === null)) {
                     body = '';
                 }
                 xhr.send(body);
                 break;
             }
             case 2:
                 break;
             case 3:
                break;
             case 4: {
                 if (xhr.status == 200) {
                    var json = JSON.parse(xhr.responseText);
                    if (json.result == 1 && (json.errorCode == 14 || json.errorCode == 15)) {
                        appendScope(uri);
                        authorization(function() {
                            if (method.toUpperCase() == 'GET' || method.toUpperCase() == 'DELETE') {
                                uri = rebuildUri(uri);
                            } else {
                                body.set('accessToken', mAccessToken);
                            }
                            sendRequest(method, uri, body, callback);
                        }, function(errorCode, errorMessage) {
                            callback(xhr.status, xhr.responseText);
                        });
                        return;
                    }
                 }
                 callback(xhr.status, xhr.responseText);
                 break;
             }
             default:
                 break;
             }
        };
        xhr.open(method, uri);
    }
    parent.sendRequest = sendRequest;


    function addEventListener(uri, eventCallback, successCallback, errorCallback) {
        dConnect.addEventListener(uri, eventCallback, successCallback,
            function(errorCode, errorMessage) {
                if (errorCode == 14 || errorCode == 15) {
                    appendScope(uri);
                    authorization(function() {
                        uri = rebuildUri(uri);
                        addEventListener(uri, eventCallback, successCallback, errorCallback);
                    }, function(errorCode, errorMessage) {
                         errorCallback(errorCode, errorMessage);
                    });
                } else {
                   errorCallback(errorCode, errorMessage);
                }
            });
    }
    parent.addEventListener = addEventListener;


    function removeEventListener(uri, successCallback, errorCallback) {
        dConnect.removeEventListener(uri, successCallback,
            function(errorCode, errorMessage) {
                if (errorCode == 14 || errorCode == 15) {
                    appendScope(uri);
                    authorization(function() {
                        uri = rebuildUri(uri);
                        console.log(uri);
                        addEventListener(uri, eventCallback, successCallback, errorCallback);
                    }, function(errorCode, errorMessage) {
                         errorCallback(errorCode, errorMessage);
                    });
                } else {
                   errorCallback(errorCode, errorMessage);
                }
            });
    }
    parent.removeEventListener = removeEventListener;

    function getProtocol() {
        return isSSL() ? 'https' : 'http';
    }
    parent.getProtocol = getProtocol;

    function getUri(path) {
        return getProtocol() + '://' + mHost + ':' + mPort + path;
    }
    parent.getUri = getUri;

    function getPort() {
        return mPort;
    }
    parent.getPort = getPort;

    function getProfile() {
        return getQuery('profile');
    }
    parent.getProfile = getProfile;


    function getServiceId() {
        return getQuery('serviceId');
    }
    parent.getServiceId = getServiceId;


    function getResourceUri() {
        return getQuery('resource');
    }
    parent.getResourceUri = getResourceUri;

    function getMimeType() {
        return getQuery('mimeType');
    }
    parent.getMimeType = getMimeType;

    function getAccessToken() {
        return mAccessToken;
    }
    parent.getAccessToken = getAccessToken;


    function getSessionKey() {
        return mSessionKey;
    }
    parent.getSessionKey = getSessionKey;


    function showAlert(message, errorCode, errorMessage) {
        alert(message + "\n errorCode: " + errorCode + " \n " + errorMessage);
    }
    parent.showAlert = showAlert;


    function createTemplate(templateId, data) {
        var template = document.getElementById(templateId).text;
        var html = template.replace(/{#(\w+)}/g, function(m, key) {
            var text = data[key] || '';
            return text;
        });
        return html;
    }
    parent.createTemplate = createTemplate;

    function replaceUri(jsonObject) {
        var mimeType = 'image/png';
        for (var key in jsonObject) {
            var value = jsonObject[key];
            if (value instanceof Object && !(value instanceof Array)) {
                replaceUri(value);
            } else {
                if (key == 'uri') {
                    if (jsonObject['mimeType']) {
                        mimeType = jsonObject['mimeType'];
                    }
                    jsonObject[key] = '<a href=resource.html?mimeType=' + encodeURIComponent(mimeType) + '&resource=' + encodeURIComponent(value) + '>' + value + "</a>";
                }
            }
        }
    }

    function formatJSON(jsonText) {
        var jsonBefore = JSON.parse(jsonText);
        replaceUri(jsonBefore);
        var json = JSON.stringify(jsonBefore, null, "    ");
        return json.replace(/\r?\n/g, "<br>").replace(/\s{2}/g, "&nbsp;&nbsp;");
    }
    parent.formatJSON = formatJSON;


    function escapeText(text) {
        return text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
    parent.escapeText = escapeText;

    return parent;
})(util || {}, this.self || global);
