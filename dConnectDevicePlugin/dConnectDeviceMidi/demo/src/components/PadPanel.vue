<template>
  <v-container style="width: 400px">
    <v-row v-for="i in rows" :key="i">
      <v-col v-for="k in subPads(i-1)" :key="k.id">
        <v-btn @mouseup="event(i-1, k.id, false)" @mousedown="event(i-1, k.id, true)">{{ k.name }}</v-btn>
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
    event: function(row, col, on) {
      this.$emit('touch', this.index(row, col), on);
    },
    index: function(row, col) {
      return row * this.cols + col;
    },
    subPads: function(row) {
      console.log("subPads: ", this.pads);
      let result = [];
      for (let d = 0; d < this.cols; d++) {
        let index = row * this.cols + d;
        console.log("subPads: index = " + index);
        result.push(this.pads[index]);
      }
      console.log("subPads: result", result);
      return result;
    }
  },
  data: () => ({
  })
}
</script>
<style scoped>
</style>