import { fileURLToPath, URL } from "url";

import { HaloUIPluginBundlerKit } from "@halo-dev/ui-plugin-bundler-kit";
import Shiki from "@shikijs/markdown-it";
import Vue from "@vitejs/plugin-vue";
import Icons from "unplugin-icons/vite";
import Markdown from "unplugin-vue-markdown/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [
    Vue({
      include: [/\.vue$/, /\.md$/],
    }),
    Icons({ compiler: "vue3" }),
    HaloUIPluginBundlerKit(),
    Markdown({
      async markdownItSetup(md) {
        md.use(await Shiki({ theme: "github-dark" }));
      },
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});
