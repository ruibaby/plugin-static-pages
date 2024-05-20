import { definePlugin } from "@halo-dev/console-shared";
import Projects from "./views/Projects.vue";
import { markRaw } from "vue";
import CarbonWebServicesContainer from "~icons/carbon/web-services-container";
import "./styles/tailwind.css";
import ProjectDetail from "./views/ProjectDetail.vue";
import FilesEditor from "./views/FilesEditor.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "ToolsRoot",
      route: {
        path: "static-pages",
        name: "StaticPageProjects",
        component: Projects,
        meta: {
          title: "静态网页服务",
          description: "提供静态网页部署服务",
          searchable: true,
          menu: {
            name: "静态网页服务",
            icon: markRaw(CarbonWebServicesContainer),
            priority: 0,
          },
        },
      },
    },
    {
      parentName: "ToolsRoot",
      route: {
        path: "static-pages/:name",
        name: "StaticPageProjectDetail",
        component: ProjectDetail,
        meta: {
          title: "静态网页详情",
          searchable: false,
        },
      },
    },
    {
      parentName: "ToolsRoot",
      route: {
        path: "static-pages/:name/files-editor",
        name: "StaticPageFilesEditor",
        component: FilesEditor,
        meta: {
          title: "文件编辑",
          searchable: false,
          hideFooter: true,
        },
      },
    },
  ],
  extensionPoints: {},
});
