<script lang="ts" setup>
import { ProjectStatusPhaseEnum, type Project } from '@/api/generated';
import { VAvatar, VButton, VSpace, VStatusDot } from '@halo-dev/components';
import { computed, ref } from 'vue';
import ProjectEditModal from './ProjectEditModal.vue';

const props = withDefaults(
  defineProps<{
    project: Project;
  }>(),
  {}
);

function handleOpen() {
  window.open(`/${props.project.spec.directory}`, '_blank');
}

const editModalVisible = ref(false);

const errorMessage = computed(() => {
  if (props.project.status?.phase === ProjectStatusPhaseEnum.Ready) {
    return;
  }

  const condition = props.project.status?.conditions?.[0];

  if (!condition) {
    return;
  }

  return condition.message;
});
</script>

<template>
  <ProjectEditModal v-if="editModalVisible" :project="project" @close="editModalVisible = false" />

  <div
    class="cursor-pointer rounded-lg space-y-4 bg-white flex flex-col px-4 py-3 group shadow transition-all hover:ring-1"
  >
    <div class="flex items-center gap-4 flex-none">
      <VAvatar :src="project.spec.icon" :alt="project.spec.title" size="xs" />

      <RouterLink
        :to="{
          name: 'StaticPageProjectDetail',
          params: { name: project.metadata.name },
        }"
        class="line-clamp-1 text-base font-semibold hover:text-gray-600 hover:underline"
      >
        {{ project.spec.title }}
      </RouterLink>

      <VStatusDot v-if="!!project.metadata.deletionTimestamp" animate state="warning" />

      <VStatusDot v-if="errorMessage" v-tooltip="errorMessage" animate state="warning" />
    </div>
    <ul class="space-y-2 text-sm text-gray-600 flex-1">
      <li>{{ project.spec.description }}</li>
      <li class="line-clamp-1">/{{ project.spec.directory }}</li>
    </ul>
    <div class="flex justify-end flex-none">
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
