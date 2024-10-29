<template>
  <Modal :is-open="isOpen">
    <template #title>
      <h3 class="text-amber-300 font-extrabold text-xl">Edit conversation settings</h3>
    </template>
    <template #body>
      <div class="text-black">
        <div class="mb-4 w-full">

          <form>

            <h3 class="mb-4 text-black font-bold">System prompt:</h3>
            <ul class="items-center w-full text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-lg sm:flex dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <li class="w-full border-b border-gray-200 sm:border-b-0 sm:border-r dark:border-gray-600">
                <div class="flex items-center ps-3">
                  <input v-model="selectModel" id="default-system-prompt" @click="defaultSystemPromptSelect()" type="radio" value="default" name="list-radio"
                         class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-700 dark:focus:ring-offset-gray-700 focus:ring-2 dark:bg-gray-600 dark:border-gray-500">
                  <label for="default-system-prompt" @click="defaultSystemPromptSelect()"
                         class="w-full py-3 ms-2 text-sm font-medium text-gray-900 dark:text-gray-300">Default System prompt </label>
                </div>
              </li>
              <li class="w-full border-b border-gray-200 sm:border-b-0 sm:border-r dark:border-gray-600">
                <div class="flex items-center ps-3">
                  <input v-model="selectModel" id="custom-system-prompt" @click="customSystemPromptSelect()" type="radio" value="custom" name="list-radio"
                         class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-700 dark:focus:ring-offset-gray-700 focus:ring-2 dark:bg-gray-600 dark:border-gray-500">
                  <label for="custom-system-prompt" @click="customSystemPromptSelect()"
                         class="w-full py-3 ms-2 text-sm font-medium text-gray-900 dark:text-gray-300">Custom System prompt</label>
                </div>
              </li>
              <!--              <li class="w-full border-b border-gray-200 sm:border-b-0 sm:border-r dark:border-gray-600">-->
              <!--                <div class="flex items-center ps-3">-->
              <!--                  <input v-model="selectModel" id="user-system-prompt" @click="isPromptsFromOtherConversationShow = true" type="radio" value="other-conversation" name="list-radio"-->
              <!--                         class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-700 dark:focus:ring-offset-gray-700 focus:ring-2 dark:bg-gray-600 dark:border-gray-500">-->
              <!--                  <label for="user-system-prompt" @click="isPromptsFromOtherConversationShow = true"-->
              <!--                         class="w-full py-3 ms-2 text-sm font-medium text-gray-900 dark:text-gray-300">System prompt from other conversations</label>-->
              <!--                </div>-->
              <!--              </li>-->
            </ul>

            <!--            <div v-if="isPromptsFromOtherConversationShow">-->
            <!--              <label for="countries" class="block mb-2 text-sm font-bold text-black ">Select system prompt from other conversation</label>-->
            <!--              <select id="countries"-->
            <!--                      class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">-->
            <!--                <option selected>TODO</option>-->
            <!--              </select>-->
            <!--            </div>-->


            <label for="310c4c8f-81ae-42f0-afcc-efc1e6affa5d" class="font-bold">System prompt:</label>
            <textarea placeholder="System prompt ..." type=""
                      id="310c4c8f-81ae-42f0-afcc-efc1e6affa5d"
                      :disabled="isTextPromptDisable"
                      v-model="systemPromptsText"
                      rows="12"
                      class="text-blue-100 font-bold w-full block rounded-lg border dark:border-none dark:bg-neutral-600 py-[9px] px-3 pr-4 text-sm focus:border-blue-400 focus:ring-1 focus:ring-blue-400 focus:outline-none"></textarea>
          </form>
        </div>
      </div>
    </template>
    <template #footer>
      <button type="button"
              @click="save(systemPromptsText)"
              class="font-bold text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800">
        Save
      </button>
      <button
          class="font-bold p-3 focus:outline-none text-white bg-red-700 hover:bg-red-800 focus:ring-4 focus:ring-red-300 rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-red-600 dark:hover:bg-red-700 dark:focus:ring-red-900"
          @click="closeModal()">Close
      </button>
    </template>
  </Modal>
</template>

<script>
import Modal from "@/components/Modal.vue";
import {defineComponent, onMounted, ref, watch} from 'vue';
import {GptApiService} from "@/services/GptApiService.js";

export default defineComponent({
  name: "ConversationSettingsModal",
  components: {Modal},
  props: {
    isOpen: {
      type: Boolean,
      required: true
    },
    conversationId: {
      type: Number,
      required: true
    }
  },
  setup: function (props, {emit}) {
    const isOpen = ref(props.isOpen);
    const conversationId = ref(props.conversationId);
    const isPromptsFromOtherConversationShow = ref(false);

    const systemPromptsApiResponse = ref([]);
    const defaultSystemPromptsApiResponse = ref("");
    const currentSystemPromptsApiResponse = ref("");
    const systemPromptsText = ref("");
    const selectModel = ref("");
    const isTextPromptDisable = ref(false);

    onMounted(() => {
      // Any mounted logic
    });

    watch(() => props.isOpen, (newValue) => {
      isOpen.value = props.isOpen;
      if (isOpen.value === true) {
        conversationId.value = props.conversationId;
        GptApiService.getSystemPrompts(conversationId.value)
            .then(systemPrompts => {
              systemPrompts.forEach(systemPromptFromAPI => {
                if (systemPromptFromAPI.isSystemPrompt === true) {
                  defaultSystemPromptsApiResponse.value = systemPromptFromAPI.prompt;
                } else if (systemPromptFromAPI.isCurrentConversationsPrompt === true) {
                  currentSystemPromptsApiResponse.value = systemPromptFromAPI.prompt;
                }

                if (currentSystemPromptsApiResponse.value === systemPromptsApiResponse.value || currentSystemPromptsApiResponse.value == "") {

                  isTextPromptDisable.value = true;
                  selectModel.value = "default"
                  systemPromptsText.value = defaultSystemPromptsApiResponse.value;
                } else if (currentSystemPromptsApiResponse.value !== "") {

                  isTextPromptDisable.value = false;
                  selectModel.value = "custom"
                  systemPromptsText.value = currentSystemPromptsApiResponse.value;
                }

              })
            })
      }
    });

    const closeModal = () => {
      isOpen.value = false;
      emit('update:isOpen', false);
    }

    const save = (value) => {

      if(value !== defaultSystemPromptsApiResponse.value) {
        emit('update:systemPrompt', value);
      } else {
        emit('update:systemPrompt', "");
      }
      isOpen.value = false;
      emit('update:isOpen', false);
    }

    const customSystemPromptSelect = () => {
      // isPromptsFromOtherConversationShow.value = false;
      isTextPromptDisable.value = false;
    }

    const defaultSystemPromptSelect = () => {
      // isPromptsFromOtherConversationShow.value = false;
      isTextPromptDisable.value = true;
      systemPromptsText.value = defaultSystemPromptsApiResponse.value;
    }

    return {
      isOpen,
      closeModal,
      save,
      customSystemPromptSelect,
      defaultSystemPromptSelect,
      isPromptsFromOtherConversationShow,
      systemPromptsText,
      selectModel,
      isTextPromptDisable
    };
  }
});
</script>