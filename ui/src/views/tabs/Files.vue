<script lang="ts" setup>
import { staticPageConsoleApiClient } from '@/api';
import type { Project, ProjectFile } from '@/api/generated';
import FileIcon from '@/components/FileIcon.vue';
import FileUploadModal from '@/components/FileUploadModal.vue';
import { normalizePath } from '@/utils/path';
import { Dialog, VButton, VSpace } from '@halo-dev/components';
import { utils } from '@halo-dev/ui-shared';
import { useQuery, useQueryClient } from '@tanstack/vue-query';
import { useRouteQuery } from '@vueuse/router';
import prettyBytes from 'pretty-bytes';
import { computed, ref, toRefs } from 'vue';
import TablerArrowBackUp from '~icons/tabler/arrow-back-up';
import TablerExternalLink from '~icons/tabler/external-link';
import TablerHome from '~icons/tabler/home';

const queryClient = useQueryClient();

const props = withDefaults(defineProps<{ project: Project }>(), {});

const { project } = toRefs(props);

const selectedDir = useRouteQuery<string>('dir', '/', { mode: 'push' });

const { data } = useQuery({
  queryKey: ['plugin-static-pages:files', project.value.metadata.name, selectedDir],
  queryFn: async () => {
    const { data } = await staticPageConsoleApiClient.project.listFilesInProject({
      name: props.project.metadata.name,
      path: selectedDir.value,
    });

    return data.sort((a, b) => {
      if (a.directory && !b.directory) return -1;
      if (!a.directory && b.directory) return 1;
      return a.name!.localeCompare(b.name!);
    });
  },
});

function handleClickRow(file: ProjectFile) {
  if (file.directory) {
    if (selectedDir.value === '/') {
      selectedDir.value = `/${file.name}`;
    } else {
      selectedDir.value = `${selectedDir.value}/${file.name}`;
    }
    return;
  }
}

function handleBack() {
  const items = selectedDir.value.split('/').filter(Boolean);
  if (items.length === 1) {
    selectedDir.value = '/';
  } else {
    selectedDir.value = `/${items.slice(0, items.length - 1).join('/')}`;
  }
}

const breadcrumbItems = computed(() => {
  const items = selectedDir.value.split('/').filter(Boolean);
  return items.map((item, index) => {
    const path = items.slice(0, index + 1).join('/');
    return {
      name: item,
      path: `/${path}` || '/',
    };
  });
});

function handleOpenFile(file: ProjectFile) {
  window.open(
    normalizePath('/', project.value.spec.directory, selectedDir.value, file.name || ''),
    '_blank'
  );
}

function handleDeleteFile(file: ProjectFile) {
  Dialog.warning({
    title: `删除文件${file.directory ? '夹' : ''}：${file.name}`,
    description: `确定要删除文件${file.directory ? '夹' : ''}：${file.name}吗？此操作无法恢复。`,
    confirmType: 'danger',
    async onConfirm() {
      const path = normalizePath(selectedDir.value, file.name || '');

      await staticPageConsoleApiClient.project.deleteFileInProject({
        name: props.project.metadata.name,
        path,
      });

      queryClient.invalidateQueries([
        'plugin-static-pages:files',
        project.value.metadata.name,
        selectedDir.value,
      ]);
    },
  });
}

function handleCleanup() {
  Dialog.warning({
    title: '清空项目文件',
    description: '确定要清空所有的项目文件吗？此操作无法恢复。',
    confirmType: 'danger',
    async onConfirm() {
      await staticPageConsoleApiClient.project.deleteFileInProject({
        name: props.project.metadata.name,
        path: '/',
      });

      queryClient.invalidateQueries([
        'plugin-static-pages:files',
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
    'plugin-static-pages:files',
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

  <div class=":uno: flex items-center justify-between px-4 py-4">
    <nav aria-label="Breadcrumb">
      <ol role="list" class=":uno: flex items-center space-x-2">
        <li>
          <div>
            <span
              class=":uno: cursor-pointer text-gray-400 hover:text-gray-500"
              @click="selectedDir = '/'"
            >
              <TablerHome class=":uno: h-5 w-5 flex-shrink-0" />
            </span>
          </div>
        </li>
        <li v-for="(item, index) in breadcrumbItems" :key="index">
          <div class=":uno: flex items-center">
            <svg
              class=":uno: h-5 w-5 flex-shrink-0 text-gray-300"
              fill="currentColor"
              viewBox="0 0 20 20"
              aria-hidden="true"
            >
              <path d="M5.555 17.776l8-16 .894.448-8 16-.894-.448z" />
            </svg>
            <span
              class=":uno: ml-2 cursor-pointer text-sm text-gray-500 font-medium hover:text-gray-700"
              @click="selectedDir = item.path"
            >
              {{ item.name }}
            </span>
          </div>
        </li>
      </ol>
    </nav>

    <VSpace>
      <VButton type="secondary" @click="uploadModalVisible = true"> 上传 </VButton>
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

  <div class=":uno: rounded-b-base mt-3 flow-root overflow-hidden">
    <div class=":uno: overflow-x-auto">
      <div class=":uno: inline-block min-w-full align-middle">
        <table class=":uno: min-w-full divide-y divide-gray-300">
          <thead>
            <tr>
              <th
                scope="col"
                class=":uno: px-4 py-3.5 text-left text-sm text-gray-900 font-semibold"
              >
                文件名
              </th>
              <th
                scope="col"
                class=":uno: px-4 py-3.5 text-left text-sm text-gray-900 font-semibold"
              >
                类型
              </th>
              <th
                scope="col"
                class=":uno: px-4 py-3.5 text-left text-sm text-gray-900 font-semibold"
              >
                大小
              </th>
              <th
                scope="col"
                class=":uno: px-4 py-3.5 text-left text-sm text-gray-900 font-semibold"
              >
                修改时间
              </th>
              <th scope="col" class=":uno: relative py-3.5 pl-3 pr-4 sm:pr-3"></th>
            </tr>
          </thead>
          <tbody class=":uno: bg-white">
            <tr
              v-if="selectedDir !== '/'"
              class=":uno: group cursor-pointer select-none even:bg-gray-50 hover:bg-blue-50"
              @click="handleBack()"
            >
              <td class=":uno: whitespace-nowrap px-4 py-4 text-sm text-gray-900 font-medium">
                <div class=":uno: inline-flex items-center gap-2">
                  <TablerArrowBackUp class=":uno: text-gray-600 group-hover:text-blue-600" />
                  <span class=":uno: group-hover:text-blue-600"> .. </span>
                </div>
              </td>
              <td class=":uno: whitespace-nowrap px-4 py-4 text-sm text-gray-500">--</td>
              <td class=":uno: whitespace-nowrap px-4 py-4 text-sm text-gray-500">--</td>
              <td class=":uno: cursor-pointer whitespace-nowrap px-4 py-4 text-sm text-gray-500">
                --
              </td>
              <td
                class=":uno: relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-3"
              ></td>
            </tr>
            <tr
              v-for="file in data"
              :key="file.path"
              class=":uno: group cursor-pointer even:bg-gray-50 hover:bg-blue-50"
              @click="handleClickRow(file)"
            >
              <td class=":uno: whitespace-nowrap px-4 py-4 text-sm text-gray-900 font-medium">
                <div class=":uno: inline-flex items-center gap-2">
                  <FileIcon :type="file.type!" />
                  <span class=":uno: group-hover:text-blue-600">
                    {{ file.name }}
                  </span>
                  <TablerExternalLink
                    v-if="!file.directory"
                    class=":uno: invisible text-gray-600 group-hover:visible hover:text-gray-900"
                    @click.stop="handleOpenFile(file)"
                  />
                </div>
              </td>
              <td class=":uno: whitespace-nowrap px-4 py-4 text-sm text-gray-500">
                {{ file.type }}
              </td>
              <td class=":uno: whitespace-nowrap px-4 py-4 text-sm text-gray-500">
                {{ prettyBytes(file.size || 0) }}
              </td>
              <td class=":uno: cursor-pointer whitespace-nowrap px-4 py-4 text-sm text-gray-500">
                <span v-tooltip="utils.date.format(file.lastModifiedTime)">
                  {{ utils.date.timeAgo(file.lastModifiedTime) }}
                </span>
              </td>
              <td
                class=":uno: relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-3"
              >
                <span
                  class=":uno: invisible text-red-500 group-hover:visible hover:text-red-400"
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
