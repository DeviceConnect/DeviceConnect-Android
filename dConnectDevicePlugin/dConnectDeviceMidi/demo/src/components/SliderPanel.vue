<template>
  <v-container>
    <v-row>
      <v-col v-for="slider in sliders" :key="slider.id">
        <!-- スライダー名 -->
        <div style="text-align:center"><b>{{ slider.name }}</b></div>
      </v-col>
    </v-row>
    <v-row>
      <v-col v-for="slider in sliders" :key="slider.id">
        <!-- スライダー本体 -->
        <v-slider vertical
          v-bind:value="slider.value"
          v-on:input="event($event, slider)"
          thumb-label="always"
          max="127"
          min="0">
        </v-slider>
      </v-col>
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