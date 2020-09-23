import Vue from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify';
import dConnect from './libs/core.js'

Vue.config.productionTip = false
Vue.prototype.$dConnect = dConnect({
  appName: 'MIDI',
});

new Vue({
  router,
  vuetify,
  render: h => h(App)
}).$mount('#app');