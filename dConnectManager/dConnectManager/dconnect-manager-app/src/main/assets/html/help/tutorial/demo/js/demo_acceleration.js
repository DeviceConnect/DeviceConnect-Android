
var demoAcceleration = (function(parent, global) {

    var mServiceId = null;
    var mDataList = new Queue();

    var mTimerId;

    var mContext;
    var mWidth = 380;
    var mHeight = 200;

    var mDataSize = mWidth / 4;

    function init() {
        util.init(function(service) {
            mServiceId = service.id;
        });

        var canvas = document.getElementById('graph');
        if (!canvas || !canvas.getContext) {
            return false;
        }
        mContext = canvas.getContext('2d');
        mContext.lineWidth = 1;
        drawGraph();
    }
    parent.init = init;


    function registerEvent() {
        if (mServiceId == null) {
            showNotFound();
            return;
        }

        var builder = new dConnect.URIBuilder();
        builder.setProfile('deviceorientation');
        builder.setAttribute('ondeviceorientation');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        var uri = builder.build();

        dConnect.addEventListener(uri, function(message) {
            var json = JSON.parse(message);
            if (json.orientation) {
                mDataList.enqueue(json.orientation);
                if (mDataList.size() > mDataSize) {
                    mDataList.dequeue();
                }
            }
        }, function() {
            console.log("開始成功");
            mTimerId = setInterval(function() {
                drawGraph();
                drawDataList();
            }, 200);
        }, function(errorCode, errorMessage) {
            util.showAlert("開始失敗", errorCode, errorMessage);
        });
    }
    parent.registerEvent = registerEvent;


    function unregisterEvent() {
        if (mServiceId == null) {
            showNotFound();
            return;
        }

        var builder = new dConnect.URIBuilder();
        builder.setProfile('deviceorientation');
        builder.setAttribute('ondeviceorientation');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        var uri = builder.build();
        dConnect.removeEventListener(uri, function() {
            clearInterval(mTimerId);
        }, function(errorCode, errorMessage) {
            util.showAlert("停止失敗", errorCode, errorMessage);
        });
    }
    parent.unregisterEvent = unregisterEvent;


    function drawGraph() {
        mContext.fillStyle = 'rgb(255, 255, 255)';
        mContext.fillRect(0, 0, mWidth, mHeight);

        mContext.strokeStyle = 'rgb(0, 0, 0)';
        mContext.beginPath();
        mContext.moveTo(0, mHeight / 2);
        mContext.lineTo(mWidth, mHeight / 2);
        mContext.stroke();

        mContext.font = "8px 'ＭＳ Ｐゴシック'";

        var h = (mHeight / 2) / 10;
        for (var i = 1; i < 10; i++) {
            mContext.strokeStyle = 'rgb(200, 200, 200)';

            var hh1 = (mHeight / 2) + h * i;
            mContext.beginPath();
            mContext.moveTo(0, hh1);
            mContext.lineTo(mWidth, hh1);
            mContext.stroke();

            var hh2 = (mHeight / 2) - h * i;
            mContext.beginPath();
            mContext.moveTo(0, hh2);
            mContext.lineTo(mWidth, hh2);
            mContext.stroke();

            if (i % 2 == 0) {
                mContext.strokeStyle = 'rgb(0, 0, 0)';
                mContext.strokeText(-2.5 * i, 2, hh1);
                mContext.strokeText(2.5 * i, 2, hh2);
            }
        }

        mContext.font = "14px 'ＭＳ Ｐゴシック'";
        mContext.strokeStyle = 'rgb(255, 0, 0)';
        mContext.strokeText('x軸', mWidth - 90, mHeight - 5);
        mContext.strokeStyle = 'rgb(0, 255, 0)';
        mContext.strokeText('y軸', mWidth - 60, mHeight - 5);
        mContext.strokeStyle = 'rgb(0, 0, 255)';
        mContext.strokeText('z軸', mWidth - 30, mHeight - 5);
    }

    function drawDataList() {
        mContext.strokeStyle = 'rgb(255, 0, 0)';
        mContext.beginPath();
        for (var i = 0; i < mDataList.size() - 1; i++) {
            var a = mDataList.get(i).accelerationIncludingGravity;
            var h = (mHeight / 2) - a.x * 5
            if (i == 0) {
                mContext.moveTo(i * 4, h);
            } else {
                mContext.lineTo(i * 4, h);
            }
        }
        mContext.stroke();

        mContext.strokeStyle = 'rgb(0, 255, 0)';
        mContext.beginPath();
        for (var i = 0; i < mDataList.size(); i++) {
            var a = mDataList.get(i).accelerationIncludingGravity;
            var h = (mHeight / 2) - a.y * 5
            if (i == 0) {
                mContext.moveTo(i * 4, h);
            } else {
                mContext.lineTo(i * 4, h);
            }
        }
        mContext.stroke();

        mContext.strokeStyle = 'rgb(0, 0, 255)';
        mContext.beginPath();
        for (var i = 0; i < mDataList.size(); i++) {
            var a = mDataList.get(i).accelerationIncludingGravity;
            var h = (mHeight / 2) - a.z * 5
            if (i == 0) {
                mContext.moveTo(i * 4, h);
            } else {
                mContext.lineTo(i * 4, h);
            }
        }
        mContext.stroke();
    }

    function showNotFound() {
        alert("HOSTデバイスプラグインが存在しません。");
    }

    return parent;
})(demoAcceleration || {}, this.self || global);

