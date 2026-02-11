import { rsbuildConfig } from '@halo-dev/ui-plugin-bundler-kit';
import Shiki from '@shikijs/markdown-it';
import { UnoCSSRspackPlugin } from '@unocss/webpack/rspack';
import MonacoWebpackPlugin from 'monaco-editor-webpack-plugin';
import Icons from 'unplugin-icons/rspack';
import Markdown from 'unplugin-vue-markdown/webpack';

export default rsbuildConfig({
  rsbuild: {
    resolve: {
      alias: {
        '@': './src',
      },
    },
    tools: {
      rspack: {
        experiments: {
          outputModule: true,
        },
        plugins: [
          Icons({
            compiler: 'vue3',
          }),
          UnoCSSRspackPlugin(),
          Markdown({
            async markdownItSetup(md) {
              md.use(await Shiki({ theme: 'github-dark' }));
            },
          }),
          new MonacoWebpackPlugin({
            languages: ['markdown', 'html', 'css', 'javascript', 'json', 'xml'],
          }),
        ],
        module: {
          rules: [
            {
              test: /\.md$/,
              loader: 'vue-loader',
              options: {
                experimentalInlineMatchResource: true,
              },
            },
          ],
        },
      },
    },
  },
});
