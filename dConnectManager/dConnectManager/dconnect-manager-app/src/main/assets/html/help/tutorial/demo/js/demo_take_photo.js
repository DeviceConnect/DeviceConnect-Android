
var demoTakePhoto = (function(parent, global) {
    var mServiceId;
    var mMediaRecorders;
    var mImageSizes;
    var mPreviewSizes;
    var mPreviewTarget;

    function init() {
        util.init(function(service) {
            mServiceId = service.id;
            getCameraTarget();
        });
    }
    parent.init = init;

    function refreshImg(uri) {
        document.body.style.backgroundImage = 'url(' + uri + ')';
    }

    function getCameraTarget() {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastreamrecording');
        builder.setAttribute('mediarecorder');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        var uri = builder.build();
        dConnect.get(uri, null, function(json) {
            mMediaRecorders = json.recorders;
            createMediaRecorders();
        }, function(errorCode, errorMessage) {
            util.showAlert('カメラ情報の取得に失敗しました。。', errorCode, errorMessage);
        });
    }

    function getCameraOption(target) {
        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastreamrecording');
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
            onStartPreview();
        }, function(errorCode, errorMessage) {
            util.showAlert('カメラ情報の取得に失敗しました。。', errorCode, errorMessage);
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
        for (var i = 0; i < mImageSizes.length; i++) {
            var option = document.createElement('option');
            option.setAttribute('value', i);
            option.innerHTML = mImageSizes[i].width + 'x' + mImageSizes[i].height;

            if (mImageSizes[i].width < minWidth) {
                option.selected = true;
                minWidth = mImageSizes[i].width
            }

            imageSizes.appendChild(option);
        }

        minWidth = 10000000;
        var previewSizes = document.recorder.previewSize;
        for (var i = 0; i < mPreviewSizes.length; i++) {
            var option = document.createElement('option');
            option.setAttribute('value', i);
            option.innerHTML = mPreviewSizes[i].width + 'x' + mPreviewSizes[i].height;

            if (mPreviewSizes[i].width < minWidth) {
                option.selected = true;
                minWidth = mPreviewSizes[i].width
            }

            previewSizes.appendChild(option);
        }
    }

    function startPreview() {
        var target = document.recorder.target.value;

        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastreamrecording');
        builder.setAttribute('preview');
        builder.setServiceId(mServiceId);
        builder.setAccessToken(util.getAccessToken());
        if (target !== null && target !== undefined) {
            builder.addParameter('target', target);
        }
        var uri = builder.build();
        dConnect.put(uri, null, null, function(json) {
            refreshImg(json.uri);
        }, function(errorCode, errorMessage) {
            util.showAlert('プレビュー開始に失敗しました。', errorCode, errorMessage);
        });
    }

    function stopPreview(callback) {
        var target = mPreviewTarget;

        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastreamrecording');
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
            console.log('プレビュー停止に失敗しました。' + errorCode + ":" + errorMessage);
        });
    }

    function showPhoto(uri) {
        var elem = document.getElementById('photos');
        elem.style.display = 'block';
        elem.onclick = function() {
            elem.style.display = 'none';
        };

        var image = document.getElementById('photo');
        image.setAttribute('src', uri);
    }

    function onClickPhoto(elem) {
        var uri = elem.getAttribute("src")
        showPhoto(uri);
    }

    function addPhoto(uri) {
        var elem = document.createElement('img');
        elem.setAttribute('src', uri);
        elem.setAttribute('class', 'thumbnail')
        elem.setAttribute('crossorigin', 'anonymous')
        elem.setAttribute('alt', '写真');
        elem.onclick = function() {
            showPhoto(uri);
        };

        var tag = document.getElementById('thumbnails');
        tag.appendChild(elem);
    }

    function onTakePhoto() {
        var target = mPreviewTarget;

        var builder = new dConnect.URIBuilder();
        builder.setProfile('mediastreamrecording');
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
            util.showAlert('撮影に失敗しました。', errorCode, errorMessage);
        });
    }
    parent.onTakePhoto = onTakePhoto;

    function onStartPreview() {
        if (mPreviewTarget) {
            stopPreview(function() {
                mPreviewTarget = document.recorder.target.value;
                startPreview();
            });
        } else {
            mPreviewTarget = document.recorder.target.value;
            startPreview();
        }
    }
    parent.onStartPreview = onStartPreview;

    function onStopPreview() {
        stopPreview(null);
        mPreviewTarget = undefined;
    }
    parent.onStopPreview = onStopPreview;

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
            builder.setProfile('mediastreamrecording');
            builder.setAttribute('options');
            builder.setServiceId(mServiceId);
            builder.setAccessToken(util.getAccessToken());
            builder.addParameter('target', target);
            builder.addParameter('imageWidth', imageWidth);
            builder.addParameter('imageHeight', imageHeight);
            builder.addParameter('previewWidth', previewWidth);
            builder.addParameter('previewHeight', previewHeight);
            builder.addParameter('mimeType', 'image/png');
            var uri = builder.build();
            dConnect.put(uri, null, null, function(json) {
                if (mPreviewTarget) {
                    startPreview();
                }
            }, function(errorCode, errorMessage) {
                util.showAlert('設定に失敗しました。', errorCode, errorMessage);
            });
        });
    }
    parent.onChangeOption = onChangeOption;

    window.onbeforeunload = function(e) {
        onStopPreview();
        return;
    };

    return parent;
})(demoTakePhoto || {}, this.self || global);
