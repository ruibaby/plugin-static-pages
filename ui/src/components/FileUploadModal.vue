<script lang="ts" setup>
import type { Project } from '@/types';
import { VButton, VModal, VTabItem, VTabs } from '@halo-dev/components';
import { ref } from 'vue';
// @ts-ignore
import docCli from '@/docs/cli.md';

withDefaults(defineProps<{ project: Project; path?: string }>(), {
  path: '/',
});

const emit = defineEmits<{
  (event: 'close'): void;
}>();

const modal = ref();

const activeTab = ref('file');

const autoUnzip = ref(false);
</script>

<template>
  <VModal
    ref="modal"
    title="上传文件"
    :width="920"
    height="calc(100vh - 20px)"
    :centered="false"
    @close="emit('close')"
  >
    <VTabs v-model:active-id="activeTab" type="outline">
      <VTabItem id="file" label="选择文件上传">
        <FormKit
          v-model="autoUnzip"
          type="checkbox"
          label="自动解压"
          help="建议仅上传 zip 时开启"
        ></FormKit>
        <UppyUpload
          :endpoint="`/apis/console.api.staticpage.halo.run/v1alpha1/projects/${project.metadata.name}/upload`"
          :meta="{
            dir: path,
            unzip: autoUnzip ? 'true' : 'false',
          }"
          width="100%"
          :allowed-meta-fields="['unzip', 'dir']"
          :done-button-handler="() => emit('close')"
        />
      </VTabItem>
      <VTabItem id="cli" label="通过 CLI 上传">
        <docCli />
      </VTabItem>
    </VTabs>

    <template #footer>
      <VButton @click="modal.close()">关闭</VButton>
    </template>
  </VModal>
</template>
