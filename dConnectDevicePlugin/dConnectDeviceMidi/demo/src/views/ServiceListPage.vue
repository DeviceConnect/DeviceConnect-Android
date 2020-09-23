<template>
  <v-container>
    <v-card v-if="hasServices" class="mx-auto">
      <v-list>
        <v-list-item v-for="item in targetServices" :key="item.id" @click="nextPage(item.id)">
          <v-list-item-content>
            <v-list-item-title v-text="item.name"></v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card>
  </v-container>
</template>
<script>
export default {
  name: 'ServiceListPage',

  props: {
    allServices: {
      type: Array
    }
  },

  methods: {
    nextPage: function(serviceId) {
      console.log('nextPage: serviceId = ' + serviceId);
      this.$router.push({
        path: `/profile-select/${serviceId}`,
        query: {
          ip: this.$route.query.ip
        }
      });
    }
  },

  computed: {
    hasServices: {
      get: function() { return this.allServices.length > 0; }
    },
    targetServices: {
      get: function() {
        let services = [];
        for (var key in this.allServices) {
          let item = this.allServices[key];
          if (!item.online || ! (item.scopes.includes('midi') && item.scopes.includes('soundModule'))) {
            continue;
          }
          services.push({
            id: item.id,
            name: item.name,
          });
        }
        return services;
      }
    }
  },

  data: () => ({
  }),
}
</script>