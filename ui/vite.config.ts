import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Icons from "unplugin-icons/vite";
import { HaloUIPluginBundlerKit } from "@halo-dev/ui-plugin-bundler-kit";
import Markdown from "unplugin-vue-markdown/vite";
import Shiki from "@shikijs/markdown-it";

export default defineConfig({
  plugins: [
    Vue({
      include: [/\.vue$/, /\.md$/],
    }),
    VueJsx(),
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
