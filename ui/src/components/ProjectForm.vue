<script lang="ts" setup>
import type { ProjectFormState } from "@/types/form";

withDefaults(
  defineProps<{
    formState?: ProjectFormState;
  }>(),
  {
    formState: undefined,
  }
);

const emit = defineEmits<{
  (event: "submit", data: ProjectFormState): void;
}>();

function onSubmit(data: ProjectFormState) {
  emit("submit", data);
}
</script>

<template>
  <FormKit
    id="static-page-project-form"
    type="form"
    name="static-page-project-form"
    :config="{ validationVisibility: 'submit' }"
    @submit="onSubmit"
  >
    <FormKit
      type="text"
      name="title"
      :model-value="formState?.title"
      label="名称"
      validation="required"
    ></FormKit>
    <FormKit
      type="text"
      name="directory"
      :model-value="formState?.directory"
      label="目录"
      help="/{目录}"
      validation="required:trim|matches:/^\S(.*\S)?$/"
      :validation-messages="{
        matches: '不能以空格开头或结尾',
      }"
    ></FormKit>
    <FormKit
      type="textarea"
      name="description"
      :model-value="formState?.description"
      label="描述"
    ></FormKit>
    <FormKit
      type="repeater"
      :value="formState?.rewrites"
      name="rewrites"
      label="重写规则"
    >
      <FormKit
        type="text"
        name="source"
        label="源"
        validation="required"
      ></FormKit>
      <FormKit
        type="text"
        name="target"
        label="目标"
        validation="required"
      ></FormKit>
    </FormKit>
  </FormKit>
</template>
