<template>
  <v-container>
    <v-row>
      <v-slider vertical
        v-for="slider in sliders"
        v-bind:value="slider.value" v-on:input="event($event, slider)"
        :key="slider.id"
        max="127"
        min="0"></v-slider>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: {
    sliders: {
      type: Array
    }
  },
  methods: {
    event: function(newValue, slider) {
      slider.value = newValue;
      this.$emit('change', slider);
    }
  }
}
</script>