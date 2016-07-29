
var main = (function(parent, global) {
    function init() {
        util.init(function(json) {
            console.log(json);
        });
    }
    parent.init = init;

    return parent;
})(main || {}, this.self || global);
