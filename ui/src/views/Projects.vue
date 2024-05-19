<script lang="ts" setup>
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { IconAddCircle, VLoading, VPageHeader } from "@halo-dev/components";
import CarbonWebServicesContainer from "~icons/carbon/web-services-container";
import type { ListResponse, Project } from "@/types";
import ProjectCard from "@/components/ProjectCard.vue";
import { ref } from "vue";
import ProjectCreationModal from "@/components/ProjectCreationModal.vue";

const { data, isLoading } = useQuery({
  queryKey: ["plugin-static-pages:list"],
  queryFn: async () => {
    const { data } = await apiClient.get<ListResponse<Project>>(
      "/apis/staticpage.halo.run/v1alpha1/projects"
    );
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.items.some(
      (project) => !!project.metadata.deletionTimestamp
    );

    return hasDeletingData ? 1000 : false;
  },
});

const creationModalVisible = ref(false);
</script>

<template>
  <ProjectCreationModal
    v-if="creationModalVisible"
    @close="creationModalVisible = false"
  />

  <VPageHeader title="静态网页服务">
    <template #icon>
      <CarbonWebServicesContainer class="sp-mr-2 sp-self-center" />
    </template>
  </VPageHeader>

  <div class="sp-m-2 md:sp-m-4">
    <VLoading v-if="isLoading" />
    <Transition v-else appear name="fade">
      <div
        class="sp-grid sp-grid-cols-1 sp-gap-3 sm:sp-grid-cols-2 lg:sp-grid-cols-3 xl:sp-grid-cols-4 2xl:sp-grid-cols-5"
      >
        <ProjectCard
          v-for="project in data?.items"
          :key="project.metadata.name"
          :project="project"
        />

        <div
          class="sp-group sp-flex sp-min-h-[10rem] sp-cursor-pointer sp-flex-col sp-items-center sp-justify-center sp-space-y-2 sp-rounded-lg sp-border sp-border-dashed sp-bg-white sp-px-4 sp-py-3 sp-shadow sp-transition-all hover:sp-border-solid hover:sp-border-indigo-300"
          @click="creationModalVisible = true"
        >
          <div
            class="sp-inline-flex sp-rounded-full sp-bg-indigo-100 sp-p-2 sp-transition-all group-hover:sp-p-2.5"
          >
            <IconAddCircle
              class="sp-text-xl sp-text-indigo-500 group-hover:sp-text-indigo-700"
            />
          </div>
          <span
            class="sp-text-sm sp-text-gray-600 group-hover:sp-text-indigo-600"
          >
            新建项目
          </span>
        </div>
      </div>
    </Transition>
  </div>
</template>
