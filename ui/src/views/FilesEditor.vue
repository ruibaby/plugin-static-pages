<script lang="ts" setup>
import type { Project } from "@/types";
import { apiClient } from "@/utils/api-client";
import {
  VAvatar,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRoute } from "vue-router";
import FilesTreeSection from "./components/FilesTreeSection.vue";
import { useRouteQuery } from "@vueuse/router";
import FileContentEditor from "./components/FileContentEditor.vue";
import { useLocalStorage } from "@vueuse/core";

const route = useRoute();

const showSidebar = useLocalStorage("plugin-static-pages:show-sidebar", true);

const { data: project } = useQuery<Project>({
  queryKey: ["plugin-static-pages:detail", route.params.name],
  queryFn: async () => {
    const { data } = await apiClient.get<Project>(
      `/apis/staticpage.halo.run/v1alpha1/projects/${route.params.name}`
    );
    return data;
  },
});

const selectedFilePath = useRouteQuery<string | undefined>("path");
</script>

<template>
  <VPageHeader :title="project?.spec.title || '加载中...'">
    <template #icon>
      <VAvatar
        :src="project?.spec.icon"
        class="sp-mr-2 sp-self-center"
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

  <div class="sp-m-0 sp-rounded sp-bg-white md:sp-m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['sp-h-full', '!sp-p-0']"
    >
      <div
        class="sp-grid sp-h-full sp-grid-cols-12 sp-divide-y sm:sp-divide-x sm:sp-divide-y-0"
        :class="{ '!sp-divide-none': !showSidebar }"
      >
        <div
          v-show="showSidebar"
          class="sp-relative sp-col-span-12 sp-h-full sp-overflow-auto sp-p-2 sm:sp-col-span-6 lg:sp-col-span-5 xl:sp-col-span-3"
        >
          <FilesTreeSection
            v-if="project"
            v-model="selectedFilePath"
            :project="project"
          />
        </div>

        <div
          class="sp-col-span-12 sm:sp-col-span-6 lg:sp-col-span-7 xl:sp-col-span-9"
          :class="{ '!sp-col-span-12': !showSidebar }"
        >
          <FileContentEditor
            v-if="project"
            :project="project"
            :path="selectedFilePath"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>
