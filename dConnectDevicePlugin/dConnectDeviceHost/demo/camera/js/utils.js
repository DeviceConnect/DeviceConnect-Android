export function serviceDiscovery(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'GET',
      path: '/gotapi/serviceDiscovery',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function drawImage(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'POST',
      path: '/gotapi/canvas/drawImage',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function deleteImage(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'DELETE',
      path: '/gotapi/canvas/drawImage',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function getMediaList(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'GET',
      path: '/gotapi/mediaPlayer/mediaList',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function setMedia(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'PUT',
      path: '/gotapi/mediaPlayer/media',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function playMedia(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'PUT',
      path: '/gotapi/mediaPlayer/play',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function stopMedia(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'PUT',
      path: '/gotapi/mediaPlayer/stop',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function getFileList(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'GET',
      path: '/gotapi/file/list',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function getRecorderList(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'GET',
      path: '/gotapi/mediaStreamRecording/mediaRecorder',
      params
    })
    .then((json) => {
      const result = json.result;
      const recorders = json.recorders;
      if (result !== 0 || recorders === undefined) {
        reject(json);
        return;
      }
      resolve({session, serviceId:params.serviceId, recorders});
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export function getRecorderOptions(session, serviceId, recorder) {
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

export function putRecorderOptions(session, serviceId, target, options) {
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
        previewMaxFrameRate: options.previewMaxFrameRate,
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

export function startPreview(session, serviceId, target, imgTagSelector) {
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

export function stopPreview(session, serviceId, target, imgTagSelector) {
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

export function takePhoto(session, serviceId, target) {
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

export function startRecording(session, serviceId, target) {
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

export function stopRecording(session, serviceId, target) {
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