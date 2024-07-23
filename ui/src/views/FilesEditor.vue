<script lang="ts" setup>
import { staticPageCoreApiClient } from '@/api';
import type { Project } from '@/api/generated';
import { VAvatar, VButton, VCard, VPageHeader, VSpace } from '@halo-dev/components';
import { useQuery } from '@tanstack/vue-query';
import { useLocalStorage } from '@vueuse/core';
import { useRouteQuery } from '@vueuse/router';
import { useRoute } from 'vue-router';
import FileContentEditor from './components/FileContentEditor.vue';
import FilesTreeSection from './components/FilesTreeSection.vue';

const route = useRoute();

const showSidebar = useLocalStorage('plugin-static-pages:show-sidebar', true);

const { data: project } = useQuery<Project>({
  queryKey: ['plugin-static-pages:detail', route.params.name],
  queryFn: async () => {
    const { data } = await staticPageCoreApiClient.project.getProject({
      name: route.params.name as string,
    });
    return data;
  },
});

const selectedFilePath = useRouteQuery<string | undefined>('path');
</script>

<template>
  <VPageHeader :title="project?.spec.title || '加载中...'">
    <template #icon>
      <VAvatar
        :src="project?.spec.icon"
        class="mr-2 self-center"
        :alt="project?.spec.title"
        size="xs"
      />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" @click="$router.back()"> 返回</VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 rounded bg-white md:m-4">
    <VCard style="height: calc(100vh - 5.5rem)" :body-class="['h-full', '!p-0']">
      <div
        class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0"
        :class="{ '!divide-none': !showSidebar }"
      >
        <div
          v-show="showSidebar"
          class="relative col-span-12 h-full overflow-auto p-2 sm:col-span-6 lg:col-span-5 xl:col-span-3"
        >
          <FilesTreeSection v-if="project" v-model="selectedFilePath" :project="project" />
        </div>

        <div
          class="col-span-12 sm:col-span-6 lg:col-span-7 xl:col-span-9"
          :class="{ '!col-span-12': !showSidebar }"
        >
          <FileContentEditor v-if="project" :project="project" :path="selectedFilePath" />
        </div>
      </div>
    </VCard>
  </div>
</template>
