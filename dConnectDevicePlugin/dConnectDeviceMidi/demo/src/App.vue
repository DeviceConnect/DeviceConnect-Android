<template>
  <v-app>
    <v-app-bar
      app
      color="primary"
      dark
    >
      <v-icon @click="backPage">mdi-arrow-left</v-icon>
      <v-toolbar-title>{{ title }}</v-toolbar-title>
    </v-app-bar>

    <v-main>
      <v-alert tile type="warning" transition="scale-transition" :value="alert">{{ error.errorMessage }}</v-alert>
      
      <router-view :allServices="services" />
    </v-main>
  </v-app>
</template>

<script>
function loadAccessToken(host) {
  return localStorage.getItem('token-' + host);
}

function storeAccessToken(host, token) {
  localStorage.setItem('token-' + host, token);
}

export default {
  name: 'App',
  data: () => ({
    services: [],
    title: '',
    error: {},
    alert: false
  }),
  watch: {
    '$route': 'onRouteChange'
  },
  methods: {
    backPage: function() {
      this.$router.back();
    },
    onRouteChange: function() {
      this.title = this.$route.meta.title;
      if (this.$route.meta.useDeviceConnect) {
        this.connect();
      }
    },
    connect: function() {
      let query = this.$route.query;
      let host = query.ip;
      if (!host) {
        host = 'localhost';
      }

      this.$dConnect.checkAvailability(host)
      .then(json => {
        console.log('Host ' + host + ' is available.', json);
        this.serviceDiscovery(host);
      })
      .catch(err => {
        console.warn('Host ' + host + ' is not available.', err);
        if (this.$dConnect.isAndroid()) {
          this.$dConnect.startDeviceConnect({
            onstart: function() {
              this.serviceDiscovery(host);
            },
            onerror: function() {
              this.error = {
                errorMessage: 'DeviceConnectシステムの起動に失敗しました。'
              };
              this.alert = true;
            }
          })
          return;
        } else {
          this.error = {
            errorMessage: 'DeviceConnectシステムへの接続に失敗しました。'
          };
          this.alert = true;
        }
      });
    },
    serviceDiscovery: function(host) {
      let scopes = ['serviceDiscovery', 'serviceInformation', 'midi', 'soundModule'];
      if (!this.$dConnect.isConnected(host)) {
        let accessToken = loadAccessToken(host);
        if (accessToken) {
          this.$dConnect.addSession({ host, scopes, accessToken });
        }
      }

      this.$dConnect.connect({ host, scopes })
      .then(result => {
        storeAccessToken(host, result.session.accessToken);

        this.services = result.services;
      })
      .catch(err => {
        console.error("ServiceList: error", err);
        this.error = err;
        this.alert = true;
      });
    }
  }
};
</script>
