import { viteConfig } from '@halo-dev/ui-plugin-bundler-kit';
import path from 'node:path';
import UnoCSS from 'unocss/vite';
import Icons from 'unplugin-icons/vite';
import Markdown from 'unplugin-vue-markdown/vite';

import Shiki from '@shikijs/markdown-it';

export default viteConfig({
  format: 'esm',
  vue: {
    include: [/\.vue$/, /\.md$/],
  },
  vite: {
    resolve: {
      alias: {
        '@': path.resolve(import.meta.dirname, 'src'),
      },
    },
    plugins: [
      Icons({
        compiler: 'vue3',
      }),
      UnoCSS({
        mode: 'vue-scoped',
      }),
      Markdown({
        async markdownItSetup(md) {
          md.use(await Shiki({ theme: 'github-dark' }));
        },
      }),
    ],
  },
});
