
var main = (function(parent, global) {

    var mIconList = {
        'battery' : 'images/icon01_battery.png',
        'mediastreamrecording' : 'images/icon02_mediaStreamRecording.png',
        'deviceorientation' : 'images/icon03_deviceOrientation.png',
        'vibration' : 'images/icon04_vibration.png',
        'settings' : 'images/icon05_settings.png',
        'serviceinformation' : 'images/icon05_settings.png',
        'canvas' : 'images/icon06_canvas.png',
        'mediaplayer' : 'images/icon07_mediaPlayer.png',
        'notification' : 'images/icon08_notification.png',
        'file' : 'images/icon09_file.png',
        'filedescriptor' : 'images/icon09_file.png',
        'proximity' : 'images/icon10_proximity.png',
        'phone' : 'images/icon11_phone.png',
        'keyevent' : 'images/icon12_keyEvent.png',
        'light' : 'images/icon13_light.png',
        'temperature' : 'images/icon14_temperature.png',
        'humidity' : 'images/icon15_humidity.png',
        'atmosphericpressure' : 'images/icon16_atmosphericPressure.png',
        'geolocation' : 'images/icon26_geolocation.png',
    };

    function init() {
        util.init(function(name, json) {
            setServiceInfo(name, util.getServiceId());
            createProfileList(json.supports);
        });
    }
    parent.init = init;

    function setServiceInfo(name, serviceId) {
        document.getElementById('serviceInfo').innerHTML = createServiceInfo(name, serviceId);
    }

    function createProfileCell(url, content, icon) {
        var data = {
            'url' : url,
            'content' : content,
            'icon' : icon
        };
        return util.createTemplate('profileCell', data);
    }

    function createProfileRow(content) {
        var data = {
            'content' : content
        };
        return util.createTemplate('profileRow', data);
    }

    function createProfileTable(content) {
        var data = {
            'content' : content
        };
        return util.createTemplate('profileTable', data);
    }

    function createServiceInfo(name, serviceId) {
        var data = {
            'name' : name,
            'id' : serviceId
        };
        return util.createTemplate('service', data);
    }

    function createProfile(profile) {
        var url = 'checker.html?serviceId=' + util.getServiceId() + '&profile=' + profile;
        var icon = mIconList[profile.toLowerCase()];
        if (!icon) {
            icon = 'images/icon21_other.png';
        }
        return createProfileCell(url, profile, icon);
    }

    function createProfileList(profileList) {
        var rowSize = 2;
        var contentHTML = '';
        var countRow = profileList.length / rowSize;
        for (var i = 0; i < countRow; i++) {
            var content = '';
            for (var j = 0; j < rowSize; j++) {
                if (i * rowSize + j < profileList.length) {
                    content += createProfile(profileList[i * rowSize + j]);
                }
            }
            contentHTML += createProfileRow(content);
        }

        document.getElementById('profileList').innerHTML = createProfileTable(contentHTML);
    }

    return parent;
})(main || {}, this.self || global);
