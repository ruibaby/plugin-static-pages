import { defineConfig, presetAttributify, presetWind3, transformerCompileClass } from 'unocss';

export default defineConfig({
  presets: [presetWind3(), presetAttributify()],
  transformers: [transformerCompileClass()],
});
