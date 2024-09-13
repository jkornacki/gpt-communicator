<template>
  <div class="mt-1 text-black mb-4" style="border-top: solid black 1px;">
    <h5 class="text-2xl text-black" style="border-bottom: 1px dotted #d1d1d1; padding: 1em 0" v-if="sendBy === 'USER' ">
      Prompt: {{ htmlToRender }}
    </h5>
    <div class="response text-xl font-bold text-black" v-if="sendBy === 'GPT'" v-html="processedHtml">
    </div>
  </div>
</template>

<script setup>
import {defineProps, onMounted, ref} from 'vue'
import hljs from 'highlight.js'
import ClipboardJS from 'clipboard'
import showdown from "showdown";

// Definiujemy props
const props = defineProps({
  itemId: {
    type: Number,
    required: true
  },
  htmlToRender: {
    type: String,
    required: true
  },
  sendBy: {
    type: String,
    required: true
  }
})


// Przetworzony HTML
const processedHtml = ref('')

// Funkcja do przetwarzania HTML
const processHtml = (html) => {
  const converter = new showdown.Converter();
  converter.setOption('noHeaderId', true);
  converter.setOption('smartIndentationFix', true);
  converter.setOption('tables', true);
  converter.setOption('tasklists', true);
  converter.setOption('ghCodeBlocks', true);
  converter.setOption('simpleLineBreaks', true);

  let processedHtml = converter.makeHtml(html);

  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = processedHtml

  // Dodawanie klas do elementów ol i ul
  tempDiv.querySelectorAll('ol').forEach((ol) => {
    ol.classList.add('list-decimal');
    ol.classList.add('pl-5');
  })

  tempDiv.querySelectorAll('ul').forEach((ul) => {
    ul.classList.add('list-disc');
    ul.classList.add('pl-5');
  })

  tempDiv.querySelectorAll('table').forEach((table) => {
    table.classList.add('table-auto');
    table.classList.add('border');
    table.classList.add('border-collapse');
    table.classList.add('border-b-gray-900');
    table.classList.add('m-2');
  });
  tempDiv.querySelectorAll('th').forEach((th) => {
    th.classList.add("border");
    th.classList.add("border-slate-600");
    th.classList.add("bg-gray-300");
    th.classList.add("p-2");
    th.classList.add("font-bold");
  });

  tempDiv.querySelectorAll('p a').forEach((th) => {
    th.classList.add("text-blue-700");
  });

  tempDiv.querySelectorAll('tr:nth-child(2n) td').forEach((th) => {
    th.classList.add("border");
    th.classList.add("border-slate-600");
    // th.classList.add("bg-gray-100");
    th.classList.add("p-4");
  });
  tempDiv.querySelectorAll('tr:nth-child(2n-1) td').forEach((th) => {
    th.classList.add("border");
    th.classList.add("border-slate-600");
    th.classList.add("bg-gray-200");
    th.classList.add("p-4");
  });

  processedHtml = tempDiv.innerHTML
  hljs.configure({ ignoreUnescapedHTML: true });

  setTimeout(() => {
    document.querySelectorAll('pre code').forEach((block) => {
      if (block.dataset.highlighted) {
        delete block.dataset.highlighted;
      }

      hljs.highlightElement(block);
      block.dataset.highlighted = true;
    });

    // Dodanie przycisku "Copy to Clipboard" do każdego <pre>
    document.querySelectorAll('pre').forEach(pre => {
      const copyButton = document.createElement('button')
      copyButton.innerText = 'Copy'
      copyButton.classList.add('copy-btn')
      pre.appendChild(copyButton)
    })

    // Inicjalizacja ClipboardJS
    new ClipboardJS('.copy-btn', {
      target: (trigger) => {
        return trigger.previousElementSibling
      }
    })
  })

  return processedHtml
}

// Przetwarzanie HTML podczas montowania komponentu
onMounted(() => {
  processedHtml.value = processHtml(props.htmlToRender)
})
</script>

<style scoped>
.copy-btn {
  margin-top: 0.5em;
  padding: 0.2em 0.5em;
  font-size: 0.9em;
  cursor: pointer;
}
</style>