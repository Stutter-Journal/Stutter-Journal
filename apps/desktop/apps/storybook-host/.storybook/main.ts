import { fileURLToPath } from 'node:url';
import { dirname } from 'node:path';
import type { StorybookConfig } from '@storybook/angular';

const config: StorybookConfig = {
  stories: [
    '../src/app/**/*.@(mdx|stories.@(js|jsx|ts|tsx))',
    '../../../libs/**/*.stories.@(ts|tsx|js|jsx|mdx)',
    '../../../libs/**/**/*.mdx',
  ],
  addons: ['@storybook/addon-styling-webpack'],
  framework: {
    name: getAbsolutePath('@storybook/angular'),
    options: {
      builder: {
        viteConfigPath: 'apps/storybook-host/vite.config.mts',
      },
    },
  },
};

export default config;

// To customize your Vite configuration you can use the viteFinal field.
// Check https://storybook.js.org/docs/react/builders/vite#configuration
// and https://nx.dev/recipes/storybook/custom-builder-configs

function getAbsolutePath(value: string): any {
  return dirname(fileURLToPath(import.meta.resolve(`${value}/package.json`)));
}
