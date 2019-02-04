
var main = (function(parent, global) {

    var mIconList = {
        'battery' : 'images/icon01_battery.png',
        'mediastreamrecording' : 'images/icon02_mediaStreamRecording.png',
        'deviceorientation' : 'images/icon03_deviceOrientation.png',
        'vibration' : 'images/icon04_vibration.png',
        'setting' : 'images/icon05_settings.png',
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
        'touch' : 'images/icon27_touch.png',
        'connection' : 'images/icon33_connect6.png',
    };

    var delay = 0;

    function init() {
        util.init(function(name, json) {
            createProfileList(json.supports);
        });
    }
    parent.init = init;

    function createProfileCell(url, content, icon) {
        if (content.length > 15) {
            content = content.slice(0, 12) + "...";
        }
        var data = {
            'url' : url,
            'content' : content,
            'icon' : icon,
            'delay' : delay
        };
        delay += 0.08;
        return util.createTemplate('profileCell', data);
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
        url += '&ssl=' + (util.isSSL() ? 'on' : 'off');
        url += '&port=' + util.getPort();
        var icon = mIconList[profile.toLowerCase()];
        if (!icon) {
            icon = 'images/icon21_other.png';
        }
        return createProfileCell(url, profile, icon);
    }

    function resize() {
        var profileDiv = document.getElementById('profileList');
        var width = profileDiv.clientWidth / 3;
        var profiles = document.getElementsByClassName('grid');
        for (var i = 0; i < profiles.length; i++) {
            profiles[i].style.width = width + 'px';
        }
    }

    function createProfileList(profileList) {
        var rowSize = 3;
        var contentHTML = '';
        var countRow = profileList.length / rowSize;
        for (var i = 0; i < countRow; i++) {
            var content = '';
            for (var j = 0; j < rowSize; j++) {
                if (i * rowSize + j < profileList.length) {
                    content += createProfile(profileList[i * rowSize + j]);
                }
            }
            contentHTML += content;
        }

        document.getElementById('profileList').innerHTML = createProfileTable(contentHTML);

        resize();

        window.addEventListener('resize', function() {
            resize();
        }, false);
    }

    return parent;
})(main || {}, this.self || global);
