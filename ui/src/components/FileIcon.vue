<script lang="ts" setup>
import { computed, markRaw, type Component, type Raw } from "vue";
import VscodeIconsFileTypeHtml from "~icons/vscode-icons/file-type-html";
import VscodeIconsFileTypeCss from "~icons/vscode-icons/file-type-css";
import VscodeIconsDefaultFile from "~icons/vscode-icons/default-file";
import VscodeIconsDefaultFolder from "~icons/vscode-icons/default-folder";
import VscodeIconsFileTypeImage from "~icons/vscode-icons/file-type-image";
import VscodeIconsFileTypeXml from "~icons/vscode-icons/file-type-xml";
import VscodeIconsFileTypeJs from "~icons/vscode-icons/file-type-js";
import VscodeIconsFileTypeSvg from "~icons/vscode-icons/file-type-svg";
import VscodeIconsFileTypeText from "~icons/vscode-icons/file-type-text";

const props = withDefaults(defineProps<{ type: string }>(), {});

const iconMap: Record<string, Raw<Component>> = {
  html: markRaw(VscodeIconsFileTypeHtml),
  css: markRaw(VscodeIconsFileTypeCss),
  directory: markRaw(VscodeIconsDefaultFolder),
  png: markRaw(VscodeIconsFileTypeImage),
  xml: markRaw(VscodeIconsFileTypeXml),
  js: markRaw(VscodeIconsFileTypeJs),
  svg: markRaw(VscodeIconsFileTypeSvg),
  txt: markRaw(VscodeIconsFileTypeText),
};

const icon = computed(() => {
  return iconMap[props.type] ?? markRaw(VscodeIconsDefaultFile);
});
</script>

<template>
  <component :is="icon" />
</template>
