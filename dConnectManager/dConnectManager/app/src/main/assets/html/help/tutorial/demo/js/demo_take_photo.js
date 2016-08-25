
var demoTakePhoto = (function(parent, global) {
    var mServiceId;
    var mMediaRecorders;
    var mImageSizes;
    var mPreviewSizes;

    function init() {
        util.init(function(service) {
            mServiceId = service.id;
            getCameraTarget();
        });
    }
    parent.init = init;

    function refreshImg(uri, id) {
        var img = document.getElementById(id);
        if (img) {
            img.src = uri + '?' + Date.now();
        }
    }

    function getCameraTarget() {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastream_recording');
        builder.setAttribute('mediarecorder');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        var uri = builder.build();
        dConnect.get(uri, null, function(json) {
            mMediaRecorders = json.recorders;
            createMediaRecorders();
        }, function(errorCode, errorMessage) {
            util.showAlert("カメラ情報の取得に失敗しました。。", errorCode, errorMessage);
        });
    }

    function getCameraOption(target) {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastream_recording');
        builder.setAttribute('options');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        if (target) {
            builder.addParameter('target', target);
        }
        var uri = builder.build();
        dConnect.get(uri, null, function(json) {
            mImageSizes = json.imageSizes;
            mPreviewSizes = json.previewSizes;
            createOptions();
        }, function(errorCode, errorMessage) {
            util.showAlert("カメラ情報の取得に失敗しました。。", errorCode, errorMessage);
        });
    }

    function createMediaRecorders() {
        var target = document.recorder.target;
        for (var i = 0; i < mMediaRecorders.length; i++) {
            if (mMediaRecorders[i].mimeType.indexOf('image/') == 0) {
                var option = document.createElement('option');
                option.setAttribute('value', mMediaRecorders[i].id);
                option.innerHTML = mMediaRecorders[i].name;
                target.appendChild(option);
            }
        }
        getCameraOption(mMediaRecorders[0].id);
    }

    function createOptions() {
        var minWidth = 10000000;
        var imageSizes = document.recorder.imageSize;
        for (var i = 0; i < mMediaRecorders.length; i++) {
            var option = document.createElement('option');
            option.setAttribute('value', i);
            option.innerHTML = mImageSizes[i].width + "x" + mImageSizes[i].height;

            if (mImageSizes[i].width < minWidth) {
                option.selected = true;
                minWidth = mImageSizes[i].width
            }

            imageSizes.appendChild(option);
        }

        minWidth = 10000000;
        var previewSizes = document.recorder.previewSize;
        for (var i = 0; i < mMediaRecorders.length; i++) {
            var option = document.createElement('option');
            option.setAttribute('value', i);
            option.innerHTML = mPreviewSizes[i].width + "x" + mPreviewSizes[i].height;

            if (mPreviewSizes[i].width < minWidth) {
                option.selected = true;
                minWidth = mImageSizes[i].width
            }

            previewSizes.appendChild(option);
        }

        startPreview();
    }

    function startPreview() {
        var target = document.recorder.target.value;

        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastream_recording');
        builder.setAttribute('preview');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        if (target !== null && target !== undefined) {
            builder.addParameter('target', target);
        }
        var uri = builder.build();
        dConnect.put(uri, null, null, function(json) {
            refreshImg(json.uri, 'preview');
        }, function(errorCode, errorMessage) {
            util.showAlert("プレビュー開始に失敗しました。", errorCode, errorMessage);
        });
    }

    function stopPreview(callback) {
        var target = document.recorder.target.value;

        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastream_recording');
        builder.setAttribute('preview');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        if (target !== null && target !== undefined) {
            builder.addParameter('target', target);
        }
        var uri = builder.build();
        dConnect.delete(uri, null, function(json) {
            if (callback) {
                setTimeout(callback, 2000);
            }
        }, function(errorCode, errorMessage) {
            util.showAlert("プレビュー停止に失敗しました。", errorCode, errorMessage);
        });
    }

    function addPhoto(uri) {
        var elem = document.createElement("img");
        elem.setAttribute("src", uri);
        elem.setAttribute("class", "photo")
        elem.setAttribute("crossorigin", "anonymous")
        elem.setAttribute("alt", "写真");

        var tag = document.getElementById('photos');
        tag.appendChild(elem);
    }

    function onTakePhoto() {
        var target = document.recorder.target.value;

        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastream_recording');
        builder.setAttribute('takephoto');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        if (target) {
            builder.addParameter('target', target);
        }
        var uri = builder.build();
        dConnect.post(uri, null, null, function(json) {
            addPhoto(json.uri);
        }, function(errorCode, errorMessage) {
            util.showAlert("撮影に失敗しました。", errorCode, errorMessage);
        });
    }
    parent.onTakePhoto = onTakePhoto;

    function onChangeTarget() {
        var target = document.recorder.target.value;
        getCameraOption(target);
    }
    parent.onChangeTarget = onChangeTarget;

    function onChangeOption() {
        if (!mImageSizes || !mPreviewSizes) {
            return;
        }

        stopPreview(function() {
            var imageSizeIndex = document.recorder.imageSize.value;
            var previewSizeIndex = document.recorder.previewSize.value;
            var target = document.recorder.target.value;

            var imageWidth = mImageSizes[imageSizeIndex].width;
            var imageHeight = mImageSizes[imageSizeIndex].height;
            var previewWidth = mPreviewSizes[previewSizeIndex].width;
            var previewHeight = mPreviewSizes[previewSizeIndex].height;

            var builder = new dConnect.URIBuilder();
            builder.setProfile('mediastream_recording');
            builder.setAttribute('options');
            builder.setServiceId(mServiceId);
            builder.setAccessToken(util.getAccessToken());
            builder.addParameter('target', target);
            builder.addParameter('imageWidth', imageWidth);
            builder.addParameter('imageHeight', imageHeight);
            builder.addParameter('previewWidth', previewWidth);
            builder.addParameter('previewHeight', previewHeight);
            builder.addParameter('mimeType', "image/png");
            var uri = builder.build();
            dConnect.put(uri, null, null, function(json) {
                startPreview();
            }, function(errorCode, errorMessage) {
                util.showAlert("設定に失敗しました。", errorCode, errorMessage);
            });
        });
    }
    parent.onChangeOption = onChangeOption;

    return parent;
})(demoTakePhoto || {}, this.self || global);
