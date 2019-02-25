import * as SDK from './core.js'

const host = '192.168.11.5';
const scopes = [
  'serviceDiscovery',
  'serviceInformation',
  'mediaStreamRecording',
  'mediaPlayer',
  'canvas'
];
const sdk = new SDK.DeviceConnectClient({ appName: 'test' });
let _currentSession = null;

const app = new Vue({
  el: '#app',
  data () {
    return {
      launching: true,
      dialog: false,

      // Host サービスのレコーダーの配列
      recorders: [
      ],

      // プレビュー中のレコーダー
      activeRecorderId: null,

      // レコーダー設定画面で選択されたレコーダー
      selectedRecorder: {
        host: null,
        service: null,
        id: 'mock',
        name: 'Mock Recorder',
        previewSize: null,
        imageSize: null,
        options: {
          previewSizes: [],
          imageSizes: [],
          frameRate: 30
        }
      }
    }
  },
  computed: {
    recorderNames: function() {
      console.log('recorderNames: recorders', this.recorders);
      return this.recorders.map((r) => {
        return { text:r.name, value:r.id };
      });
    }
  },
  methods: {
    changeRecorderOption: function() {
      const recorder = this.selectedRecorder;
      if (!recorder) {
        return;
      }
      const imageSize = recorder.imageSize.split(' x ');
      const previewSize = recorder.previewSize.split(' x ');
      const options = {
        imageWidth: imageSize[0],
        imageHeight: imageSize[1],
        previewWidth: previewSize[0],
        previewHeight: previewSize[1]
      };
      putRecorderOption(_currentSession, recorder.service, recorder.id, options)
      .then(() => {
        console.log('Changed Recorder Option: service=' + recorder.service + ', target=' + recorder.id);
        if (this.activeRecorderId === null) {
          return Promise.resolve();
        }
        return stopPreview(_currentSession, recorder.service, this.activeRecorderId)
      })
      .then(() => {
        console.log('Changed Recorder Option: service=' + recorder.service + ', target=' + recorder.id);
        return startPreview(_currentSession, recorder.service, recorder.id, '#preview')
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

  // レコーダー情報を Vue に反映.
  let current = null;
  results.forEach(result => {
    const r = result.recorder;
    const o = result.options;
    const json = {
      id:r.id,
      name:r.name,
      options:o,
      imageWidth: r.imageWidth,
      imageHeight: r.imageHeight,
      previewWidth: r.previewWidth,
      previewHeight: r.previewHeight,
      frameRate:r.previewMaxFrameRate
    };
    app.recorders.push(json);
    if (r.id === 'camera_0') {
      current = json;
    }
  })
  const result = results[0];
  const selected = app.selectedRecorder;
  if (current !== null) {
    selected.host = result.session.host;
    selected.service = result.serviceId;
    selected.id = current.id;
    selected.previewSize = current.previewWidth + ' x ' + current.previewHeight;
    selected.imageSize = current.imageWidth + ' x ' + current.imageHeight;

    const op = selected.options;
    op.frameRate = current.frameRate;
    op.previewSizes = current.options.previewSizes.map(s => {
      const size = (s.width + ' x ' + s.height);
      return {text:size, value:size}
    });
    op.imageSizes = current.options.imageSizes.map(s => {
      const size = (s.width + ' x ' + s.height);
      return {text:size, value:size}
    });
  }
  console.log('selected recorder', selected);

  console.log('Recorder Option:', result.options);
  return startPreview(result.session, result.serviceId, result.recorder.id, '#preview');
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
