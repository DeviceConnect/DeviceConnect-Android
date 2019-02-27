export default class {

  constructor() {
    this._storage = localStorage;
    console.log('Local Storage: ' + localStorage.length);
  }

  setObject(key, value) {
    this._storage.setItem(key, JSON.stringify(value));
  }

  getObject(key) {
    const value = this._storage.getItem(key);
    console.log('Local Storage: key=' + key + ', value=' + value);
    if (value == null) {
      return null;
    }
    return JSON.parse(value);
  }
}