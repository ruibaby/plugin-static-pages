<script lang="ts" setup>
import { staticPageCoreApiClient } from '@/api';
import type { Project } from '@/api/generated';
import ProjectEditModal from '@/components/ProjectEditModal.vue';
import {
  IconSettings,
  VAvatar,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
  VSpace,
  VTabbar,
} from '@halo-dev/components';
import { useQuery } from '@tanstack/vue-query';
import { useRouteQuery } from '@vueuse/router';
import { markRaw, ref, type Component, type Raw } from 'vue';
import { useRoute } from 'vue-router';
import Detail from './tabs/Detail.vue';
import Files from './tabs/Files.vue';

interface Tab {
  id: string;
  label: string;
  component: Raw<Component>;
}

const route = useRoute();

const { data: project, isLoading } = useQuery<Project>({
  queryKey: ['plugin-static-pages:detail', route.params.name],
  queryFn: async () => {
    const { data } = await staticPageCoreApiClient.project.getProject({
      name: route.params.name as string,
    });
    return data;
  },
});

const tabs: Tab[] = [
  {
    id: 'detail',
    label: '详情',
    component: markRaw(Detail),
  },
  {
    id: 'files',
    label: '文件管理',
    component: markRaw(Files),
  },
];

const activeTab = useRouteQuery('tab', tabs[0].id);

function handleOpen() {
  window.open(`/${project.value?.spec.directory}`, '_blank');
}

const editModalVisible = ref(false);
</script>

<template>
  <ProjectEditModal
    v-if="editModalVisible && project"
    :project="project"
    @close="editModalVisible = false"
  />
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
        <VButton size="sm" @click="handleOpen"> 访问</VButton>
        <VButton @click="editModalVisible = true">
          <template #icon>
            <IconSettings class="size-full" />
          </template>
          设置
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <VLoading v-if="isLoading" />

  <div v-else class="m-0 md:m-4">
    <VCard :body-class="['!p-0', '!overflow-visible']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="rounded-b-base bg-white">
        <template v-for="tab in tabs" :key="tab.id">
          <component :is="tab.component" v-if="activeTab === tab.id" :project="project" />
        </template>
      </div>
    </VCard>
  </div>
</template>
