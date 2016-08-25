function Queue() {
    this.__a = new Array();
}

Queue.prototype.enqueue = function(o) {
    this.__a.push(o);
}

Queue.prototype.dequeue = function() {
    if (this.__a.length > 0) {
        return this.__a.shift();
    }
    return null;
}

Queue.prototype.get = function(i) {
    if (i < 0 || this.__a.length <= i) {
        return;
    }
    return this.__a[i];
}

Queue.prototype.size = function() {
    return this.__a.length;
}

Queue.prototype.toString = function() {
    return '[' + this.__a.join(',') + ']';
}
