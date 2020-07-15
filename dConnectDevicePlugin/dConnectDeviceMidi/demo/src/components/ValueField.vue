<template>
  <v-form>
    <v-text-field :value="value" outlined :rules="rules">
      <v-icon slot="prepend" @click="dec()">mdi-minus</v-icon>
      <v-icon slot="append-outer" @click="inc()">mdi-plus</v-icon>
    </v-text-field>
  </v-form>
</template>
<script>
export default {
  name: 'ValueField',
  model: {
    prop: 'value',
    event: 'change'
  },
  props: {
    value: {
      type: Number
    }
  },
  methods: {
    inc: function() {
      this.$emit('change', this.value + 1)
    },
    dec: function() {
      this.$emit('change', this.value - 1)
    }
  },
  data: () => ({
    delta: 0,
    rules: [
      v => (v !== '') || '値を入力してください'
    ]
  })
}
</script>