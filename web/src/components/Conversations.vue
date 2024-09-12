<template>
  <div class="conversations font-extrabold text-black text-lg">
    - <router-link class="text-blue-600 dark:text-blue-500 hover:underline" :to="link" v-if="!isCurrentConversation">{{ title }}</router-link>
    - <router-link class="text-blue-700 dark:text-blue-700 hover:underline text-2xl" :to="link" v-if="isCurrentConversation">{{ title }}</router-link>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

// Definiujemy props
const props = defineProps({
  link: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  isCurrentConversation: {
    type: Boolean,
    required: true
  }
})
</script>
<style scoped>
.conversations {
  /* Add your styles here */
}
</style>