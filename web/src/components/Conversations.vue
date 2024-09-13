<template>
  <div class="conversations font-extrabold text-black text-lg">
    <span v-if="!isCurrentConversation">
      - <router-link class="text-blue-950 dark:text-blue-500 hover:underline" :to="link" v-if="!isCurrentConversation">{{ title }}</router-link>
    </span>
    <span v-if="isCurrentConversation">
      - <router-link class="text-blue-950 dark:text-blue-700 hover:underline bg-emerald-100" :to="link" v-if="isCurrentConversation">{{ title }}</router-link>
    </span>

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