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
      
      <!-- メイン -->
      <router-view :allServices="services" />

      <!-- エラー表示 -->
      <v-snackbar top color="error" timeout="-1" v-model="alert" :multi-line="true">
        {{ error.errorMessage }}
        <template v-slot:action="{ attrs }">
          <v-btn
            text
            v-bind="attrs"
            @click="alert = false"
            >閉じる</v-btn>
        </template>
      </v-snackbar>

      <!-- 起動プロンプト -->
      <v-snackbar top color="info" timeout="-1" v-model="launch" :multi-line="true">
        DeviceConnect システムが見つかりませんでした。
        <template v-slot:action="{ attrs }">
          <v-btn
            text
            v-bind="attrs"
            @click="onLaunchConfirmed"
            >起動する</v-btn>
        </template>
      </v-snackbar>
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
    alert: false,
    launch: false
  }),

  watch: {
    '$route': 'onRouteChange'
  },

  computed: {
    hostName: {
      get: function() {
        let host = this.$route.query.ip;
        if (!host) {
          host = 'localhost';
        }
        return host;
      }
    }
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
    onLaunchConfirmed: function() {
      this.launch = false;
      let that = this;
      let host = this.hostName;
      console.log(`onLaunchConfirmed: ${host}`);
      this.$dConnect.startDeviceConnect({
        host,
        onstart: function() {
          that.serviceDiscovery(host);
        },
        onerror: function() {
          that.error = {
            errorMessage: 'DeviceConnectシステムの起動に失敗しました。'
          };
          that.alert = true;
        }
      })
    },
    connect: function() {
      let host = this.hostName;
      let that = this;
      this.$dConnect.checkAvailability(host)
      .then(json => {
        console.log('Host ' + host + ' is available.', json);
        that.serviceDiscovery(host);
      })
      .catch(err => {
        console.warn('Host ' + host + ' is not available.', err);
        if (this.$dConnect.isAndroid()) {
          this.launch = true;
        } else {
          this.error = {
            errorMessage: 'DeviceConnectシステムへの接続に失敗しました。'
          };
          this.alert = true;
        }
      });
    },
    serviceDiscovery: function(host) {
      console.log(`serviceDiscovery: ${host}`);
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
