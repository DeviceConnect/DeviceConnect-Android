class Session {

  constructor(host, scopes, ssl) {
    this._host = host;
    this._scopes = scopes;
    this._clientId = 'dummy';
    this._token = 'dummy';
    this._webSocket = null;
    this._ssl = ssl || false;
    this._port = 4035;
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
      const scheme = (this._ssl === false) ? 'ws' : 'wss';
      const host = this._host;
      const url = scheme + '://' + host + ':4035/gotapi/websocket';
      const token = this._token;
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
              resolve();
            } else {
              reject();
            }
          }
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
  }

  getRestScheme() {
    return this._ssl ? 'https' : 'http';
  }

  uri(path) {
    const scheme = this.getRestScheme();
    return scheme + '://' + this._host + ':' + this._port + path;
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
  }

  get sessions() {
    return this._sessions;
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
      const session = new Session(host, scopes, ssl);
      this._sessions[host] = session;

      // Check Availability
      this.checkAvailability(host)

      // Authorization
      .then(json => {
        console.log('Device Connect system is available: host=' + host);
        return this.createClient(host);
      })
      .then(json => {
        const result = json.result;
        const clientId = json.clientId;
        if (result === 0 && clientId) {
          console.log('Created client: clientId=' + clientId);

          this._sessions[host].clientId = clientId;
          return this.requestAccessToken(host, scopes);
        } else {
          reject({ what: 'connect', reason: 'no-client' });
        }
      })

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
          reject({ what: 'connect', reason: 'no-access-token' });
        }
      })

      // Service Discovery
      .then(json => {
        return this.fetchServices(host);
      })

      .then(json => {
        console.log('Fetched services:', json.services);
        const result = json.result;
        if (result === 0) {
          resolve({session, services:json.services});
        } else {
          reject({ what: 'connect', reason: 'no-service', message: json.errorMessage });
        }
      })
      .catch(err => {
        console.error('Error', err);
        reject({ what: 'connect', reason: 'disconnected' });
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

  connectWebSocket(host) {
    const session = this._sessions[host];

  }

  fetchServices(host) {
    const session = this._sessions[host];
    return session.fetch();
  }
}