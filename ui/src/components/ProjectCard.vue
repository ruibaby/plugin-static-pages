<script lang="ts" setup>
import type { Project } from "@/types";
import { VButton, VSpace, VStatusDot } from "@halo-dev/components";

const props = withDefaults(
  defineProps<{
    project: Project;
  }>(),
  {}
);

function handleOpen() {
  window.open(`/${props.project.spec.directory}/index.html`, "_blank");
}
</script>

<template>
  <div
    class="sp-cursor-pointer sp-rounded-lg sp-bg-white sp-px-4 sp-py-3 sp-group sp-shadow sp-transition-all hover:sp-ring-1"
  >
    <div class="sp-flex sp-items-center sp-gap-4">
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
    <ul class="sp-mt-4 sp-space-y-2 sp-text-sm sp-text-gray-600">
      <li>{{ project.spec.description }}</li>
      <li class="sp-line-clamp-1">/{{ project.spec.directory }}</li>
    </ul>
    <div class="sp-mt-4 sp-flex sp-justify-end">
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
        <VButton size="sm">设置</VButton>
      </VSpace>
    </div>
  </div>
</template>
