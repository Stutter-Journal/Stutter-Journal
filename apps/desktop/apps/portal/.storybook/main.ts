import type { StorybookConfig } from '@storybook/angular';
import path from 'path';
import { fileURLToPath } from 'url';
import { createRequire } from 'module';

const config: StorybookConfig = {
  stories: ['../src/**/*.stories.@(js|jsx|ts|tsx|mdx)'],
  addons: [],
  framework: {
    name: '@storybook/angular',
    options: {
      builder: {
        viteConfigPath: 'apps/portal/vite.config.mts',
      },
    },
  },
  webpackFinal: async (config) => {
    const __dirname = path.dirname(fileURLToPath(import.meta.url));
    const tailwindConfig = path.resolve(__dirname, '../tailwind.config.js');
    const require = createRequire(import.meta.url);
    const postcssLoader = {
      loader: 'postcss-loader',
      options: {
        postcssOptions: {
          plugins: [
            require('tailwindcss')(tailwindConfig),
            require('autoprefixer'),
          ],
        },
      },
    };
    const stylesEntry = path.resolve(__dirname, '../src/styles.scss');
    const scssRule = {
      test: /\.scss$/,
      include: [stylesEntry],
      use: [
        { loader: 'style-loader' },
        {
          loader: 'css-loader',
          options: {
            importLoaders: 2,
          },
        },
        postcssLoader,
        {
          loader: 'resolve-url-loader',
          options: {
            sourceMap: true,
          },
        },
        {
          loader: 'sass-loader',
          options: {
            sourceMap: true,
          },
        },
      ],
    };

    if (config.module?.rules) {
      config.module.rules.unshift(scssRule);
      for (const rule of config.module.rules) {
        if (!rule || typeof rule !== 'object') {
          continue;
        }
        const test = 'test' in rule ? rule.test : undefined;
        if (!test || !test.toString().includes('scss')) {
          continue;
        }
        const use = 'use' in rule ? rule.use : undefined;
        if (!Array.isArray(use)) {
          continue;
        }
        const hasPostcss = use.some(
          (entry) => typeof entry === 'object' && entry?.loader?.includes('postcss-loader')
        );
        if (hasPostcss) {
          continue;
        }
        const resolveIndex = use.findIndex(
          (entry) => typeof entry === 'object' && entry?.loader?.includes('resolve-url-loader')
        );
        const sassIndex = use.findIndex(
          (entry) => typeof entry === 'object' && entry?.loader?.includes('sass-loader')
        );
        const insertIndex =
          resolveIndex !== -1 ? resolveIndex : sassIndex !== -1 ? sassIndex : use.length;
        use.splice(insertIndex, 0, postcssLoader);
      }
    }

    return config;
  },
};

export default config;
