<template>
  <v-app>
    <v-app-bar
      app
      color="primary"
      dark
    >
      <v-spacer></v-spacer>
    </v-app-bar>

    <v-content>
      <router-view :allServices="services" />
    </v-content>
  </v-app>
</template>

<script>
export default {
  name: 'App',
  data: () => ({
    services: []
  }),
  watch: {
    '$route': 'connect'
  },
  methods: {
    connect: function() {
      let query = this.$route.query;
      let host = query.ip;
      console.log('App: query = ', query);
      if (!host) {
        host = 'localhost';
      }
      this.$dConnect.connect({ host })
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
