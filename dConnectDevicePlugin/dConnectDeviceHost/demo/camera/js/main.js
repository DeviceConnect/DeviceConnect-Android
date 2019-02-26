import * as SDK from './core.js'

// SDK 初期化
let _currentSession = null;
const host = '192.168.11.5';
const scopes = [
  'serviceDiscovery',
  'serviceInformation',
  'mediaStreamRecording',
  'mediaPlayer',
  'canvas'
];
const sdk = new SDK.DeviceConnectClient({ appName: 'test' });

// ルーティング設定
Vue.component('app-recorder', {
  template: '#app-recorder'
})
Vue.component('app-qr', {
  template: '#app-qr'
})
const router = new VueRouter({
  routes: [
    { path: '/', component: { template: '<app-recorder></app-recorder>' } },
    //{ path: '/viewer', component: { template: '<app-viewer></app-viewer>' } },
    { path: '/qr', component: { template: '<app-qr></app-qr>' } }
  ]
});

const app = new Vue({
  el: '#app',
  router,
  data () {
    return {
      host: host,
      hostService: null,
      launching: true,
      dialog: false,
      showDrawer: false,
      pages: [
        { path: '/', title: '撮影', icon: 'camera_alt' },
        //{ path: '/viewer', title: 'ビューア', icon: 'collections' },
        { path: '/qr', title: 'QRコード', icon: 'crop_free' }
      ],

      // Host サービスのレコーダーの配列
      recorders: [
      ],

      // プレビュー中のレコーダー
      activeRecorderId: null,

      // 現在のレコーダー設定
      recorderSettings: {
        enabled: false,
        id: null,
        previewSize: null,
        imageSize: null,
        frameRate: null
      }
    }
  },
  computed: {
    recorderNames: function() {
      console.log('recorderNames: recorders', this.recorders);
      return this.recorders.map((r) => {
        return { text:r.recorder.name, value:r.recorder.id };
      });
    }
  },
  methods: {
    showPage: function(path) {
      //updateButton(path);
      router.push({ path: path });
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
        console.log('Changed Recorder Option: service=' + serviceId + ', target=' + settings.id);
        return startPreview(_currentSession, serviceId, settings.id, '#preview')
      })
      .catch((err) => {
        console.error('Failed to restart preview.', err);
      });
    }
  }
});

sdk.connect({ host, scopes })
.then(result => {
  console.log('Connected', result.services);
  _currentSession = result.session;
  const services = result.services;
  app.launching = false;

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
  app.recorderSettings.id = current.recorder.id;
  app.recorderSettings.previewSize = current.recorder.previewWidth + ' x ' + current.recorder.previewHeight;
  app.recorderSettings.imageSize = current.recorder.imageWidth + ' x ' + current.recorder.imageHeight;
  app.recorderSettings.frameRate = current.recorder.previewMaxFrameRate;

  console.log('Recorder Option:', current.options);
  return startPreview(current.session, current.serviceId, current.recorder.id, '#preview');
})
.then((target) => {
  console.log('Active Recorder: target=' + target);
  app.activeRecorderId = target;
})
.catch(e => {
  console.warn('Could not connected.', e);
})

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
