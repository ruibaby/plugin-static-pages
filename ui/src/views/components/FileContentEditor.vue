<script lang="ts" setup>
import type { Project } from "@/types";
import { normalizePath } from "@/utils/path";
import { VButton, VSpace } from "@halo-dev/components";
import { useLocalStorage } from "@vueuse/core";
import { computed } from "vue";
import RiMenuFoldLine from "~icons/ri/menu-fold-line";
import RiMenuUnfoldLine from "~icons/ri/menu-unfold-line";

const SUPPORTED_EDIT_FILES = [
  ".md",
  ".html",
  ".htm",
  ".css",
  ".js",
  ".json",
  ".svg",
  ".xml",
];

const IMAGE_FILES = [".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg"];

const props = withDefaults(defineProps<{ project: Project; path?: string }>(), {
  path: undefined,
});

const showSidebar = useLocalStorage("plugin-static-pages:show-sidebar", true);

const isEditableFile = computed(() => {
  if (!props.path) {
    return false;
  }
  return SUPPORTED_EDIT_FILES.some((ext) => props.path?.endsWith(ext));
});

const isImageFile = computed(() => {
  if (!props.path) {
    return false;
  }
  return IMAGE_FILES.some((ext) => props.path?.endsWith(ext));
});

const fullPath = computed(() => {
  if (!props.path) {
    return "";
  }
  return normalizePath("/", props.project.spec.directory, props.path);
});
</script>

<template>
  <div
    class="sp-flex sp-h-12 sp-items-center sp-justify-between sp-border-b sp-px-2"
  >
    <VSpace>
      <div
        class="sp-inline-flex sp-cursor-pointer sp-items-center sp-justify-center sp-rounded sp-p-1.5 sp-transition-all hover:sp-bg-gray-100"
        :class="{ 'sp-bg-gray-100': !showSidebar }"
        @click="showSidebar = !showSidebar"
      >
        <RiMenuFoldLine v-if="showSidebar" />
        <RiMenuUnfoldLine v-else />
      </div>
      <span v-if="path" class="sp-text-sm sp-font-semibold sp-text-gray-900">
        {{ path }}
      </span>
    </VSpace>
    <VSpace v-if="path && isEditableFile">
      <VButton type="secondary"> 保存</VButton>
    </VSpace>
  </div>
  <div
    v-if="!path"
    class="sp-flex sp-h-full sp-w-full sp-items-center sp-justify-center"
  >
    <span class="sp-text-sm sp-text-gray-900"> 当前未选择文件 </span>
  </div>
  <div v-else-if="isImageFile" class="sp-p-2">
    <img :src="fullPath" />
  </div>
  <div
    v-else-if="isEditableFile"
    class="sp-h-full sp-w-full"
    style="height: calc(100vh - 11rem)"
  >
    {{ path }}
  </div>
  <div
    v-else
    class="sp-flex sp-h-full sp-w-full sp-items-center sp-justify-center"
  >
    <span class="sp-text-sm sp-text-gray-900"> 当前文件不支持编辑和预览 </span>
  </div>
</template>
