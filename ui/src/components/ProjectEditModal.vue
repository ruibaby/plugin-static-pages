<script lang="ts" setup>
import { Dialog, Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import ProjectForm from "./ProjectForm.vue";
import type { ProjectFormState } from "@/types/form";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import axios, { type AxiosResponse } from "axios";
import { ref } from "vue";
import type { Project } from "@/types";

const props = withDefaults(
  defineProps<{
    project: Project;
  }>(),
  {}
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const queryClient = useQueryClient();

const modal = ref();

const { mutate, isLoading } = useMutation({
  mutationKey: ["plugin-static-pages:update-project"],
  mutationFn: async ({ data }: { data: ProjectFormState }) => {
    const { data: projectToUpdate } = await axios.get(
      `/apis/staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}`
    );

    projectToUpdate.spec = {
      ...projectToUpdate.spec,
      ...data,
    };

    return await axios.put<Project, AxiosResponse<Project>, Project>(
      `/apis/staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}`,
      projectToUpdate
    );
  },
  onSuccess() {
    Toast.success("保存成功");
    queryClient.invalidateQueries({ queryKey: ["plugin-static-pages:list"] });
    queryClient.invalidateQueries({ queryKey: ["plugin-static-pages:detail"] });
    modal.value.close();
  },
  retry: 3,
  onError() {
    Toast.error("保存失败，请重试");
  },
});

function onSubmit(data: ProjectFormState) {
  mutate({ data });
}

function handleDelete() {
  Dialog.warning({
    title: "删除静态网页项目",
    description: "确定要删除该静态网页项目吗？此操作无法恢复。",
    confirmType: "danger",
    async onConfirm() {
      await axios.delete(
        `/apis/staticpage.halo.run/v1alpha1/projects/${props.project.metadata.name}`
      );

      Toast.success("删除成功");

      modal.value.close();

      queryClient.invalidateQueries({ queryKey: ["plugin-static-pages:list"] });
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
      <div class="sp-flex sp-justify-between">
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
