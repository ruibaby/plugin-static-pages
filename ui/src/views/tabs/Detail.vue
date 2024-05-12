<script lang="ts" setup>
import type { Project } from "@/types";
import { VDescriptionItem, VDescription } from "@halo-dev/components";

withDefaults(defineProps<{ project: Project }>(), {});
</script>

<template>
  <div class="sp-overflow-hidden rounded-b-base">
    <VDescription>
      <VDescriptionItem
        label="ID"
        :content="project.metadata.name"
      ></VDescriptionItem>
      <VDescriptionItem
        label="名称"
        :content="project.spec.title"
      ></VDescriptionItem>
      <VDescriptionItem
        label="描述"
        :content="project.spec.description"
      ></VDescriptionItem>
      <VDescriptionItem
        label="目录"
        :content="project.spec.directory"
      ></VDescriptionItem>
    </VDescription>
  </div>
</template>
