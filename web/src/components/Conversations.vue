<template>
  <div class="conversations font-extrabold text-black text-lg bg-zinc-700">
    <div v-if="!isTitleEdited">
      <span v-if="!isCurrentConversation" class="text-amber-100 font-bold">
      - <router-link class="text-blue-700 dark:text-blue-100 font-bold hover:underline" :to="link" v-if="!isCurrentConversation">{{
          currentTitle
        }}</router-link>
       </span>
      <div class="flex justify-between items-center" v-if="isCurrentConversation">
        <span class="text-amber-100 font-bold">
          - <router-link class="dark:text-red-500 hover:underline font-bold" :to="link" v-if="isCurrentConversation">{{ currentTitle }}</router-link>
        </span>
        <div>
          <button class="bg-blue-600 text-white py-1 px-1 rounded ml-2" @click="editTitleClick">
            <font-awesome-icon icon="fa-solid fa-pen"/>
          </button>
          <button class="bg-red-600 text-white py-1 px-1 rounded ml-2" @click="removeConversationClick">
            <font-awesome-icon icon="fa-solid fa-trash"/>
          </button>
        </div>
      </div>

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

<script>
import {defineComponent, onMounted, ref} from 'vue';
import {GptApiService} from '@/services/GptApiService.js';

export default defineComponent({
  props: {
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
  },
  emits: ['update:conversationRemoved'],
  setup(props, {emit}) {
    const isTitleEdited = ref(false);
    const currentTitle = ref(props.title);

    const editTitleClick = () => {
      isTitleEdited.value = true;
    };

    const removeConversationClick = () => {
      emit('update:conversationRemoved', props.itemId);
    };

    const saveTitle = () => {
      isTitleEdited.value = false;

      GptApiService.editConversationTitle(props.itemId, currentTitle.value)
          .then(value => {
            console.info(`Conversation ${props.itemId} edited`);
          })
          .catch(reason => {
            console.info(reason);
            alert('Save title error');
          });
    };

    onMounted(() => {
      // Any mounted logic
    });

    return {
      isTitleEdited,
      currentTitle,
      editTitleClick,
      removeConversationClick,
      saveTitle
    };
  }
});
</script>

<style scoped>
.conversations {
  /* Add your styles here */
}
</style>