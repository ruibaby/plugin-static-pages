<script lang="ts" setup>
import { css } from "@codemirror/lang-css";
import { html } from "@codemirror/lang-html";
import { javascript } from "@codemirror/lang-javascript";
import type { LanguageSupport } from "@codemirror/language";
import { EditorView } from "@codemirror/view";
import { computed } from "vue";
import { Codemirror } from "vue-codemirror";

const props = withDefaults(defineProps<{ path: string }>(), {});

const modelValue = defineModel({ type: String, default: "" });

const languages: Record<string, LanguageSupport> = {
  html: html(),
  js: javascript(),
  css: css(),
};

const currentLanguage = computed(() => {
  const ext = props.path.split(".").pop();
  if (!ext) {
    return languages.html;
  }
  return languages[ext] ?? languages.html;
});
</script>

<template>
  <codemirror
    v-model="modelValue"
    placeholder="..."
    :style="{ height: '100%', overflow: 'auto' }"
    :autofocus="true"
    :indent-with-tab="false"
    :tab-size="2"
    :extensions="[currentLanguage, EditorView.lineWrapping]"
  />
</template>
