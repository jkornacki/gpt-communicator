<template>
  <div class="flex flex-col h-screen" style="width: 100vh !important; border-radius: 15px;">
    <!-- Header -->
    <header class="bg-gray-100 p-4 border-b">
      <h1 class="text-2xl font-bold text-center text-red-300">GPT DEMO</h1>
    </header>

    <!-- Main content area -->
    <div class="flex flex-1 overflow-hidden">
      <nav class="w-64 bg-gray-100 p-4 border-r">
        <router-link class="text-blue-600 dark:text-blue-500 hover:underline" to="/" >New Chat</router-link>
        <h2 class="text-2xl text-black">Conversations:</h2>
        <div v-for="conversation in conversations" :key="conversation.id">
          <Conversations :link="conversation.link" :title="conversation.title" :isCurrentConversation="id===conversation.id"/>
        </div>
      </nav>

      <main class="flex-1 flex flex-col overflow-hidden bg-white">
        <div class="flex-1 p-4 overflow-y-auto">

          <div v-for="item in conversationItems" :key="item.id">
            <ConversationItem :item-id="item.id" :html-to-render="item.htmlToRender" :send-by="item.sendBy"/>
          </div>


        </div>

        <footer class="bg-gray-100 p-4 border-t flex justify-center items-center" style=" overflow: scroll !important;">
          <div class="text-black w-full h-80">
            <h3 class="font-bold text-lg">Prompt</h3>
            <textarea id="prompt-ta" class="border rounded-2xl w-full h-3/4 p-3 text-xl"/>
            <button
                @click="handleSend()"
                class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded ml-1">Send
            </button>

          </div>

        </footer>
      </main>
    </div>
  </div>
</template>

<script>

import {defineComponent, onMounted, ref, watch} from 'vue';
import {GptApiService} from "@/services/GptApiService.js";
import Conversations from "@/components/Conversations.vue";
import {useRoute, useRouter} from "vue-router";
import ConversationItem from "@/components/ConversationItem.vue";

export default defineComponent({
  name: "ChatView",
  components: {ConversationItem, Conversations},
  setup() {
    const route = useRoute();
    const router = useRouter();
    const conversationId = ref(route.params.id);

    const conversations = ref([]);
    const conversationItems = ref([]);

    const handleSend = () => {
      const textarea = document.getElementById('prompt-ta');
      if (textarea) {
        const content = textarea.value;
        console.log('Prompt content:', content);


        GptApiService.sendAnthropicPrompt(
            {
              "prompt": content,
            },
            conversationId.value
        ).then(response => {
          console.log(response);

          const conversationId = response.conversationId;
          const conversationTitle = response.conversationTitle;

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

          conversations.value.push({id: conversationId, link: `/conversations/${conversationId}`, title: conversationTitle})
          router.push(`/conversations/${conversationId}`);

        })
      }
    };

    watch(() => route.params.id,
        (newId, oldId) => {
          console.log(`Route param id changed from ${oldId} to ${newId}`);
          conversationId.value = newId;
          if(newId !== undefined) {
            GptApiService.getAllConversation()
                .then(response => {
                  conversations.value = [];
                  response.forEach(conversation => {
                    conversations.value.push({
                      id: conversation.conversationId,
                      link: `/conversations/${conversation.conversationId}`,
                      title: conversation.conversationTitle,
                      isCurrentConversation: newId !== undefined && conversation.conversationId === newId
                    })
                  })
                });

            GptApiService.getConversationItems(conversationId.value)
                .then(conversationApiResponse =>{
                  conversationItems.value = [];
                  conversationApiResponse.items.forEach( item => {

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
                isCurrentConversation: conversationId.value !== undefined && conversation.conversationId === conversationId.value
              })
            })
          })

      if(conversationId.value !== undefined) {
        GptApiService.getConversationItems(conversationId.value)
            .then(conversationApiResponse =>{
              conversationApiResponse.items.forEach( item => {

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
