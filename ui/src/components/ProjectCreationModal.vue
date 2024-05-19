<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import type { ProjectFormState } from "@/types/form";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import axios, { type AxiosResponse } from "axios";
import ProjectForm from "./ProjectForm.vue";
import type { Project } from "@/types";
import { useRouter } from "vue-router";

const router = useRouter();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const queryClient = useQueryClient();

const { mutate, isLoading } = useMutation({
  mutationKey: ["create-project"],
  mutationFn: async ({ data }: { data: ProjectFormState }) => {
    const { data: project } = await axios.post<
      Project,
      AxiosResponse<Project>,
      Project
    >(`/apis/staticpage.halo.run/v1alpha1/projects`, {
      apiVersion: "staticpage.halo.run/v1alpha1",
      kind: "Project",
      metadata: {
        name: "",
        generateName: "project-",
      },
      spec: {
        title: data.title,
        icon: data.icon,
        directory: data.directory,
        description: data.description,
        rewrites: data.rewrites,
      },
    });

    return project;
  },
  onSuccess(data) {
    Toast.success("创建成功");
    queryClient.invalidateQueries({ queryKey: ["plugin-static-pages:list"] });
    emit("close");
    router.push({
      name: "StaticPageProjectDetail",
      params: { name: data.metadata.name },
    });
  },
  onError() {
    Toast.error("创建失败");
  },
});

function onSubmit(data: ProjectFormState) {
  mutate({ data });
}
</script>

<template>
  <VModal
    :width="600"
    title="创建新的静态网页项目"
    :centered="false"
    @close="emit('close')"
  >
    <ProjectForm @submit="onSubmit" />
    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          :loading="isLoading"
          @click="$formkit.submit('static-page-project-form')"
        >
          创建
        </VButton>
        <VButton type="default" @click="emit('close')">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
