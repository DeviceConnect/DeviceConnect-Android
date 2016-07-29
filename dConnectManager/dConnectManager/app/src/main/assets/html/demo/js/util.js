var util = (function(parent, global) {
    var mAccessToken = null;
    var mSessionKey = "test-session-key";
    var mHost = "localhost";

    function init(callback) {
        dConnect.setHost(mHost);
        dConnect.setExtendedOrigin("file://android_asset/");
        checkDeviceConnect(callback);
    }
    parent.init = init;

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
                alert('WARNING: Origin of this app is invalid. Maybe the origin is not registered in whiteList.');
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

            if (window.Android) {
                mAccessToken = Android.getCookie('accessToken');
            } else {
                mAccessToken = getCookie('accessToken');
            }

            openWebSocketIfNeeded();

            serviceInformation(callback);
        });
    }

    function authorization(callback) {
        var scopes = Array(
            'servicediscovery',
            'serviceinformation',
            'system',
            'battery',
            'connect',
            'deviceorientation',
            'filedescriptor',
            'file',
            'mediaplayer',
            'mediastreamrecording',
            'notification',
            'phone',
            'proximity',
            'settings',
            'vibration',
            'light',
            'remotecontroller',
            'drivecontroller',
            'mhealth',
            'sphero',
            'dice',
            'temperature',
            'camera',
            'canvas',
            'health',
            'touch',
            'humandetect',
            'keyevent',
            'omnidirectionalimage',
            'tv',
            'powermeter',
            'humidity',
            'illuminance',
            'videochat',
            'airconditioner',
            'atmosphericpressure',
            'gpio');
        dConnect.authorization(scopes, 'ヘルプ',
            function(clientId, accessToken) {
                mAccessToken = accessToken;
                if (window.Android) {
                    Android.setCookie("accessToken", mAccessToken);
                } else {
                    setCookie("accessToken", mAccessToken);
                }
                callback();
            },
            function(errorCode, errorMessage) {
                showAlert("認証に失敗しました", errorCode, errorMessage);
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
        if (!dConnect.isConnectedWebSocket()) {
            dConnect.connectWebSocket(mSessionKey, function(errorCode, errorMessage) {
                console.log('Failed to open websocket: ' + errorCode + ' - ' + errorMessage);
            });
            console.log('WebSocket opened.');
        } else {
            console.log('WebSocket has opened already.');
        }
    }

    function setCookie(key, value) {
        document.cookie = key + '=' + value;
    }

    function getCookie(name) {
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

    function sendRequest(method, uri, body, cb) {
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
                 console.log("サーバ接続を確立しました。\nxhr.readyState=" + xhr.readyState + "\nxhr.statusText=" + xhr.statusText);
                 try {
                     xhr.setRequestHeader("X-GotAPI-Origin".toLowerCase(), "file://android_assets");
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
                 console.log("リクエストを送信しました。\nxhr.readyState=" + xhr.readyState + "\nxhr.statusText=" + xhr.statusText);
                 break;
             case 3:
                 console.log("リクエストの処理中。\nxhr.readyState=" + xhr.readyState + "\nxhr.statusText=" + xhr.statusText);
                break;
             case 4:
                 cb(xhr.status, xhr.responseText);
                 break;
             default:
                 break;
             }
        };
        xhr.open(method, uri);
    }
    parent.sendRequest = sendRequest;


    function getUri(path) {
        return 'http://' + mHost + ':4035' + path;
    }
    parent.getUri = getUri;


    function getProfile() {
        return getQuery('profile');
    }
    parent.getProfile = getProfile;


    function getServiceId() {
        return getQuery('serviceId');
    }
    parent.getServiceId = getServiceId;


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


    function formatJSON(jsonText) {
        var jsonBefore = JSON.parse(jsonText);
        var json = JSON.stringify(jsonBefore, null, "    ");
        return json.replace(/\r?\n/g, "<br>").replace(/\s/g, "&nbsp;");
    }
    parent.formatJSON = formatJSON;

    return parent;
})(util || {}, this.self || global);
