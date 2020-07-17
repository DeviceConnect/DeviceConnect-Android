<template>
  <v-container style="width: 400px">
    <v-row v-for="i in rows" :key="i">
      <v-col v-for="pad in subPads(i-1)" :key="pad.id">
        <v-btn
          block
          style="height: 120px"
          @mouseup="event(pad, false)"
          @mousedown="event(pad, true)">
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
    event: function(pad, on) {
      this.$emit('touch', pad, on);
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