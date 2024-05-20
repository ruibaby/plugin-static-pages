<script lang="ts" setup>
import type { Project, ProjectFile } from "@/types";
import { apiClient } from "@/utils/api-client";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref, toRefs } from "vue";
import prettyBytes from "pretty-bytes";
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import FileIcon from "@/components/FileIcon.vue";
import TablerHome from "~icons/tabler/home";
import TablerArrowBackUp from "~icons/tabler/arrow-back-up";
import { useRouteQuery } from "@vueuse/router";
import { Dialog, VButton, VSpace } from "@halo-dev/components";
import { normalizePath } from "@/utils/path";
import TablerExternalLink from "~icons/tabler/external-link";
import FileUploadModal from "@/components/FileUploadModal.vue";

const queryClient = useQueryClient();

const props = withDefaults(defineProps<{ project: Project }>(), {});

const { project } = toRefs(props);

const selectedDir = useRouteQuery<string>("dir", "/", { mode: "push" });

const { data } = useQuery({
  queryKey: [
    "plugin-static-pages:files",
    project.value.metadata.name,
    selectedDir,
  ],
  queryFn: async () => {
    const { data } = await apiClient.get<ProjectFile[]>(
      `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/files?path=${selectedDir.value}`
    );
    return data.sort((a, b) => {
      if (a.directory && !b.directory) return -1;
      if (!a.directory && b.directory) return 1;
      return a.name.localeCompare(b.name);
    });
  },
});

function handleClickRow(file: ProjectFile) {
  if (file.directory) {
    if (selectedDir.value === "/") {
      selectedDir.value = `/${file.name}`;
    } else {
      selectedDir.value = `${selectedDir.value}/${file.name}`;
    }
    return;
  }
}

function handleBack() {
  const items = selectedDir.value.split("/").filter(Boolean);
  if (items.length === 1) {
    selectedDir.value = "/";
  } else {
    selectedDir.value = `/${items.slice(0, items.length - 1).join("/")}`;
  }
}

const breadcrumbItems = computed(() => {
  const items = selectedDir.value.split("/").filter(Boolean);
  return items.map((item, index) => {
    const path = items.slice(0, index + 1).join("/");
    return {
      name: item,
      path: `/${path}` || "/",
    };
  });
});

function handleOpenFile(file: ProjectFile) {
  window.open(
    normalizePath(
      "/",
      project.value.spec.directory,
      selectedDir.value,
      file.name
    ),
    "_blank"
  );
}

function handleDeleteFile(file: ProjectFile) {
  Dialog.warning({
    title: `删除文件${file.directory ? "夹" : ""}：${file.name}`,
    description: `确定要删除文件${file.directory ? "夹" : ""}：${
      file.name
    }吗？此操作无法恢复。`,
    confirmType: "danger",
    async onConfirm() {
      const path = normalizePath(selectedDir.value, file.name);

      await apiClient.delete(
        `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/files?path=${path}`
      );

      queryClient.invalidateQueries([
        "plugin-static-pages:files",
        project.value.metadata.name,
        selectedDir.value,
      ]);
    },
  });
}

function handleCleanup() {
  Dialog.warning({
    title: "清空项目文件",
    description: "确定要清空所有的项目文件吗？此操作无法恢复。",
    confirmType: "danger",
    async onConfirm() {
      await apiClient.delete(
        `/apis/console.api.staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}/files?path=/`
      );

      queryClient.invalidateQueries([
        "plugin-static-pages:files",
        project.value.metadata.name,
        selectedDir.value,
      ]);
    },
  });
}

// Upload
const uploadModalVisible = ref(false);

function onUploadModalClose() {
  uploadModalVisible.value = false;
  queryClient.invalidateQueries([
    "plugin-static-pages:files",
    project.value.metadata.name,
    selectedDir.value,
  ]);
}
</script>

<template>
  <FileUploadModal
    v-if="uploadModalVisible"
    :project="project"
    :path="selectedDir"
    @close="onUploadModalClose"
  />

  <div class="sp-flex sp-px-4 sp-py-4 sp-justify-between sp-items-center">
    <nav aria-label="Breadcrumb">
      <ol role="list" class="sp-flex sp-items-center sp-space-x-2">
        <li>
          <div>
            <span
              class="sp-text-gray-400 hover:sp-text-gray-500 sp-cursor-pointer"
              @click="selectedDir = '/'"
            >
              <TablerHome class="sp-h-5 sp-w-5 sp-flex-shrink-0" />
            </span>
          </div>
        </li>
        <li v-for="(item, index) in breadcrumbItems" :key="index">
          <div class="sp-flex sp-items-center">
            <svg
              class="sp-h-5 sp-w-5 sp-flex-shrink-0 sp-text-gray-300"
              fill="currentColor"
              viewBox="0 0 20 20"
              aria-hidden="true"
            >
              <path d="M5.555 17.776l8-16 .894.448-8 16-.894-.448z" />
            </svg>
            <span
              class="sp-ml-2 sp-cursor-pointer sp-text-sm sp-font-medium sp-text-gray-500 hover:sp-text-gray-700"
              @click="selectedDir = item.path"
            >
              {{ item.name }}
            </span>
          </div>
        </li>
      </ol>
    </nav>

    <VSpace>
      <VButton type="secondary" @click="uploadModalVisible = true">
        上传
      </VButton>
      <VButton
        type="default"
        @click="
          $router.push({
            name: 'StaticPageFilesEditor',
            params: { name: project.metadata.name },
          })
        "
      >
        编辑
      </VButton>
      <VButton type="danger" @click="handleCleanup">清空</VButton>
    </VSpace>
  </div>

  <div class="sp-mt-3 sp-flow-root sp-overflow-hidden rounded-b-base">
    <div class="sp-overflow-x-auto">
      <div class="sp-inline-block sp-min-w-full sp-align-middle">
        <table class="sp-min-w-full sp-divide-y sp-divide-gray-300">
          <thead>
            <tr>
              <th
                scope="col"
                class="sp-px-4 sp-py-3.5 sp-text-left sp-text-sm sp-font-semibold sp-text-gray-900"
              >
                文件名
              </th>
              <th
                scope="col"
                class="sp-px-4 sp-py-3.5 sp-text-left sp-text-sm sp-font-semibold sp-text-gray-900"
              >
                类型
              </th>
              <th
                scope="col"
                class="sp-px-4 sp-py-3.5 sp-text-left sp-text-sm sp-font-semibold sp-text-gray-900"
              >
                大小
              </th>
              <th
                scope="col"
                class="sp-px-4 sp-py-3.5 sp-text-left sp-text-sm sp-font-semibold sp-text-gray-900"
              >
                修改时间
              </th>
              <th
                scope="col"
                class="sp-relative sp-py-3.5 sp-pl-3 sp-pr-4 sm:sp-pr-3"
              ></th>
            </tr>
          </thead>
          <tbody class="sp-bg-white">
            <tr
              v-if="selectedDir !== '/'"
              class="even:sp-bg-gray-50 hover:sp-bg-blue-50 sp-cursor-pointer sp-group sp-select-none"
              @click="handleBack()"
            >
              <td
                class="sp-whitespace-nowrap sp-py-4 sp-px-4 sp-text-sm sp-font-medium sp-text-gray-900"
              >
                <div class="sp-inline-flex sp-items-center sp-gap-2">
                  <TablerArrowBackUp
                    class="sp-text-gray-600 group-hover:sp-text-blue-600"
                  />
                  <span class="group-hover:sp-text-blue-600"> .. </span>
                </div>
              </td>
              <td
                class="sp-whitespace-nowrap sp-px-4 sp-py-4 sp-text-sm sp-text-gray-500"
              >
                --
              </td>
              <td
                class="sp-whitespace-nowrap sp-px-4 sp-py-4 sp-text-sm sp-text-gray-500"
              >
                --
              </td>
              <td
                class="sp-whitespace-nowrap sp-px-4 sp-py-4 sp-text-sm sp-text-gray-500 sp-cursor-pointer"
              >
                --
              </td>
              <td
                class="sp-relative sp-whitespace-nowrap sp-py-4 sp-pl-3 sp-pr-4 sp-text-right sp-text-sm sp-font-medium sm:sp-pr-3"
              ></td>
            </tr>
            <tr
              v-for="file in data"
              :key="file.path"
              class="even:sp-bg-gray-50 hover:sp-bg-blue-50 sp-cursor-pointer sp-group"
              @click="handleClickRow(file)"
            >
              <td
                class="sp-whitespace-nowrap sp-py-4 sp-px-4 sp-text-sm sp-font-medium sp-text-gray-900"
              >
                <div class="sp-inline-flex sp-items-center sp-gap-2">
                  <FileIcon :type="file.type" />
                  <span class="group-hover:sp-text-blue-600">
                    {{ file.name }}
                  </span>
                  <TablerExternalLink
                    v-if="!file.directory"
                    class="sp-invisible group-hover:sp-visible sp-text-gray-600 hover:sp-text-gray-900"
                    @click.stop="handleOpenFile(file)"
                  />
                </div>
              </td>
              <td
                class="sp-whitespace-nowrap sp-px-4 sp-py-4 sp-text-sm sp-text-gray-500"
              >
                {{ file.type }}
              </td>
              <td
                class="sp-whitespace-nowrap sp-px-4 sp-py-4 sp-text-sm sp-text-gray-500"
              >
                {{ prettyBytes(file.size) }}
              </td>
              <td
                class="sp-whitespace-nowrap sp-px-4 sp-py-4 sp-text-sm sp-text-gray-500 sp-cursor-pointer"
              >
                <span v-tooltip="formatDatetime(file.lastModifiedTime)">
                  {{ relativeTimeTo(file.lastModifiedTime) }}
                </span>
              </td>
              <td
                class="sp-relative sp-whitespace-nowrap sp-py-4 sp-pl-3 sp-pr-4 sp-text-right sp-text-sm sp-font-medium sm:sp-pr-3"
              >
                <span
                  class="sp-text-red-500 hover:sp-text-red-400 group-hover:sp-visible sp-invisible"
                  @click.stop="handleDeleteFile(file)"
                >
                  删除
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
