import * as SDK from './core.js'
import * as API from './utils.js'
import Storage from './storage.js'

// イベントバス
const EventBus = new Vue();

// SDK 初期化
let _currentSession = null;
const host = '192.168.11.5';
const scopes = [
  'serviceDiscovery',
  'serviceInformation',
  'mediaStreamRecording',
  'mediaPlayer',
  'canvas',
  'file'
];
const sdk = new SDK.DeviceConnectClient({ appName: 'test' });
const storage = new Storage();
let app;

// 撮影画面
Vue.component('app-recorder', {
  template: '#app-recorder',
  mounted () {
    EventBus.$on('on-launched', this.onLaunched);
    EventBus.$on('on-photo', this.onPhoto);
    EventBus.$on('on-start-recording', this.onStartRecording);
    EventBus.$on('on-stop-recording', this.onStopRecording);
    this.launched = !app.launching;
    if (this.launched === true) {
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

// ビューア画面
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
    console.log('Viewer: mounted: file=', this.$route.params.file);

    // ファイル一覧を取得 → タイル表示
    const sdk = this.$root.sdk;
    const host = this.$root.host;
    sdk.offer(host, API.serviceDiscovery, {})
    .then((json) => {
      let hostService = null;
      json.services.forEach((s) => { if (s.name === 'Host') { hostService = s; } });
      if (hostService === null) {
        Promise.reject({ reason:'no-host-service' });
        return;
      }
      console.log('Viewer: Host Service', hostService);
      return sdk.offer(host, API.getFileList, { serviceId:hostService.id, order:'updateDate,desc' });
    })
    .then((json) => {
      console.log('Viewer: File List: ', json);
      const batePath = 'http://' + host + ':4035/gotapi/files?uri=content%3A%2F%2Forg.deviceconnect.android.deviceplugin.host.provider.included%2F';
      const promises = []
      json.files.forEach((file) => {
        if (file.fileType === '0') {
          if (file.mimeType === 'image/jpeg') {
            promises.push(new Promise((resolve, reject) => {
              const image = new Image();
              const uri = batePath + file.fileName;
              image.onload = function() { resolve({ type: 'image', uri, width:image.width, height:image.height }) }
              image.src = uri;
            }))
          } else if (file.mimeType === 'video/mp4' ) {
            promises.push(new Promise((resolve, reject) => { resolve({ type: 'video', uri: batePath + file.fileName }) }));
          }
        }
      })
      Promise.all(promises).then(mediaList => {
        this.mediaList = mediaList;
      });
    })
    .catch((err) => {
      console.error('Failed to get file list for viewer.', err)
    })
  },
  data () {
    return {
      gallery: null,
      mediaList: []
    }
  },
  computed: {
    mediaItems() {
      return this.mediaList.map(m => {
        if (m.type === 'image') {
          return { type:m.type, uri:m.uri };
        } else {
          return { type:m.type, uri:'../img/play.png' };
        }
      });
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
        if (media.type === 'image') {
          return { src:media.uri, w:media.width, h:media.height }
        } else {
          const html = '<video class="video-player" src="' + media.uri + '" controls></video>';
          return { html }
        }
      }); 
      
      var options = {
          index,
          history: false,
          focus: false,
          closeOnScroll: false,
          closeOnVerticalDrag: false,
          showAnimationDuration: 250,
          hideAnimationDuration: 250
      };
      
      var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
      gallery.init();
    }
  }
})

// QRコード画面
Vue.component('app-qr', {
  template: '#app-qr',
  created() {
    this.uri = location.protocol + '//' + location.host + '/org.deviceconnect.android.deviceplugin.host/demo/camera/index.html';
  },
  mounted() {
    console.log('QR Code: uri=' + this.uri);
    const container = document.querySelector('#qrcode-container');
    container.appendChild(this.generateQR(this.uri));
  },
  data() {
    return {
      uri: null,
      message: '「QRコード」は株式会社デンソーウェーブの登録商標です。'
    }
  },
  methods: {
    generateQR(text) {
      const qr = qrcode(11, 'L');
      qr.addData(text);
      qr.make();

      const img = this.createElementFromHTML(qr.createImgTag());
      img.id = 'qrcode';
      img.removeAttribute('width');
      img.removeAttribute('height');
      return img;
    },
    createElementFromHTML(html) {
      const parent = document.createElement('div');
      parent.innerHTML = html;
      const element = parent.firstChild;
      return element;
    }
  }
})

const router = new VueRouter({
  routes: [
    { path: '/', component: { template: '<app-recorder></app-recorder>' } },
    { path: '/viewer/:file?', component: { template: '<app-viewer></app-viewer>' } },
    { path: '/qr', component: { template: '<app-qr></app-qr>' } }
  ]
});

app = new Vue({
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
      sdk: sdk,
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
      API.putRecorderOption(_currentSession, serviceId, settings.id, options)
      .then(() => {
        console.log('Changed Recorder Option: service=' + serviceId + ', target=' + settings.id);
        if (this.activeRecorderId === null) {
          return Promise.resolve();
        }
        return API.stopPreview(_currentSession, serviceId, this.activeRecorderId)
      })
      .then(() => {
        console.log('Stopped preview: service=' + serviceId + ', target=' + this.activeRecorderId);
        return API.startPreview(_currentSession, serviceId, settings.id, '#preview')
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
      API.takePhoto(_currentSession, this.hostService.id, this.activeRecorderId)
      .then((json) => {
        let uri = json.uri;
        console.log('Photo: uri=' + uri);
        if (uri) {
          uri = uri.replace('localhost', host);
          this.storeMedia({ type:'image', uri });
          EventBus.$emit('on-photo', { uri })
        }
      })
      .catch((err) => {
        console.error('Failed to take photo.', err);
      })
    },
    startRecording: function() {
      const target = this.activeRecorderId;
      API.startRecording(_currentSession, this.hostService.id, target)
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
      API.stopRecording(_currentSession, this.hostService.id, target)
      .then((json) => {
        let uri = json.uri;
        console.log('Stopped recording: target=' + target);
        if (uri) {
          uri = uri.replace('localhost', host);
          this.storeMedia({ type:'video', uri });
          EventBus.$emit('on-stop-recording');
        }
      })
      .catch((err) => {
        console.error('Failed to stop recording.', err);
      });
    },
    startPreview() {
      const target = this.activeRecorderId;
      API.startPreview(_currentSession, this.hostService.id, target, '#preview')
      .catch((err) => {
        console.error('Failed to start preview.', err);
      })
    },
    stopPreview() {
      const target = this.activeRecorderId;
      API.stopPreview(_currentSession, this.hostService.id, target)
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
    return API.getRecorderList(result.session, hostService.id)
  })
  .then(result => {
    console.log('Recorders:', result.recorders);

    // 各レコーダーのオプションを取得
    const promises = result.recorders.map(recorder => {
      return API.getRecorderOption(result.session, result.serviceId, recorder)
    })
    return Promise.all(promises)
  })
  .then(results => {
    app.recorders = results;

    // レコーダー情報を Vue に反映.
    let current = null;
    results.forEach(result => {
      if (result.recorder.id === 'camera_0' || result.recorder.id === 'video_0') {
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
    if (err.what === 'ws' && err.code === 3) {
      _currentSession.accessToken = null;
      storage.setObject('session', { host, scopes, accessToken: null });
    }

    console.warn('Could not connected.', err);
    EventBus.$emit('connection-error', { message: err.errorMessage });
  })
}
