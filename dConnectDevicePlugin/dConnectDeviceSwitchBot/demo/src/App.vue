<template>
  <div id="app">
    <SwitchBotDemo/>
  </div>
</template>

<script>
import SwitchBotDemo from './components/SwitchBotDemo.vue'

export default {
  name: 'app',
  components: {
    SwitchBotDemo
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  /*color: #2c3e50;
  margin-top: 60px;*/
}
</style>
