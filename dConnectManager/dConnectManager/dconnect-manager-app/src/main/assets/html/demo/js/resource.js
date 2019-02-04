
var main = (function(parent, global) {
    function init() {
        var mimeType = decodeURIComponent(util.getMimeType());
        var uri = util.getResourceUri();
        if (mimeType.indexOf('image') === 0) {
            var elem = document.getElementById('image');
            elem.src = uri;
            elem.onerror = function() {
                alert('Not found the image.');
            }
            elem.onload = function() {
                console.log("onload: " + uri);
            }
        } else {
            sendRequest('GET', uri, null, function(status, responseText) {
                var elem = document.getElementById('text');
                if (status == 200) {
                    elem.innerHTML = util.escapeText(responseText);
                } else {
                    elem.innerHTML = "通信に失敗しました。";
                }
            });
        }
    }
    parent.init = init;


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

    function sendRequest(method, uri, body, callback) {
         var xhr = createXMLHttpRequest();
         xhr.onreadystatechange = function() {
             switch (xhr.readyState) {
             case 1:
                 try {
                     xhr.setRequestHeader("X-GotAPI-Origin".toLowerCase(), "file://android_assets");
                 } catch (e) {
                     return;
                 }
                 xhr.send(body);
                 break;
             case 2:
             case 3:
                break;
             case 4:
                 callback(xhr.status, xhr.responseText);
                 break;
             default:
                 break;
             }
        };
        xhr.open(method, uri);
    }

    return parent;
})(main || {}, this.self || global);
