import * as SDK from './core.js'
import Storage from './storage.js'

// イベントバス
const EventBus = new Vue();

// SDK 初期化
let _currentSession = null;
const host = '192.168.0.19';
const scopes = [
  'serviceDiscovery',
  'serviceInformation',
  'mediaStreamRecording',
  'mediaPlayer',
  'canvas'
];
const sdk = new SDK.DeviceConnectClient({ appName: 'test' });
const storage = new Storage();

// ルーティング設定
Vue.component('app-recorder', {
  template: '#app-recorder',
  props: [],
  mounted () {
    EventBus.$on('on-launched', this.onLaunched);
    EventBus.$on('on-photo', this.onPhoto);
    EventBus.$on('on-start-recording', this.onStartRecording);
    EventBus.$on('on-stop-recording', this.onStopRecording);
    if (app && !app.launching) {
      this.startPreview();
    }
  },
  beforeDestroy () {
    this.stopPreview();
  },
  data () {
    return {
      launched: false,
      latestPhotoUri: null,
      isRecording: false,
      isStartingRecording: false,
      isStoppingRecording: false,
      isTakingPhoto: false
    }
  },
  computed: {
    canTakePhoto: function() {
      return this.launched && !this.isRecording;
    },
    canStopRecording: function() {
      return this.launched && !this.isStartingRecording && !this.isStoppingRecording && !this.isTakingPhoto;
    },
    canStartRecording: function() {
      return this.launched && !this.isStartingRecording && !this.isStoppingRecording && !this.isTakingPhoto;
    }
  },
  methods: {
    requestTakePhoto: function() {
      this.isTakingPhoto = true;
      EventBus.$emit('take-photo');
    },
    startRecording: function() {
      this.isStartingRecording = true;
      EventBus.$emit('start-recording');
    },
    stopRecording: function() {
      this.isStoppingRecording = true;
      EventBus.$emit('stop-recording');
    },
    startPreview: function() {
      EventBus.$emit('start-preview');
    },
    stopPreview: function() {
      EventBus.$emit('stop-preview');
    },
    onLaunched: function() {
      this.launched = true;
      this.startPreview();
    },
    onPhoto: function(event) {
      console.log('onPhoto: uri=' + event.uri);
      this.isTakingPhoto = false;
      this.latestPhotoUri = event.uri;
    },
    onStartRecording: function() {
      console.log('onStartRecording:');
      this.isRecording = true;
      this.isStartingRecording = false;
    },
    onStopRecording: function() {
      console.log('onStopRecording:');
      this.isRecording = false;
      this.isStoppingRecording = false;
    }
  }
})
Vue.component('app-viewer', {
  template: '#app-viewer',
  created () {
    const mediaList = storage.getObject('mediaList');
    if (mediaList !== null) {
      this.mediaList = mediaList;
    }
    console.log('Created viewer: mediaList', this.mediaList);
  },
  mounted () {
    //this.openPhotoSwipe();
  },
  data () {
    return {
      mediaList: []
    }
  },
  methods: {
    showImage (index) {
      this.openPhotoSwipe(index);
    },
    openPhotoSwipe (index) {
      var pswpElement = document.querySelectorAll('.pswp')[0];
      console.log('PhotoSwipe: ' + pswpElement);

      var items = this.mediaList.map(media => {
        return { src:media.uri, w:200, h:200 }
      });
      
      var options = {
          index,
          history: false,
          focus: false,
          showAnimationDuration: 250,
          hideAnimationDuration: 250
      };
      
      var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
      gallery.init();
    }
  }
})
Vue.component('app-qr', {
  template: '#app-qr'
})
const router = new VueRouter({
  routes: [
    { path: '/', component: { template: '<app-recorder></app-recorder>' } },
    { path: '/viewer', component: { template: '<app-viewer></app-viewer>' } },
    { path: '/qr', component: { template: '<app-qr></app-qr>' } }
  ]
});

const app = new Vue({
  el: '#app',
  router,
  mounted () {
    const info = storage.getObject('session');
    console.log('Latest session:', info);
    if (info !== null) {
      sdk.addSession({ host:info.host, accessToken:info.accessToken, scopes:info.scopes });
    }
    EventBus.$on('on-launched', this.onLaunched)
    EventBus.$on('take-photo', function() { app.requestTakePhoto(); })
    EventBus.$on('start-recording', function() { app.startRecording(); })
    EventBus.$on('stop-recording', function() { app.stopRecording(); })
    EventBus.$on('start-preview', function() { app.startPreview(); })
    EventBus.$on('stop-preview', function() { app.stopPreview(); })
    EventBus.$on('connection-error', this.onError)
    connect();
  },
  data () {
    return {
      host: host,
      hostService: null,
      launching: true,
      dialog: false,
      showError: false,
      showErrorTime: 60000,
      showDrawer: false,
      pages: [
        { path: '/', title: '撮影', icon: 'camera_alt' },
        { path: '/viewer', title: 'ビューア', icon: 'collections' },
        { path: '/qr', title: 'QRコード', icon: 'crop_free' }
      ],

      // Host サービスのレコーダーの配列
      recorders: [],

      // 撮影したメディアの配列
      mediaList: [],

      // プレビュー中のレコーダー
      activeRecorderId: null,

      // 現在のレコーダー設定
      recorderSettings: {
        enabled: false,
        id: null,
        previewSize: null,
        imageSize: null,
        frameRate: null
      },

      connectionError: null
    }
  },
  computed: {
    recorderNames: function() {
      console.log('recorderNames: recorders', this.recorders);
      return this.recorders.map((r) => {
        return { text:r.recorder.name, value:r.recorder.id };
      });
    },
    connectionErrorText: function() {
      if (this.connectionError !== null) {
        return this.connectionError.message;
      }
      return '';
    },
    settingsEnabled: function() {
      return !this.launching && (this.connectionError == null);
    }
  },
  methods: {
    onLaunched() {},
    showPage: function(path) {
      //updateButton(path);
      router.push({ path: path });
    },
    onError: function(event) {
      this.connectionError = { message: event.message }
      this.showError = true;
    },
    supportedPreviewSizes: function(recorderId) {
      console.log('supportedPreviewSizes: id=' + recorderId);
      const recorders = this.recorders;
      if (recorderId && recorders) {
        for (let k in recorders) {
          let r = recorders[k];
          if (recorderId === r.recorder.id) {
            console.log('options.previewSizes: ' + r.options.previewSizes);
            return r.options.previewSizes.map(s => s.width + ' x ' + s.height);
          }
        }
      }
      return null;
    },
    supportedImageSizes: function(recorderId) {
      console.log('supportedImageSizes: id=' + recorderId);
      const recorders = this.recorders;
      if (recorderId && recorders) {
        for (let k in recorders) {
          let r = recorders[k];
          if (recorderId === r.recorder.id) {
            console.log('options.imageSizes: ' + r.options.imageSizes);
            return r.options.imageSizes.map(s => s.width + ' x ' + s.height);
          }
        }
      }
      return null;
    },
    changeRecorderOption: function() {
      const settings = this.recorderSettings;
      const imageSize = settings.imageSize.split(' x ');
      const previewSize = settings.previewSize.split(' x ');
      const options = {
        imageWidth: imageSize[0],
        imageHeight: imageSize[1],
        previewWidth: previewSize[0],
        previewHeight: previewSize[1],
        previewMaxFrameRate: settings.frameRate
      };
      const serviceId = this.hostService.id;
      putRecorderOption(_currentSession, serviceId, settings.id, options)
      .then(() => {
        console.log('Changed Recorder Option: service=' + serviceId + ', target=' + settings.id);
        if (this.activeRecorderId === null) {
          return Promise.resolve();
        }
        return stopPreview(_currentSession, serviceId, this.activeRecorderId)
      })
      .then(() => {
        console.log('Stopped preview: service=' + serviceId + ', target=' + this.activeRecorderId);
        return startPreview(_currentSession, serviceId, settings.id, '#preview')
      })
      .then(() => {
        console.log('Started preview: service=' + serviceId + ', target=' + settings.id);
        this.activeRecorderId = settings.id;
      })
      .catch((err) => {
        console.error('Failed to restart preview.', err);
      });
    },
    requestTakePhoto: function() {
      takePhoto(_currentSession, this.hostService.id, this.activeRecorderId)
      .then((json) => {
        const uri = json.uri;
        console.log('Photo: uri=' + uri);
        if (uri) {
          this.storeMedia({ type:'image', uri:json.uri });
          EventBus.$emit('on-photo', { uri: uri.replace('localhost', host) })
        }
      })
      .catch((err) => {
        console.error('Failed to take photo.', err);
      })
    },
    startRecording: function() {
      const target = this.activeRecorderId;
      startRecording(_currentSession, this.hostService.id, target)
      .then((json) => {
        console.log('Started recording: target=' + target);
        EventBus.$emit('on-start-recording');
      })
      .catch((err) => {
        console.error('Failed to start recording.', err);
      });
    },
    stopRecording: function() {
      const target = this.activeRecorderId;
      stopRecording(_currentSession, this.hostService.id, target)
      .then((json) => {
        console.log('Stopped recording: target=' + target);
        this.storeMedia({ type:'video', uri:json.uri });
        EventBus.$emit('on-stop-recording');
      })
      .catch((err) => {
        console.error('Failed to stop recording.', err);
      });
    },
    startPreview() {
      const target = this.activeRecorderId;
      startPreview(_currentSession, this.hostService.id, target, '#preview')
      .catch((err) => {
        console.error('Failed to start preview.', err);
      })
    },
    stopPreview() {
      const target = this.activeRecorderId;
      stopPreview(_currentSession, this.hostService.id, target)
      .catch((err) => {
        console.error('Failed to stop preview.', err);
      })
    },
    reconnect() {
      connect();
    },
    storeMedia(media) {
      this.mediaList.push(media);
      console.log('storeMedia: ', this.mediaList);
      storage.setObject('mediaList', this.mediaList);
    }
  }
});

function connect() {
  sdk
  .connect({ host, scopes })
  .then(result => {
    console.log('Connected', result.services);
    _currentSession = result.session;

    // セッション情報を保存
    storage.setObject('session', {
      host,
      scopes,
      accessToken: _currentSession.accessToken
    });

    const services = result.services;

    let hostService = null;
    for (let k in services) {
      let service = services[k];
      if (service.name === 'Host') {
        hostService = service;
        break;
      }
    }
    if (hostService === null) {
      throw new Error('No Host Service.');
    }
    app.hostService = hostService;

    // レコーダー情報を取得
    return getRecorderList(result.session, hostService.id)
  })
  .then(result => {
    console.log('Recorders:', result.recorders);

    // 各レコーダーのオプションを取得
    const promises = result.recorders.map(recorder => {
      return getRecorderOption(result.session, result.serviceId, recorder)
    })
    return Promise.all(promises)
  })
  .then(results => {
    app.recorders = results;

    // レコーダー情報を Vue に反映.
    let current = null;
    results.forEach(result => {
      if (result.recorder.id === 'camera_0') {
        current = result;
      }
    });
    const r = current.recorder;
    const settings = app.recorderSettings;
    settings.id = r.id;
    settings.previewSize = r.previewWidth + ' x ' + r.previewHeight;
    settings.imageSize = r.imageWidth + ' x ' + r.imageHeight;
    settings.frameRate = r.previewMaxFrameRate;

    app.activeRecorderId = r.id;
    app.launching = false;
    EventBus.$emit('on-launched');
    console.log('Active Recorder: target=' + r.id);
  })
  .catch((err) => {
    console.warn('Could not connected.', err);
    EventBus.$emit('connection-error', { message: err.errorMessage });
  })
}

function getRecorderList(session, serviceId) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'GET',
      path: '/gotapi/mediaStreamRecording/mediaRecorder',
      params: {
        serviceId
      }
    })
    .then((json) => {
      const result = json.result;
      const recorders = json.recorders;
      if (result !== 0 || recorders === undefined) {
        reject(json);
        return;
      }
      resolve({session, serviceId, recorders});
    })
    .catch((err) => {
      reject(err);
    })
  });
}

function getRecorderOption(session, serviceId, recorder) {
  return new Promise((resolve, reject) => {
    const target = recorder.id;
  
    session.request({
      method: 'GET',
      path: '/gotapi/mediaStreamRecording/options',
      params: {
        serviceId,
        target
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve({session, serviceId, recorder, options:json});
    })
    .catch((err) => {
      reject(err);
    })
  })
}

function putRecorderOption(session, serviceId, target, options) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'PUT',
      path: '/gotapi/mediaStreamRecording/options',
      params: {
        serviceId,
        target,
        imageWidth: options.imageWidth,
        imageHeight: options.imageHeight,
        previewWidth: options.previewWidth,
        previewHeight: options.previewHeight,
        mimeType: 'video/x-mjpeg'
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve();
    })
    .catch((err) => {
      reject(err);
    })
  })
}

function startPreview(session, serviceId, target, imgTagSelector) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'PUT',
      path: '/gotapi/mediaStreamRecording/preview',
      params: {
        serviceId,
        target
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      const imgTag = document.querySelector(imgTagSelector);
      imgTag.src = json.uri.replace('localhost', session.host);
      resolve(target);
    })
    .catch((err) => {
      reject(err);
    })
  })
}

function stopPreview(session, serviceId, target, imgTagSelector) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'DELETE',
      path: '/gotapi/mediaStreamRecording/preview',
      params: {
        serviceId,
        target
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      if (imgTagSelector) {
        const imgTag = document.querySelector(imgTagSelector);
        imgTag.src = null;
      }
      resolve();
    })
    .catch((err) => {
      reject(err);
    })
  })
}

function takePhoto(session, serviceId, target) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'POST',
      path: '/gotapi/mediaStreamRecording/takePhoto',
      params: {
        serviceId,
        target
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      json.uri = json.uri.replace('localhost', session.host);
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  })
}

function startRecording(session, serviceId, target) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'POST',
      path: '/gotapi/mediaStreamRecording/record',
      params: {
        serviceId,
        target
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      json.uri = json.uri.replace('localhost', session.host);
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  })
}

function stopRecording(session, serviceId, target) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'PUT',
      path: '/gotapi/mediaStreamRecording/stop',
      params: {
        serviceId,
        target
      }
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      json.uri = json.uri.replace('localhost', session.host);
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  })
}
