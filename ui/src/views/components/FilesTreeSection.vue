<script lang="ts" setup>
import type { Project, ProjectFile } from "@/types";
import { apiClient } from "@/utils/api-client";
import {
  Dialog,
  IconDeleteBin,
  IconExternalLinkLine,
  IconRefreshLine,
  VLoading,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import type { Stat } from "@he-tree/tree-utils";
import { computed, h, ref, shallowRef } from "vue";
import { BaseTree } from "@he-tree/vue";
import "@he-tree/vue/style/default.css";
import RiArrowDownSLine from "~icons/ri/arrow-down-s-line";
import RiArrowRightSLine from "~icons/ri/arrow-right-s-line";
import RiFileAddLine from "~icons/ri/file-add-line";
import RiFolderAddLine from "~icons/ri/folder-add-line";
import FileIcon from "@/components/FileIcon.vue";
import { normalizePath } from "@/utils/path";
import ContextMenu from "@imengyu/vue3-context-menu";
import "@imengyu/vue3-context-menu/lib/vue3-context-menu.css";

const props = withDefaults(defineProps<{ project: Project }>(), {});

const selectedFilePath = defineModel({ type: String });

const queryClient = useQueryClient();

const {
  data: rootFiles,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: ["plugin-static-pages:files", props.project.metadata.name, "/"],
  queryFn: async () => {
    const { data } = await apiClient.get<ProjectFile[]>(
      `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/files?path=/`
    );
    return data
      .sort((a, b) => {
        if (a.directory && !b.directory) return -1;
        if (!a.directory && b.directory) return 1;
        return a.name.localeCompare(b.name);
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
    return stat.data.name;
  }
  return getFileFullPath(parent) + "/" + stat.data.name;
}

async function onNodeOpen(stat: Stat<ProjectFile>) {
  if (!stat.data.directory) {
    return;
  }

  const { data } = await apiClient.get<ProjectFile[]>(
    `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${
      props.project.metadata.name
    }/files?path=${normalizePath("/", getFileFullPath(stat))}`
  );

  const childrenMap = stat.children.map((item) => item.data.name);

  data.forEach((file) => {
    if (childrenMap.includes(file.name)) {
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

  selectedFilePath.value = normalizePath("/", getFileFullPath(stat));
}

function onContextMenu(
  e: MouseEvent,
  node: ProjectFile,
  stat: Stat<ProjectFile>
) {
  e.preventDefault();

  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    theme: "mac",
    zIndex: 999,
    items: [
      {
        label: "访问",
        icon: h(IconExternalLinkLine),
        onClick: () => {
          window.open(
            normalizePath(
              "/",
              props.project.spec.directory,
              getFileFullPath(stat)
            ),
            "_blank"
          );
        },
      },
      {
        label: "删除",
        icon: h(IconDeleteBin),
        onClick: () => {
          Dialog.warning({
            title: `删除文件${node.directory ? "夹" : ""}：${node.name}`,
            description: `确定要删除文件${node.directory ? "夹" : ""}：${
              node.name
            }吗？此操作无法恢复。`,
            confirmType: "danger",
            async onConfirm() {
              const path = normalizePath("/", getFileFullPath(stat));

              await apiClient.delete(
                `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/files?path=${path}`
              );

              queryClient.invalidateQueries([
                "plugin-static-pages:files",
                props.project.metadata.name,
                "/",
              ]);
            },
          });
        },
      },
    ],
  });
}
</script>

<template>
  <div
    class="sp-p-1 sp-flex sp-items-center sp-justify-end sp-gap-2 sp-rounded sp-bg-gray-100"
  >
    <div
      v-tooltip="'添加文件夹'"
      class="sp-cursor-pointer sp-rounded-full sp-p-1.5 sp-transition-all hover:sp-bg-gray-200"
    >
      <RiFolderAddLine class="sp-text-sm sp-text-gray-600" />
    </div>
    <div
      v-tooltip="'添加文件'"
      class="sp-cursor-pointer sp-rounded-full sp-p-1.5 sp-transition-all hover:sp-bg-gray-200"
    >
      <RiFileAddLine class="sp-text-sm sp-text-gray-600" />
    </div>
    <div
      v-tooltip="'刷新'"
      class="sp-cursor-pointer sp-rounded-full sp-p-1.5 sp-transition-all hover:sp-bg-gray-200"
      @click="refetch()"
    >
      <IconRefreshLine
        :class="{ 'sp-animate-spin': isFetching || dragging }"
        class="sp-text-sm sp-text-gray-600"
      />
    </div>
  </div>

  <VLoading v-if="isLoading" />

  <div v-else-if="!rootFiles?.length" class="sp-my-4 sp-flex sp-justify-center">
    <span class="sp-text-sm sp-text-gray-600">暂无文件</span>
  </div>

  <Transition v-else appear name="fade">
    <BaseTree
      ref="tree"
      v-model="rootFiles"
      :each-droppable="onEachDroppable"
      virtualization
      class="mt-2"
      :class="{
        'sp-pointer-events-none sp-cursor-wait sp-opacity-60': dragging,
      }"
      :node-key="getNodeKey"
      :default-open="false"
      :stat-handler="statHandler"
      @after-drop="onAfterDrop"
      @open:node="onNodeOpen"
    >
      <template #default="{ node, stat }">
        <div
          class="sp-group sp-flex sp-w-full sp-cursor-pointer sp-items-center sp-justify-between sp-gap-2 sp-rounded sp-p-1 hover:sp-bg-gray-100"
          :class="{
            'sp-bg-gray-100': false,
          }"
          @click="handleClickDocTree(node, stat)"
          @contextmenu="onContextMenu($event, node, stat)"
        >
          <div class="sp-inline-flex sp-items-center sp-gap-2">
            <FileIcon :type="node.type" class="sp-size-4" />
            <span class="sp-line-clamp-1 sp-flex-1 sp-select-none sp-text-sm">
              {{ node.name }}
            </span>
          </div>

          <div v-if="node.directory" class="sp-inline-flex sp-gap-2">
            <RiArrowRightSLine v-if="!stat.open" class="sp-size-4" />
            <RiArrowDownSLine v-else class="sp-size-4" />
          </div>
        </div>
      </template>
    </BaseTree>
  </Transition>
</template>
