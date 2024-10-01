<template>

  <div class="w-full h-screen">

    <div class="flex flex-col h-screen w-full">
      <!-- Header -->
      <header class="bg-gray-100 p-4 border-b">
        <h1 class="text-2xl font-bold text-center text-green-900">GPT DEMO</h1>
      </header>

      <!-- Main content area -->
      <div class="flex flex-1 overflow-hidden">
        <nav class="w-80 bg-gray-200 p-4 border-r">
          <router-link class="text-green-500 text-2xl dark:text-blue-500 hover:underline" to="/">New Chat</router-link>
          <h2 class="text-2xl text-black">Conversations:</h2>
          <div v-for="conversation in conversations" :key="conversation.id">
            <Conversations
                :itemId="conversation.id"
                :link="conversation.link"
                :title="conversation.title"
                :isCurrentConversation="conversation.isCurrentConversation"
            />
          </div>
        </nav>

        <main class="flex-1 flex flex-col overflow-hidden bg-white">
          <div class="flex-1 p-4 overflow-y-auto w-full" id="chat-content">

            <div v-for="item in conversationItems" :key="item.id">
              <ConversationItem :item-id="item.id" :html-to-render="item.htmlToRender" :send-by="item.sendBy"/>
            </div>


          </div>

          <footer class="bg-gray-100 p-4 border-t flex justify-center items-center" style=" overflow: scroll !important;">
            <div class="text-black w-full h-80">
              <h3 class="font-bold text-lg">Prompt</h3>
              <textarea id="prompt-ta" class="border rounded-2xl w-full h-3/4 p-3 text-xl"/>
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
            conversationItems.value.push(
                {
                  id: item.id,
                  sendBy: item.sendBy,
                  htmlToRender: item.sendBy === "USER" ? item.prompt : item.content,
                  createdAt: item.createdAt
                }
            )
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
      conversationId
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
