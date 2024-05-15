<script lang="ts" setup>
import type { Project } from "@/types";
import { VButton, VSpace, VStatusDot } from "@halo-dev/components";
import { ref } from "vue";
import ProjectEditModal from "./ProjectEditModal.vue";

const props = withDefaults(
  defineProps<{
    project: Project;
  }>(),
  {}
);

function handleOpen() {
  window.open(`/${props.project.spec.directory}/index.html`, "_blank");
}

const editModalVisible = ref(false);
</script>

<template>
  <ProjectEditModal
    v-if="editModalVisible"
    :project="project"
    @close="editModalVisible = false"
  />

  <div
    class="sp-cursor-pointer sp-rounded-lg sp-space-y-4 sp-bg-white sp-flex sp-flex-col sp-px-4 sp-py-3 sp-group sp-shadow sp-transition-all hover:sp-ring-1"
  >
    <div class="sp-flex sp-items-center sp-gap-4 sp-flex-none">
      <RouterLink
        :to="{
          name: 'StaticPageProjectDetail',
          params: { name: project.metadata.name },
        }"
        class="sp-line-clamp-1 sp-text-base sp-font-semibold hover:sp-text-gray-600 hover:sp-underline"
      >
        {{ project.spec.title }}
      </RouterLink>

      <VStatusDot
        v-if="!!project.metadata.deletionTimestamp"
        animate
        state="warning"
      />
    </div>
    <ul class="sp-space-y-2 sp-text-sm sp-text-gray-600 sp-flex-1">
      <li>{{ project.spec.description }}</li>
      <li class="sp-line-clamp-1">/{{ project.spec.directory }}</li>
    </ul>
    <div class="sp-flex sp-justify-end sp-flex-none">
      <VSpace>
        <VButton
          size="sm"
          @click="
            $router.push({
              name: 'StaticPageProjectDetail',
              params: { name: project.metadata.name },
            })
          "
        >
          详情
        </VButton>
        <VButton size="sm" @click="handleOpen">访问</VButton>
        <VButton size="sm" @click="editModalVisible = true">设置</VButton>
      </VSpace>
    </div>
  </div>
</template>
