<script lang="ts" setup>
// grapesjs
import grapesjs, { type Editor } from "grapesjs";
import "grapesjs/dist/css/grapes.min.css";

// grapesjs plugins
import grapesjsBlocksBasic from "grapesjs-blocks-basic";
import grapesjsPresetWebpage from "grapesjs-preset-webpage";
// @ts-ignore
import grapesjsPluginForms from "grapesjs-plugin-forms";
import grapesjsTabs from "grapesjs-tabs";

import { onMounted, onUnmounted, shallowRef, watch } from "vue";

const props = withDefaults(defineProps<{ modelValue: string }>(), {
  modelValue: "",
});

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

const editor = shallowRef<Editor | undefined>();

watch(
  () => props.modelValue,
  (value) => {
    const editorContent = editor.value?.getHtml();

    if (value === editorContent) {
      return;
    }

    editor.value?.setComponents(value);
  },
  {
    immediate: true,
  }
);

onMounted(() => {
  editor.value = grapesjs.init({
    // Indicate where to init the editor. You can also pass an HTMLElement
    container: "#gjs",
    // Get the content for the canvas directly from the element
    // As an alternative we could use: `components: '<h1>Hello World Component!</h1>'`,
    fromElement: false,
    // Size of the editor
    height: "100%",
    width: "100%",
    // Disable the storage manager for the moment
    storageManager: false,
    // Avoid any default panel
    // panels: { defaults: [] },
    plugins: [
      grapesjsBlocksBasic,
      grapesjsPresetWebpage,
      grapesjsTabs,
      grapesjsPluginForms,
    ],
    pluginsOpts: {
      "grapesjs-tabs": {
        tabsBlock: { category: "Extra" },
      },
    },
  });

  editor.value.onReady(() => {
    editor.value?.setComponents(props.modelValue);
    editor.value?.on("update", () => {
      const newContent = editor.value?.getHtml() || "";

      if (newContent !== props.modelValue) {
        emit("update:modelValue", newContent);
      }
    });
  });
});

onUnmounted(() => {
  editor.value?.destroy();
});
</script>

<template>
  <div id="gjs" class="sp-size-full"></div>
</template>
