import { definePlugin } from '@halo-dev/ui-shared';
import 'uno.css';
import { markRaw } from 'vue';
import CarbonWebServicesContainer from '~icons/carbon/web-services-container';
import './styles/main.css';

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'ToolsRoot',
      route: {
        path: 'static-pages',
        name: 'StaticPageProjects',
        component: () => import('./views/Projects.vue'),
        meta: {
          title: '静态网页服务',
          description: '提供静态网页部署服务',
          searchable: true,
          permissions: ['*'],
          menu: {
            name: '静态网页服务',
            icon: markRaw(CarbonWebServicesContainer),
            priority: 0,
          },
        },
      },
    },
    {
      parentName: 'ToolsRoot',
      route: {
        path: 'static-pages/:name',
        name: 'StaticPageProjectDetail',
        component: () => import('./views/ProjectDetail.vue'),
        meta: {
          title: '静态网页详情',
          searchable: false,
          permissions: ['*'],
        },
      },
    },
    {
      parentName: 'ToolsRoot',
      route: {
        path: 'static-pages/:name/files-editor',
        name: 'StaticPageFilesEditor',
        component: () => import('./views/FilesEditor.vue'),
        meta: {
          title: '文件编辑',
          searchable: false,
          permissions: ['*'],
          hideFooter: true,
        },
      },
    },
  ],
  extensionPoints: {},
});
