import * as SDK from './core.js'
import * as API from './utils.js'
import Storage from './storage.js'

// イベントバス
const EventBus = new Vue();

// SDK 初期化
let _currentSession = null;
const host = ipAddress() || location.hostname;
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
    if (app) {
      this.launched = !app.launching;
      if (this.launched === true) {
        this.startPreview();
      }
    }
  },
  beforeDestroy () {
    this.stopPreview();
  },
  data () {
    return {
      launched: false,
      latestMediaUri: null,
      latestMediaThumbnailUri: null,
      isRecording: false,
      isStartingRecording: false,
      isStoppingRecording: false,
      isTakingPhoto: false
    }
  },
  computed: {
    canTakePhoto: function() {
      return this.launched;
    },
    canStopRecording: function() {
      return this.launched && !this.isStartingRecording && !this.isStoppingRecording && !this.isTakingPhoto;
    },
    canStartRecording: function() {
      return this.launched && !this.isStartingRecording && !this.isStoppingRecording && !this.isTakingPhoto;
    }
  },
  methods: {
    requestDrawer() {
      EventBus.$emit('show-drawer');
    },
    requestRecorderSettingDialog() {
      EventBus.$emit('show-recorder-setting-dialog');
    },
    requestTakePhoto: function() {
      if (!this.canTakePhoto) {
        return;
      }
      this.isTakingPhoto = true;
      EventBus.$emit('take-photo');
    },
    startRecording: function() {
      if (!this.canStartRecording) {
        return;
      }
      this.isStartingRecording = true;
      EventBus.$emit('start-recording');
    },
    stopRecording: function() {
      if (!this.canStopRecording) {
        return;
      }
      this.isStoppingRecording = true;
      EventBus.$emit('stop-recording');
    },
    startPreview: function() {
      EventBus.$emit('start-preview');
    },
    stopPreview: function() {
      EventBus.$emit('stop-preview');
    },
    showMedia: function() {
      const encodedUri = encodeURIComponent(this.latestMediaUri);
      EventBus.$emit('show-media', { uri:encodedUri });
    },
    onLaunched: function() {
      this.launched = true;
      this.startPreview();
    },
    onPhoto: function(event) {
      console.log('onPhoto: uri=' + event.uri);
      this.latestMediaUri = event.uri;
      this.latestMediaThumbnailUri = event.uri;
      this.isTakingPhoto = false;
    },
    onStartRecording: function() {
      console.log('onStartRecording:');
      this.isRecording = true;
      this.isStartingRecording = false;
    },
    onStopRecording: function(event) {
      console.log('onStopRecording: uri=' + event.uri + ', thumbnailUri= ' + event.thumbnailUri);
      this.latestMediaUri = event.uri;
      this.latestMediaThumbnailUri = event.thumbnailUri;
      this.isRecording = false;
      this.isStoppingRecording = false;
    }
  }
})

// ビューア画面
Vue.component('app-viewer', {
  template: '#app-viewer',
  mounted () {
    this.init();
  },
  data() {
    return {
      gallery: null,
      castDialog: false, //キャスト設定ダイアログの表示フラグ
      fetching: false, //キャスト先の候補を取得中
      mediaList: [], //メディア一覧
      history: [], //表示履歴: [0]=現在, [1]=前回
      castableItems: [],
      currentCastTargetId: null, //キャスト先のサービスID. 空文字の場合はキャストしない.
      hasCastTarget: false
    }
  },
  watch: {
    currentCastTargetId: function(newValue) {
      this.hasCastTarget = newValue !== '';
    }
  },
  computed: {
    mediaItems() {
      return this.mediaList.map(m => {
        if (m.type === 'image') {
          return { type:m.type, uri:m.uri };
        } else {
          return { type:m.type, uri:m.thumbnailUri };
        }
      });
    }
  },
  methods: {
    init() {
      const fileUri = this.$route.params.file;
      console.log('Viewer: init: file=' + fileUri);
      
      let castTargetId = storage.getString('castTargetId');
      if (castTargetId !== null) {
        console.log('Viewer: init: castTargetId is found: ', castTargetId);
      } else {
        console.log('Viewer: init: castTargetId is not found: ', castTargetId);
        castTargetId = '';
      }
      this.currentCastTargetId = castTargetId;
      this.hasCastTarget = castTargetId !== '';
      console.log('Viewer: init: currentCastTargetId: ', this.currentCastTargetId);
  
      const mediaList = storage.getObject('mediaList');
      console.log('Viewer: init: get mediaList', mediaList);
  
      if (mediaList !== null) {
        const promises = [];
        mediaList.forEach((media, index) => {
          if (media.type === 'image') {
            promises.push(new Promise((resolve, reject) => {
              const image = new Image();
              image.onload = function() { resolve({ type:'image', uri:media.uri, width:image.width, height:image.height }) }
              image.onerror = function() { console.error('Viewer: onerror: uri=' + media.uri) }
              image.src = media.uri;
            }))
          } else {
            promises.push(new Promise((resolve, reject) => { resolve(media) }))
          }
        });
        Promise.all(promises).then(mediaList => {
  
          // 指定されたメディアのインデックスを特定.
          let mediaIndex = -1;
          mediaList.forEach((media, index) => {
            if (media.uri === fileUri) {
              mediaIndex = index;
            }
          });
  
          this.mediaList = mediaList;
          if (mediaIndex >= 0) {
            this.showImage(mediaIndex);
          }
        });
      }
    },
    showImage (index) {
      this.openPhotoSwipe(index);
    },
    openPhotoSwipe (index) {
      const pswpElement = document.querySelectorAll('.pswp')[0];
      console.log('PhotoSwipe: ' + pswpElement);

      const items = this.mediaList.map(media => {
        if (media.type === 'image') {
          return { type:'image', src:media.uri, w:media.width, h:media.height }
        } else {
          const html = '<video class="video-player" src="' + media.uri + '" controls></video>';
          return { type:'video', html, mediaId:media.id }
        }
      }); 
      
      const options = {
          index,
          history: false,
          focus: false,
          closeOnScroll: false,
          closeOnVerticalDrag: false,
          showAnimationDuration: 250,
          hideAnimationDuration: 250
      };
      
      const gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
      const viewer = this;
      gallery.listen('beforeChange', function() {
        viewer.history[1] = viewer.history[0];
        viewer.history[0] = gallery.currItem;
        viewer.onMediaChange(gallery)
      });
      gallery.listen('close', function() { viewer.onGallaryClose(gallery) });
      gallery.init();
    },
    onMediaChange(gallery) {
      const serviceId = this.currentCastTargetId;
      if (serviceId !== '') {
        const item = gallery.currItem;
        console.log('Viewer: gallery: beforeChange: index=' + gallery.getCurrentIndex(), item);

        // 前回の画面を閉じてから、メディア再生.
        this.closeMedia(this.history[1], serviceId)
        .then((json) => {
          return this.openMedia(item, serviceId);
        });
      }
    },
    onGallaryClose(gallery) {
      const serviceId = this.currentCastTargetId;
      if (serviceId !== '') {
        this.closeMedia(gallery.currItem, serviceId);
      }
    },
    openMedia(item, serviceId) {
      if (item.type === 'image') {
        return this.drawImage({ serviceId, uri:item.src, mimeType:'image/jpeg', mode:'scales' });
      } else if (item.type === 'video') {
        return this.startVideo({ serviceId, mediaId:item.mediaId });
      }
    },
    closeMedia(item, serviceId) {
      if (!item) {
        return Promise.resolve({result:0});
      }
      if (item.type === 'image') {
        return this.deleteImage({ serviceId }).catch((err) => {
          if (err.errorCode === 16) {
            return Promise.resolve({ result: 0 });
          } else {
            return Promise.reject(err);
          }
        });
      } else if (item.type === 'video') {
        return this.stopVideo({ serviceId }).catch((err) => {
          if (err.errorCode === 16) {
            return Promise.resolve({ result: 0 });
          } else {
            return Promise.reject(err);
          }
        });
      }
    },
    showRecorder() {
      EventBus.$emit('show-recorder');
    },
    requestDrawer() {
      EventBus.$emit('show-drawer');
    },
    requestCastDialog() {
      this.castDialog = true;
      if (this.fetching) {
        return;
      }
      this.fetching = true;

      this.offer(API.serviceDiscovery)
      .then((json) => {
        console.log('Viewer: ', json);
        let castableList = [];
        const expectedScopes = ['canvas', 'mediaPlayer'];
        if (json.services) {
          castableList = json.services.filter(service => {
            return service.scopes.some(scope => {
              return expectedScopes.some(expected => { return expected === scope; })
            })
          })
        }
        castableList.splice(0, 0, { id: '', name: 'キャストしない' });
        console.log('Viwer: castable services', castableList);
        this.castableItems = castableList.map(castable => { return { id:castable.id, name:castable.name } })
        this.fetching = false;
      })
      .catch((err) => {
        console.error('Viewer: Failed to fetch services.', err);
        this.fetching = false;
      })
    },
    closeCastDialog(change) {
      this.castDialog = false;
      const id = this.currentCastTargetId;
      console.log('Viewer: closeCastDialog: currentCastTargetId', id);
      storage.setString('castTargetId', this.currentCastTargetId);
    },
    drawImage(params) {
      return this.offer(API.drawImage, params);
    },
    deleteImage(params) {
      return this.offer(API.deleteImage, params);
    },
    startVideo(params) {
      this.offer(API.setMedia, params)
      .then((json) => {
        console.log('Viewer: Set video');
        return this.offer(API.playMedia, { serviceId:params.serviceId })
      })
    },
    stopVideo(params) {
      return this.offer(API.stopMedia, params);
    },
    offer(api, params) {
      params = params || {};
      return this.$root.sdk.offer(this.$root.host, api, params)
    }
  }
})

// QRコード画面
Vue.component('app-qr', {
  template: '#app-qr',
  created() {
    this.uri = location.protocol + '//' + location.host + location.pathname + location.search;
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
    },
    requestDrawer() {
      EventBus.$emit('show-drawer');
    }
  }
})

const routes = [
  { name: 'recorder', path: '/', component: { template: '<app-recorder></app-recorder>' } },
  { name: 'viewer', path: '/viewer/:file?', component: { template: '<app-viewer></app-viewer>' } },
  { name: 'qr', path: '/qr', component: { template: '<app-qr></app-qr>' } }
];
const router = new VueRouter({ routes });

// アプリケーション定義
app = new Vue({
  el: '#app',
  router,
  created() {
    this.checkMode(this.$router.currentRoute);
    this.$router.afterEach((to, from) => {
      this.checkMode(to);
    })
  },
  mounted() {
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
    EventBus.$on('show-drawer', this.openDrawer)
    EventBus.$on('show-recorder', this.openRecorder)
    EventBus.$on('show-recorder-setting-dialog', this.openRecorderSettingDialog)
    EventBus.$on('show-media', this.openMedia)

    // メディア一覧の復元
    const mediaList = storage.getObject('mediaList');
    if (mediaList !== null) {
      this.mediaList = mediaList;
    }

    // Device Connect システムと接続
    connect();
  },
  data() {
    return {
      mode: null, // 'recorder', 'viewer', 'qr'
      sdk: sdk,
      host: host,
      hostService: null,
      launching: true,
      dialog: false,
      showError: false,
      showErrorTime: 60000,
      showDrawer: false,
      pages: [
        { mode:'recorder', path: '/', title: '撮影', icon: 'camera_alt' },
        { mode:'viewer', path: '/viewer', title: 'ビューア', icon: 'collections' },
        { mode:'qr', path: '/qr', title: 'QRコード', icon: 'crop_free' }
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
    openDrawer() { this.showDrawer = true; },
    openRecorderSettingDialog() { this.dialog = true; },
    checkMode(route) {
      this.mode = this.getMode(route.path);
      console.log('App: Current Mode: ' + this.mode + ' for ' + route.path);
    },
    getMode(path) {
      for (let k in routes) { if (path.startsWith(routes[k].path)) { return routes[k].mode; } }
      return routes[0].mode;
    },
    isMode(mode) {
      return this.mode === mode;
    },
    showPage: function(path) {
      const currentRoute = router.currentRoute;
      const next = router.resolve(path);
      console.log('showPath: current route: ', currentRoute);
      console.log('showPath: next route: ', next.route);
      if (currentRoute.name === next.route.name) {
        this.showDrawer = false;
      } else {
        router.push({ path: path });
      }
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
      console.log('Changing Recorder Option...', options);
      API.putRecorderOptions(_currentSession, serviceId, settings.id, options)
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
        const path = json.path;
        console.log('Photo: path=' + path + ', uri=' + uri);
        if (uri && path) {
          uri = uri.replace('localhost', host);
          this.storeMedia({ type:'image', uri, path });
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
      const media = {};
      API.stopRecording(_currentSession, this.hostService.id, target)
      .then((json) => {
        let uri = json.uri;
        const path = json.path;
        console.log('Stopped recording: target=' + target);
        if (uri && path) {
          uri = uri.replace('localhost', host);
          media.type = 'video';
          media.uri = uri;
          media.path = path;
          return API.getMediaList(_currentSession, { serviceId: this.hostService.id });
        } else {
          reject(); // TODO: エラー詳細を通知
        }
      })
      .then((json) => {
        // path に対応する mediaId を特定.
        // (MediaPlayer Play APIのパラメータとして必要)
        let mediaId = null;
        let thumbnailUri = null;
        json.media.some((m) => {
          if (media.path.includes(m.title)) {
            mediaId = m.mediaId;
            if (m.imageUri) {
              thumbnailUri = m.imageUri.replace('localhost', host);
            }
            return true;
          }
        });
        console.log('Recorder: Recorded Video: mediaId=' + mediaId);
        if (mediaId !== null) {
          media.id = mediaId;
          media.thumbnailUri = thumbnailUri;
          this.storeMedia(media);
          EventBus.$emit('on-stop-recording', { uri:media.uri, thumbnailUri });
        } else {
          reject(); // TODO: エラー詳細を通知
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
    openRecorder(args) {
      router.push({ path: '/' });
    },
    openMedia(args) {
      router.push({ path: '/viewer/' + args.uri });
    },
    storeMedia(media) {
      console.log('storeMedia: before: ', this.mediaList);
      this.mediaList.push(media);
      console.log('storeMedia: after: ', this.mediaList);
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
    const promises = result.recorders
    .map(recorder => {
      return API.getRecorderOptions(result.session, result.serviceId, recorder)
    })
    return Promise.all(promises)
  })
  .then(results => {
    app.recorders = results.filter(result => {
      const types = result.options.mimeType;
      return (((types.includes('image/jpg')
        || types.includes('image/jpeg'))
        || types.includes('video/mp4'))
        && types.includes('video/x-mjpeg'));
    });

    // レコーダー情報を Vue に反映.
    let current = null;
    results.forEach(result => { if (result.recorder.id === 'camera_0') { current = result; } });
    if (current == null) {
      current = results[0];
    }
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
    if (err.reason === 'ws-invalid-access-token') {
      sdk.deleteSession(host);
      storage.setObject('session', { host, scopes, accessToken: null });
    }

    console.warn('Could not connected.', err);
    EventBus.$emit('connection-error', { message: err.errorMessage });
  })
}

function ipAddress() {
  return (new URL(document.location)).searchParams.get('ip');
}