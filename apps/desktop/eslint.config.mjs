import nx from '@nx/eslint-plugin';

export default [
  {
    files: ['**/*.json'],
    // Override or add rules here
    rules: {},
    languageOptions: {
      parser: await import('jsonc-eslint-parser'),
    },
  },
  ...nx.configs['flat/base'],
  ...nx.configs['flat/typescript'],
  ...nx.configs['flat/javascript'],
  {
    ignores: [
      '**/dist',
      '**/vite.config.*.timestamp*',
      '**/vitest.config.*.timestamp*',
    ],
  },
  {
    files: ['**/*.ts', '**/*.tsx', '**/*.js', '**/*.jsx'],
    rules: {
      '@nx/enforce-module-boundaries': [
        'error',
        {
          enforceBuildableLibDependency: true,
          allow: [
            '^.*/eslint(\\.base)?\\.config\\.[cm]?[jt]s$',
            'xstate',
            'xstate/*',
            'rxjs',
            'rxjs/*',
            '@angular/core/rxjs-interop',
          ],
          depConstraints: [
            {
              sourceTag: 'scope:shared',
              onlyDependOnLibsWithTags: ['scope:shared'],
            },
            {
              sourceTag: 'scope:shop',
              onlyDependOnLibsWithTags: ['scope:shop', 'scope:shared'],
            },
            {
              sourceTag: 'scope:api',
              onlyDependOnLibsWithTags: ['scope:api', 'scope:shared'],
            },
            {
              sourceTag: 'type:data',
              onlyDependOnLibsWithTags: ['type:data'],
            },
            {
              sourceTag: 'type:data-access',
              onlyDependOnLibsWithTags: [
                'type:data-access',
                'type:util',
                'type:model',
              ],
            },
            {
              sourceTag: 'type:feature',
              onlyDependOnLibsWithTags: [
                'type:feature',
                'type:data-access',
                'type:ui',
                'type:util',
                'type:model',
                'npm:public',
              ],
            },
            {
              sourceTag: 'type:ui',
              onlyDependOnLibsWithTags: ['type:ui', 'type:util', 'type:model'],
            },
            {
              sourceTag: 'type:model',
              onlyDependOnLibsWithTags: ['type:model', 'type:util'],
            },
            {
              sourceTag: 'type:util',
              onlyDependOnLibsWithTags: ['type:util', 'type:model'],
            },
            {
              sourceTag: 'type:app',
              onlyDependOnLibsWithTags: [
                'type:feature',
                'type:data-access',
                'type:ui',
                'type:util',
                'type:model',
              ],
            },
          ],
        },
      ],
    },
  },
  {
    files: ['libs/ui/**/*.ts'],
    rules: {
      '@angular-eslint/directive-selector': 'off',
      '@angular-eslint/component-selector': 'off',
    },
  },
  {
    files: [
      '**/*.ts',
      '**/*.tsx',
      '**/*.cts',
      '**/*.mts',
      '**/*.js',
      '**/*.jsx',
      '**/*.cjs',
      '**/*.mjs',
    ],
    // Override or add rules here
    rules: {},
  },
];
