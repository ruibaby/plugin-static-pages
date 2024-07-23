<script lang="ts" setup>
import { staticPageCoreApiClient } from '@/api';
import type { Project } from '@/api/generated';
import type { ProjectFormState } from '@/types/form';
import { Dialog, Toast, VButton, VModal, VSpace } from '@halo-dev/components';
import { useMutation, useQueryClient } from '@tanstack/vue-query';
import { ref } from 'vue';
import ProjectForm from './ProjectForm.vue';

const props = withDefaults(
  defineProps<{
    project: Project;
  }>(),
  {}
);

const emit = defineEmits<{
  (event: 'close'): void;
}>();

const queryClient = useQueryClient();

const modal = ref();

const { mutate, isLoading } = useMutation({
  mutationKey: ['plugin-static-pages:update-project'],
  mutationFn: async ({ data }: { data: ProjectFormState }) => {
    return await staticPageCoreApiClient.project.patchProject({
      name: props.project.metadata.name,
      // TODO: 类型异常
      jsonPatchInner: [
        {
          op: 'add',
          path: '/spec/title',
          value: data.title,
        },
        {
          op: 'add',
          path: '/spec/icon',
          value: data.icon || '',
        },
        {
          op: 'add',
          path: '/spec/description',
          value: data.description || '',
        },
        {
          op: 'add',
          path: '/spec/directory',
          value: data.directory,
        },
        {
          op: 'add',
          path: '/spec/rewrites',
          value: data.rewrites || [],
        },
      ],
    });
  },
  onSuccess() {
    Toast.success('保存成功');
    queryClient.invalidateQueries({ queryKey: ['plugin-static-pages:list'] });
    queryClient.invalidateQueries({ queryKey: ['plugin-static-pages:detail'] });
    modal.value.close();
  },
  retry: 3,
  onError() {
    Toast.error('保存失败，请重试');
  },
});

function onSubmit(data: ProjectFormState) {
  mutate({ data });
}

function handleDelete() {
  Dialog.warning({
    title: '删除静态网页项目',
    description: '确定要删除该静态网页项目吗？此操作无法恢复。',
    confirmType: 'danger',
    async onConfirm() {
      await staticPageCoreApiClient.project.deleteProject({ name: props.project.metadata.name });

      Toast.success('删除成功');

      modal.value.close();

      queryClient.invalidateQueries({ queryKey: ['plugin-static-pages:list'] });
    },
  });
}
</script>

<template>
  <VModal
    ref="modal"
    :width="600"
    title="编辑静态网页项目信息"
    :centered="false"
    @close="emit('close')"
  >
    <ProjectForm
      :form-state="{
        title: project.spec.title,
        icon: project.spec.icon,
        description: project.spec.description,
        directory: project.spec.directory,
        rewrites: project.spec.rewrites || [],
      }"
      @submit="onSubmit"
    />
    <template #footer>
      <div class="flex justify-between">
        <VSpace>
          <VButton
            type="secondary"
            :loading="isLoading"
            @click="$formkit.submit('static-page-project-form')"
          >
            保存
          </VButton>
          <VButton type="default" @click="modal.close()">取消</VButton>
        </VSpace>
        <VButton type="danger" @click="handleDelete">删除</VButton>
      </div>
    </template>
  </VModal>
</template>
