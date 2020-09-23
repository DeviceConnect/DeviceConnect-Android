<template>
  <v-form>
    <v-text-field :value="value" outlined :rules="rules" :disabled="inputDisabled">
      <v-icon slot="prepend" @click="dec()" :disabled="isMin">mdi-minus</v-icon>
      <v-icon slot="append-outer" @click="inc()" :disabled="isMax">mdi-plus</v-icon>
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
    },
    inputDisabled: {
      type: Boolean
    },
    max : {
      type: Number
    },
    min : {
      type: Number
    }
  },
  computed: {
    isMax: function() { return this.max <= this.value; },
    isMin: function() { return this.min >= this.value; }
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