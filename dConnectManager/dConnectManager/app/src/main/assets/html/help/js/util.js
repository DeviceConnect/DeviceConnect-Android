
var mAccessToken = null;

function init() {
    dConnect.setExtendedOrigin("file:///android_asset/");
    checkDeviceConnect();
    mAccessToken = getCookie('accessToken');
}

function startManager(onavailable) {
  var requested = false;
  var errorCallback = function(errorCode, errorMessage) {
    switch (errorCode) {
      case dConnect.constants.ErrorCode.ACCESS_FAILED:
        if (!requested) {
          requested = true;
          dConnect.startManager();
        }
        setTimeout(function() {
          dConnect.checkDeviceConnect(onavailable, errorCallback);
        }, 500);
        break;
      case dConnect.constants.ErrorCode.INVALID_SERVER:
        alert('WARNING: Device Connect Manager may be spoofed.');
        break;
      case dConnect.constants.ErrorCode.INVALID_ORIGIN:
        alert('WARNING: Origin of this app is invalid. Maybe the origin is not registered in whitelist.');
        break;
      default:
        alert(errorMessage);
        break;
    }
  };

  dConnect.checkDeviceConnect(onavailable, errorCallback);
}

function checkDeviceConnect() {
    startManager(function(apiVersion) {
        alert('Device Connect API version:' + apiVersion);
    });
}

function authorization(callback) {
    var scopes = Array(
        'servicediscovery',
        'serviceinformation',
        'system',
        'battery',
        'deviceorientation',
        'mediastream_recording',
        'vibration');
    dConnect.authorization(scopes, 'ヘルプ',
        function(clientId, accessToken) {
            mAccessToken = accessToken;
            setCookie("accessToken", mAccessToken);
            callback();
        },
        function(errorCode, errorMessage) {
            alert("認証に失敗しました。errorCode=" + errorCode + " errorMessage=" + errorMessage);
        });
}

function searchHostDevicePluginInternal(callback) {
    dConnect.discoverDevices(mAccessToken, function(json) {
        for (var i = 0; i < json.services.length; i++) {
            if (json.services[i].name.toLowerCase().indexOf("host") == 0) {
                callback(json.services[i]);
            }
        }
    }, function(errorCode, errorMessage) {
        alert("#### errorCode: " + errorCode);
    });
}

function searchHostDevicePlugin(callback) {
    if (mAccessToken == null) {
        authorization(function() {
            searchHostDevicePluginInternal(callback);
        });
    } else {
        searchHostDevicePluginInternal(callback);
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
