<script lang="ts" setup>
import { staticPageConsoleApiClient } from '@/api';
import type { Project, ProjectFile } from '@/api/generated';
import FileIcon from '@/components/FileIcon.vue';
import { normalizePath } from '@/utils/path';
import {
  Dialog,
  IconDeleteBin,
  IconExternalLinkLine,
  IconRefreshLine,
  VLoading,
} from '@halo-dev/components';
import type { Stat } from '@he-tree/tree-utils';
import { BaseTree } from '@he-tree/vue';
import '@he-tree/vue/style/default.css';
import ContextMenu from '@imengyu/vue3-context-menu';
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css';
import { useQuery, useQueryClient } from '@tanstack/vue-query';
import { computed, h, ref, shallowRef } from 'vue';
import RiArrowDownSLine from '~icons/ri/arrow-down-s-line';
import RiArrowRightSLine from '~icons/ri/arrow-right-s-line';
import RiFileAddLine from '~icons/ri/file-add-line';
import RiFolderAddLine from '~icons/ri/folder-add-line';
import FileCreationModal from './FileCreationModal.vue';

const props = withDefaults(defineProps<{ project: Project }>(), {});

const selectedFilePath = defineModel({ type: String });

const queryClient = useQueryClient();

const {
  data: rootFiles,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: ['plugin-static-pages:files', props.project.metadata.name, '/'],
  queryFn: async () => {
    const { data } = await staticPageConsoleApiClient.project.listFilesInProject({
      name: props.project.metadata.name,
      path: '/',
    });

    return data
      .sort((a, b) => {
        if (a.directory && !b.directory) return -1;
        if (!a.directory && b.directory) return 1;
        return a.name!.localeCompare(b.name!);
      })
      .map((item) => {
        return { ...item, children: [] };
      });
  },
  enabled: computed(() => !!props.project),
});

const tree = shallowRef<InstanceType<typeof BaseTree> | null>(null);
const dragging = ref(false);

function onEachDroppable(stat: Stat<ProjectFile>) {
  return true;
}

function getNodeKey(stat: Stat<ProjectFile>, index: number) {
  return stat.data?.path || index;
}

function statHandler(stat: Stat<ProjectFile>) {
  return stat;
}

async function onAfterDrop() {}

function getFileFullPath(stat: Stat<ProjectFile>): string {
  const parent = stat.parent;
  if (!parent) {
    return stat.data.name!;
  }
  return getFileFullPath(parent) + '/' + stat.data.name;
}

async function onNodeOpen(stat: Stat<ProjectFile>) {
  if (!stat.data.directory) {
    return;
  }

  const { data } = await staticPageConsoleApiClient.project.listFilesInProject({
    name: props.project.metadata.name,
    path: normalizePath('/', getFileFullPath(stat)),
  });

  const childrenMap = stat.children.map((item) => item.data.name);

  data.forEach((file) => {
    if (childrenMap.includes(file.name!)) {
      return;
    }

    tree.value?.processor.add(file, stat);
  });
}

function handleClickDocTree(node: ProjectFile, stat: Stat<ProjectFile>) {
  if (node.directory) {
    stat.open = !stat.open;
    return;
  }

  selectedFilePath.value = normalizePath('/', getFileFullPath(stat));
}

function onContextMenu(e: MouseEvent, node: ProjectFile, stat: Stat<ProjectFile>) {
  e.preventDefault();

  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    theme: 'mac',
    zIndex: 999,
    items: [
      {
        label: '创建文件夹',
        icon: h(RiFolderAddLine),
        hidden: !stat.data.directory,
        onClick: () => {
          handleOpenFileCreationModal(true, getFileFullPath(stat));
        },
      },
      {
        label: '创建文件',
        icon: h(RiFileAddLine),
        hidden: !stat.data.directory,
        onClick: () => {
          handleOpenFileCreationModal(false, getFileFullPath(stat));
        },
      },
      {
        label: '访问',
        icon: h(IconExternalLinkLine),
        onClick: () => {
          window.open(
            normalizePath('/', props.project.spec.directory, getFileFullPath(stat)),
            '_blank'
          );
        },
      },
      {
        label: '删除',
        icon: h(IconDeleteBin),
        onClick: () => {
          Dialog.warning({
            title: `删除文件${node.directory ? '夹' : ''}：${node.name}`,
            description: `确定要删除文件${node.directory ? '夹' : ''}：${
              node.name
            }吗？此操作无法恢复。`,
            confirmType: 'danger',
            async onConfirm() {
              const path = normalizePath('/', getFileFullPath(stat));

              await staticPageConsoleApiClient.project.deleteFileInProject({
                name: props.project.metadata.name,
                path,
              });

              queryClient.invalidateQueries([
                'plugin-static-pages:files',
                props.project.metadata.name,
                '/',
              ]);
            },
          });
        },
      },
    ],
  });
}

// File creation

const fileCreationModalVisible = ref(false);
const isCreateDir = ref(false);
const creationBaseDir = ref('/');

function handleOpenFileCreationModal(isDir: boolean, baseDir: string) {
  isCreateDir.value = isDir;
  creationBaseDir.value = baseDir;
  fileCreationModalVisible.value = true;
}
</script>

<template>
  <FileCreationModal
    v-if="fileCreationModalVisible"
    :is-dir="isCreateDir"
    :base-dir="creationBaseDir"
    :project="project"
    @close="fileCreationModalVisible = false"
  />

  <div class="p-1 flex items-center justify-end gap-2 rounded bg-gray-100">
    <div
      v-tooltip="'添加文件夹'"
      class="cursor-pointer rounded-full p-1.5 transition-all hover:bg-gray-200"
      @click="handleOpenFileCreationModal(true, '/')"
    >
      <RiFolderAddLine class="text-sm text-gray-600" />
    </div>
    <div
      v-tooltip="'添加文件'"
      class="cursor-pointer rounded-full p-1.5 transition-all hover:bg-gray-200"
      @click="handleOpenFileCreationModal(false, '/')"
    >
      <RiFileAddLine class="text-sm text-gray-600" />
    </div>
    <div
      v-tooltip="'刷新'"
      class="cursor-pointer rounded-full p-1.5 transition-all hover:bg-gray-200"
      @click="refetch()"
    >
      <IconRefreshLine
        :class="{ 'animate-spin': isFetching || dragging }"
        class="text-sm text-gray-600"
      />
    </div>
  </div>

  <VLoading v-if="isLoading" />

  <div v-else-if="!rootFiles?.length" class="my-4 flex justify-center">
    <span class="text-sm text-gray-600">暂无文件</span>
  </div>

  <Transition v-else appear name="fade">
    <BaseTree
      ref="tree"
      v-model="rootFiles"
      :each-droppable="onEachDroppable"
      virtualization
      class="mt-2"
      :class="{
        'pointer-events-none cursor-wait opacity-60': dragging,
      }"
      :node-key="getNodeKey"
      :default-open="false"
      :stat-handler="statHandler"
      @after-drop="onAfterDrop"
      @open:node="onNodeOpen"
    >
      <template #default="{ node, stat }">
        <div
          class="group flex w-full cursor-pointer items-center justify-between gap-2 rounded p-1 hover:bg-gray-100"
          :class="{
            'bg-gray-100': selectedFilePath === normalizePath('/', getFileFullPath(stat)),
          }"
          @click="handleClickDocTree(node, stat)"
          @contextmenu="onContextMenu($event, node, stat)"
        >
          <div class="inline-flex items-center gap-2">
            <FileIcon :type="node.type" class="size-4" />
            <span class="line-clamp-1 flex-1 select-none text-sm">
              {{ node.name }}
            </span>
          </div>

          <div v-if="node.directory" class="inline-flex gap-2">
            <RiArrowRightSLine v-if="!stat.open" class="size-4" />
            <RiArrowDownSLine v-else class="size-4" />
          </div>
        </div>
      </template>
    </BaseTree>
  </Transition>
</template>
