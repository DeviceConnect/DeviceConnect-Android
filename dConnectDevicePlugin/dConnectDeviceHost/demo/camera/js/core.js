class Session {

  constructor(host, scopes, ssl) {
    this._host = host;
    this._scopes = scopes;
    this._clientId = null;
    this._token = null;
    this._webSocket = null;
    this._ssl = ssl || false;
    this._port = 4035;
    this._connected = false;
    this._pendingOffers = [];
  }

  get connected() {
    return this._connected;
  }

  get host() {
    return this._host;
  }

  get services() {
    return this._services;
  }

  get clientId() {
    return this._clientId;
  }
  set clientId(id) {
    this._clientId = id;
  }

  get accessToken() {
    return this._token;
  }
  set accessToken(token) {
    this._token = token;
  }

  connect() {
    return new Promise((resolve, reject) => {
      if (this._connected) {
        resolve({ result: 0 });
        return;
      }

      const scheme = (this._ssl === false) ? 'ws' : 'wss';
      const host = this._host;
      const url = scheme + '://' + host + ':4035/gotapi/websocket';
      const token = this._token;
      const session = this;
      console.log('Connecting Websocket: url=' + url);

      try {
        const socket = new WebSocket(url);
        socket.onopen = function(event) {
          console.log(host + ' - open');
          const json = '{accessToken:"' + token + '"}';
          socket.send(json);
          console.log(host + ' - send JSON: ' + json);
        };
        socket.onmessage = function(event) {
          const message = event.data;
          console.log(host + ' - message: ' + message);

          const json = JSON.parse(message);
          if (json.result !== undefined) {
            if (json.result === 0) {
              session._connected = true;
              console.log('onmessage: this=', this);
              resolve(json);
            } else {
              reject({ what:'ws', code:json.errorCode });
            }
          }
        };
        socket.onclose = function(event) {
          console.log(host + ' - close');
          session._webSocket = null;
          session._connected = false;
        };
        this._webSocket = socket;
      } catch (e) {
        reject();
      }
    });
  }

  disconnect() {
    if (this._webSocket !== null) {
      this._webSocket.close();
      this._webSocket = null;
    }
    this._connected = false;
  }

  getRestScheme() {
    return this._ssl ? 'https' : 'http';
  }

  uri(path) {
    const scheme = this.getRestScheme();
    return scheme + '://' + this._host + ':' + this._port + path;
  }

  offer(func, params) {
    if (this._connected === true) {
      return func(this, params);
    } else {
      return new Promise((resolve, reject) => {
        this._pendingOffers.push({func, params, resolve, reject});
      });
    }
  }

  processPendingOffers() {
    const session = this;
    this._pendingOffers.forEach(offer => {
      func(session, offer.params).then(r => { offer.resolve(r) }).cache(e => { offer.reject(e) })
    })
  }

  request(args) {
    const method = args.method.toUpperCase();
    const path = args.path;
    
    const params = args.params || {};
    params.accessToken = this.accessToken;

    const headers = {};
    headers['Origin'] = location.origin;

    const query = new URLSearchParams();
    for (let key in params) {
      query.set(key, params[key]);
    }

    let uri = this.uri(path);
    let body = null;
    if (method === 'POST' || method === 'PUT') {
      body = query.toString();
      headers['Content-Type'] = 'application/x-www-form-urlencoded; charset=utf-8';
    } else if (method === 'GET' || method === 'DELETE') {
      uri += '?' + query.toString();
    }

    return fetch(uri, {
      method,
      headers,
      body,
      mode: 'cors',
      cache: 'no-cache'
    }).then(res => res.json());
  }

  fetch() {
    const scheme = this.getRestScheme();
    const host = this._host;
    const params = new URLSearchParams();
    params.set('accessToken', this.accessToken);
    const query = params.toString();

    return fetch(scheme + "://" + host + ":" + this._port + "/gotapi/serviceDiscovery?" + query, {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache',
      headers: {
        "Origin": location.origin
      }
    }).then(res => {
      return res.json();
    });
  }
}

/**
 * Device Connect Client SDK for Javascript.
 */
export class DeviceConnectClient {

  constructor(op) {
    this.appName = op.appName || 'Application';
    this._sessions = {};
    this._pendingOffers = {};
  }

  get sessions() {
    return this._sessions;
  }

  addSession(args) {
    const host = args.host;
    const session = new Session(args.host, args.scopes);
    session.accessToken = args.accessToken;
    this._sessions[host] = session;
  }

  deleteSession(host) {
    if (this._sessions[host]) {
      this._sessions[host].disconnect();
    }
    this._sessions[host] = undefined;
  }

  isAndroid() {
    var userAgent = window.navigator.userAgent.toLowerCase();
    return (userAgent.indexOf('android') != -1);
  }

  startDeviceConnect(option) {
    location.href = 'gotapi://start/server?package=org.deviceconnect.android.manager';
    this.waitAvailable(option);
  }

  waitAvailable(option) {
    option.oncheck = option.oncheck || function() {};
    option.count = option.count || 10;

    option.oncheck(option.count);
    option.count--;
    this.checkAvailability(option.host)
    .then((json) => {
      option.onstart(json);
    })
    .catch((err) => {
      if (option.count <= 0) {
        option.onerror();
      } else {
        setTimeout(function(op) {
          this.waitAvailable(op);
        }.bind(this, option), option.interval || 1000);
      }
    })
  }

  /**
   * DeviceConnect システムに接続する.
   *
   * @param {object} option - 
   * @return {Promise} DeviceConnect システムへの接続処理の Promise
   */
  connect(option) {
    const host = option.host || 'localhost';
    const scopes = option.scopes || ['serviceDiscovery', 'serviceInformation'];
    const ssl = option.ssl || false;

    console.log('connect(): host=' + host)

    return new Promise((resolve, reject) => {
      let session = this._sessions[host];
      if (!session) {
        session = new Session(host, scopes, ssl);
        this._sessions[host] = session;
      }

      // Authorization
      this.authorize(session, host, scopes)

      // Establish WebSokcet
      .then(json => {
        console.log('Response:', json);

        const result = json.result;
        const accessToken = json.accessToken;
        if (result === 0 && accessToken) {
          console.log('Got Access Token: accessToken=' + accessToken);
          const session = this._sessions[host];
          session.accessToken = accessToken;

          console.log('Connecting to host=' + host);
          return session.connect();
        } else {
          reject({ what: 'connect', reason: 'no-access-token', errorMessage: '本アプリの使用が認可されませんでした。' });
        }
      })
      .catch(err => {
        if (err.code === 4) {
          reject({ what: 'connect', reason: 'ws-duplicated', errorMessage: '別ブラウザで既にWebSocketが接続されています。' });
        } else if (err.code === 3) {
          reject({ what: 'connect', reason: 'ws-invalid-access-token', errorMessage: 'アクセストークンが不正のためにWebSocketを接続できませんでした。' });
        } else {
          reject({ what: 'connect', reason: 'ws-unknown--error', errorMessage: '不明なエラーによりWebSocketを接続できませんでした。' });
        }
      })

      // Service Discovery
      .then(json => {
        this.processPendingOffers(session);

        return this.fetchServices(host);
      })

      .then(json => {
        console.log('Fetched services:', json.services);
        const result = json.result;
        if (result === 0) {
          resolve({session, services:json.services});
        } else {
          reject({ what: 'connect', reason: 'no-service', errorMessage: 'サービス検索に失敗しました。' });
        }
      })
    });
  }

  checkAvailability(host) {
    return fetch("http://" + host + ":4035/gotapi/availability", {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache',
      headers: {
        "Origin": location.origin
      }
    }).then(res => {
      return res.json();
    });
  }

  authorize(session, host, scopes) {
    if (session.accessToken !== null) {
      console.log('AccessToken: ' + session.accessToken);
      return Promise.resolve({ result:0, accessToken:session.accessToken });
    }
    return this.createClient(host)
    .then((json) => {
      console.log('clientId: ' + json.clientId);
      const result = json.result;
      const clientId = json.clientId;
      if (result === 0) {
        console.log('Created client: clientId=' + clientId);

        this._sessions[host].clientId = clientId;
        return this.requestAccessToken(host, scopes);
      } else {
        console.warn('authorize: createClient: erroCode=' + json.errorCode);
        if (json.errorCode === 2) {
          // LocalOAuth が OFF の場合は代用のアクセストークンを返す.
          // WebSocket 接続確立時に代用を送信する必要がある.
          return Promise.resolve({ result:0, accessToken:'placeHolder' });
        }
        return Promise.reject({ what: 'connect', reason: 'no-client', errorMessage: 'クライアントIDを取得できませんでした。' });
      }
    })
  }

  createClient(host) {
    return fetch("http://" + host + ":4035/gotapi/authorization/grant", {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache',
      headers: {
        "Origin": location.origin
      }
    }).then(res => {
      return res.json();
    });
  }

  requestAccessToken(host, scopes) {
    const session = this._sessions[host];
    const params = new URLSearchParams();
    params.set('clientId', session.clientId);
    params.set('applicationName', this.appName);
    params.set('scope', scopes.join(','));
    const query = params.toString();
    console.log('requestAccessToken: query=' + query)

    return fetch("http://" + host + ":4035/gotapi/authorization/accessToken?" + query, {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache',
      headers: {
        "Origin": location.origin
      }
    }).then(res => {
      return res.json();
    });
  }

  fetchServices(host) {
    const session = this._sessions[host];
    return session.fetch();
  }

  offer(host, api, params) {
    const session = this._sessions[host];
    console.log('offer: session=', session);
    if (session) {
      console.log('offer: connected=' + session.connected);
      if (session.connected) {
        return session.offer(api, params);
      }
    }

    let offers = this._pendingOffers[host];
    if (!offers) {
      offers = [];
      this._pendingOffers[host] = offers;
    }
    return new Promise((resolve, reject) => {
      offers.push({func:api, params, resolve, reject});
    });
  }

  processPendingOffers(session) {
    let offers = this._pendingOffers[session.host];
    if (!offers) {
      return;
    }
    offers.forEach(offer => {
      offer.func(session, offer.params).then(r => { offer.resolve(r) }).catch(e => { offer.reject(e) })
    })
    this._pendingOffers[session.host] = null;
  }
}