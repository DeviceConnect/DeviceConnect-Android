<template>
  <v-container>
    <v-container>
      <v-row>
        <v-col>パッド</v-col>
        <v-col>
          <v-select
            v-model="pad.value"
            :items="pad.profiles"
            label="プロファイル"
            outlined
          ></v-select>
        </v-col>
      </v-row>
      <v-row>
        <v-col>スライダー</v-col>
        <v-col>
          <v-select
            v-model="slider.value"
            :items="slider.profiles"
            label="プロファイル"
            outlined
          ></v-select>
        </v-col>
      </v-row>
    </v-container>
    <router-link :to="nextRoute"><v-btn block>決定</v-btn></router-link>
  </v-container>
</template>

<script>
export default {
  name: 'ProfileSelectPage',

  computed: {
    nextPath: {
      get: function() {
        let serviceId = this.$route.params.id;
        return `/settings/${serviceId}`;
      }
    },
    nextRoute: {
      get: function() {
        return {
          path: this.nextPath,
          query: {
            pad: this.pad.value,
            slider: this.slider.value,
            ip: this.$route.query.ip
          }
        };
      }
    }
  },

  data: () => ({
    pad: {
      value: 'midi',
      profiles: [
        {
          text: 'MIDI', value: 'midi'
        },
        {
          text: 'Sound Module', value: 'soundModule'
        },
        {
          text: '使用しない', value: 'off'
        }
      ]
    },
    slider: {
      value: 'midi',
      profiles: [
        {
          text: 'MIDI', value: 'midi'
        },
        {
          text: '使用しない', value: 'off'
        }
      ]
    },
  })
};
</script>
