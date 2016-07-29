
var main = (function(parent, global) {

    function init() {
//        util.init(function(json) {
//            createSupportApi();
//        });

        document.getElementById('main').innerHTML = createCommand();
    }
    parent.init = init;


    function onChangeValue(nav, name) {
        var elem = document.forms[nav];
        elem['t_' + name].value = elem[name].value;
    }
    parent.onChangeValue = onChangeValue;


    function onSendRequest(nav) {
        var method;
        var path;
        var accessToken;
        var serviceId;
        var data = [];
        var body = null;

        data.push("serviceId=" + util.getServiceId());
        data.push("accessToken=" + util.getAccessToken());

        var formElem = document.forms[nav];
        for (var key in formElem) {
            var elem = formElem[key];
            if (elem && elem.tagName) {
                if (elem.tagName.toLowerCase() == 'input') {
                    if (elem.name == 'deviceconnect.method') {
                        method = elem.value;
                    } else if (elem.name == 'deviceconnect.path') {
                        path = elem.value;
                    } else if (elem.name.indexOf('t_') != 0) {
                        data.push(elem.name + "=" + elem.value);
                    }
                } else if (elem.tagName.toLowerCase() == 'select') {
                    data.push(elem.name + "=" + elem.value);
                }
            }
        }

        if (method == 'GET' || method == 'DELETE') {
            path = path + "?" + data.join('&');
        } else {
            body = data.join('&');
        }

        document.getElementById(nav + '_request').innerHTML = createRequest(method + " " + path);

        util.sendRequest(method, util.getUri(path), body, function(status, response) {
            if (status == 200) {
                document.getElementById(nav + '_response').innerHTML = createResponse(util.formatJSON(response));
            } else {
                document.getElementById(nav + '_response').innerHTML = createResponse("" + status);
            }
        });
    }
    parent.onSendRequest = onSendRequest;


    function createCommand() {
        var data = {
            'title': 'GET /gotapi/availability',
            'nav' : 'nav1',
            'content' : createParam()
        };
        return util.createTemplate('command', data);
    }

    function createParam() {
        var list = [
            "abc1", "abc2", "abc3"
        ];
        var data = {
            'nav' : "TEST",
            'method' : 'GET',
            'path' : '/gotapi/availability',
            'content' : createTextParam("text0", "default") + createSelectParam("test1", list) + createSliderParam()
        };
        return util.createTemplate('param', data);
    }

    function createTextParam(name, value) {
        var data = {
            'name' : name,
            'value' : value
        };
        return util.createTemplate('param_text', data);
    }

    function createNumberParam(name, value) {
        var data = {
            'name' : name,
            'value' : value
        };
        return util.createTemplate('param_number', data);
    }

    function createSelectParam(name, list) {
        var text = "";
        for (var i = 0; i < list.length; i++) {
            text += '<option value="' + list[i] + '">' + list[i] + '</option>';
        }
        var data = {
            'name' : name,
            'value' : text
        };
        return util.createTemplate('param_select', data);
    }

    function createSliderParam() {
        var data = {
            'nav' : 'TEST',
            'name' : "test3",
            'value' : "0.5",
            'step' : 0.01,
            'min' : "0.0",
            'max' : "1.0"
        };
        return util.createTemplate('param_slider', data);
    }

    function createRequest(t) {
        var data = {
            'body' : t
        };
        return util.createTemplate('request', data);
    }

    function createResponse(t) {
        var data = {
            'body' : t
        };
        return util.createTemplate('response', data);
    }

    function createSupportApi(json) {
        var data = {
            title : "abc"
        };
        var htmlText = util.createTemplate('request', data);
        document.getElementById('main').innerHTML = htmlText;
    }

    return parent;
})(main || {}, this.self || global);
