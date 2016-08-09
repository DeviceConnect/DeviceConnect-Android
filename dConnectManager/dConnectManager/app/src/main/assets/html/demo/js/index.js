
var main = (function(parent, global) {

    var mIconList = {
        'battery' : 'images/icon01_battery.png',
        'mediastreamrecording' : 'images/icon02_mediaREC.png',
        'deviceorientation' : 'images/icon05_acceleration3.png',
        'vibration' : 'images/icon07_vibration.png',
        'settings' : 'images/icon08_setting.png',
        'serviceinformation' : 'images/icon08_setting.png',
        'canvas' : 'images/icon09_canvas.png',
        'mediaplayer' : 'images/icon10_mediaPlayer.png',
        'notification' : 'images/icon11_notification.png',
        'file' : 'images/icon12_file.png',
        'filedescriptor' : 'images/icon12_file.png',
        'proximity' : 'images/icon15_proximity3.png',
        'phone' : 'images/icon17_phone.png',
        'keyevent' : 'images/icon19_kye2.png',
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
