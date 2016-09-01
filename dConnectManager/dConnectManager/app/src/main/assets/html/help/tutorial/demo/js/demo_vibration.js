
var demoVibration = (function(parent, global) {

    var vibrationPattern = [
        "1000",
        "1000,1000,1000,1000,1000,1000,1000,1000,1000,1000",
        "2000",
        "2000,2000,2000,2000,2000,2000,2000,2000,2000,2000"
    ];

    var mServiceId;

    function init() {
        util.init(function(service) {
            mServiceId = service.id;
        });
    }
    parent.init = init;


    function startVibration() {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('vibration');
        builder.setAttribute('vibrate');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        builder.addParameter('pattern', getVibrationPattern());
        var uri = builder.build();
        dConnect.put(uri, null, null, function(json) {
        }, function(errorCode, errorMessage) {
            util.showAlert("バイブレーションの開始に失敗しました。", errorCode, errorMessage);
        });
    }
    parent.startVibration = startVibration;


    function stopVibration() {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('vibration');
        builder.setAttribute('vibrate');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        var uri = builder.build();
        dConnect.delete(uri, null, function(json) {
        }, function(errorCode, errorMessage) {
            util.showAlert("バイブレーションの停止に失敗しました。", errorCode, errorMessage);
        });
    }
    parent.stopVibration = stopVibration;


    function getVibrationPattern() {
        var pattern = document.vibration.pattern.value;
        return vibrationPattern[pattern];
    }

    return parent;
})(demoVibration || {}, this.self || global);
