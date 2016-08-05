
var main = (function(parent, global) {
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

    function createProfileCell(url, content) {
        var data = {
            'url' : url,
            'content' : content
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
        return createProfileCell(url, profile);
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
