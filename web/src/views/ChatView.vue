<template>

  <div class="w-full h-screen">

    <div class="flex flex-col h-screen w-full">
      <!-- Header -->
      <header class="bg-zinc-700 p-4">
        <h1 class="text-2xl font-bold text-center text-amber-300">GPT Communicator</h1>
      </header>

      <!-- Main content area -->
      <div class="flex flex-1 overflow-hidden">
        <nav class="w-96 bg-zinc-700 p-4 border-zinc-700">
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

        <main class="flex-1 flex flex-col overflow-hidden bg-zinc-700">
          <div class="flex-1 p-4 overflow-y-auto w-full bg-white" id="chat-content">

            <div v-for="item in conversationItems" :key="item.id">
              <ConversationItem :item-id="item.id" :html-to-render="item.htmlToRender" :send-by="item.sendBy"/>
            </div>


          </div>

          <footer class="bg-gray-100 p-4 border-t flex justify-center items-center" style=" overflow: scroll !important;">
            <div class="text-black w-full h-80">
              <h3 class="font-bold text-lg">Prompt</h3>
              <textarea id="prompt-ta" class="block p-2.5 w-full h-64 text-xl text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder="Prompt ....."/>
              <button
                  v-if="!isSendBtnDisable"
                  :disabled="isSendBtnDisable"
                  @click="handleSend()"
                  class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded ml-1">Send
              </button>

            </div>

          </footer>
        </main>
      </div>
    </div>
  </div>
</template>

<script>

import {defineComponent, onMounted, ref, watch, nextTick} from 'vue';
import {GptApiService} from "@/services/GptApiService.js";
import Conversations from "@/components/Conversations.vue";
import {useRoute, useRouter} from "vue-router";
import ConversationItem from "@/components/ConversationItem.vue";

export default defineComponent({
  name: "ChatView",
  components: {ConversationItem, Conversations},
  setup() {
    const isSendBtnDisable = ref(false);
    const route = useRoute();
    const router = useRouter();
    const conversationId = ref(route.params.id);

    const conversations = ref([]);
    const conversationItems = ref([]);


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
        isSendBtnDisable.value = true;
        const content = textarea.value;
        console.log('Prompt content:', content);


        GptApiService.sendAnthropicPrompt(
            {
              "prompt": content,
            },
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


    return {
      isSendBtnDisable,
      conversations,
      conversationItems,
      handleSend,
      conversationId,
      removeConversationById
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

/* Add some styles for the copy button */
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
</style>
