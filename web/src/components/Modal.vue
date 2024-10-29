<!-- BoModal.vue -->
<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal" :class="modalSize == 'normal' ? 'small-height-size-modal' : 'normal-height-size-modal' ">
      <header class="modal-header">
        <slot name="title"></slot>
      </header>
      <div class="modal-body">
        <slot name="body"></slot>
      </div>
      <footer class="modal-footer">
        <slot name="footer">
        </slot>
      </footer>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, onUnmounted, ref, watch} from 'vue';
import type {PropType} from 'vue'

export default defineComponent({
  name: "BoModal",
  props: {
    isOpen: {
      type: Boolean,
      require: true
    },
    modalSize: {
      type: String,
      require: false,
      default: ""
    },
    saveAction: {
      type: Function as PropType<() => void>,
      default: null
    }
  },
  setup(props) {

    const isOpen = ref(props.isOpen)
    const modalSize = ref('normal')

    watch(() => props.isOpen, (newValue) => {
      isOpen.value = props.isOpen;
      if (newValue === true) {
        if (props.modalSize != undefined) {

          if (props.modalSize != "small") {
            modalSize.value = "small";
          }

        }
        document.body.classList.add('no-scroll');
      } else {
        document.body.classList.remove('no-scroll');
      }
    });

    onUnmounted(() => {
      document.body.classList.remove('no-scroll');
    });

    const handleSave = () => {
      if (props.saveAction) {
        props.saveAction();
      }
    };

    return {
      isOpen: isOpen,
      saveAction: props.saveAction,
      handleSave,
      modalSize
    };
  }
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #ffffff;
  width: calc(100% - 5%); /* Daje 20px marginesu z każdej strony */
  max-width: 900px;
  /*min-height: 30px;
  height: 300px;
  /*max-width: 60%; /* Aby był maksymalnie na 80% szerokości ekranu */
  overflow-y: auto;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  border: rgba(252, 211, 77, .3) 1px solid;
}

.normal-height-size-modal {
  max-height: calc(100% - 5%);
}

.small-height-size-modal {
  max-height: 300px;
}

.modal-header, .modal-footer {
  padding: 15px;
  background: #333;
  flex-shrink: 0; /* Zapobiega kurczeniu się tych elementów */
}

.modal-body {
  padding: 15px;
  overflow-y: auto;
}
</style>