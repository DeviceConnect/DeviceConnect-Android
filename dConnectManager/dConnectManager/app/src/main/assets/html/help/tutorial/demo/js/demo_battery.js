
function initBattery() {
    init();

    searchHostDevicePlugin(function(service) {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('battery');
        builder.setServiceId(service.id);
        builder.setAccessToken(mAccessToken);
        var uri = builder.build();
        dConnect.get(uri, null, function(json) {
            var elem = document.getElementById("battery");
            elem.innerHTML = "" + json.level * 100 + "%";
        }, function(errorCode, errorMessage) {
            alert("# " + errorCode);
        });
    });

}
