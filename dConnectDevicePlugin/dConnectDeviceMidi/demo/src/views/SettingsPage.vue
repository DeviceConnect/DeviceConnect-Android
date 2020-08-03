<template>
  <v-container>
    <v-tabs>
      <v-tab v-if="usePad">パッド</v-tab>
      <v-tab v-if="useSlider">スライダー</v-tab>

      <!-- パッド設定 -->
      <v-tab-item v-if="usePad">
        <v-expansion-panels v-model="padPanel">
          <v-expansion-panel v-for="pad in pads" :key="pad.id">
            <v-expansion-panel-header>{{ pad.name }}</v-expansion-panel-header>
            <v-expansion-panel-content>

              <!-- MIDIプロファイル -->
              <v-form v-if="useProfileForPad('midi')">
                <v-container>
                  <v-row>
                    <v-col>チャンネル</v-col>
                    <v-col>
                      <value-field v-model="pad.channel" :min="0" :max="15"></value-field>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>ノート番号</v-col>
                    <v-col>
                      <value-field v-model="pad.noteNumber" :min="0" :max="127"></value-field>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>強弱</v-col>
                    <v-col>
                      <v-radio-group mandatory v-if="hasTouchEvent" v-model="pad.velocityMode">
                        <v-radio value="touch" label="画面タッチの強さ"></v-radio>
                        <v-radio value="fixed" label="固定値"></v-radio>
                      </v-radio-group>
                      <value-field v-model="pad.velocity" :inputDisabled="pad.velocityMode !== 'fixed'" :min="0" :max="127"></value-field>
                    </v-col>
                  </v-row>
                </v-container>
              </v-form>

              <!-- SoundModuleプロファイル -->
              <v-form v-if="useProfileForPad('soundModule')">
                <v-container>
                  <v-row>
                    <v-col>チャンネル</v-col>
                    <v-col>
                      <value-field v-model="pad.channel" :min="0" :max="15"></value-field>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>音階</v-col>
                    <v-col>
                      <v-text-field outlined v-model="pad.noteName"></v-text-field>
                    </v-col>
                  </v-row>
                </v-container>
              </v-form>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-tab-item>

      <!-- スライダー設定 -->
      <v-tab-item v-if="useSlider">
        <v-expansion-panels v-model="sliderPanel">
          <v-expansion-panel v-for="slider in sliders" :key="slider.id">
            <v-expansion-panel-header>{{ slider.name }}</v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-form>
                <v-container>
                  <v-row>
                    <v-col>チャンネル</v-col>
                    <v-col>
                      <value-field v-model="slider.channel" :min="0" :max="15"></value-field>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>コントロール番号</v-col>
                    <v-col>
                      <value-field v-model="slider.controlNumber" :min="0" :max="127"></value-field>
                    </v-col>
                  </v-row>
                </v-container>
              </v-form>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-tab-item>
    </v-tabs>
    <router-link :to="nextRoute"><v-btn block>決定</v-btn></router-link>
  </v-container>
</template>
<script>
import ValueField from '../components/ValueField.vue'
export default {
  name: 'SettingsPage',

  components: {
    ValueField
  },

  computed: {
    hasTouchEvent: {
      get: function() { return 'ontouchstart' in window; }
    },
    usePad: function() {
      return this.$route.query.pad !== 'off';
    },
    useSlider: function() {
      return this.$route.query.slider !== 'off';
    },
    nextPath: {
      get: function() {
        let serviceId = this.$route.params.id;
        return `/controller/${serviceId}`;
      }
    },
    nextQuery: {
      get: function() {
        let q = {};
        q.ip = this.$route.query.ip;
        if (this.usePad) {
          q['pad_count'] = this.pads.length;
          q['pad_profile'] = this.$route.query.pad;
          if (this.useProfileForPad('midi')) {
            for (let k in this.pads) {
              let pad = this.pads[k];
              q['pad_' + k + '_name'] = pad.name;
              q['pad_' + k + '_midi_channel'] = pad.channel;
              q['pad_' + k + '_midi_note'] = pad.noteNumber;
              q['pad_' + k + '_midi_velocity'] = pad.velocity;
              q['pad_' + k + '_midi_velocity_mode'] = pad.velocityMode;
            }
          } else if (this.useProfileForPad('soundModule')) {
            for (let k in this.pads) {
              let pad = this.pads[k];
              q['pad_' + k + '_name'] = pad.name;
              q['pad_' + k + '_midi_channel'] = pad.channel;
              q['pad_' + k + '_midi_note'] = pad.noteName;
            }
          }
        }
        if (this.useSlider) {
          q['slider_count'] = this.sliders.length;
          for (let k in this.sliders) {
            let slider = this.sliders[k];
            q['slider_' + k + '_name'] = slider.name;
            q['slider_' + k + '_midi_channel'] = slider.channel;
            q['slider_' + k + '_midi_control_number'] = slider.controlNumber;
          }
        }
        return q;
      }
    },
    nextRoute: {
      get: function() {
        return {
          path: this.nextPath,
          query: this.nextQuery
        };
      }
    },
  },

  methods: {
    useProfileForPad: function(profileName) {
      return this.$router.currentRoute.query.pad === profileName;
    },
  },

  data: () => ({
    padPanel: 0,
    pads: [],
    sliderPanel: 0,
    sliders: []
  }),

  mounted: function() {
    for (let i = 0; i < 4; i++) {
      this.pads.push({
        id: i,
        name: 'PAD' + (i + 1),
        channel: 0,
        noteNumber: 40,
        noteName: 'A4',
        velocity: 127,
        velocityMode: this.hasTouchEvent ? 'touch' : 'fixed'
      });
    }
    for (let i = 0; i < 4; i++) {
      this.sliders.push({
        id: i,
        name: 'S' + (i + 1),
        channel: 0,
        controlNumber: 0
      });
    }
  }
}
</script>