<template>
  <v-dialog
    v-model="show"
    hide-overlay
    persistent
    width="300"
  >
    <v-card
      color="primary"
      dark
    >
      <v-card-text>
        Awaiting consensus
        <v-progress-linear
          indeterminate
          color="white"
          class="mb-0"
        ></v-progress-linear>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
  export default {
    name: 'ProgressDialog',
    props: ['show'],
    data () {
      return {
      }
    }
  }
</script>

<style scoped>
</style>
