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
      <router-view :allServices="services" />
    </v-main>
  </v-app>
</template>

<script>
export default {
  name: 'App',
  data: () => ({
    services: [],
    title: ''
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
      this.connect();
    },
    connect: function() {
      let query = this.$route.query;
      let host = query.ip;
      console.log('App: query = ', query);
      if (!host) {
        host = 'localhost';
      }
      this.$dConnect.connect({ host, scopes: ['serviceDiscovery', 'serviceInformation', 'midi', 'soundModule'] })
      .then(result => {
        this.services = result.services;
      })
      .catch(err => {
        console.error("ServiceList: error", err);
      });
    }
  }
};
</script>
