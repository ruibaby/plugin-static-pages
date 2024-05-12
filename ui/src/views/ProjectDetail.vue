<script lang="ts" setup>
import type { Project } from "@/types";
import { apiClient } from "@/utils/api-client";
import {
  IconSettings,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { markRaw, type Component, type Raw } from "vue";
import { useRoute } from "vue-router";
import CarbonWebServicesContainer from "~icons/carbon/web-services-container";
import Detail from "./tabs/Detail.vue";
import Files from "./tabs/Files.vue";
import { useRouteQuery } from "@vueuse/router";

interface Tab {
  id: string;
  label: string;
  component: Raw<Component>;
}

const route = useRoute();

const { data, isLoading } = useQuery<Project>({
  queryKey: ["plugin-static-pages:detail", route.params.name],
  queryFn: async () => {
    const { data } = await apiClient.get<Project>(
      `/apis/staticpage.halo.run/v1alpha1/projects/${route.params.name}`
    );
    return data;
  },
});

const tabs: Tab[] = [
  {
    id: "detail",
    label: "详情",
    component: markRaw(Detail),
  },
  {
    id: "files",
    label: "文件管理",
    component: markRaw(Files),
  },
];

const activeTab = useRouteQuery("tab", tabs[0].id);

function handleOpen() {
  window.open(`/${data.value?.spec.directory}/index.html`, "_blank");
}
</script>

<template>
  <VPageHeader :title="data?.spec.title || '加载中...'">
    <template #icon>
      <CarbonWebServicesContainer class="sp-mr-2 sp-self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" @click="handleOpen"> 访问 </VButton>
        <VButton>
          <template #icon>
            <IconSettings class="sp-h-full sp-w-full" />
          </template>
          设置
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <VLoading v-if="isLoading" />

  <div v-else class="sp-m-0 md:sp-m-4">
    <VCard :body-class="['!sp-p-0', '!sp-overflow-visible']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          class="sp-w-full !sp-rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="rounded-b-base sp-bg-white">
        <template v-for="tab in tabs" :key="tab.id">
          <component
            :is="tab.component"
            v-if="activeTab === tab.id"
            :project="data"
          />
        </template>
      </div>
    </VCard>
  </div>
</template>
