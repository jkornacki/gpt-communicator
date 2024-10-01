<template>
  <div class="conversations font-extrabold text-black text-lg bg-zinc-700">
    <div v-if="!isTitleEdited">
      <span v-if="!isCurrentConversation" class="text-amber-100 font-bold">
      - <router-link class="text-blue-700 dark:text-blue-100 font-bold hover:underline" :to="link" v-if="!isCurrentConversation">{{
          currentTitle
        }}</router-link>
       </span>
      <span v-if="isCurrentConversation" class="text-amber-100 font-bold">
      - <router-link class="dark:text-red-500 hover:underline font-bold" :to="link" v-if="isCurrentConversation">{{ currentTitle }}</router-link>
      <button class="bg-blue-600 text-white py-1 px-1 rounded ml-2" @click="editTitleClick">
        <font-awesome-icon icon="fa-solid fa-pen"/>
      </button>
      </span>
    </div>

    <div v-if="isTitleEdited">
      <input type="text" v-model="currentTitle" autofocus
             v-on:keyup.enter="saveTitle"
             class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
      >
      <button class="bg-green-800 text-white py-1 px-1 rounded ml-2" @click="saveTitle">
        <font-awesome-icon icon="fa-solid fa-check"/>
      </button>
    </div>

  </div>
</template>

<script setup>
import {defineProps, onMounted, ref} from 'vue'
import {GptApiService} from "@/services/GptApiService.js";

// Definiujemy props
const props = defineProps({
  itemId: {
    type: Number,
    required: true
  },
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

let isTitleEdited = ref(false);
let currentTitle = ref(props.title);

const editTitleClick = () => {
  isTitleEdited.value = true;
}

const saveTitle = () => {
  isTitleEdited.value = false;

  GptApiService.editConversationTitle(props.itemId, currentTitle.value)
      .then(value => {
      })
      .catch(reason => {
        console.info(reason);
        alert("Save title error");
      })
}

onMounted(() => {

});


</script>
<style scoped>
.conversations {
  /* Add your styles here */
}
</style>