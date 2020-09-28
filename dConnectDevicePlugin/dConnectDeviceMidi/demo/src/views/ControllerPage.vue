<template>
  <v-container>
    <v-row>
      <v-col v-if="usePad">
        <pad-panel :rows="2" :cols="2" :pads="pads" @touch="onTouch" @mouse="onMouse"></pad-panel>
      </v-col>
      <v-col v-if="useSlider">
        <slider-panel :sliders="sliders" @change="onSlide"></slider-panel>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
import PadPanel from '../components/PadPanel.vue'
import SliderPanel from '../components/SliderPanel.vue'

function postMidiMessage(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'POST',
      path: '/gotapi/midi/message',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

function postSoundModuleNote(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'POST',
      path: '/gotapi/soundModule/note',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

function deleteSoundModuleNote(session, params) {
  return new Promise((resolve, reject) => {
    session.request({
      method: 'DELETE',
      path: '/gotapi/soundModule/note',
      params
    })
    .then((json) => {
      const result = json.result;
      if (result !== 0) {
        reject(json);
        return;
      }
      resolve(json);
    })
    .catch((err) => {
      reject(err);
    })
  });
}

export default {
  name: 'ControllerPage',

  components: {
    PadPanel,
    SliderPanel
  },

  data: () => ({
    pads: [],
    sliders: [],
    isHelio: false
  }),

  computed: {
    hasTouchEvent: {
      get: function() { return 'ontouchstart' in window; }
    },
    usePad: {
      get: function() {
        if (this.$route.query.pad_count === undefined) {
          return false;
        }
        return this.$route.query.pad_count > 0;
      }
    },
    useSlider: {
      get: function() {
        if (this.$route.query.slider_count === undefined) {
          return false;
        }
        return this.$route.query.slider_count > 0;
      }
    },
    host: {
      get: function() {
        return this.$route.query.ip || 'localhost';
      }
    }
  },

  methods: {
    onTouch: function(pad, on, force) {
      console.log('onTouch: pad.id = ' + pad.id + ", on = " + on + ", force = " + force);
      if (this.hasTouchEvent) {
        this.sendPadMessage(pad, on, force);
      }
    },

    onMouse: function(pad, on) {
      console.log('onMouse: pad.id = ' + pad.id + ", on = " + on);
      if (!this.hasTouchEvent || this.isHelio) {
        this.sendPadMessage(pad, on);
      }
    },

    onSlide: function(slider) {
      console.log('onSlide: slider.id = ' + slider.id + ", slider.value = " + slider.value);
      this.sendSliderMessage(slider);
    },

    sendPadMessage: function(pad, on, force) {
      if (pad.profile === 'midi') {
        let midiMessage = this.createNoteMessage(pad, on, force);
        this.sendMidiMessage(midiMessage);
      } else if (pad.profile === 'soundModule') {
        this.sendSoundModuleMessage(pad, on);
      }
    },

    sendSliderMessage: function(slider) {
      let midiMessage = this.createControlChangeMessage(slider);
      this.sendMidiMessage(midiMessage);
    },

    sendMidiMessage: function(midiMessage) {
       this.$dConnect.offer(this.host, postMidiMessage, {
        serviceId: this.$route.params.id,
        message: midiMessage.toString()
      });
    },

    sendSoundModuleMessage: function(pad, on) {
      this.$dConnect.offer(this.host, on ? postSoundModuleNote : deleteSoundModuleNote, {
        serviceId: this.$route.params.id,
        note: pad.note,
        channel: pad.channel
      });
    },

    createNoteMessage: function(pad, on, force) {
      let messageType = on ? 0b1001 : 0b1000;
      let velocity = pad.velocityMode === 'touch' ? 127 * force : pad.velocity;
      return Int8Array.from([
        (messageType << 4) | pad.channel & 0x0F,
        pad.note & 0x7F,
        velocity & 0x7F
      ]);
    },

    createControlChangeMessage: function(slider) {
      let messageType = 0b1011;
      return Int8Array.from([
        (messageType << 4) | slider.channel & 0x0F,
        slider.controlNumber & 0x7F,
        slider.value & 0x7F
      ]);
    }
  },

  mounted: function() {
    console.log('mounted: ', this.$route.query);

    let userAgent = navigator.userAgent;
    if (userAgent) {
      this.isHelio = userAgent.toLowerCase().includes('helio/');
    }

    let query = this.$route.query;
    let padCount = query['pad_count'];
    let padProfile = query['pad_profile'];
    if (padCount) {
      for (let k = 0; k < padCount; k++) {
        let pad;
        if (padProfile === 'midi' || padProfile === 'soundModule') {
          pad = {
            id: k,
            profile: padProfile,
            name: query['pad_' + k + '_name'],
            channel: query['pad_' + k + '_midi_channel'],
            note: query['pad_' + k + '_midi_note'],
            velocity: query['pad_' + k + '_midi_velocity'],
            velocityMode: query['pad_' + k + '_midi_velocity_mode'],
          };
        }
        if (pad) {
          this.pads.push(pad);
        }
      }
    }
    let sliderCount = query['slider_count'];
    if (sliderCount) {
      for (let k = 0; k < sliderCount; k++) {
        this.sliders.push({
          id: k,
          profile: 'midi',
          name: query['slider_' + k + '_name'],
          channel: query['slider_' + k + '_midi_channel'],
          controlNumber: query['slider_' + k + '_midi_control_number'],
          value: 0
        });
      }
    }
  }
}
</script>