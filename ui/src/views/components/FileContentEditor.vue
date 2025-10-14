<script lang="ts" setup>
import { staticPageConsoleApiClient } from '@/api';
import type { Project } from '@/api/generated';
import { normalizePath } from '@/utils/path';
import { Toast, VButton, VLoading, VSpace } from '@halo-dev/components';
import { useEventListener, useLocalStorage } from '@vueuse/core';
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import RiMenuFoldLine from '~icons/ri/menu-fold-line';
import RiMenuUnfoldLine from '~icons/ri/menu-unfold-line';

const HTMLVisualEditor = defineAsyncComponent({
  loader: () => import('@/components/HTMLVisualEditor.vue'),
  loadingComponent: VLoading,
});
const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/CodeEditor.vue'),
  loadingComponent: VLoading,
});

const SUPPORTED_EDIT_FILES = [
  '.md',
  '.html',
  '.htm',
  '.css',
  '.js',
  '.json',
  '.svg',
  '.xml',
  '.txt',
];

const IMAGE_FILES = ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.svg'];

const props = withDefaults(defineProps<{ project: Project; path?: string }>(), {
  path: undefined,
});

const showSidebar = useLocalStorage('plugin-static-pages:show-sidebar', true);

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
    return '';
  }
  return normalizePath('/', props.project.spec.directory, props.path);
});

// File Content
const content = ref('');
const processing = ref(false);

async function handleFetchContent() {
  if (!SUPPORTED_EDIT_FILES.some((ext) => props.path?.endsWith(ext))) {
    return;
  }

  const { data } = await staticPageConsoleApiClient.project.getFileContent({
    name: props.project.metadata.name,
    path: props.path,
  });

  // Fix monaco editor
  if (props.path?.endsWith('.json')) {
    content.value = JSON.stringify(data, null, 2);
  } else {
    content.value = data;
  }
}

async function handleSaveContent() {
  if (!props.path) {
    return;
  }

  try {
    processing.value = true;

    await staticPageConsoleApiClient.project.writeContentToFile({
      name: props.project.metadata.name,
      path: props.path,
      writeContentRequest: {
        content: content.value,
      },
    });

    Toast.success('保存成功');
    handleFetchContent();
  } catch (error) {
    Toast.error('保存失败');
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

useEventListener('keydown', (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault();
    handleSaveContent();
  }
});

const enableVisualEditor = useLocalStorage('plugin-static-pages:enable-visual-editor', false);
</script>

<template>
  <div class=":uno: h-12 flex items-center justify-between border-b px-2">
    <VSpace>
      <div
        class=":uno: inline-flex cursor-pointer items-center justify-center rounded p-1.5 transition-all hover:bg-gray-100"
        :class="{ ':uno: bg-gray-100': !showSidebar }"
        @click="showSidebar = !showSidebar"
      >
        <RiMenuFoldLine v-if="showSidebar" />
        <RiMenuUnfoldLine v-else />
      </div>
      <span v-if="path" class=":uno: text-sm text-gray-900 font-semibold">
        {{ path }}
      </span>
    </VSpace>
    <VSpace v-if="path && isEditableFile">
      <VButton v-if="path.endsWith('.html')" @click="enableVisualEditor = !enableVisualEditor">
        {{ enableVisualEditor ? '代码编辑' : '可视化编辑（实验性）' }}
      </VButton>
      <VButton :loading="processing" type="secondary" @click="handleSaveContent"> 保存 </VButton>
    </VSpace>
  </div>
  <div v-if="!path" class=":uno: size-full flex items-center justify-center">
    <span class=":uno: text-sm text-gray-900"> 当前未选择文件 </span>
  </div>
  <div v-else-if="isImageFile" class=":uno: p-2">
    <img :src="fullPath" />
  </div>
  <div
    v-else-if="isEditableFile"
    style="height: calc(100vh - 8.5rem)"
    class=":uno: size-full overflow-auto"
  >
    <HTMLVisualEditor v-if="enableVisualEditor && path.endsWith('.html')" v-model="content" />
    <CodeEditor v-else v-model="content" :path="path" />
  </div>
  <div v-else class=":uno: size-full flex items-center justify-center">
    <span class=":uno: text-sm text-gray-900"> 当前文件不支持编辑和预览 </span>
  </div>
</template>
