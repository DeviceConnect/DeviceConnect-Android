
var demoBattery = (function(parent, global) {

    var mServiceId;
    var mBatteryLevel;
    var mBatteryCharging;

    function init() {
        util.init(function(service) {
            mServiceId = service.id;
            getBatteryInfo(service.id);
        });
    }
    parent.init = init;

    function getBatteryInfo(serviceId) {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('battery');
        builder.setServiceId(serviceId);
        builder.setAccessToken(util.getAccessToken());
        var uri = builder.build();
        dConnect.get(uri, null, function(json) {
            mBatteryLevel = json.level;
            mBatteryCharging = json.charging;
            showBattery();
            registerEvent();
            registerEvent2();
        }, function(errorCode, errorMessage) {
            util.showAlert("バッテリー情報の取得に失敗しました", errorCode, errorMessage);
        });
    }

    function showBattery(json) {
        var imageName;
        if (mBatteryLevel > 0.66) {
            imageName = 'images/battery_1.png';
        } else if (mBatteryLevel > 0.33) {
            imageName = 'images/battery_2.png';
        } else {
            imageName = 'images/battery_3.png';
        }
        if (mBatteryCharging) {
            imageName = 'images/battery_4.png';
        }

        var img = document.getElementById("batteryImage");
        img.src = imageName;

        var elem = document.getElementById("battery");
        elem.innerHTML = mBatteryLevel * 100 + "%";
    }

    function registerEvent() {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('battery');
        builder.setAttribute('onchargingchange');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        builder.setSessionKey(util.getSessionKey());
        var uri = builder.build();

        dConnect.addEventListener(uri, function(message) {
            var json = JSON.parse(message);
            mBatteryCharging = json.battery.charging;
            showBattery();
        }, null, function(errorCode, errorMessage) {
            util.showAlert("バッテリー", errorCode, errorMessage);
        });
    }

    function registerEvent2() {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('battery');
        builder.setAttribute('onbatterychange');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        builder.setSessionKey(util.getSessionKey());
        var uri = builder.build();

        dConnect.addEventListener(uri, function(message) {
            var json = JSON.parse(message);
            mBatteryLevel = json.battery.level;
            showBattery();
        }, null, function(errorCode, errorMessage) {
            util.showAlert("バッテリー", errorCode, errorMessage);
        });
    }

    return parent;
})(demoBattery || {}, this.self || global);


