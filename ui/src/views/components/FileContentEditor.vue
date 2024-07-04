<script lang="ts" setup>
import CodeEditor from "@/components/CodeEditor.vue";
import HTMLVisualEditor from "@/components/HTMLVisualEditor.vue";
import type { Project } from "@/types";
import { normalizePath } from "@/utils/path";
import { axiosInstance } from "@halo-dev/api-client";
import { Toast, VButton, VSpace } from "@halo-dev/components";
import { useEventListener, useLocalStorage } from "@vueuse/core";
import { computed, onMounted, ref, watch } from "vue";
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
  ".txt",
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

// File Content
const content = ref("");
const processing = ref(false);

async function handleFetchContent() {
  if (!SUPPORTED_EDIT_FILES.some((ext) => props.path?.endsWith(ext))) {
    return;
  }

  const { data } = await axiosInstance.get(
    `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/file-content?path=${props.path}`
  );
  content.value = data;
}

async function handleSaveContent() {
  if (!props.path) {
    return;
  }

  try {
    processing.value = true;

    await axiosInstance.put(
      `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/file-content?path=${props.path}`,
      { content: content.value }
    );

    Toast.success("保存成功");
    handleFetchContent();
  } catch (error) {
    Toast.error("保存失败");
  } finally {
    processing.value = false;
  }
}

onMounted(() => {
  handleFetchContent();
});

watch(
  () => props.path,
  (value) => {
    if (value) {
      handleFetchContent();
    }
  }
);

useEventListener("keydown", (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === "s") {
    e.preventDefault();
    handleSaveContent();
  }
});

const enableVisualEditor = useLocalStorage(
  "plugin-static-pages:enable-visual-editor",
  false
);
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
      <VButton
        v-if="path.endsWith('.html')"
        @click="enableVisualEditor = !enableVisualEditor"
      >
        {{ enableVisualEditor ? "代码编辑" : "可视化编辑（实验性）" }}
      </VButton>
      <VButton
        :loading="processing"
        type="secondary"
        @click="handleSaveContent"
      >
        保存
      </VButton>
    </VSpace>
  </div>
  <div
    v-if="!path"
    class="sp-flex sp-size-full sp-items-center sp-justify-center"
  >
    <span class="sp-text-sm sp-text-gray-900"> 当前未选择文件 </span>
  </div>
  <div v-else-if="isImageFile" class="sp-p-2">
    <img :src="fullPath" />
  </div>
  <div
    v-else-if="isEditableFile"
    style="height: calc(100vh - 8.5rem)"
    class="sp-size-full sp-overflow-auto"
  >
    <HTMLVisualEditor
      v-if="enableVisualEditor && path.endsWith('.html')"
      v-model="content"
    />
    <CodeEditor v-else v-model="content" :path="path" />
  </div>
  <div v-else class="sp-flex sp-size-full sp-items-center sp-justify-center">
    <span class="sp-text-sm sp-text-gray-900"> 当前文件不支持编辑和预览 </span>
  </div>
</template>
