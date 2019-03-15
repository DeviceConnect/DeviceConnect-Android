export default class {

  constructor() {
    this._storage = localStorage;
    console.log('Local Storage: ' + localStorage.length);
  }

  setString(key, value) {
    if (value !== null) {
      this._storage.setItem(key, value);
    } else {
      this._storage.removeItem(key);
    }
  }

  getString(key) {
    return this._storage.getItem(key);
  }

  setObject(key, value) {
    const strValue = JSON.stringify(value);
    this._storage.setItem(key, strValue);
  }

  getObject(key) {
    const value = this.getString(key);
    if (value == null) {
      return null;
    }
    return JSON.parse(value);
  }
}