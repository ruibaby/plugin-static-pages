<script lang="ts" setup>
import { staticPageCoreApiClient } from '@/api';
import ProjectCard from '@/components/ProjectCard.vue';
import ProjectCreationModal from '@/components/ProjectCreationModal.vue';
import { IconAddCircle, VLoading, VPageHeader } from '@halo-dev/components';
import { useQuery } from '@tanstack/vue-query';
import { ref } from 'vue';
import CarbonWebServicesContainer from '~icons/carbon/web-services-container';

const { data, isLoading } = useQuery({
  queryKey: ['plugin-static-pages:list'],
  queryFn: async () => {
    const { data } = await staticPageCoreApiClient.project.listProject();
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.items.some((project) => !!project.metadata.deletionTimestamp);

    return hasDeletingData ? 1000 : false;
  },
});

const creationModalVisible = ref(false);
</script>

<template>
  <ProjectCreationModal v-if="creationModalVisible" @close="creationModalVisible = false" />

  <VPageHeader title="静态网页服务">
    <template #icon>
      <CarbonWebServicesContainer class="mr-2 self-center" />
    </template>
  </VPageHeader>

  <div class="m-2 md:m-4">
    <VLoading v-if="isLoading" />
    <Transition v-else appear name="fade">
      <div
        class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5"
      >
        <ProjectCard
          v-for="project in data?.items"
          :key="project.metadata.name"
          :project="project"
        />

        <div
          class="group flex min-h-[10rem] cursor-pointer flex-col items-center justify-center space-y-2 rounded-lg border border-dashed bg-white px-4 py-3 shadow transition-all hover:border-solid hover:border-indigo-300"
          @click="creationModalVisible = true"
        >
          <div class="inline-flex rounded-full bg-indigo-100 p-2 transition-all group-hover:p-2.5">
            <IconAddCircle class="text-xl text-indigo-500 group-hover:text-indigo-700" />
          </div>
          <span class="text-sm text-gray-600 group-hover:text-indigo-600"> 新建项目 </span>
        </div>
      </div>
    </Transition>
  </div>
</template>
