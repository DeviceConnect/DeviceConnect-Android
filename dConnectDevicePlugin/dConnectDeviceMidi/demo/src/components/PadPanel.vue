<template>
  <v-container style="width: 400px">
    <v-row v-for="i in rows" :key="i">
      <v-col v-for="pad in subPads(i-1)" :key="pad.id">
        <v-btn
          block
          style="height: 120px"
          @mousedown="mouseEvent($event, pad, true)"
          @mouseup="mouseEvent($event, pad, false)"
          @touchstart="touchEvent($event, pad, true)"
          @touchend="touchEvent($event, pad, false)">
          {{ pad.name }}
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
export default {
  name: 'PadPanel',
  props: {
    rows: {
      type: Number
    },
    cols: {
      type: Number
    },
    pads: {
      type: Array
    }
  },
  methods: {
    mouseEvent: function(event, pad, on) {
      this.$emit('mouse', pad, on);
    },
    touchEvent: function(event, pad, on) {
      let force;
      if (event.touches.length > 0) {
        force = event.touches[0].force;
      }
      if (force === undefined || force === null) {
        force = 1.0;
      }
      this.$emit('touch', pad, on, force);
    },
    subPads: function(row) {
      if (this.pads.length == 0) {
        return [];
      }
      let result = [];
      for (let d = 0; d < this.cols; d++) {
        let index = row * this.cols + d;
        result.push(this.pads[index]);
      }
      return result;
    }
  },
  data: () => ({
  })
}
</script>
<style scoped>
</style>