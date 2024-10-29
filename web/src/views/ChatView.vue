<template>

  <div class="w-full h-screen">

    <div class="flex flex-col h-screen w-full">
      <!-- Header -->
      <header class="bg-zinc-700 p-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-center text-amber-300 flex-grow">GPT Communicator</h1>

      </header>

      <!-- Main content area -->
      <div class="flex flex-1 overflow-hidden">
        <nav class="bg-zinc-700 p-4 border-zinc-700 overflow-auto" :style="{ width: navWidth }">
          <router-link class="font-bold text-2xl dark:text-amber-300 text-amber-300 hover:underline" to="/">New Chat</router-link>
          <h2 class="text-2xl font-extrabold text-amber-100 border-b border-amber-300 pt-3 pb-2">Conversations:</h2>
          <div v-for="conversation in conversations" :key="conversation.id">
            <Conversations
                :itemId="conversation.id"
                :link="conversation.link"
                :title="conversation.title"
                :isCurrentConversation="conversation.isCurrentConversation"
                @update:conversationRemoved="removeConversationById($event)"
            />
          </div>
        </nav>

        <div class="resizer" @mousedown="startResize"></div>
        <main class="flex-1 flex flex-col overflow-hidden bg-zinc-700">
          <div class="flex-1 p-4 overflow-y-auto w-full bg-white" id="chat-content">

            <div v-for="item in conversationItems" :key="item.id">
              <ConversationItem :item-id="item.id" :html-to-render="item.htmlToRender" :send-by="item.sendBy"/>
            </div>

          </div>

          <div class="prompt-resizer" @mousedown="startResizePrompt"></div>

          <footer class="bg-gray-100 p-4 border-t flex justify-center items-center " style=" overflow: scroll !important;">
            <div class="text-black w-full" :style="{height: promptHeight}">
              <h3 class="font-bold text-lg">Prompt</h3>
              <textarea id="prompt-ta"
                        class="block p-2.5 w-full text-xl text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                        placeholder="Prompt ....." :style="{ height: promptHeightTA }"/>

              <div class="mt-1 mb-1">
                <button
                    v-if="!isSendBtnDisable"
                    :disabled="isSendBtnDisable"
                    @click="handleSend()"
                    class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded ml-1">Send
                </button>

                <button
                    class="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded ml-1"
                    @click="isConversationSettingsOpen = !isConversationSettingsOpen"
                >
                  <font-awesome-icon icon="fa-solid fa-gears"/>
                </button>

              </div> <!-- buttons div -->

            </div>

          </footer>
        </main>
      </div>
    </div>
  </div>
  <ConversationSettingsModal
      :is-open="isConversationSettingsOpen"
      :conversation-id="conversationId !== undefined ? conversationId : -1"
      @update:isOpen="isConversationSettingsOpen = $event"
      @update:systemPrompt="customSystemPrompt = $event"
  />
</template>

<script>

import {defineComponent, nextTick, onMounted, ref, watch} from 'vue';
import {GptApiService} from "@/services/GptApiService.js";
import Conversations from "@/components/Conversations.vue";
import {useRoute, useRouter} from "vue-router";
import ConversationItem from "@/components/ConversationItem.vue";
import ConversationSettingsModal from "@/components/ConversationSettingsModal.vue";

export default defineComponent({
  name: "ChatView",
  components: {ConversationSettingsModal, ConversationItem, Conversations},
  setup() {
    const isSendBtnDisable = ref(false);
    const route = useRoute();
    const router = useRouter();
    const conversationId = ref(route.params.id);

    const conversations = ref([]);
    const conversationItems = ref([]);
    const customSystemPrompt = ref("");


    const scrollToBottom = () => {
      const chatContent = document.getElementById("chat-content");
      if (chatContent) {
        chatContent.scroll({
          top: chatContent.scrollHeight,
          behavior: 'smooth'
        });
      }
    };

    const removeConversationById = (conversationId) => {

      let text = "Remove conversation ?";
      if (confirm(text) === true) {
        GptApiService.deleteConversation(conversationId)
            .then(ignore => {
              console.info(`Conversation ${conversationId} mark as deleted`);
              conversations.value = conversations.value = conversations.value.filter(conversation => conversation.id !== conversationId);
              router.push(`/`);
            })
            .catch(reason => {
              console.info(reason);
              alert("Delete conversation error");
            })
      }
    }

    const handleSend = () => {
      const textarea = document.getElementById('prompt-ta');
      if (textarea) {

        if(textarea.value === "") {
          alert("Empty prompt");
          return;
        }
        isSendBtnDisable.value = true;
        const content = textarea.value;
        console.log('Prompt content:', content);
        console.log('Custom system prompt:', customSystemPrompt.value);

        let apiRequest = {
          "prompt": content
        }
        if(customSystemPrompt.value !== "") {
          apiRequest = {
            "prompt": content,
            "systemPrompt": customSystemPrompt.value
          }
        }
        GptApiService.sendAnthropicPrompt(apiRequest,
            conversationId.value
        ).then(response => {
          isSendBtnDisable.value = false;
          console.log(response);

          const responseConversationId = response.conversationId;
          const responseConversationTitle = response.conversationTitle;

          const items = response.items;
          items.forEach(item => {

            if (!conversationItems.value.some(existingItem => existingItem.id === item.id)) {
              conversationItems.value.push(
                  {
                    id: item.id,
                    sendBy: item.sendBy,
                    htmlToRender: item.sendBy === "USER" ? item.prompt : item.content,
                    createdAt: item.createdAt
                  }
              )
            }

          })

          if (conversationId.value === undefined) {
            conversations.value.push({
              id: responseConversationId,
              link: `/conversations/${responseConversationId}`,
              title: responseConversationTitle,
              isCurrentConversation: true
            })
            router.push(`/conversations/${responseConversationId}`);
          }

          nextTick(() => {
            scrollToBottom();
          });

        });

      }
    };

    watch(() => route.params.id,
        (newId, oldId) => {
          console.log(`Route param id changed from ${oldId} to ${newId}`);
          conversationId.value = newId;
          if (newId !== undefined) {
            GptApiService.getAllConversation()
                .then(response => {
                  conversations.value = [];
                  response.forEach(conversation => {
                    conversations.value.push({
                      id: conversation.conversationId,
                      link: `/conversations/${conversation.conversationId}`,
                      title: conversation.conversationTitle,
                      isCurrentConversation: newId !== undefined && Number(conversation.conversationId) === Number(newId)
                    })
                  })
                });

            GptApiService.getConversationItems(conversationId.value)
                .then(conversationApiResponse => {
                  conversationItems.value = [];
                  conversationApiResponse.items.forEach(item => {

                    conversationItems.value.push(
                        {
                          id: item.id,
                          sendBy: item.sendBy,
                          htmlToRender: item.sendBy === "USER" ? item.prompt : item.content,
                          createdAt: item.createdAt
                        }
                    )

                  })
                });
          } else {
            conversationItems.value = [];
            conversations.value.forEach(conversations => {
              conversations.isCurrentConversation = false;
            })
          }
        }
    );

    onMounted(() => {
      // Optional: Additional ClipboardJS setup if needed

      GptApiService.getAllConversation()
          .then(response => {
            response.forEach(conversation => {
              conversations.value.push({
                id: conversation.conversationId,
                link: `/conversations/${conversation.conversationId}`,
                title: conversation.conversationTitle,
                isCurrentConversation: conversationId.value !== undefined && Number(conversation.conversationId) === Number(conversationId.value)
              })
            })
          });

      if (conversationId.value !== undefined) {
        GptApiService.getConversationItems(conversationId.value)
            .then(conversationApiResponse => {
              conversationApiResponse.items.forEach(item => {

                conversationItems.value.push(
                    {
                      id: item.id,
                      sendBy: item.sendBy,
                      htmlToRender: item.sendBy === "USER" ? item.prompt : item.content,
                      createdAt: item.createdAt
                    }
                )

              })
            })
      }
    });

    const isPromptHide = ref(false);
    // Odczytanie szerokości z localStorage lub ustawienie wartości domyślnej
    const savedNavWidth = localStorage.getItem('navWidth') || '24rem';
    const navWidth = ref(savedNavWidth); // Initial width of nav

    const startResize = (event) => {
      document.addEventListener('mousemove', resize);
      document.addEventListener('mouseup', stopResize);
    };

    const resize = (event) => {
      const newWidth = event.clientX;
      if (newWidth > 191 && newWidth < 600) { // 384px = 24rem
        navWidth.value = `${newWidth}px`;
      }
    };

    const stopResize = () => {
      document.removeEventListener('mousemove', resize);
      document.removeEventListener('mouseup', stopResize);
      localStorage.setItem('navWidth', navWidth.value);
    };

    const promptHeight = ref(localStorage.getItem('promptHeight') || '24rem');
    const promptHeightTA = ref(localStorage.getItem('promptHeightTA') || '270px');

    const startResizePrompt = (event) => {
      document.addEventListener('mousemove', resizePrompt);
      document.addEventListener('mouseup', stopResizePrompt);
    };

    const resizePrompt = (event) => {
      const height = self.innerHeight - event.clientY;
      if (height > 139 && height < 700) {
        const promptHeightTAVal = height - 80;
        promptHeight.value = `${height}px`;
        promptHeightTA.value = `${promptHeightTAVal}px`;
        // console.log(`Resize Prompt: ${promptHeight.value} promptHeightTAVal: ${promptHeightTA.value}`);
      }

    };
    const stopResizePrompt = () => {
      document.removeEventListener('mousemove', resizePrompt);
      document.removeEventListener('mouseup', stopResizePrompt);
      localStorage.setItem('promptHeight', promptHeight.value);
      localStorage.setItem('promptHeightTA', promptHeightTA.value);
      console.log(`stopResizePrompt Resize Prompt: ${promptHeight.value} promptHeightTAVal: ${promptHeightTA.value}`);
    };

    const isConversationSettingsOpen = ref(false);


    return {
      isSendBtnDisable,
      conversations,
      conversationItems,
      handleSend,
      conversationId,
      removeConversationById,
      isPromptHide,
      navWidth,
      startResize,
      startResizePrompt,
      promptHeight,
      promptHeightTA,
      isConversationSettingsOpen,
      customSystemPrompt
    };
  }
});

</script>

<style>
@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    align-items: center;
  }
}

.copy-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 4px 8px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.copy-btn:hover {
  background-color: #45a049;
}

pre {
  position: relative;
  padding-top: 24px; /* Make space for the button */
}

.resizer {
  width: 4px;
  height: 100%;
  cursor: col-resize;
  background-color: #333;
  //background-color: rgb(252, 211, 77, .9);
}

.prompt-resizer {
  width: 100%;
  height: 4px;
  cursor: row-resize;
  background-color: #333;
  //background-color: rgba(252, 211, 77, .9);
}
</style>
